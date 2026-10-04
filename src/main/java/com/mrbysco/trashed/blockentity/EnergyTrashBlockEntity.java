package com.mrbysco.trashed.blockentity;

import com.mrbysco.trashed.init.TrashedRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.VoidingEnergyHandler;

public class EnergyTrashBlockEntity extends BlockEntity {

	protected EnergyTrashBlockEntity(BlockEntityType<?> entityType, BlockPos pos, BlockState state) {
		super(entityType, pos, state);
	}

	public EnergyTrashBlockEntity(BlockPos pos, BlockState state) {
		this(TrashedRegistry.ENERGY_TRASH_TILE.get(), pos, state);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return this.saveWithoutMetadata(registries);
	}

	public EnergyHandler getStorage() {
		return VoidingEnergyHandler.INSTANCE;
	}
}