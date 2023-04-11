package moe.wolfgirl.dynamicjs.forge;

import dev.architectury.platform.forge.EventBuses;
import moe.wolfgirl.dynamicjs.DynamicJS;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DynamicJS.MOD_ID)
public class DynamicJSForge {
    public DynamicJSForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(DynamicJS.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        DynamicJS.init();
    }
}