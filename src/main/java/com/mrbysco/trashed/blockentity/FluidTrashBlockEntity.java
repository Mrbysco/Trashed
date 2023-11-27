package com.mrbysco.trashed.blockentity;

import com.mrbysco.trashed.block.FluidTrashBlock;
import com.mrbysco.trashed.init.TrashedRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.capabilities.Capabilities;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidTrashBlockEntity extends BlockEntity {
	protected FluidTank tank = new FluidTank(1000000);
	private LazyOptional<IFluidHandler> holder = LazyOptional.of(() -> tank);

	protected FluidTrashBlockEntity(BlockEntityType<?> entityType, BlockPos pos, BlockState state) {
		super(entityType, pos, state);
	}

	public FluidTrashBlockEntity(BlockPos pos, BlockState state) {
		this(TrashedRegistry.FLUID_TRASH_TILE.get(), pos, state);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		tank.readFromNBT(tag);
	}

	@Override
	public void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tank.writeToNBT(tag);
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

	@Override
	@NotNull
	public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
		if (capability == Capabilities.FLUID_HANDLER && level != null && getBlockState().getBlock() instanceof FluidTrashBlock && getBlockState().getValue(FluidTrashBlock.ENABLED)) {
			if (this.holder == null)
				this.holder = LazyOptional.of(() -> tank);
			return holder.cast();
		}

		return super.getCapability(capability, facing);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, FluidTrashBlockEntity trashBlockEntity) {
		if (level != null) {
			if (!trashBlockEntity.tank.isEmpty()) {
				trashBlockEntity.tank.drain(trashBlockEntity.tank.getFluidAmount(), FluidAction.EXECUTE);
			}
		}
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		if (holder != null) {
			this.holder.invalidate();
			this.holder = null;
		}
	}
}