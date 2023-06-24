package com.mrbysco.trashed.data;

import com.mrbysco.trashed.Trashed;
import com.mrbysco.trashed.init.TrashedDamageTypes;
import com.mrbysco.trashed.init.TrashedRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class TrashedDatagen {
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		PackOutput packOutput = generator.getPackOutput();
		ExistingFileHelper helper = event.getExistingFileHelper();

		if (event.includeServer()) {
			generator.addProvider(event.includeServer(), new Loots(packOutput));
			generator.addProvider(event.includeServer(), new Recipes(packOutput));

			generator.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(
					packOutput, CompletableFuture.supplyAsync(TrashedDatagen::getProvider), Set.of(Trashed.MOD_ID)));
		}
	}

	private static HolderLookup.Provider getProvider() {
		final RegistrySetBuilder registryBuilder = new RegistrySetBuilder();
		registryBuilder.add(Registries.DAMAGE_TYPE, context -> {
			context.register(TrashedDamageTypes.TRASHED, new DamageType("trashed", 0.0F));
		});
		RegistryAccess.Frozen regAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
		return registryBuilder.buildPatch(regAccess, VanillaRegistries.createLookup());
	}

	private static class Loots extends LootTableProvider {
		public Loots(PackOutput packOutput) {
			super(packOutput, Set.of(), List.of(
					new SubProviderEntry(Blocks::new, LootContextParamSets.BLOCK)
			));
		}

		@Override
		protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationContext) {
			map.forEach((name, table) -> table.validate(validationContext));
		}

		private static class Blocks extends BlockLootSubProvider {

			protected Blocks() {
				super(Set.of(), FeatureFlags.REGISTRY.allFlags());
			}

			@Override
			protected void generate() {
				this.dropSelf(TrashedRegistry.TRASH_CAN.get());
				this.dropSelf(TrashedRegistry.FLUID_TRASH_CAN.get());
				this.dropSelf(TrashedRegistry.ENERGY_TRASH_CAN.get());
			}

			@Override
			protected Iterable<Block> getKnownBlocks() {
				return (Iterable<Block>) TrashedRegistry.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
			}
		}
	}

	private static class Recipes extends RecipeProvider {
		public Recipes(PackOutput packOutput) {
			super(packOutput);
		}

		@Override
		protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
			ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TrashedRegistry.ENERGY_TRASH_CAN.get())
					.pattern("SSS")
					.pattern("CRC")
					.pattern("CCC")
					.define('S', Tags.Items.STONE)
					.define('C', Tags.Items.COBBLESTONE)
					.define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
					.unlockedBy("has_stone", has(Tags.Items.STONE))
					.unlockedBy("has_cobblestone", has(Tags.Items.COBBLESTONE))
					.unlockedBy("has_redstone_block", has(Tags.Items.STORAGE_BLOCKS_REDSTONE))
					.save(consumer);

			ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TrashedRegistry.FLUID_TRASH_CAN.get())
					.pattern("SSS")
					.pattern("CBC")
					.pattern("CCC")
					.define('S', Tags.Items.STONE)
					.define('C', Tags.Items.COBBLESTONE)
					.define('B', Items.BUCKET)
					.unlockedBy("has_stone", has(Tags.Items.STONE))
					.unlockedBy("has_cobblestone", has(Tags.Items.COBBLESTONE))
					.unlockedBy("has_bucket", has(Items.BUCKET))
					.save(consumer);

			ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TrashedRegistry.TRASH_CAN.get())
					.pattern("SSS")
					.pattern("CHC")
					.pattern("CCC")
					.define('S', Tags.Items.STONE)
					.define('C', Tags.Items.COBBLESTONE)
					.define('H', Items.HOPPER)
					.unlockedBy("has_stone", has(Tags.Items.STONE))
					.unlockedBy("has_cobblestone", has(Tags.Items.COBBLESTONE))
					.unlockedBy("has_hopper", has(Items.HOPPER))
					.save(consumer);
		}
	}
}