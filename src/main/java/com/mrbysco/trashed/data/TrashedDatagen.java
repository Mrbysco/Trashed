package com.mrbysco.trashed.data;

import com.mrbysco.trashed.Trashed;
import com.mrbysco.trashed.init.TrashedDamageTypes;
import com.mrbysco.trashed.init.TrashedRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
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
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class TrashedDatagen {
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		PackOutput packOutput = generator.getPackOutput();
		CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

		if (event.includeServer()) {
			generator.addProvider(event.includeServer(), new Loots(packOutput, lookupProvider));
			generator.addProvider(event.includeServer(), new Recipes(packOutput, lookupProvider));

			generator.addProvider(true, new TrashedDatagenProvider(
					packOutput,
					event.getLookupProvider(),
					Set.of(Trashed.MOD_ID)
			));
		}
	}

	public static class TrashedLanguageProvider extends LanguageProvider {
		public TrashedLanguageProvider(PackOutput output) {
			super(output, Trashed.MOD_ID, "en_us");
		}

		@Override
		protected void addTranslations() {
			this.add(TrashedRegistry.TRASH_CAN.get(), "Trash Can");
			this.add(TrashedRegistry.FLUID_TRASH_CAN.get(), "Fluid Trash Can");
			this.add(TrashedRegistry.ENERGY_TRASH_CAN.get(), "Energy Trash Can");

			this.add("trashed.container.trashcan", "Trash Can");
			this.add("trashed.trash_tooltip", "Jump in to trash yourself (Trash Can needs to be two blocks tall)");
			this.add("death.attack.trashed", "%1$s was thrown into the trash");

			this.addConfig("trashing", "Trashing", "Trashing Settings");
			this.addConfig("itemTrashQuantity", "Item Trash Quantity", "The quantity of items the Trash Can destroys every cycle [Default: 1]");

		}

		private void addConfig(String path, String name, @Nullable String description) {
			this.add("trashed.configuration." + path, name);
			if (description != null && !description.isEmpty())
				this.add("trashed.configuration." + path + ".tooltip", description);
		}
	}

	public static class TrashedDatagenProvider extends DatapackBuiltinEntriesProvider {
		public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
				.add(Registries.DAMAGE_TYPE, context -> {
					context.register(TrashedDamageTypes.TRASHED, new DamageType("trashed", 0.0F));
				});

		public TrashedDatagenProvider(PackOutput output, CompletableFuture<Provider> registries, Set<String> modIds) {
			super(output, registries, BUILDER, modIds);
		}
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

			protected Blocks(HolderLookup.Provider provider) {
				super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
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