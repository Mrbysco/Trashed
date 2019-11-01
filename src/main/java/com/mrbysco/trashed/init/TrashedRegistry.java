package com.mrbysco.trashed.init;

import com.mrbysco.trashed.Trashed;
import com.mrbysco.trashed.block.TrashBlock;
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

    public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, Trashed.MOD_ID);
    public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, Trashed.MOD_ID);
    public static final DeferredRegister<TileEntityType<?>> TILES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, Trashed.MOD_ID);

    public static final RegistryObject<Block> TRASH_CAN = BLOCKS.register("trash_can", () ->  new TrashBlock(blockBuilder()));
    public static final RegistryObject<Item> TRASH_CAN_ITEM  = ITEMS.register("trash_can", () -> new BlockItem(TRASH_CAN.get(), itemProperties));
    public static final RegistryObject<TileEntityType<TrashTile>> TRASH_TILE = TILES.register("trash_can", () -> TileEntityType.Builder.create(() -> new TrashTile(), TRASH_CAN.get()).build(null));
    public static final RegistryObject<TileEntityType<TrashTile>> TRASH_SLAVE_TILE = TILES.register("trash_slave_tile", () -> TileEntityType.Builder.create(() -> new TrashTile(), TRASH_CAN.get()).build(null));

    private static Block.Properties blockBuilder() { return Block.Properties.create(Material.ROCK).hardnessAndResistance(2.0F, 6.0F); }
}
