package com.mrbysco.trashed.tile;

import com.mrbysco.trashed.block.EnergyTrashBlock;
import com.mrbysco.trashed.init.TrashedRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnergyTrashBlockEntity extends BlockEntity {
    protected EnergyStorage storage = new EnergyStorage(1000000);
    private LazyOptional<IEnergyStorage> holder = LazyOptional.of(() -> storage);

    protected EnergyTrashBlockEntity(BlockEntityType<?> entityType, BlockPos pos, BlockState state) {
        super(entityType, pos, state);
    }

    public EnergyTrashBlockEntity(BlockPos pos, BlockState state) {
        this(TrashedRegistry.ENERGY_TRASH_TILE.get(), pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        storage.deserializeNBT(tag);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag = super.save(tag);
        tag.put("storage", storage.serializeNBT());
        return tag;
    }

    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        if (capability == CapabilityEnergy.ENERGY && level != null && getBlockState().getBlock() instanceof EnergyTrashBlock && getBlockState().getValue(EnergyTrashBlock.ENABLED)) {
            if (this.holder == null)
                this.holder = LazyOptional.of(() -> storage);
            return holder.cast();
        }

        return super.getCapability(capability, facing);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EnergyTrashBlockEntity trashTile) {
        if (level != null) {
            if(!trashTile.isEmpty()) {
                trashTile.storage.extractEnergy(trashTile.storage.getEnergyStored(), false);
            }
        }
    }

    public boolean isEmpty() {
        return storage.getEnergyStored() < 1;
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