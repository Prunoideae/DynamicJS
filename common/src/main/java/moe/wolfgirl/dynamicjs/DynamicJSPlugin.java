package moe.wolfgirl.dynamicjs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import moe.wolfgirl.dynamicjs.clazz.DynamicClass;

public class DynamicJSPlugin extends KubeJSPlugin {
    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("DynamicClass", new DynamicClass.Wrapper(event.manager));
    }
}
