package com.mrbysco.trashed.block;

import com.mrbysco.trashed.block.base.TrashBase;
import com.mrbysco.trashed.blockentity.FluidTrashBlockEntity;
import com.mrbysco.trashed.init.TrashedRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidUtil;

import javax.annotation.Nullable;
import java.util.List;

public class FluidTrashBlock extends TrashBase implements SimpleWaterloggedBlock {

    public FluidTrashBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ENABLED, true).setValue(WATERLOGGED, false));
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
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (!worldIn.isClientSide) {
            FluidUtil.interactWithFluidHandler(player, hand, worldIn, pos, result.getDirection());
        }
        return super.use(state, worldIn, pos, player, hand, result);
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = worldIn.getBlockEntity(pos);
            if (blockEntity instanceof FluidTrashBlockEntity) {
                worldIn.updateNeighbourForOutputSignal(pos, this);
            }

            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }

    /**
     * Power section
     */

    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (oldState.getBlock() != state.getBlock()) {
            this.updateState(worldIn, pos, state);
        }
    }

    private void updateState(Level worldIn, BlockPos pos, BlockState state) {
        boolean flag = !worldIn.hasNeighborSignal(pos);
        if (flag != state.getValue(ENABLED)) {
            worldIn.setBlockAndUpdate(pos, state.setValue(ENABLED, Boolean.valueOf(flag)));
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        this.updateState(worldIn, pos, state);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidTrashBlockEntity(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTrashTicker(level, blockEntityType, TrashedRegistry.FLUID_TRASH_TILE.get());
    }

    @Nullable
    protected static <T extends BlockEntity> BlockEntityTicker<T> createTrashTicker(Level level, BlockEntityType<T> p_151989_, BlockEntityType<? extends FluidTrashBlockEntity> p_151990_) {
        return level.isClientSide ? null : createTickerHelper(p_151989_, p_151990_, FluidTrashBlockEntity::serverTick);
    }
}
