package com.mrbysco.trashed.blockentity;

import com.mrbysco.trashed.init.TrashedRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TrashSlaveBlockEntity extends BlockEntity {

	protected TrashSlaveBlockEntity(BlockEntityType<?> entityType, BlockPos pos, BlockState state) {
		super(entityType, pos, state);
	}

	public TrashSlaveBlockEntity(BlockPos pos, BlockState state) {
		this(TrashedRegistry.TRASH_SLAVE_TILE.get(), pos, state);
	}

	@NotNull
	@Override
	public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		return this.level.getBlockEntity(this.worldPosition.below()).getCapability(cap, side);
	}
}
