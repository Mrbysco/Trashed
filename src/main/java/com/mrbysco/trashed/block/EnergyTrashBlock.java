package com.mrbysco.trashed.block;

import com.mrbysco.trashed.block.base.TrashBase;
import com.mrbysco.trashed.tile.EnergyTrashTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

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

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
}
