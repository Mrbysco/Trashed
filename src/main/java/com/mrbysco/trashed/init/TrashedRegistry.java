package com.mrbysco.trashed.init;

import com.mrbysco.trashed.Trashed;
import com.mrbysco.trashed.block.EnergyTrashBlock;
import com.mrbysco.trashed.block.FluidTrashBlock;
import com.mrbysco.trashed.block.TrashBlock;
import com.mrbysco.trashed.tile.EnergyTrashBlockEntity;
import com.mrbysco.trashed.tile.FluidTrashBlockEntity;
import com.mrbysco.trashed.tile.TrashBlockEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TrashedRegistry {
    private static final Item.Properties itemProperties = new Item.Properties().tab(CreativeModeTab.TAB_MISC);

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Trashed.MOD_ID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Trashed.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Trashed.MOD_ID);

    public static final RegistryObject<Block> TRASH_CAN = BLOCKS.register("trash_can", () ->  new TrashBlock(blockBuilder()));
    public static final RegistryObject<Block> FLUID_TRASH_CAN = BLOCKS.register("fluid_trash_can", () ->  new FluidTrashBlock(blockBuilder()));
    public static final RegistryObject<Block> ENERGY_TRASH_CAN = BLOCKS.register("energy_trash_can", () ->  new EnergyTrashBlock(blockBuilder()));

    public static final RegistryObject<Item> TRASH_CAN_ITEM  = ITEMS.register("trash_can", () -> new BlockItem(TRASH_CAN.get(), itemProperties));
    public static final RegistryObject<Item> FLUID_TRASH_CAN_ITEM  = ITEMS.register("fluid_trash_can", () -> new BlockItem(FLUID_TRASH_CAN.get(), itemProperties));
    public static final RegistryObject<Item> ENERGY_TRASH_CAN_ITEM  = ITEMS.register("energy_trash_can", () -> new BlockItem(ENERGY_TRASH_CAN.get(), itemProperties));

    public static final RegistryObject<BlockEntityType<TrashBlockEntity>> TRASH_TILE = BLOCK_ENTITIES.register("trash_can", () -> BlockEntityType.Builder.of(TrashBlockEntity::new, TRASH_CAN.get()).build(null));
    public static final RegistryObject<BlockEntityType<TrashBlockEntity>> TRASH_SLAVE_TILE = BLOCK_ENTITIES.register("trash_slave_tile", () -> BlockEntityType.Builder.of(TrashBlockEntity::new, TRASH_CAN.get()).build(null));
    public static final RegistryObject<BlockEntityType<FluidTrashBlockEntity>> FLUID_TRASH_TILE = BLOCK_ENTITIES.register("fluid_trash_can", () -> BlockEntityType.Builder.of(FluidTrashBlockEntity::new, FLUID_TRASH_CAN.get()).build(null));
    public static final RegistryObject<BlockEntityType<EnergyTrashBlockEntity>> ENERGY_TRASH_TILE = BLOCK_ENTITIES.register("energy_trash_can", () -> BlockEntityType.Builder.of(EnergyTrashBlockEntity::new, ENERGY_TRASH_CAN.get()).build(null));

    private static Block.Properties blockBuilder() { return Block.Properties.of(Material.STONE).strength(2.0F, 6.0F); }
}
