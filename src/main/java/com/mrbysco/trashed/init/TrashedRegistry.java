package com.mrbysco.trashed.init;

import com.mrbysco.trashed.Trashed;
import com.mrbysco.trashed.block.EnergyTrashBlock;
import com.mrbysco.trashed.block.FluidTrashBlock;
import com.mrbysco.trashed.block.TrashBlock;
import com.mrbysco.trashed.blockentity.EnergyTrashBlockEntity;
import com.mrbysco.trashed.blockentity.FluidTrashBlockEntity;
import com.mrbysco.trashed.blockentity.TrashBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class TrashedRegistry {
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Trashed.MOD_ID);
	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Trashed.MOD_ID);
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Trashed.MOD_ID);

	public static final DeferredBlock<TrashBlock> TRASH_CAN = BLOCKS.register("trash_can", () -> new TrashBlock(blockBuilder()));
	public static final DeferredBlock<FluidTrashBlock> FLUID_TRASH_CAN = BLOCKS.register("fluid_trash_can", () -> new FluidTrashBlock(blockBuilder()));
	public static final DeferredBlock<EnergyTrashBlock> ENERGY_TRASH_CAN = BLOCKS.register("energy_trash_can", () -> new EnergyTrashBlock(blockBuilder()));

	public static final DeferredItem<BlockItem> TRASH_CAN_ITEM = ITEMS.registerSimpleBlockItem("trash_can", TRASH_CAN);
	public static final DeferredItem<BlockItem> FLUID_TRASH_CAN_ITEM = ITEMS.registerSimpleBlockItem("fluid_trash_can", FLUID_TRASH_CAN);
	public static final DeferredItem<BlockItem> ENERGY_TRASH_CAN_ITEM = ITEMS.registerSimpleBlockItem("energy_trash_can", ENERGY_TRASH_CAN);

	public static final Supplier<BlockEntityType<TrashBlockEntity>> TRASH_TILE = BLOCK_ENTITY_TYPES.register("trash_can", () -> BlockEntityType.Builder.of(TrashBlockEntity::new, TRASH_CAN.get()).build(null));
	public static final Supplier<BlockEntityType<TrashBlockEntity>> TRASH_SLAVE_TILE = BLOCK_ENTITY_TYPES.register("trash_slave_tile", () -> BlockEntityType.Builder.of(TrashBlockEntity::new, TRASH_CAN.get()).build(null));
	public static final Supplier<BlockEntityType<FluidTrashBlockEntity>> FLUID_TRASH_TILE = BLOCK_ENTITY_TYPES.register("fluid_trash_can", () -> BlockEntityType.Builder.of(FluidTrashBlockEntity::new, FLUID_TRASH_CAN.get()).build(null));
	public static final Supplier<BlockEntityType<EnergyTrashBlockEntity>> ENERGY_TRASH_TILE = BLOCK_ENTITY_TYPES.register("energy_trash_can", () -> BlockEntityType.Builder.of(EnergyTrashBlockEntity::new, ENERGY_TRASH_CAN.get()).build(null));

	private static Block.Properties blockBuilder() {
		return Block.Properties.ofFullCopy(Blocks.STONE).strength(2.0F, 6.0F);
	}
}
