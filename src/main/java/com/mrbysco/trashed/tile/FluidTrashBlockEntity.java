package com.mrbysco.trashed.tile;

import com.mrbysco.trashed.block.FluidTrashBlock;
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
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
    public CompoundTag save(CompoundTag tag) {
        tag = super.save(tag);
        tank.writeToNBT(tag);
        return tag;
    }

    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && level != null && getBlockState().getBlock() instanceof FluidTrashBlock && getBlockState().getValue(FluidTrashBlock.ENABLED)) {
            if (this.holder == null)
                this.holder = LazyOptional.of(() -> tank);
            return holder.cast();
        }

        return super.getCapability(capability, facing);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FluidTrashBlockEntity trashTile) {
        if (level != null) {
            if(!trashTile.tank.isEmpty()) {
                trashTile.tank.drain(trashTile.tank.getFluidAmount(), FluidAction.EXECUTE);
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