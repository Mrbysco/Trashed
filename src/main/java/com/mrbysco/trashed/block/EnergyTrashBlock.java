package com.mrbysco.trashed.block;

import com.mojang.serialization.MapCodec;
import com.mrbysco.trashed.block.base.TrashBase;
import com.mrbysco.trashed.blockentity.EnergyTrashBlockEntity;
import com.mrbysco.trashed.init.TrashedRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
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
	public static final MapCodec<EnergyTrashBlock> CODEC = simpleCodec(EnergyTrashBlock::new);

	public EnergyTrashBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return CODEC;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		if (state.getValue(ENABLED)) {
			return SINGLE_SHAPE;
		} else {
			return SINGLE_DISABLED_SHAPE;
		}
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity instanceof EnergyTrashBlockEntity) {
				level.updateNeighbourForOutputSignal(pos, this);
			}

			super.onRemove(state, level, pos, newState, isMoving);
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter blockGetter, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, blockGetter, tooltip, flagIn);
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
	protected static <T extends BlockEntity> BlockEntityTicker<T> createTrashTicker(Level level, BlockEntityType<T> blockEntityTicker,
																					BlockEntityType<? extends EnergyTrashBlockEntity> entityType) {
		return level.isClientSide ? null : createTickerHelper(blockEntityTicker, entityType, EnergyTrashBlockEntity::serverTick);
	}
}
