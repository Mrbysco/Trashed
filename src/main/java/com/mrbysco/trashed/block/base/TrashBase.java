package com.mrbysco.trashed.block.base;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public abstract class TrashBase extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock, EntityBlock {
	protected static final VoxelShape BOTTOM_PLATE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 0.5D, 14.0D);
	protected static final VoxelShape SINGLE_INSIDE = Block.box(2.5D, 0.0D, 2.5D, 13.5D, 13.0D, 13.5D);
	protected static final VoxelShape SINGLE_INSIDE_HOLLOW = Block.box(2.5D, 0.0D, 2.5D, 13.5D, 12.0D, 13.5D);
	protected static final VoxelShape SINGLE_OUTSIDE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

	protected static final VoxelShape SINGLE_SHAPE = Shapes.or(BOTTOM_PLATE, Shapes.join(SINGLE_OUTSIDE, SINGLE_INSIDE, BooleanOp.ONLY_FIRST));
	protected static final VoxelShape SINGLE_DISABLED_SHAPE = Shapes.or(BOTTOM_PLATE, Shapes.join(SINGLE_OUTSIDE, SINGLE_INSIDE_HOLLOW, BooleanOp.ONLY_FIRST));

	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

	public TrashBase(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ENABLED, true).setValue(WATERLOGGED, false));
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
		return true;
	}

	/**
	 * WaterLogging section
	 */
	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor levelAccessor, BlockPos currentPos, BlockPos facingPos) {
		if (stateIn.getValue(WATERLOGGED)) {
			levelAccessor.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		return super.updateShape(stateIn, facing, facingState, levelAccessor, currentPos, facingPos);
	}

	@Override
	public boolean canPlaceLiquid(Player player, BlockGetter level, BlockPos pos, BlockState state, Fluid fluidIn) {
		return SimpleWaterloggedBlock.super.canPlaceLiquid(player, level, pos, state, fluidIn);
	}

	@Override
	public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidStateIn) {
		return SimpleWaterloggedBlock.super.placeLiquid(level, pos, state, fluidStateIn);
	}

	@SuppressWarnings("deprecation")
	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	/**
	 * Rotation section
	 */
	@SuppressWarnings("deprecation")
	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@SuppressWarnings("deprecation")
	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, WATERLOGGED, ENABLED);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	/**
	 * Power section
	 */

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (oldState.getBlock() != state.getBlock()) {
			this.updateState(level, pos, state);
		}
	}

	private void updateState(Level level, BlockPos pos, BlockState state) {
		boolean flag = !level.hasNeighborSignal(pos);
		if (flag != state.getValue(ENABLED)) {
			level.setBlockAndUpdate(pos, state.setValue(ENABLED, flag));
		}
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		this.updateState(level, pos, state);
	}

	@Nullable
	protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> blockEntityType, BlockEntityType<E> blockEntityType1, BlockEntityTicker<? super E> entityTicker) {
		return blockEntityType1 == blockEntityType ? (BlockEntityTicker<A>) entityTicker : null;
	}
}
