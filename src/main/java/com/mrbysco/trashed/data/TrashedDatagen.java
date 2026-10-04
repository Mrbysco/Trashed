package com.mrbysco.trashed.data;

import com.mrbysco.trashed.Trashed;
import com.mrbysco.trashed.block.TrashBlock;
import com.mrbysco.trashed.block.TrashType;
import com.mrbysco.trashed.init.TrashedDamageTypes;
import com.mrbysco.trashed.init.TrashedRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.SingleRegistryBootstrap;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.locale.Language;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber
public class TrashedDatagen {

	public static final RegistrySetBuilder RELOADABLE_BUILDER = new RegistrySetBuilder()
			.add(RecipeProvider.asBootstrap(TrashedRecipeProvider::new))
			.add(Registries.LOOT_TABLE, TrashedLootProvider.create());

	public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
			.add(Registries.DAMAGE_TYPE, context -> {
				context.register(TrashedDamageTypes.TRASHED, new DamageType("trashed", 0.0F));
			});

	@SubscribeEvent
	public static void gatherData(GatherDataEvent.Client event) {
		event.createWorldRegistryObjects(BUILDER);
		event.createReloadableRegistryObjects(RELOADABLE_BUILDER);

		event.createProvider(TrashedBlockTags::new);
		event.createProvider(TrashedDamageTypeTags::new);

		event.createProvider(TrashedLanguageProvider::new);
		event.createProvider(TrashedModelProvider::new);
	}

	public static class TrashedModelProvider extends ModelProvider {
		public TrashedModelProvider(PackOutput output) {
			super(output, Trashed.MOD_ID);
		}

		@Override
		protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
			registerExtendedTrashCan(blockModels, TrashedRegistry.TRASH_CAN.get());
			registerTrashCan(blockModels, TrashedRegistry.FLUID_TRASH_CAN.get());
			registerTrashCan(blockModels, TrashedRegistry.ENERGY_TRASH_CAN.get());
		}

		private void registerExtendedTrashCan(BlockModelGenerators blockModels, TrashBlock block) {
			Identifier regular = ModelLocationUtils.getModelLocation(block);
			Identifier disabled = ModelLocationUtils.getModelLocation(block, "_disabled");

			Identifier bottom = ModelLocationUtils.getModelLocation(block, "_bottom");
			Identifier bottom_disabled = ModelLocationUtils.getModelLocation(block, "_bottom_disabled");
			Identifier top = ModelLocationUtils.getModelLocation(block, "_top");
			Identifier top_disabled = ModelLocationUtils.getModelLocation(block, "_top_disabled");

			blockModels.registerSimpleItemModel(block, regular);
			blockModels.blockStateOutput
					.accept(
							MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(regular))
									.with(
											PropertyDispatch.modify(TrashBlock.TYPE, TrashBlock.ENABLED)
													.select(TrashType.SINGLE, true, VariantMutator.MODEL.withValue(regular))
													.select(TrashType.SINGLE, false, VariantMutator.MODEL.withValue(disabled))
													.select(TrashType.BOTTOM, true, VariantMutator.MODEL.withValue(bottom))
													.select(TrashType.BOTTOM, false, VariantMutator.MODEL.withValue(bottom_disabled))
													.select(TrashType.TOP, true, VariantMutator.MODEL.withValue(top))
													.select(TrashType.TOP, false, VariantMutator.MODEL.withValue(top_disabled))
									)
									.with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING)
					);
		}

		private void registerTrashCan(BlockModelGenerators blockModels, Block block) {
			Identifier regular = ModelLocationUtils.getModelLocation(block);
			Identifier disabled = ModelLocationUtils.getModelLocation(block, "_disabled");
			blockModels.registerSimpleItemModel(block, regular);
			blockModels.blockStateOutput
					.accept(
							MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(regular))
									.with(PropertyDispatch.modify(TrashBlock.ENABLED)
											.select(true, VariantMutator.MODEL.withValue(regular))
											.select(false, VariantMutator.MODEL.withValue(disabled)))
									.with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING)
					);
		}
	}

	public static class TrashedLanguageProvider extends LanguageProvider {
		public TrashedLanguageProvider(PackOutput output) {
			super(output, Trashed.MOD_ID, Language.DEFAULT);
		}

		@Override
		protected void addTranslations() {
			addBlock(TrashedRegistry.TRASH_CAN, "Trash Can");
			addBlock(TrashedRegistry.FLUID_TRASH_CAN, "Fluid Trash Can");
			addBlock(TrashedRegistry.ENERGY_TRASH_CAN, "Energy Trash Can");

			add("trashed.container.trashcan", "Trash Can");
			add("trashed.trash_tooltip", "Jump in to trash yourself (Trash Can needs to be two blocks tall)");
			add("death.attack.trashed", "%1$s was thrown into the trash");

			addConfig("trashing", "Trashing", "Trashing Settings");
			addConfig("itemTrashQuantity", "Item Trash Quantity", "The quantity of items the Trash Can destroys every cycle [Default: 1]");

		}

		private void addConfig(String path, String name, @Nullable String description) {
			this.add("trashed.configuration." + path, name);
			if (description != null && !description.isEmpty())
				this.add("trashed.configuration." + path + ".tooltip", description);
		}
	}

	private static class TrashedLootProvider {

		public static SingleRegistryBootstrap<LootTable> create() {
			return new LootTableProvider(
					BuiltInLootTables.all(),
					List.of(
							new LootTableProvider.SubProviderEntry(Blocks::new, LootContextParamSets.BLOCK)
					)
			);
		}

		private static class Blocks extends BlockLootSubProvider {

			protected Blocks(LootTableSubProvider.Context provider) {
				super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
			}

			@Override
			protected void generate() {
				dropSelf(TrashedRegistry.TRASH_CAN.get());
				dropSelf(TrashedRegistry.FLUID_TRASH_CAN.get());
				dropSelf(TrashedRegistry.ENERGY_TRASH_CAN.get());
			}

			@Override
			protected Iterable<Block> getKnownBlocks() {
				return TrashedRegistry.BLOCKS.getEntries().stream().map(holder -> (Block) holder.get())::iterator;
			}
		}
	}

	private static class TrashedRecipeProvider extends RecipeProvider {
		public TrashedRecipeProvider(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
			super(recipeOutput, advancementOutput);
		}

		@Override
		protected void buildRecipes() {
			shaped(RecipeCategory.REDSTONE, TrashedRegistry.ENERGY_TRASH_CAN.get())
					.pattern("SSS").pattern("CRC").pattern("CCC")
					.define('S', Tags.Items.STONES)
					.define('C', Tags.Items.COBBLESTONES)
					.define('R', Tags.Items.STORAGE_BLOCKS_REDSTONE)
					.unlockedBy("has_stone", has(Tags.Items.STONES))
					.unlockedBy("has_cobblestone", has(Tags.Items.COBBLESTONES))
					.unlockedBy("has_redstone_block", has(Tags.Items.STORAGE_BLOCKS_REDSTONE)).save(this.output);

			shaped(RecipeCategory.REDSTONE, TrashedRegistry.FLUID_TRASH_CAN.get())
					.pattern("SSS").pattern("CBC").pattern("CCC")
					.define('S', Tags.Items.STONES)
					.define('C', Tags.Items.COBBLESTONES)
					.define('B', Tags.Items.BUCKETS_EMPTY)
					.unlockedBy("has_stone", has(Tags.Items.STONES))
					.unlockedBy("has_cobblestone", has(Tags.Items.COBBLESTONES))
					.unlockedBy("has_bucket", has(Tags.Items.BUCKETS_EMPTY)).save(this.output);

			shaped(RecipeCategory.REDSTONE, TrashedRegistry.TRASH_CAN.get())
					.pattern("SSS").pattern("CHC").pattern("CCC")
					.define('S', Tags.Items.STONES)
					.define('C', Tags.Items.COBBLESTONES)
					.define('H', Items.HOPPER)
					.unlockedBy("has_stone", has(Tags.Items.STONES))
					.unlockedBy("has_cobblestone", has(Tags.Items.COBBLESTONES))
					.unlockedBy("has_hopper", has(Items.HOPPER)).save(this.output);
		}
	}

	public static class TrashedBlockTags extends BlockTagsProvider {
		public TrashedBlockTags(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
			super(packOutput, lookupProvider, Trashed.MOD_ID);
		}

		@Override
		protected void addTags(Provider provider) {
			this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
					.add(TrashedRegistry.TRASH_CAN.key())
					.add(TrashedRegistry.FLUID_TRASH_CAN.key())
					.add(TrashedRegistry.ENERGY_TRASH_CAN.key());
		}
	}

	public static class TrashedDamageTypeTags extends DamageTypeTagsProvider {
		public TrashedDamageTypeTags(PackOutput output, CompletableFuture<Provider> lookupProvider) {
			super(output, lookupProvider, Trashed.MOD_ID);
		}

		@Override
		protected void addTags(Provider registries) {
			tag(DamageTypeTags.CAN_BREAK_ARMOR_STAND)
					.add(TrashedDamageTypes.TRASHED);

			tag(DamageTypeTags.BYPASSES_ARMOR)
					.add(TrashedDamageTypes.TRASHED);

			tag(DamageTypeTags.ALWAYS_MOST_SIGNIFICANT_FALL)
					.add(TrashedDamageTypes.TRASHED);

			tag(DamageTypeTags.BYPASSES_COOLDOWN)
					.add(TrashedDamageTypes.TRASHED);

			tag(DamageTypeTags.BYPASSES_INVULNERABILITY)
					.add(TrashedDamageTypes.TRASHED);

			tag(DamageTypeTags.BYPASSES_RESISTANCE)
					.add(TrashedDamageTypes.TRASHED);

			tag(DamageTypeTags.NO_IMPACT)
					.add(TrashedDamageTypes.TRASHED);

			tag(DamageTypeTags.NO_KNOCKBACK)
					.add(TrashedDamageTypes.TRASHED);

			tag(DamageTypeTags.WITHER_IMMUNE_TO)
					.add(TrashedDamageTypes.TRASHED);
		}
	}
}