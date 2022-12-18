package com.mrbysco.trashed.generator;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.mrbysco.trashed.init.TrashedRegistry.BLOCKS;
import static com.mrbysco.trashed.init.TrashedRegistry.ENERGY_TRASH_CAN;
import static com.mrbysco.trashed.init.TrashedRegistry.FLUID_TRASH_CAN;
import static com.mrbysco.trashed.init.TrashedRegistry.TRASH_CAN;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class TrashedCreator {
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		PackOutput packOutput = generator.getPackOutput();
		ExistingFileHelper helper = event.getExistingFileHelper();

		if (event.includeServer()) {
			generator.addProvider(event.includeServer(), new Loots(packOutput));
		}
	}

	private static class Loots extends LootTableProvider {
		public Loots(PackOutput packOutput) {
			super(packOutput, Set.of(), List.of(
					new SubProviderEntry(Blocks::new, LootContextParamSets.BLOCK)
			));
		}

		@Override
		protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationContext) {
			map.forEach((name, table) -> LootTables.validate(validationContext, name, table));
		}

		private static class Blocks extends BlockLootSubProvider {

			protected Blocks() {
				super(Set.of(), FeatureFlags.REGISTRY.allFlags());
			}

			@Override
			protected void generate() {
				this.dropSelf(TRASH_CAN.get());
				this.dropSelf(FLUID_TRASH_CAN.get());
				this.dropSelf(ENERGY_TRASH_CAN.get());
			}

			@Override
			protected Iterable<Block> getKnownBlocks() {
				return (Iterable<Block>) BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
			}
		}
	}
}