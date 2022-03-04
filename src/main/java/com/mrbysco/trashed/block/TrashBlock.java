package com.mrbysco.trashed.block;

import com.mrbysco.trashed.block.base.TrashBase;
import com.mrbysco.trashed.blockentity.TrashBlockEntity;
import com.mrbysco.trashed.blockentity.TrashSlaveBlockEntity;
import com.mrbysco.trashed.init.TrashedRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public class TrashBlock extends TrashBase implements SimpleWaterloggedBlock {
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
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
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
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
		if (!worldIn.isClientSide) {
			if (player.getItemInHand(hand).getItem() == TrashedRegistry.TRASH_CAN_ITEM.get() && result.getDirection().equals(Direction.UP)) {
				return InteractionResult.FAIL;
			} else {
				BlockEntity tile = getTrashBlockEntity(worldIn, state, pos);
				if (tile instanceof TrashBlockEntity) {
					NetworkHooks.openGui((ServerPlayer) player, (TrashBlockEntity) tile, pos);
				}
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!worldIn.isClientSide && state.getBlock() != newState.getBlock()) {
			BlockPos tePos = pos;
			if (state.getValue(TYPE) == TrashType.BOTTOM) {
				worldIn.removeBlockEntity(pos.above());
				worldIn.setBlockAndUpdate(pos.above(), worldIn.getBlockState(pos.above()).setValue(TYPE, TrashType.SINGLE));
				BlockEntity tile = getTrashBlockEntity(worldIn, state, pos);
				BlockEntity tile2 = getTrashBlockEntity(worldIn, state, pos.above());
				if (tile != null && tile instanceof TrashBlockEntity oldBE && tile2 != null && tile2 instanceof TrashBlockEntity newBE) {
					newBE.setItems(oldBE.getItems());
				}
				tePos = pos.above();
			} else if (state.getValue(TYPE) == TrashType.TOP && !worldIn.isEmptyBlock(pos.below())) {
				worldIn.setBlockAndUpdate(pos.below(), worldIn.getBlockState(pos.below()).setValue(TYPE, TrashType.SINGLE));
			}

			if (state.getValue(TYPE) == TrashType.SINGLE) {
				BlockEntity tile = getTrashBlockEntity(worldIn, state, tePos);
				if (tile != null && tile instanceof TrashBlockEntity) {
					Containers.dropContents(worldIn, tePos, (TrashBlockEntity) tile);
					worldIn.updateNeighbourForOutputSignal(getTrashPos(state, tePos), this);
				}
			}

			super.onRemove(state, worldIn, pos, newState, isMoving);
		}
	}

	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
		if (stack.hasCustomHoverName()) {
			BlockEntity tile = getTrashBlockEntity(worldIn, state, pos);
			if (tile instanceof TrashBlockEntity) {
				((TrashBlockEntity) tile).setCustomName(stack.getHoverName());
			}
		}
	}

	@Override
	public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
		BlockEntity blockEntity = getTrashBlockEntity(worldIn, state, pos);
		if (blockEntity != null && blockEntity instanceof TrashBlockEntity) {
			((TrashBlockEntity) blockEntity).onEntityCollision(entityIn);
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
			if (state.getValue(TYPE) == TrashType.BOTTOM) {
				boolean flag2 = !worldIn.hasNeighborSignal(pos.above());
				if (flag2) {
					worldIn.setBlockAndUpdate(pos, state.setValue(ENABLED, Boolean.valueOf(flag)));
					BlockState state2 = worldIn.getBlockState(pos.above());
					worldIn.setBlockAndUpdate(pos.above(), state2.setValue(ENABLED, flag));
				}
			} else if (state.getValue(TYPE) == TrashType.TOP) {
				boolean flag2 = !worldIn.hasNeighborSignal(pos.below());
				if (flag2) {
					worldIn.setBlockAndUpdate(pos, state.setValue(ENABLED, Boolean.valueOf(flag)));
					BlockState state2 = worldIn.getBlockState(pos.below());
					worldIn.setBlockAndUpdate(pos.below(), state2.setValue(ENABLED, flag));
				}
			} else {
				worldIn.setBlockAndUpdate(pos, state.setValue(ENABLED, Boolean.valueOf(flag)));
			}
		}
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		this.updateState(worldIn, pos, state);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level worldIn, BlockPos pos) {
		return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(getTrashBlockEntity(worldIn, state, pos));
	}

	public BlockPos getTrashPos(BlockState state, BlockPos pos) {
		if (state.getValue(TYPE) == TrashType.TOP) {
			return pos.below();
		} else {
			return pos;
		}
	}

	public BlockEntity getTrashBlockEntity(Level worldIn, BlockState state, BlockPos pos) {
		if (state.getBlock() == this) {
			if (state.getValue(TYPE) == TrashType.TOP) {
				return worldIn.getBlockEntity(pos.below());
			} else {
				return worldIn.getBlockEntity(pos);
			}
		}
		return null;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(new TranslatableComponent("trashed.trash_tooltip").withStyle(ChatFormatting.GOLD));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, WATERLOGGED, ENABLED, TYPE);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		Level worldIn = context.getLevel();
		BlockState bottomBlock = worldIn.getBlockState(pos.below());
		if (bottomBlock.getBlock() == this && bottomBlock.getValue(TYPE) == TrashType.SINGLE) {
			worldIn.setBlockAndUpdate(pos.below(), bottomBlock.setValue(TYPE, TrashType.BOTTOM));
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