package com.mrbysco.trashed.tile;

import com.mrbysco.trashed.block.FluidTrashBlock;
import com.mrbysco.trashed.init.TrashedRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidTrashTile extends TileEntity implements ITickableTileEntity  {
    protected FluidTank tank = new FluidTank(1000000);
    private LazyOptional<IFluidHandler> holder = LazyOptional.of(() -> tank);

    protected FluidTrashTile(TileEntityType<?> type) {
        super(type);
    }

    public FluidTrashTile() {
        this(TrashedRegistry.FLUID_TRASH_TILE.get());
    }

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);
        tank.readFromNBT(tag);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        tag = super.write(tag);
        tank.writeToNBT(tag);
        return tag;
    }

    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && world != null && getBlockState().getBlock() instanceof FluidTrashBlock && getBlockState().get(FluidTrashBlock.ENABLED))
            return holder.cast();

        return super.getCapability(capability, facing);
    }

    @Override
    public void tick() {
        if (this.world != null && !this.world.isRemote) {
            if(!this.tank.isEmpty()) {
                this.tank.drain(this.tank.getFluidAmount(), FluidAction.EXECUTE);
            }
        }
    }

    @Override
    public void updateContainingBlockInfo() {
        super.updateContainingBlockInfo();
        if (this.holder != null) {
            this.holder.invalidate();
            this.holder = null;
        }
    }

    @Override
    public void remove() {
        super.remove();

        if(holder != null) {
            holder.invalidate();
        }
    }
}