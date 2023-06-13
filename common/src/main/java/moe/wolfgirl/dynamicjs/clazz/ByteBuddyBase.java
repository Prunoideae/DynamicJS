package moe.wolfgirl.dynamicjs.clazz;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.script.ScriptManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ByteBuddyBase<T> {
    protected final Class<T> wrapped;
    protected final List<Class<?>> interfaces = new ArrayList<>();
    protected final List<Pair<String, Function<CallDelegator.CallInfo<T>, Object>>> callbacks = new ArrayList<>();
    protected final ScriptManager manager;

    ByteBuddyBase(Class<T> wrapped, ScriptManager manager) {
        this.wrapped = wrapped;
        this.manager = manager;

    }

    @SuppressWarnings("unchecked")
    public <Q> ByteBuddyBase<T> impl(Class<Q> clazz, Consumer<InterfaceImplementation<Q, T>> impl) {
        interfaces.add(clazz);
        var implementation = new InterfaceImplementation<Q, T>(clazz, manager);
        impl.accept(implementation);
        for (Pair<String, Function<CallDelegator.CallInfo<Q>, Object>> pair : implementation.get()) {
            override(pair.getFirst(), (Function<CallDelegator.CallInfo<T>, Object>) (Object) pair.getSecond());
        }
        return this;
    }

    public ByteBuddyBase<T> override(String name, Function<CallDelegator.CallInfo<T>, Object> callback) {
        callbacks.add(new Pair<>(name, callback));
        return this;
    }

    public ByteBuddyBase<T> fixedValue(String name, Supplier<Object> supplier) {
        Supplier<Object> cached = Suppliers.memoize(supplier::get);
        return override(name, info -> cached.get());
    }
}
