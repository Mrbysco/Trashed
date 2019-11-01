package com.mrbysco.trashed.tile;

import com.mrbysco.trashed.block.TrashBlock;
import com.mrbysco.trashed.block.TrashType;
import com.mrbysco.trashed.init.TrashedRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HopperBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public class TrashTile extends LockableLootTileEntity implements ITickableTileEntity {
    private NonNullList<ItemStack> trashContents = NonNullList.<ItemStack>withSize(27, ItemStack.EMPTY);
    private int deletionCooldown = -1;
    private long tickedGameTime;

    private net.minecraftforge.common.util.LazyOptional<net.minecraftforge.items.IItemHandlerModifiable> trashHandler;

    protected TrashTile(TileEntityType<?> type) {
        super(type);
    }

    public TrashTile() {
        this(TrashedRegistry.TRASH_TILE.get());
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.trashContents;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> nonNullList) {
        this.trashContents = nonNullList;
    }

    @Override
    protected ITextComponent getDefaultName() {
        return new TranslationTextComponent("trashed.container.trashcan");
    }

    @Override
    protected Container createMenu(int id, PlayerInventory player) {
        return ChestContainer.createGeneric9X3(id, player, this);
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory playerInv, PlayerEntity player) {
        return ChestContainer.createGeneric9X3(id, playerInv, this);
    }

    @Override
    public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap, Direction side) {
        if (!this.removed && cap == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (this.trashHandler == null) {
                this.trashHandler = net.minecraftforge.common.util.LazyOptional.of(this::createHandler);
            }
            return this.trashHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    private net.minecraftforge.items.IItemHandlerModifiable createHandler() {
        BlockState state = this.getBlockState();
        if (!(state.getBlock() instanceof TrashBlock)) {
            return new net.minecraftforge.items.wrapper.InvWrapper(this);
        }
        TrashType type = state.get(TrashBlock.TYPE);
        if (type != TrashType.SINGLE) {
            BlockPos opos = this.getPos().offset(TrashBlock.getDirectionToAttached(state));
            BlockState ostate = this.getWorld().getBlockState(opos);
            if (state.getBlock() == ostate.getBlock()) {
                TrashType otype = ostate.get(TrashBlock.TYPE);
                if (otype != TrashType.SINGLE && type != otype && state.get(TrashBlock.HORIZONTAL_FACING) == ostate.get(TrashBlock.HORIZONTAL_FACING)) {
                    TileEntity ote = this.getWorld().getTileEntity(opos);
                    if (ote instanceof TrashTile) {
                        IInventory top = type == TrashType.BOTTOM ? this : (IInventory)ote;
                        IInventory bottom = type == TrashType.BOTTOM ? (IInventory)ote : this;
                        return new net.minecraftforge.items.wrapper.CombinedInvWrapper(
                                new net.minecraftforge.items.wrapper.InvWrapper(top),
                                new net.minecraftforge.items.wrapper.InvWrapper(bottom));
                    }
                }
            }
        }
        return new net.minecraftforge.items.wrapper.InvWrapper(this);
    }

    @Override
    public int getSizeInventory() {
        return this.trashContents.size();
    }

    @Override
    public boolean isEmpty() {
        for(ItemStack itemstack : this.trashContents) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private boolean isFull() {
        for(ItemStack itemstack : this.trashContents) {
            if (itemstack.isEmpty() || itemstack.getCount() != itemstack.getMaxStackSize()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void tick() {
        int i = this.pos.getX();
        int j = this.pos.getY();
        int k = this.pos.getZ();

        if (this.world != null && !this.world.isRemote) {
            --this.deletionCooldown;
            this.tickedGameTime = this.world.getGameTime();
            if (!this.isOnDeletionCooldown()) {
                this.setDeletionCooldown(0);
                this.removeItem();
            }

        }
    }

    private void removeItem() {
        for(int i = 0; i < this.getSizeInventory(); i++) {
            if(!trashContents.get(i).isEmpty()) {
                ItemStack stack = trashContents.get(i);
                stack.shrink(1);
                trashContents.set(i, stack);
                break;
            }
        }

        this.setDeletionCooldown(8);
        this.markDirty();
    }

    public void onEntityCollision(Entity entity) {
        if (entity instanceof ItemEntity) {
            BlockPos blockpos = this.getPos();
            if (VoxelShapes.compare(VoxelShapes.create(entity.getBoundingBox().offset((double)(-blockpos.getX()), (double)(-blockpos.getY()), (double)(-blockpos.getZ()))), this.getCollectionArea(), IBooleanFunction.AND)) {
                if(!this.isFull() && !this.isOnDeletionCooldown()) {
                    captureItem(this, (ItemEntity)entity);
                }
            }
        }
    }

    public static boolean captureItem(IInventory inv, ItemEntity itemEnt) {
        boolean flag = false;
        ItemStack itemstack = itemEnt.getItem().copy();
        ItemStack itemstack1 = putStackInInventoryAllSlots((IInventory)null, inv, itemstack, (Direction)null);
        if (itemstack1.isEmpty()) {
            flag = true;
            itemEnt.remove();
        } else {
            itemEnt.setItem(itemstack1);
        }

        return flag;
    }

    public static ItemStack putStackInInventoryAllSlots(@Nullable IInventory source, IInventory destination, ItemStack stack, @Nullable Direction direction) {
        if (destination instanceof ISidedInventory && direction != null) {
            ISidedInventory isidedinventory = (ISidedInventory)destination;
            int[] aint = isidedinventory.getSlotsForFace(direction);

            for(int k = 0; k < aint.length && !stack.isEmpty(); ++k) {
                stack = insertStack(source, destination, stack, aint[k], direction);
            }
        } else {
            int i = destination.getSizeInventory();

            for(int j = 0; j < i && !stack.isEmpty(); ++j) {
                stack = insertStack(source, destination, stack, j, direction);
            }
        }

        return stack;
    }

    private static ItemStack insertStack(@Nullable IInventory source, IInventory destination, ItemStack stack, int index, @Nullable Direction direction) {
        ItemStack itemstack = destination.getStackInSlot(index);
        if (canInsertItemInSlot(destination, stack, index, direction)) {
            boolean flag = false;
            boolean flag1 = destination.isEmpty();
            if (itemstack.isEmpty()) {
                destination.setInventorySlotContents(index, stack);
                stack = ItemStack.EMPTY;
                flag = true;
            } else if (canCombine(itemstack, stack)) {
                int i = stack.getMaxStackSize() - itemstack.getCount();
                int j = Math.min(stack.getCount(), i);
                stack.shrink(j);
                itemstack.grow(j);
                flag = j > 0;
            }

            if (flag) {
                if (flag1 && destination instanceof TrashTile) {
                    TrashTile TrashTile1 = (TrashTile)destination;
                    if (!TrashTile1.mayDelete()) {
                        int k = 0;
                        if (source instanceof TrashTile) {
                            TrashTile TrashTile = (TrashTile)source;
                            if (TrashTile1.tickedGameTime >= TrashTile.tickedGameTime) {
                                k = 1;
                            }
                        }

                        TrashTile1.setDeletionCooldown(8 - k);
                    }
                }

                destination.markDirty();
            }
        }

        return stack;
    }

    private static boolean canCombine(ItemStack stack1, ItemStack stack2) {
        if (stack1.getItem() != stack2.getItem()) {
            return false;
        } else if (stack1.getDamage() != stack2.getDamage()) {
            return false;
        } else if (stack1.getCount() > stack1.getMaxStackSize()) {
            return false;
        } else {
            return ItemStack.areItemStackTagsEqual(stack1, stack2);
        }
    }

    private static boolean canInsertItemInSlot(IInventory inventoryIn, ItemStack stack, int index, @Nullable Direction side) {
        if (!inventoryIn.isItemValidForSlot(index, stack)) {
            return false;
        } else {
            return !(inventoryIn instanceof ISidedInventory) || ((ISidedInventory)inventoryIn).canInsertItem(index, stack, side);
        }
    }

    public void read(CompoundNBT compound) {
        super.read(compound);
        this.trashContents = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        if (!this.checkLootAndRead(compound)) {
            ItemStackHelper.loadAllItems(compound, this.trashContents);
        }

        this.deletionCooldown = compound.getInt("DeletionCooldown");
    }

    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        if (!this.checkLootAndWrite(compound)) {
            ItemStackHelper.saveAllItems(compound, this.trashContents);
        }

        compound.putInt("DeletionCooldown", this.deletionCooldown);
        return compound;
    }

    public void setDeletionCooldown(int ticks) {
        this.deletionCooldown = ticks;
    }

    private boolean isOnDeletionCooldown() {
        return this.deletionCooldown > 0;
    }

    public boolean mayDelete() {
        return this.deletionCooldown > 8;
    }

    @Override
    public void openInventory(PlayerEntity player) {
        world.playSound(player, this.getPos(), SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 1.0F, 1.0F);
        super.openInventory(player);
    }

    public VoxelShape getCollectionArea() {
        return Block.makeCuboidShape(3.0D, 0.0D, 3.0D, 13.0D, 24.0D, 13.0D);
    }

    public VoxelShape getCollectionArea2() {
        return Block.makeCuboidShape(3.0D, 0.0D, 3.0D, 13.0D, 48.0D, 13.0D);
    }

    public long getLastUpdateTime() {
        return this.tickedGameTime;
    }

    @Override
    public void updateContainingBlockInfo() {
        super.updateContainingBlockInfo();
        if (this.trashHandler != null) {
            this.trashHandler.invalidate();
            this.trashHandler = null;
        }
    }

    @Override
    public void remove() {
        super.remove();
        if (trashHandler != null)
            trashHandler.invalidate();
    }
}
