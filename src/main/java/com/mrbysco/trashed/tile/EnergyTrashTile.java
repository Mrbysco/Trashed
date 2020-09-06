package com.mrbysco.trashed.tile;

import com.mrbysco.trashed.block.EnergyTrashBlock;
import com.mrbysco.trashed.init.TrashedRegistry;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnergyTrashTile extends TileEntity implements ITickableTileEntity  {
    protected EnergyStorage storage = new EnergyStorage(1000000);
    private final LazyOptional<IEnergyStorage> holder = LazyOptional.of(() -> storage);

    protected EnergyTrashTile(TileEntityType<?> type) {
        super(type);
    }

    public EnergyTrashTile() {
        this(TrashedRegistry.ENERGY_TRASH_TILE.get());
    }

    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
    {
        if (capability == CapabilityEnergy.ENERGY && world != null && getBlockState().getBlock() instanceof EnergyTrashBlock && getBlockState().get(EnergyTrashBlock.ENABLED))
            return holder.cast();

        return super.getCapability(capability, facing);
    }

    @Override
    public void tick() {
        if (this.world != null && !this.world.isRemote) {
            if(!isEmpty()) {
                this.storage.extractEnergy(this.storage.getEnergyStored(), false);
            }
        }
    }

    public boolean isEmpty() {
        return storage.getEnergyStored() < 1;
    }
}