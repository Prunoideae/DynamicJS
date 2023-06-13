package moe.wolfgirl.dynamicjs.clazz;

import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.rhino.JavaMembers;
import dev.latvian.mods.rhino.NativeJavaClass;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


public class DynamicClass<T> extends ByteBuddyBase<T> {

    private String name;

    DynamicClass(Class<T> wrapped, ScriptManager manager) {
        super(wrapped, manager);
        this.name = "moe.wolfgirl.generated." + wrapped.getSimpleName();
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
                .with(TypeValidation.DISABLED)
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
                builder = builder.method(ElementMatchers.named(info.method.getName())).intercept(MethodDelegation.withDefaultConfiguration()
                                .to(new CallDelegator<T>(callbackMethod, info.method.getReturnType(), manager.context)))
                        .topLevelType();
            }
        }

        try (DynamicType.Unloaded<T> loaded = builder.make()) {
            return new NativeJavaClass(manager.context, manager.topLevelScope, loaded.load(manager.getClass().getClassLoader()).getLoaded());
        }
    }

}
