package com.mrbysco.trashed;

import com.mojang.logging.LogUtils;
import com.mrbysco.trashed.config.TrashedConfig;
import com.mrbysco.trashed.init.TrashedDamageTypes;
import com.mrbysco.trashed.init.TrashedRegistry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.List;

@Mod(Trashed.MOD_ID)
public class Trashed {
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final String MOD_ID = "trashed";

	public Trashed() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, TrashedConfig.serverSpec);
		eventBus.register(TrashedConfig.class);

		eventBus.addListener(this::addTabContents);

		TrashedRegistry.ITEMS.register(eventBus);
		TrashedRegistry.BLOCKS.register(eventBus);
		TrashedRegistry.BLOCK_ENTITY_TYPES.register(eventBus);
	}

	public static DamageSource getTrashDamageSource(Entity entity) {
		return entity.damageSources().source(TrashedDamageTypes.TRASHED, entity);
	}

	private void addTabContents(final CreativeModeTabEvent.BuildContents event) {
		if (event.getTab() == CreativeModeTabs.REDSTONE_BLOCKS) {
			List<ItemStack> stacks = TrashedRegistry.ITEMS.getEntries().stream().map(reg -> new ItemStack(reg.get())).toList();
			event.acceptAll(stacks);
		}
	}
}
