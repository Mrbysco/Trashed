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
import net.neoforged.neoforge.energy.IEnergyStorage;

public class EnergyTrashBlockEntity extends BlockEntity {

	protected EnergyTrashBlockEntity(BlockEntityType<?> entityType, BlockPos pos, BlockState state) {
		super(entityType, pos, state);
	}

	public EnergyTrashBlockEntity(BlockPos pos, BlockState state) {
		this(TrashedRegistry.ENERGY_TRASH_TILE.get(), pos, state);
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


	public static void serverTick(Level level, BlockPos pos, BlockState state, EnergyTrashBlockEntity trashBlockEntity) {
		if (level != null) {
			if (!trashBlockEntity.isEmpty()) {
				IEnergyStorage energyStorage = trashBlockEntity.getStorage();
				if (energyStorage != null)
					energyStorage.receiveEnergy(energyStorage.getEnergyStored(), false);
			}
		}
	}

	public boolean isEmpty() {
		IEnergyStorage energyStorage = getStorage();
		if (energyStorage == null) return false;
		return energyStorage.getEnergyStored() < 1;
	}

	private IEnergyStorage getStorage() {
		if (level == null) return null;
		return level.getCapability(Capabilities.EnergyStorage.BLOCK, getBlockPos(), null);
	}
}