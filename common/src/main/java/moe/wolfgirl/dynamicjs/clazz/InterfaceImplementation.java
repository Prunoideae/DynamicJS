package moe.wolfgirl.dynamicjs.clazz;

import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.rhino.JavaMembers;
import dev.latvian.mods.rhino.util.HideFromJS;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @param <Q> The interface type to be implemented
 * @param <T> The base class type
 */
public class InterfaceImplementation<Q, T> {
    private final Set<String> allowedNames;
    private final List<Pair<String, Function<CallDelegator.CallInfo<Q>, Object>>> callbacks = new ArrayList<>();

    public InterfaceImplementation(Class<Q> impl, ScriptManager manager) {
        this.allowedNames = JavaMembers.lookupClass(manager.context, manager.topLevelScope, impl, impl, false)
                .getAccessibleMethods(manager.context, false)
                .stream()
                .map(info -> info.name)
                .collect(Collectors.toSet());
    }

    /**
     * @param name a key in keyof Q
     * @param callback a callback accepting CallDelegator.CallInfo{@literal <Q & T>}
     * @return itself
     */
    public InterfaceImplementation<Q, T> override(String name, Function<CallDelegator.CallInfo<Q>, Object> callback) {
        if (!allowedNames.contains(name))
            throw new RuntimeException("The method name is not in the interface!");
        callbacks.add(new Pair<>(name, callback));
        return this;
    }

    @HideFromJS
    public List<Pair<String, Function<CallDelegator.CallInfo<Q>, Object>>> get() {
        return callbacks;
    }
}
