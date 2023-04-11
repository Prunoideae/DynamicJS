package moe.wolfgirl.dynamicjs.fabric;

import moe.wolfgirl.dynamicjs.DynamicJS;
import net.fabricmc.api.ModInitializer;

public class DynamicJSFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        DynamicJS.init();
    }
}