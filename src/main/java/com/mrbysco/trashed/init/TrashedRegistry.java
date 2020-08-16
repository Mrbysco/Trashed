package com.mrbysco.trashed.init;

import com.mrbysco.trashed.Trashed;
import com.mrbysco.trashed.block.EnergyTrashBlock;
import com.mrbysco.trashed.block.FluidTrashBlock;
import com.mrbysco.trashed.block.TrashBlock;
import com.mrbysco.trashed.tile.EnergyTrashTile;
import com.mrbysco.trashed.tile.FluidTrashTile;
import com.mrbysco.trashed.tile.TrashTile;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TrashedRegistry {
    private static final Item.Properties itemProperties = new Item.Properties().group(ItemGroup.MISC);

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Trashed.MOD_ID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Trashed.MOD_ID);
    public static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Trashed.MOD_ID);

    public static final RegistryObject<Block> TRASH_CAN = BLOCKS.register("trash_can", () ->  new TrashBlock(blockBuilder()));
    public static final RegistryObject<Block> FLUID_TRASH_CAN = BLOCKS.register("fluid_trash_can", () ->  new FluidTrashBlock(blockBuilder()));
    public static final RegistryObject<Block> ENERGY_TRASH_CAN = BLOCKS.register("energy_trash_can", () ->  new EnergyTrashBlock(blockBuilder()));

    public static final RegistryObject<Item> TRASH_CAN_ITEM  = ITEMS.register("trash_can", () -> new BlockItem(TRASH_CAN.get(), itemProperties));
    public static final RegistryObject<Item> FLUID_TRASH_CAN_ITEM  = ITEMS.register("fluid_trash_can", () -> new BlockItem(FLUID_TRASH_CAN.get(), itemProperties));
    public static final RegistryObject<Item> ENERGY_TRASH_CAN_ITEM  = ITEMS.register("energy_trash_can", () -> new BlockItem(ENERGY_TRASH_CAN.get(), itemProperties));

    public static final RegistryObject<TileEntityType<TrashTile>> TRASH_TILE = TILES.register("trash_can", () -> TileEntityType.Builder.create(() -> new TrashTile(), TRASH_CAN.get()).build(null));
    public static final RegistryObject<TileEntityType<TrashTile>> TRASH_SLAVE_TILE = TILES.register("trash_slave_tile", () -> TileEntityType.Builder.create(() -> new TrashTile(), TRASH_CAN.get()).build(null));
    public static final RegistryObject<TileEntityType<FluidTrashTile>> FLUID_TRASH_TILE = TILES.register("fluid_trash_can", () -> TileEntityType.Builder.create(() -> new FluidTrashTile(), FLUID_TRASH_CAN.get()).build(null));
    public static final RegistryObject<TileEntityType<EnergyTrashTile>> ENERGY_TRASH_TILE = TILES.register("energy_trash_can", () -> TileEntityType.Builder.create(() -> new EnergyTrashTile(), ENERGY_TRASH_CAN.get()).build(null));

    private static Block.Properties blockBuilder() { return Block.Properties.create(Material.ROCK).hardnessAndResistance(2.0F, 6.0F); }
}
