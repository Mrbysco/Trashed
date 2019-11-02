package com.mrbysco.trashed;

import com.mrbysco.trashed.config.TrashedConfig;
import com.mrbysco.trashed.init.TrashedRegistry;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Trashed.MOD_ID)
public class Trashed {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "trashed";

    public static final DamageSource trashDamage = new DamageSource("trashed").setMagicDamage().setDamageBypassesArmor();

    public Trashed() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, TrashedConfig.serverSpec);
        FMLJavaModLoadingContext.get().getModEventBus().register(TrashedConfig.class);

        TrashedRegistry.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TrashedRegistry.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TrashedRegistry.TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
