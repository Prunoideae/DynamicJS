package moe.wolfgirl.dynamicjs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import moe.wolfgirl.dynamicjs.clazz.ClassWrapper;
import net.bytebuddy.agent.ByteBuddyAgent;

public class DynamicJSPlugin extends KubeJSPlugin {

    @Override
    public void init() {
        ByteBuddyAgent.install();
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("DynamicClass", new ClassWrapper(event.manager));
    }
}
