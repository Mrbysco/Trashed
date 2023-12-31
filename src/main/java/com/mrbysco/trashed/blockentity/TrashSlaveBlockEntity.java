package com.mrbysco.trashed.blockentity;

import com.mrbysco.trashed.init.TrashedRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TrashSlaveBlockEntity extends BlockEntity {

	protected TrashSlaveBlockEntity(BlockEntityType<?> entityType, BlockPos pos, BlockState state) {
		super(entityType, pos, state);
	}

	public TrashSlaveBlockEntity(BlockPos pos, BlockState state) {
		this(TrashedRegistry.TRASH_SLAVE_TILE.get(), pos, state);
	}
}
