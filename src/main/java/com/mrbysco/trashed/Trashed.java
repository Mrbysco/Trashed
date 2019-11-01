package com.mrbysco.trashed;

import com.mrbysco.trashed.init.TrashedRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Trashed.MOD_ID)
public class Trashed {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "trashed";

    public Trashed() {
        TrashedRegistry.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TrashedRegistry.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TrashedRegistry.TILES.register(FMLJavaModLoadingContext.get().getModEventBus());

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
//            ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> GuiMod::openScreen);
        });
    }
}
