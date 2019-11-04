package com.mrbysco.trashed.block;

import com.mrbysco.trashed.block.base.TrashBase;
import com.mrbysco.trashed.tile.EnergyTrashTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;

import javax.annotation.Nullable;
import java.util.List;

public class EnergyTrashBlock extends TrashBase implements IWaterLoggable {
    private static final VoxelShape BOTTOM_PLATE = Block.makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 0.5D, 14.0D);
    private static final VoxelShape SINGLE_INSIDE = Block.makeCuboidShape(2.5D, 0.0D, 2.5D, 13.5D, 13.0D, 13.5D);
    private static final VoxelShape SINGLE_INSIDE_HOLLOW = Block.makeCuboidShape(2.5D, 0.0D, 2.5D, 13.5D, 12.0D, 13.5D);
    private static final VoxelShape SINGLE_OUTSIDE = Block.makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);
    
    private static final VoxelShape SINGLE_SHAPE = VoxelShapes.or(BOTTOM_PLATE, VoxelShapes.combineAndSimplify(SINGLE_OUTSIDE, SINGLE_INSIDE, IBooleanFunction.ONLY_FIRST));
    private static final VoxelShape SINGLE_DISABLED_SHAPE = VoxelShapes.or(BOTTOM_PLATE, VoxelShapes.combineAndSimplify(SINGLE_OUTSIDE, SINGLE_INSIDE_HOLLOW, IBooleanFunction.ONLY_FIRST));
    
    public EnergyTrashBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(HORIZONTAL_FACING, Direction.NORTH).with(ENABLED, true).with(WATERLOGGED, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        if(state.get(ENABLED)) {
            return SINGLE_SHAPE;
        } else {
            return SINGLE_DISABLED_SHAPE;
        }
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        if (worldIn.isRemote) {
            return true;
        } else {
            FluidUtil.interactWithFluidHandler(player, hand, worldIn, pos, result.getFace());
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
        return new EnergyTrashTile();
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof EnergyTrashTile) {
                worldIn.updateComparatorOutputLevel(pos, this);
            }

            super.onReplaced(state, worldIn, pos, newState, isMoving);
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
            worldIn.setBlockState(pos, state.with(ENABLED, Boolean.valueOf(flag)));
        }
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        this.updateState(worldIn, pos, state);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
}
