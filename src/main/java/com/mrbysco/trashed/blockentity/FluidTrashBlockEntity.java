package com.mrbysco.trashed.blockentity;

import com.mrbysco.trashed.init.TrashedRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class FluidTrashBlockEntity extends BlockEntity {

	protected FluidTrashBlockEntity(BlockEntityType<?> entityType, BlockPos pos, BlockState state) {
		super(entityType, pos, state);
	}

	public FluidTrashBlockEntity(BlockPos pos, BlockState state) {
		this(TrashedRegistry.FLUID_TRASH_TILE.get(), pos, state);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
	}

	@Override
	public void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
		this.load(packet.getTag());
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag nbt = new CompoundTag();
		this.saveAdditional(nbt);
		return nbt;
	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		this.load(tag);
	}

	@Override
	public CompoundTag getPersistentData() {
		CompoundTag nbt = new CompoundTag();
		this.saveAdditional(nbt);
		return nbt;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, FluidTrashBlockEntity trashBlockEntity) {
		if (level != null) {
			FluidTank handler = trashBlockEntity.getStorage();
			if (handler != null && !handler.isEmpty()) {
				handler.drain(handler.getFluidAmount(), FluidAction.EXECUTE);
			}
		}
	}

	private FluidTank getStorage() {
		if (level != null && level.getCapability(Capabilities.FluidHandler.BLOCK, getBlockPos(), null) instanceof FluidTank tank) {
			return tank;
		}
		return null;
	}
}