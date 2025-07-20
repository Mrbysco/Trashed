package com.mrbysco.trashed.block;

import com.mojang.serialization.MapCodec;
import com.mrbysco.trashed.block.base.TrashBase;
import com.mrbysco.trashed.blockentity.TrashBlockEntity;
import com.mrbysco.trashed.blockentity.TrashSlaveBlockEntity;
import com.mrbysco.trashed.init.TrashedRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class TrashBlock extends TrashBase implements SimpleWaterloggedBlock {
	public static final MapCodec<TrashBlock> CODEC = simpleCodec(TrashBlock::new);

	private static final VoxelShape BOTTOM_INSIDE = Block.box(2.5D, 0.0D, 2.5D, 13.5D, 16.0D, 13.5D);
	private static final VoxelShape BOTTOM_INSIDE_HOLLOW = Block.box(2.5D, 0.0D, 2.5D, 13.5D, 15.0D, 13.5D);
	private static final VoxelShape BOTTOM_OUTSIDE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

	private static final VoxelShape TOP_SHAPE = Shapes.join(SINGLE_OUTSIDE, SINGLE_INSIDE, BooleanOp.ONLY_FIRST);
	private static final VoxelShape TOP_DISABLED_SHAPE = Shapes.join(SINGLE_OUTSIDE, SINGLE_INSIDE_HOLLOW, BooleanOp.ONLY_FIRST);
	private static final VoxelShape BOTTOM_SHAPE = Shapes.or(BOTTOM_PLATE, Shapes.join(BOTTOM_OUTSIDE, BOTTOM_INSIDE, BooleanOp.ONLY_FIRST));
	private static final VoxelShape BOTTOM_DISABLED_SHAPE = Shapes.or(BOTTOM_PLATE, Shapes.join(BOTTOM_OUTSIDE, BOTTOM_INSIDE_HOLLOW, BooleanOp.ONLY_FIRST));

	public static final EnumProperty<TrashType> TYPE = EnumProperty.create("type", TrashType.class);

	public TrashBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE, TrashType.SINGLE).setValue(ENABLED, true).setValue(WATERLOGGED, false));
	}

	@Override
	protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return CODEC;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		if (state.getValue(ENABLED)) {
			return switch (state.getValue(TYPE)) {
				default -> SINGLE_SHAPE;
				case BOTTOM -> BOTTOM_SHAPE;
				case TOP -> TOP_SHAPE;
			};
		} else {
			return switch (state.getValue(TYPE)) {
				default -> SINGLE_DISABLED_SHAPE;
				case BOTTOM -> BOTTOM_DISABLED_SHAPE;
				case TOP -> TOP_DISABLED_SHAPE;
			};
		}
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
	                                      InteractionHand hand, BlockHitResult result) {
		if (!level.isClientSide) {
			if (stack.getItem() == TrashedRegistry.TRASH_CAN_ITEM.get() && result.getDirection().equals(Direction.UP)) {
				return InteractionResult.FAIL;
			} else {
				BlockEntity tile = getTrashBlockEntity(level, state, pos);
				if (tile instanceof TrashBlockEntity) {
					player.openMenu((TrashBlockEntity) tile, pos);
				}
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack) {
		super.playerDestroy(level, player, pos, state, blockEntity, stack);

		BlockPos bePos = pos;
		if (state.getValue(TYPE) == TrashType.BOTTOM) {
			level.removeBlockEntity(pos.above());
			level.setBlockAndUpdate(pos.above(), level.getBlockState(pos.above()).setValue(TYPE, TrashType.SINGLE));
			BlockEntity tile = getTrashBlockEntity(level, state, pos);
			BlockEntity tile2 = getTrashBlockEntity(level, state, pos.above());
			if (tile instanceof TrashBlockEntity oldBE && tile2 instanceof TrashBlockEntity newBE) {
				newBE.setItems(oldBE.getItems());
			}
			bePos = pos.above();
		} else if (state.getValue(TYPE) == TrashType.TOP && !level.isEmptyBlock(pos.below())) {
			level.setBlockAndUpdate(pos.below(), level.getBlockState(pos.below()).setValue(TYPE, TrashType.SINGLE));
		}

		if (state.getValue(TYPE) == TrashType.SINGLE) {
			BlockEntity tile = getTrashBlockEntity(level, state, bePos);
			if (tile instanceof TrashBlockEntity) {
				Containers.dropContents(level, bePos, (TrashBlockEntity) tile);
				level.updateNeighbourForOutputSignal(getTrashPos(state, bePos), this);
			}
		}
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entityIn, InsideBlockEffectApplier effectApplier) {
		BlockEntity blockEntity = getTrashBlockEntity(level, state, pos);
		if (blockEntity instanceof TrashBlockEntity) {
			((TrashBlockEntity) blockEntity).onEntityCollision(entityIn);
		}
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
			if (state.getValue(TYPE) == TrashType.BOTTOM) {
				boolean flag2 = !level.hasNeighborSignal(pos.above());
				if (flag2) {
					level.setBlockAndUpdate(pos, state.setValue(ENABLED, Boolean.valueOf(flag)));
					BlockState state2 = level.getBlockState(pos.above());
					level.setBlockAndUpdate(pos.above(), state2.setValue(ENABLED, flag));
				}
			} else if (state.getValue(TYPE) == TrashType.TOP) {
				boolean flag2 = !level.hasNeighborSignal(pos.below());
				if (flag2) {
					level.setBlockAndUpdate(pos, state.setValue(ENABLED, Boolean.valueOf(flag)));
					BlockState state2 = level.getBlockState(pos.below());
					level.setBlockAndUpdate(pos.below(), state2.setValue(ENABLED, flag));
				}
			} else {
				level.setBlockAndUpdate(pos, state.setValue(ENABLED, Boolean.valueOf(flag)));
			}
		}
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @org.jetbrains.annotations.Nullable Orientation orientation, boolean movedByPiston) {
		this.updateState(level, pos, state);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
		return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(getTrashBlockEntity(level, state, pos));
	}

	public BlockPos getTrashPos(BlockState state, BlockPos pos) {
		if (state.getValue(TYPE) == TrashType.TOP) {
			return pos.below();
		} else {
			return pos;
		}
	}

	public BlockEntity getTrashBlockEntity(Level level, BlockState state, BlockPos pos) {
		if (state.getBlock() == this) {
			if (state.getValue(TYPE) == TrashType.TOP) {
				return level.getBlockEntity(pos.below());
			} else {
				return level.getBlockEntity(pos);
			}
		}
		return null;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, WATERLOGGED, ENABLED, TYPE);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		Level level = context.getLevel();
		BlockState bottomBlock = level.getBlockState(pos.below());
		if (bottomBlock.getBlock() == this && bottomBlock.getValue(TYPE) == TrashType.SINGLE) {
			level.setBlockAndUpdate(pos.below(), bottomBlock.setValue(TYPE, TrashType.BOTTOM));
			return this.defaultBlockState().setValue(FACING, bottomBlock.getValue(FACING)).setValue(ENABLED, bottomBlock.getValue(ENABLED)).setValue(TYPE, TrashType.TOP);
		} else {
			return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(ENABLED, true);
		}
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		if (state.getValue(TYPE) != TrashType.TOP) {
			return new TrashBlockEntity(pos, state);
		} else {
			return new TrashSlaveBlockEntity(pos, state);
		}
	}

	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
		return createTrashTicker(level, blockEntityType, TrashedRegistry.TRASH_TILE.get());
	}

	@Nullable
	protected static <T extends BlockEntity> BlockEntityTicker<T> createTrashTicker(Level level, BlockEntityType<T> p_151989_, BlockEntityType<? extends TrashBlockEntity> p_151990_) {
		return level.isClientSide ? null : createTickerHelper(p_151989_, p_151990_, TrashBlockEntity::serverTick);
	}
}