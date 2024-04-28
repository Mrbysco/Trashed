package com.mrbysco.trashed.data;

import com.mrbysco.trashed.Trashed;
import com.mrbysco.trashed.init.TrashedDamageTypes;
import com.mrbysco.trashed.init.TrashedRegistry;
import net.minecraft.core.Cloner;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class TrashedDatagen {
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		PackOutput packOutput = generator.getPackOutput();
		ExistingFileHelper helper = event.getExistingFileHelper();
		CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

		if (event.includeServer()) {
			generator.addProvider(event.includeServer(), new Loots(packOutput, lookupProvider));
			generator.addProvider(event.includeServer(), new Recipes(packOutput, lookupProvider));

			generator.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(
					packOutput, CompletableFuture.supplyAsync(TrashedDatagen::getProvider), Set.of(Trashed.MOD_ID)));
		}
	}

	private static RegistrySetBuilder.PatchedRegistries getProvider() {
		final RegistrySetBuilder registryBuilder = new RegistrySetBuilder();
		registryBuilder.add(Registries.DAMAGE_TYPE, context -> {
			context.register(TrashedDamageTypes.TRASHED, new DamageType("trashed", 0.0F));
		});
		RegistryAccess.Frozen regAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
		Cloner.Factory cloner$factory = new Cloner.Factory();
		net.neoforged.neoforge.registries.DataPackRegistriesHooks.getDataPackRegistriesWithDimensions().forEach(data -> data.runWithArguments(cloner$factory::addCodec));
		return registryBuilder.buildPatch(regAccess, VanillaRegistries.createLookup(), cloner$factory);
	}

	private static class Loots extends LootTableProvider {
		public Loots(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
			super(packOutput, Set.of(), List.of(
					new SubProviderEntry(Blocks::new, LootContextParamSets.BLOCK)
			), lookupProvider);
		}

		@Override
		protected void validate(WritableRegistry<LootTable> writableregistry, ValidationContext validationcontext, ProblemReporter.Collector problemreporter$collector) {
			super.validate(writableregistry, validationcontext, problemreporter$collector);
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
				return (Iterable<Block>) TrashedRegistry.BLOCKS.getEntries().stream().map(holder -> (Block) holder.get())::iterator;
			}
		}
	}

	private static class Recipes extends RecipeProvider {
		public Recipes(PackOutput packOutput, CompletableFuture<net.minecraft.core.HolderLookup.Provider> lookupProvider) {
			super(packOutput, lookupProvider);
		}

		@Override
		protected void buildRecipes(RecipeOutput recipeOutput) {
			ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TrashedRegistry.ENERGY_TRASH_CAN.get())
					.pattern("SSS")
					.pattern("CRC")
					.pattern("CCC")
					.define('S', Tags.Items.STONES)
					.define('C', Tags.Items.COBBLESTONES)
					.define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
					.unlockedBy("has_stone", has(Tags.Items.STONES))
					.unlockedBy("has_cobblestone", has(Tags.Items.COBBLESTONES))
					.unlockedBy("has_redstone_block", has(Tags.Items.STORAGE_BLOCKS_REDSTONE))
					.save(recipeOutput);

			ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TrashedRegistry.FLUID_TRASH_CAN.get())
					.pattern("SSS")
					.pattern("CBC")
					.pattern("CCC")
					.define('S', Tags.Items.STONES)
					.define('C', Tags.Items.COBBLESTONES)
					.define('B', Tags.Items.BUCKETS_EMPTY)
					.unlockedBy("has_stone", has(Tags.Items.STONES))
					.unlockedBy("has_cobblestone", has(Tags.Items.COBBLESTONES))
					.unlockedBy("has_bucket", has(Tags.Items.BUCKETS_EMPTY))
					.save(recipeOutput);

			ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TrashedRegistry.TRASH_CAN.get())
					.pattern("SSS")
					.pattern("CHC")
					.pattern("CCC")
					.define('S', Tags.Items.STONES)
					.define('C', Tags.Items.COBBLESTONES)
					.define('H', Items.HOPPER)
					.unlockedBy("has_stone", has(Tags.Items.STONES))
					.unlockedBy("has_cobblestone", has(Tags.Items.COBBLESTONES))
					.unlockedBy("has_hopper", has(Items.HOPPER))
					.save(recipeOutput);
		}
	}
}