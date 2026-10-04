package com.mrbysco.trashed.block;

import com.mojang.serialization.MapCodec;
import com.mrbysco.trashed.block.base.TrashBase;
import com.mrbysco.trashed.blockentity.FluidTrashBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import org.jetbrains.annotations.Nullable;

public class FluidTrashBlock extends TrashBase implements SimpleWaterloggedBlock {
	public static final MapCodec<FluidTrashBlock> CODEC = simpleCodec(FluidTrashBlock::new);

	public FluidTrashBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ENABLED, true).setValue(WATERLOGGED, false));
	}

	@Override
	protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return CODEC;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext context) {
		if (state.getValue(ENABLED)) {
			return SINGLE_SHAPE;
		} else {
			return SINGLE_DISABLED_SHAPE;
		}
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
		if(!player.isShiftKeyDown()) {
			var itemHandler = ItemAccess.forPlayerInteraction(player, hand).oneByOne();
			var fluidCap = itemHandler.getCapability(Capabilities.Fluid.ITEM);
			if(fluidCap != null) {
				if (!level.isClientSide()) {
					FluidUtil.interactWithFluidHandler(player, hand, level, pos, result.getDirection(), null);
					return InteractionResult.SUCCESS_SERVER;
				}


				return InteractionResult.SUCCESS;
			}
		}

		return super.useItemOn(stack, state, level, pos, player, hand, result);
	}

	//#region Power section

	@Override
	public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack) {
		super.playerDestroy(level, player, pos, state, blockEntity, stack);
		if (blockEntity instanceof FluidTrashBlockEntity) {
			level.updateNeighbourForOutputSignal(pos, this);
		}
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (oldState.getBlock() != state.getBlock()) {
			this.updateState(level, pos, state);
		}
	}

	private void updateState(Level level, BlockPos pos, BlockState state) {
		boolean flag = !level.hasNeighborSignal(pos);
		if (flag != state.getValue(ENABLED)) {
			level.setBlockAndUpdate(pos, state.setValue(ENABLED, Boolean.valueOf(flag)));
		}
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @org.jetbrains.annotations.Nullable Orientation orientation, boolean movedByPiston) {
		this.updateState(level, pos, state);
	}

	//#endregion

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new FluidTrashBlockEntity(pos, state);
	}
}
