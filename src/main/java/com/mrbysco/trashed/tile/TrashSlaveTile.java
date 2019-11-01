package com.mrbysco.trashed.tile;

import com.mrbysco.trashed.block.TrashBlock;
import com.mrbysco.trashed.init.TrashedRegistry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TrashSlaveTile extends TileEntity {
    public TrashSlaveTile(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public TrashSlaveTile() {
        this(TrashedRegistry.TRASH_SLAVE_TILE.get());
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return this.world.getTileEntity(this.pos.down()).getCapability(cap, side);
    }
}
