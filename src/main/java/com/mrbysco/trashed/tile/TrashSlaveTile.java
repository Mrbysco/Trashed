package com.mrbysco.trashed.tile;

import com.mrbysco.trashed.init.TrashedRegistry;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TrashSlaveTile extends TileEntity {

    private BlockPos masterPos;

    public TrashSlaveTile(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public TrashSlaveTile() {
        this(TrashedRegistry.TRASH_SLAVE_TILE.get());
        this.masterPos = this.getPos().down();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return this.world.getTileEntity(masterPos).getCapability(cap, side);
    }

    public void read(CompoundNBT compound) {
        super.read(compound);
        this.masterPos = BlockPos.fromLong(compound.getLong("masterPos"));
    }

    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        compound.putLong("masterPos", this.masterPos.toLong());
        return compound;
    }
}
