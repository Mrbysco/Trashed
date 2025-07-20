package com.mrbysco.trashed.init;

import com.mrbysco.trashed.Trashed;
import com.mrbysco.trashed.block.EnergyTrashBlock;
import com.mrbysco.trashed.block.FluidTrashBlock;
import com.mrbysco.trashed.block.TrashBlock;
import com.mrbysco.trashed.blockentity.EnergyTrashBlockEntity;
import com.mrbysco.trashed.blockentity.FluidTrashBlockEntity;
import com.mrbysco.trashed.blockentity.TrashBlockEntity;
import com.mrbysco.trashed.item.TrashCanItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class TrashedRegistry {
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Trashed.MOD_ID);
	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Trashed.MOD_ID);
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Trashed.MOD_ID);

	public static final DeferredBlock<TrashBlock> TRASH_CAN = BLOCKS.registerBlock("trash_can", TrashBlock::new, blockBuilder());
	public static final DeferredBlock<FluidTrashBlock> FLUID_TRASH_CAN = BLOCKS.registerBlock("fluid_trash_can", FluidTrashBlock::new, blockBuilder());
	public static final DeferredBlock<EnergyTrashBlock> ENERGY_TRASH_CAN = BLOCKS.registerBlock("energy_trash_can", EnergyTrashBlock::new, blockBuilder());

	public static final DeferredItem<BlockItem> TRASH_CAN_ITEM = ITEMS.registerItem("trash_can", (properties) -> new TrashCanItem(TRASH_CAN.get(), properties));
	public static final DeferredItem<BlockItem> FLUID_TRASH_CAN_ITEM = ITEMS.registerSimpleBlockItem("fluid_trash_can", FLUID_TRASH_CAN);
	public static final DeferredItem<BlockItem> ENERGY_TRASH_CAN_ITEM = ITEMS.registerSimpleBlockItem("energy_trash_can", ENERGY_TRASH_CAN);

	public static final Supplier<BlockEntityType<TrashBlockEntity>> TRASH_TILE = BLOCK_ENTITY_TYPES.register("trash_can", () -> new BlockEntityType<>(TrashBlockEntity::new, TRASH_CAN.get()));
	public static final Supplier<BlockEntityType<TrashBlockEntity>> TRASH_SLAVE_TILE = BLOCK_ENTITY_TYPES.register("trash_slave_tile", () -> new BlockEntityType<>(TrashBlockEntity::new, TRASH_CAN.get()));
	public static final Supplier<BlockEntityType<FluidTrashBlockEntity>> FLUID_TRASH_TILE = BLOCK_ENTITY_TYPES.register("fluid_trash_can", () -> new BlockEntityType<>(FluidTrashBlockEntity::new, FLUID_TRASH_CAN.get()));
	public static final Supplier<BlockEntityType<EnergyTrashBlockEntity>> ENERGY_TRASH_TILE = BLOCK_ENTITY_TYPES.register("energy_trash_can", () -> new BlockEntityType<>(EnergyTrashBlockEntity::new, ENERGY_TRASH_CAN.get()));

	private static Block.Properties blockBuilder() {
		return Block.Properties.of().mapColor(MapColor.STONE).strength(2.0F, 6.0F);
	}
}
