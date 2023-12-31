package com.mrbysco.trashed;

import com.mojang.logging.LogUtils;
import com.mrbysco.trashed.config.TrashedConfig;
import com.mrbysco.trashed.init.TrashedDamageTypes;
import com.mrbysco.trashed.init.TrashedRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.slf4j.Logger;

import java.util.List;

@Mod(Trashed.MOD_ID)
public class Trashed {
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final String MOD_ID = "trashed";

	public Trashed(IEventBus eventBus) {
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, TrashedConfig.serverSpec);
		eventBus.register(TrashedConfig.class);

		eventBus.addListener(this::addTabContents);
		eventBus.addListener(this::registerCapabilities);

		TrashedRegistry.ITEMS.register(eventBus);
		TrashedRegistry.BLOCKS.register(eventBus);
		TrashedRegistry.BLOCK_ENTITY_TYPES.register(eventBus);
	}

	public static DamageSource getTrashDamageSource(Entity entity) {
		return entity.damageSources().source(TrashedDamageTypes.TRASHED, entity);
	}

	private void registerCapabilities(final RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, TrashedRegistry.ENERGY_TRASH_TILE.get(), (trashBlockEntity, side) ->
				new EnergyStorage(1000000)
		);
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TrashedRegistry.FLUID_TRASH_TILE.get(), (trashBlockEntity, side) ->
				new FluidTank(1000000)
		);
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TrashedRegistry.TRASH_TILE.get(), (trashBlockEntity, side) ->
				new InvWrapper(trashBlockEntity)
		);
	}

	private void addTabContents(final BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
			List<ItemStack> stacks = TrashedRegistry.ITEMS.getEntries().stream().map(reg -> new ItemStack(reg.get())).toList();
			event.acceptAll(stacks);
		}
	}
}
