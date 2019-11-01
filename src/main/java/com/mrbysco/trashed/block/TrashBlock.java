package com.mrbysco.trashed.block;

import com.mrbysco.trashed.tile.TrashSlaveTile;
import com.mrbysco.trashed.tile.TrashTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class TrashBlock extends HorizontalBlock implements IWaterLoggable {
    private static final VoxelShape BOTTOM_PLATE = Block.makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 0.5D, 14.0D);

    private static final VoxelShape SINGLE_INSIDE = Block.makeCuboidShape(2.5D, 0.0D, 2.5D, 13.5D, 13.0D, 13.5D);
    private static final VoxelShape SINGLE_OUTSIDE = Block.makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);
    private static final VoxelShape BOTTOM_INSIDE = Block.makeCuboidShape(2.5D, 0.0D, 2.5D, 13.5D, 16.0D, 13.5D);
    private static final VoxelShape BOTTOM_OUTSIDE = Block.makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    private static final VoxelShape SINGLE_SHAPE = VoxelShapes.or(BOTTOM_PLATE, VoxelShapes.combineAndSimplify(SINGLE_OUTSIDE, SINGLE_INSIDE, IBooleanFunction.ONLY_FIRST));
    private static final VoxelShape TOP_SHAPE = VoxelShapes.combineAndSimplify(SINGLE_OUTSIDE, SINGLE_INSIDE, IBooleanFunction.ONLY_FIRST);
    private static final VoxelShape BOTTOM_SHAPE = VoxelShapes.or(BOTTOM_PLATE, VoxelShapes.combineAndSimplify(BOTTOM_OUTSIDE, BOTTOM_INSIDE, IBooleanFunction.ONLY_FIRST));

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<TrashType> TYPE = EnumProperty.create("type", TrashType.class);
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    public TrashBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(HORIZONTAL_FACING, Direction.NORTH).with(TYPE, TrashType.SINGLE).with(ENABLED, true).with(WATERLOGGED, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        switch(state.get(TYPE)) {
            default:
                return SINGLE_SHAPE;
            case BOTTOM:
                return BOTTOM_SHAPE;
            case TOP:
                return TOP_SHAPE;
        }
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand p_220051_5_, BlockRayTraceResult p_220051_6_) {
        if (worldIn.isRemote) {
            return true;
        } else {
            TileEntity tile = getTrashTile(worldIn, state, pos);
            if (tile instanceof TrashTile) {
                NetworkHooks.openGui((ServerPlayerEntity) player, (TrashTile) tile, pos);
            }

            return true;
        }
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
     * WaterLogging section
     */
    public Fluid pickupFluid(IWorld worldIn, BlockPos pos, BlockState state) {
        if (state.get(WATERLOGGED)) {
            worldIn.setBlockState(pos, state.with(WATERLOGGED, Boolean.valueOf(false)), 3);
            return Fluids.WATER;
        } else {
            return Fluids.EMPTY;
        }
    }

    @Override
    public boolean canContainFluid(IBlockReader worldIn, BlockPos pos, BlockState state, Fluid fluidIn) {
        return !state.get(WATERLOGGED) && fluidIn == Fluids.WATER;
    }

    @Override
    public boolean receiveFluid(IWorld worldIn, BlockPos pos, BlockState state, IFluidState fluidStateIn) {
        if (!state.get(WATERLOGGED) && fluidStateIn.getFluid() == Fluids.WATER) {
            if (!worldIn.isRemote()) {
                worldIn.setBlockState(pos, state.with(WATERLOGGED, Boolean.valueOf(true)), 3);
                worldIn.getPendingFluidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
            }

            return true;
        } else {
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public IFluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    /**
     * Rotation section
     */
    @SuppressWarnings("deprecation")
    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(HORIZONTAL_FACING, rot.rotate(state.get(HORIZONTAL_FACING)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(HORIZONTAL_FACING)));
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING, WATERLOGGED, TYPE, ENABLED);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockPos pos = context.getPos();
        World worldIn = context.getWorld();
        BlockState bottomBlock = worldIn.getBlockState(pos.down());
        if(bottomBlock.getBlock() == this && bottomBlock.get(TYPE) == TrashType.SINGLE) {
            worldIn.setBlockState(pos.down(), bottomBlock.with(TYPE, TrashType.BOTTOM));
            return this.getDefaultState().with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite()).with(ENABLED, true).with(TYPE, TrashType.TOP);
        } else {
            return this.getDefaultState().with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite()).with(ENABLED, true);
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
            worldIn.setBlockState(pos, state.with(ENABLED, Boolean.valueOf(flag)), 4);
            if(state.get(TYPE) == TrashType.BOTTOM) {
                worldIn.setBlockState(pos.up(), state.with(ENABLED, flag), 4);
            } else if (state.get(TYPE) == TrashType.TOP) {
                worldIn.setBlockState(pos.down(), state.with(ENABLED, flag), 4);
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

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
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
}
