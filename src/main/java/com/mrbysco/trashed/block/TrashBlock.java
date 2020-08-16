package com.mrbysco.trashed.block;

import com.mrbysco.trashed.block.base.TrashBase;
import com.mrbysco.trashed.tile.TrashSlaveTile;
import com.mrbysco.trashed.tile.TrashTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public class TrashBlock extends TrashBase implements IWaterLoggable {
    private static final VoxelShape BOTTOM_PLATE = Block.makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 0.5D, 14.0D);

    private static final VoxelShape SINGLE_INSIDE = Block.makeCuboidShape(2.5D, 0.0D, 2.5D, 13.5D, 13.0D, 13.5D);
    private static final VoxelShape SINGLE_INSIDE_HOLLOW = Block.makeCuboidShape(2.5D, 0.0D, 2.5D, 13.5D, 12.0D, 13.5D);
    private static final VoxelShape SINGLE_OUTSIDE = Block.makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);
    private static final VoxelShape BOTTOM_INSIDE = Block.makeCuboidShape(2.5D, 0.0D, 2.5D, 13.5D, 16.0D, 13.5D);
    private static final VoxelShape BOTTOM_INSIDE_HOLLOW = Block.makeCuboidShape(2.5D, 0.0D, 2.5D, 13.5D, 15.0D, 13.5D);
    private static final VoxelShape BOTTOM_OUTSIDE = Block.makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    private static final VoxelShape SINGLE_SHAPE = VoxelShapes.or(BOTTOM_PLATE, VoxelShapes.combineAndSimplify(SINGLE_OUTSIDE, SINGLE_INSIDE, IBooleanFunction.ONLY_FIRST));
    private static final VoxelShape SINGLE_DISABLED_SHAPE = VoxelShapes.or(BOTTOM_PLATE, VoxelShapes.combineAndSimplify(SINGLE_OUTSIDE, SINGLE_INSIDE_HOLLOW, IBooleanFunction.ONLY_FIRST));
    private static final VoxelShape TOP_SHAPE = VoxelShapes.combineAndSimplify(SINGLE_OUTSIDE, SINGLE_INSIDE, IBooleanFunction.ONLY_FIRST);
    private static final VoxelShape TOP_DISABLED_SHAPE = VoxelShapes.combineAndSimplify(SINGLE_OUTSIDE, SINGLE_INSIDE_HOLLOW, IBooleanFunction.ONLY_FIRST);
    private static final VoxelShape BOTTOM_SHAPE = VoxelShapes.or(BOTTOM_PLATE, VoxelShapes.combineAndSimplify(BOTTOM_OUTSIDE, BOTTOM_INSIDE, IBooleanFunction.ONLY_FIRST));
    private static final VoxelShape BOTTOM_DISABLED_SHAPE = VoxelShapes.or(BOTTOM_PLATE, VoxelShapes.combineAndSimplify(BOTTOM_OUTSIDE, BOTTOM_INSIDE_HOLLOW, IBooleanFunction.ONLY_FIRST));

    public static final EnumProperty<TrashType> TYPE = EnumProperty.create("type", TrashType.class);

    public TrashBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(HORIZONTAL_FACING, Direction.NORTH).with(TYPE, TrashType.SINGLE).with(ENABLED, true).with(WATERLOGGED, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        if(state.get(ENABLED)) {
            switch(state.get(TYPE)) {
                default:
                    return SINGLE_SHAPE;
                case BOTTOM:
                    return BOTTOM_SHAPE;
                case TOP:
                    return TOP_SHAPE;
            }
        } else {
            switch(state.get(TYPE)) {
                default:
                    return SINGLE_DISABLED_SHAPE;
                case BOTTOM:
                    return BOTTOM_DISABLED_SHAPE;
                case TOP:
                    return TOP_DISABLED_SHAPE;
            }
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        if (!worldIn.isRemote) {
            TileEntity tile = getTrashTile(worldIn, state, pos);
            if (tile instanceof TrashTile) {
                NetworkHooks.openGui((ServerPlayerEntity) player, (TrashTile) tile, pos);
            }

        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        if(state.get(TYPE) != TrashType.TOP) {
            return new TrashTile();
        } else {
            return new TrashSlaveTile();
        }
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if(state.get(TYPE) == TrashType.BOTTOM) {
                worldIn.destroyBlock(pos.up(), true);
            } else if(state.get(TYPE) == TrashType.TOP) {
                worldIn.setBlockState(pos.down(), worldIn.getBlockState(pos.down()).with(TYPE, TrashType.SINGLE));
            }

            TileEntity tile = getTrashTile(worldIn, state, pos);
            if (tile instanceof TrashTile) {
                InventoryHelper.dropInventoryItems(worldIn, pos, (TrashTile)tile);
                worldIn.updateComparatorOutputLevel(getTrashPos(state, pos), this);
            }

            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        if (stack.hasDisplayName()) {
            TileEntity tile = getTrashTile(worldIn, state, pos);
            if (tile instanceof TrashTile) {
                ((TrashTile)tile).setCustomName(stack.getDisplayName());
            }
        }
    }

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        TileEntity tileentity = getTrashTile(worldIn, state, pos);
        if (tileentity instanceof TrashTile) {
            ((TrashTile)tileentity).onEntityCollision(entityIn);
        }
    }

    /**
     * Power section
     */

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (oldState.getBlock() != state.getBlock()) {
            this.updateState(worldIn, pos, state);
        }
    }

    private void updateState(World worldIn, BlockPos pos, BlockState state) {
        boolean flag = !worldIn.isBlockPowered(pos);
        if (flag != state.get(ENABLED)) {
            if(state.get(TYPE) == TrashType.BOTTOM) {
                boolean flag2 = !worldIn.isBlockPowered(pos.up());
                if(flag2) {
                    worldIn.setBlockState(pos, state.with(ENABLED, Boolean.valueOf(flag)));
                    BlockState state2 = worldIn.getBlockState(pos.up());
                    worldIn.setBlockState(pos.up(), state2.with(ENABLED, flag));
                }
            } else if (state.get(TYPE) == TrashType.TOP) {
                boolean flag2 = !worldIn.isBlockPowered(pos.down());
                if(flag2) {
                    worldIn.setBlockState(pos, state.with(ENABLED, Boolean.valueOf(flag)));
                    BlockState state2 = worldIn.getBlockState(pos.down());
                    worldIn.setBlockState(pos.down(), state2.with(ENABLED, flag));
                }
            } else {
                worldIn.setBlockState(pos, state.with(ENABLED, Boolean.valueOf(flag)));
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        this.updateState(worldIn, pos, state);
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(BlockState state, World worldIn, BlockPos pos) {
        return Container.calcRedstone(getTrashTile(worldIn, state, pos));
    }

    public BlockPos getTrashPos(BlockState state, BlockPos pos) {
        if(state.get(TYPE) == TrashType.TOP) {
            return pos.down();
        } else {
            return pos;
        }
    }

    public TileEntity getTrashTile(World worldIn, BlockState state, BlockPos pos) {
        if(state.get(TYPE) == TrashType.TOP) {
            return worldIn.getTileEntity(pos.down());
        } else {
            return worldIn.getTileEntity(pos);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(new TranslationTextComponent("trashed.trash_tooltip").mergeStyle(TextFormatting.GOLD));
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING, WATERLOGGED, ENABLED, TYPE);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockPos pos = context.getPos();
        World worldIn = context.getWorld();
        BlockState bottomBlock = worldIn.getBlockState(pos.down());
        if(bottomBlock.getBlock() == this && bottomBlock.get(TYPE) == TrashType.SINGLE) {
            worldIn.setBlockState(pos.down(), bottomBlock.with(TYPE, TrashType.BOTTOM));
            return this.getDefaultState().with(HORIZONTAL_FACING, bottomBlock.get(HORIZONTAL_FACING)).with(ENABLED, bottomBlock.get(ENABLED)).with(TYPE, TrashType.TOP);
        } else {
            return this.getDefaultState().with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite()).with(ENABLED, true);
        }
    }
}
