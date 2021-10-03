package com.mrbysco.trashed.generator;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mrbysco.trashed.Trashed;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTable.Builder;
import net.minecraft.loot.LootTableManager;
import net.minecraft.loot.ValidationTracker;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.mrbysco.trashed.init.TrashedRegistry.*;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class TrashedCreator {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();

        if (event.includeServer()) {
            gen.addProvider(new Loots(gen));
        }
        if (event.includeClient()) {
            gen.addProvider(new Language(gen));
            gen.addProvider(new BlockStates(gen, helper));
            gen.addProvider(new ItemModels(gen, helper));
        }
    }

    private static class Loots extends LootTableProvider {
        public Loots(DataGenerator gen) {
            super(gen);
        }

        @Override
        protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, Builder>>>, LootParameterSet>> getTables() {
            return ImmutableList.of(
                    Pair.of(Blocks::new, LootParameterSets.BLOCK)
            );
        }

        @Override
        protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker) {
            map.forEach((name, table) -> LootTableManager.validateLootTable(validationtracker, name, table));
        }

        private class Blocks extends BlockLootTables {
            @Override
            protected void addTables() {
                this.registerDropSelfLootTable(TRASH_CAN.get());
                this.registerDropSelfLootTable(FLUID_TRASH_CAN.get());
                this.registerDropSelfLootTable(ENERGY_TRASH_CAN.get());
            }

            @Override
            protected Iterable<Block> getKnownBlocks() {
                return (Iterable<Block>) BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
            }
        }
    }

    private static class Language extends LanguageProvider {
        public Language(DataGenerator gen) {
            super(gen, Trashed.MOD_ID, "en_us");
        }

        @Override
        protected void addTranslations() {
            add(TRASH_CAN.get(), "Trash Can");
            add(FLUID_TRASH_CAN.get(), "Trash Can");
            add("trashed.container.trashcan", "Trash Can");
        }
    }

    private static class ItemModels extends ItemModelProvider {
        public ItemModels(DataGenerator gen, ExistingFileHelper helper) {
            super(gen, Trashed.MOD_ID, helper);
        }

        @Override
        protected void registerModels() {
            makeTier(TRASH_CAN.get());
            makeTier(FLUID_TRASH_CAN.get());
        }

        private void makeTier(Block block) {
            String path = block.getRegistryName().getPath();
            getBuilder(path)
                    .parent(new ModelFile.UncheckedModelFile(modLoc("block/" + path))); //TODO: Ask tterrag about this...
        }

        @Override
        public String getName() {
            return "Item Models";
        }
    }

    private static class BlockStates extends BlockStateProvider {

        public BlockStates(DataGenerator gen, ExistingFileHelper helper) {
            super(gen, Trashed.MOD_ID, helper);
        }

        @Override
        protected void registerStatesAndModels() {
            makeTrash(TRASH_CAN.get());
            makeTrash(FLUID_TRASH_CAN.get(), mcLoc("block/acacia_log"));
        }

        private void makeTrash(Block block, ResourceLocation texture) {
            ModelFile model = models().getBuilder(block.getRegistryName().getPath())
                    .parent(models().getExistingFile(modLoc("block/trash_can")))
                    .texture("material", texture);
            getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder().modelFile(model).build());
        }

        private void makeTrash(Block block) {
            ModelFile model = models().getBuilder(block.getRegistryName().getPath())
                    .parent(models().getExistingFile(modLoc("block/trash_can")));
            getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder().modelFile(model).build());
        }
    }
}