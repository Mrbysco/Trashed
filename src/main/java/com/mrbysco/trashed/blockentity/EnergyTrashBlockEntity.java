package com.mrbysco.trashed.blockentity;

import com.mrbysco.trashed.init.TrashedRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.registries.VanillaRegistries;
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
	public void loadAdditional(CompoundTag tag, HolderLookup.Provider lookupProvider) {
		super.loadAdditional(tag, lookupProvider);
	}

	@Override
	public void saveAdditional(CompoundTag tag, HolderLookup.Provider lookupProvider) {
		super.saveAdditional(tag, lookupProvider);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
		if (pkt.getTag() != null)
			loadAdditional(pkt.getTag(), lookupProvider);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider lookupProvider) {
		CompoundTag tag = new CompoundTag();
		this.saveAdditional(tag, lookupProvider);
		return tag;
	}

	@Override
	public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
		this.loadAdditional(tag, lookupProvider);
	}

	@Override
	public CompoundTag getPersistentData() {
		CompoundTag nbt = new CompoundTag();
		this.saveAdditional(nbt, this.level != null ? this.level.registryAccess() : VanillaRegistries.createLookup());
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