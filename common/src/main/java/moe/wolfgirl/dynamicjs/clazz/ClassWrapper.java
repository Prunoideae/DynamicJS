package moe.wolfgirl.dynamicjs.clazz;

import dev.latvian.mods.kubejs.script.ScriptManager;

public class ClassWrapper {
    private final ScriptManager manager;

    public ClassWrapper(ScriptManager manager) {
        this.manager = manager;
    }

    public <T> DynamicClass<T> wraps(Class<T> clazz) {
        return new DynamicClass<>(clazz, manager);
    }

    public DynamicClass<Object> create() {
        return wraps(Object.class);
    }
}
