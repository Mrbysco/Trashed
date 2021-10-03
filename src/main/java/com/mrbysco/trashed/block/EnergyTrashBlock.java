package com.mrbysco.trashed.block;

import com.mrbysco.trashed.block.base.TrashBase;
import com.mrbysco.trashed.init.TrashedRegistry;
import com.mrbysco.trashed.tile.EnergyTrashBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;

public class EnergyTrashBlock extends TrashBase implements SimpleWaterloggedBlock {
    
    public EnergyTrashBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        if(state.getValue(ENABLED)) {
            return SINGLE_SHAPE;
        } else {
            return SINGLE_DISABLED_SHAPE;
        }
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity tileentity = worldIn.getBlockEntity(pos);
            if (tileentity instanceof EnergyTrashBlockEntity) {
                worldIn.updateNeighbourForOutputSignal(pos, this);
            }

            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyTrashBlockEntity(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTrashTicker(level, blockEntityType, TrashedRegistry.ENERGY_TRASH_TILE.get());
    }

    @Nullable
    protected static <T extends BlockEntity> BlockEntityTicker<T> createTrashTicker(Level level, BlockEntityType<T> p_151989_, BlockEntityType<? extends EnergyTrashBlockEntity> p_151990_) {
        return level.isClientSide ? null : createTickerHelper(p_151989_, p_151990_, EnergyTrashBlockEntity::serverTick);
    }
}
