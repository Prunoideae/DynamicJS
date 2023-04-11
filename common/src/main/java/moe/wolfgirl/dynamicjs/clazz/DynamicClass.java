package moe.wolfgirl.dynamicjs.clazz;

import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.rhino.JavaMembers;
import dev.latvian.mods.rhino.NativeJavaClass;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;


public class DynamicClass<T> {

    private final Class<T> wrapped;
    private final List<Class<?>> interfaces = new ArrayList<>();
    private final List<Pair<String, Function<CallDelegator.CallInfo<T>, Object>>> callbacks = new ArrayList<>();
    private String name;
    private final ScriptManager manager;

    private DynamicClass(Class<T> wrapped, ScriptManager manager) {
        this.wrapped = wrapped;
        this.manager = manager;
        this.name = "moe.wolfgirl.generated." + wrapped.getSimpleName();
    }


    @SuppressWarnings("unchecked")
    public <Q> DynamicClass<T> impl(Class<Q> clazz, Consumer<InterfaceImplementation<Q, T>> impl) {
        interfaces.add(clazz);
        var implementation = new InterfaceImplementation<Q, T>(clazz, manager);
        impl.accept(implementation);
        for (Pair<String, Function<CallDelegator.CallInfo<Q>, Object>> pair : implementation.get()) {
            override(pair.getFirst(), (Function<CallDelegator.CallInfo<T>, Object>) (Object) pair.getSecond());
        }
        return this;
    }

    public DynamicClass<T> override(String name, Function<CallDelegator.CallInfo<T>, Object> callback) {
        callbacks.add(new Pair<>(name, callback));
        return this;
    }

    public DynamicClass<T> name(String pkgName, String className) {
        name = pkgName + "." + className;
        return this;
    }

    public DynamicClass<T> name(String simpleName) {
        return name("moe.wolfgirl.generated", simpleName);
    }

    @SuppressWarnings("unchecked")
    public Object build() throws IOException {
        var builder = new ByteBuddy()
                .subclass(wrapped)
                .name(name)
                .implement(interfaces.toArray(new Type[0]))
                .topLevelType();

        Map<String, JavaMembers.MethodInfo> mappedToOriginals = new HashMap<>();
        JavaMembers members = JavaMembers.lookupClass(manager.context, manager.topLevelScope, wrapped, wrapped, false);
        for (JavaMembers.MethodInfo accessibleMethod : members.getAccessibleMethods(manager.context, false)) {
            mappedToOriginals.put(accessibleMethod.name, accessibleMethod);
        }

        for (Class<?> anInterface : interfaces) {
            JavaMembers interfaceMembers = JavaMembers.lookupClass(manager.context, manager.topLevelScope, anInterface, anInterface, false);
            for (JavaMembers.MethodInfo accessibleMethod : interfaceMembers.getAccessibleMethods(manager.context, false)) {
                mappedToOriginals.put(accessibleMethod.name, accessibleMethod);
            }
        }


        for (Pair<String, Function<CallDelegator.CallInfo<T>, Object>> callback : callbacks) {
            var name = callback.getFirst();
            var callbackMethod = callback.getSecond();
            JavaMembers.MethodInfo info = mappedToOriginals.get(name);
            if (info != null) {
                builder = builder.method(ElementMatchers.named(info.name)).intercept(MethodDelegation.withDefaultConfiguration()
                                .to(new CallDelegator<T>(callbackMethod, info.method.getReturnType(), manager.context)))
                        .topLevelType();

            }
        }

        try (DynamicType.Unloaded<T> loaded = builder.make()) {
            return new NativeJavaClass(manager.context, manager.topLevelScope, loaded.load(wrapped.getClassLoader()).getLoaded());
        }
    }

    public static class Wrapper {
        private final ScriptManager manager;

        public Wrapper(ScriptManager manager) {
            this.manager = manager;
        }

        public <T> DynamicClass<T> wraps(Class<T> clazz) {
            return new DynamicClass<>(clazz, manager);
        }
    }
}
