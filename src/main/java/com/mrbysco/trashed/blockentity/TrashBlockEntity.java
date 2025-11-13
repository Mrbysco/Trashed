package com.mrbysco.trashed.blockentity;

import com.mrbysco.trashed.Trashed;
import com.mrbysco.trashed.block.TrashBlock;
import com.mrbysco.trashed.block.TrashType;
import com.mrbysco.trashed.config.TrashedConfig;
import com.mrbysco.trashed.init.TrashedRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

//TODO: Add a filter system!
public class TrashBlockEntity extends RandomizableContainerBlockEntity {
	private NonNullList<ItemStack> trashContents = NonNullList.withSize(27, ItemStack.EMPTY);
	private int deletionCooldown = -1;
	private long tickedGameTime;

	protected TrashBlockEntity(BlockEntityType<?> entityType, BlockPos pos, BlockState state) {
		super(entityType, pos, state);
	}

	public TrashBlockEntity(BlockPos pos, BlockState state) {
		this(TrashedRegistry.TRASH_TILE.get(), pos, state);
	}

	@Override
	public NonNullList<ItemStack> getItems() {
		return this.trashContents;
	}

	@Override
	public void setItems(NonNullList<ItemStack> nonNullList) {
		this.trashContents = nonNullList;
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("trashed.container.trashcan");
	}

	@Override
	protected AbstractContainerMenu createMenu(int id, Inventory player) {
		return ChestMenu.threeRows(id, player, this);
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
		return ChestMenu.threeRows(id, playerInv, this);
	}

	@Override
	public int getContainerSize() {
		return this.trashContents.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : this.trashContents) {
			if (!itemstack.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	private boolean isFull() {
		for (ItemStack itemstack : this.trashContents) {
			if (itemstack.isEmpty() || itemstack.getCount() != itemstack.getMaxStackSize()) {
				return false;
			}
		}

		return true;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, TrashBlockEntity trashBlockEntity) {
		if (level != null) {
			--trashBlockEntity.deletionCooldown;
			trashBlockEntity.tickedGameTime = level.getGameTime();
			if (!trashBlockEntity.isOnDeletionCooldown()) {
				trashBlockEntity.setDeletionCooldown(0);
				trashBlockEntity.updateTrash(trashBlockEntity::removeItem);
			}
		}
	}

	private boolean removeItem() {
		if (!this.isEmpty()) {
			for (int i = 0; i < this.getContainerSize(); i++) {
				if (!trashContents.get(i).isEmpty()) {
					ItemStack stack = trashContents.get(i);
					stack.shrink(TrashedConfig.SERVER.itemTrashQuantity.get());
					trashContents.set(i, stack);
					return true;
				}
			}
			return false;
		}
		return false;
	}

	public void onEntityCollision(Entity entity) {
		if (entity instanceof ItemEntity) {
			BlockPos blockpos = this.getBlockPos();
			if (Shapes.joinIsNotEmpty(Shapes.create(entity.getBoundingBox().move(-blockpos.getX(), -blockpos.getY(), -blockpos.getZ())), this.getCollectionArea(), BooleanOp.AND)) {
				this.updatePickupTrash(() -> captureItem(this, (ItemEntity) entity));
			}
		} else if (entity instanceof LivingEntity) {
			BlockPos blockpos = this.getBlockPos();
			if (Shapes.joinIsNotEmpty(Shapes.create(entity.getBoundingBox().move(-blockpos.getX(), -blockpos.getY(), -blockpos.getZ())), this.getEntityCollectionArea(), BooleanOp.AND)) {
				this.updateHurtEntity(() -> hurtEntity((LivingEntity) entity));
			}
		}
	}

	private void updateTrash(Supplier<Boolean> p_200109_1_) {
		if (this.level != null && !this.level.isClientSide()) {
			if (!this.isOnDeletionCooldown() && this.getBlockState().getBlock() instanceof TrashBlock && this.getBlockState().getValue(TrashBlock.ENABLED)) {
				boolean flag = false;

				if (!this.isFull()) {
					flag = p_200109_1_.get();
				}

				if (flag) {
					this.setDeletionCooldown(8);
					this.setChanged();
				}
			}
		}
	}

	private void updatePickupTrash(Supplier<Boolean> p_200109_1_) {
		if (this.level != null && !this.level.isClientSide()) {
			if (!this.isFull() && this.getBlockState().getBlock() instanceof TrashBlock && this.getBlockState().getValue(TrashBlock.ENABLED)) {
				boolean flag = false;

				if (!this.isFull()) {
					flag = p_200109_1_.get();
				}

				if (flag) {
					this.setDeletionCooldown(8);
					this.setChanged();
				}
			}
		}
	}

	private void updateHurtEntity(Supplier<Boolean> p_200109_1_) {
		if (this.level != null && !this.level.isClientSide()) {
			if (!this.isFull() && this.getBlockState().getBlock() instanceof TrashBlock && this.getBlockState().getValue(TrashBlock.ENABLED) && this.getBlockState().getValue(TrashBlock.TYPE) == TrashType.BOTTOM) {
				boolean flag = false;

				if (!this.isFull()) {
					flag = p_200109_1_.get();
				}

				if (flag) {
					this.setDeletionCooldown(8);
					this.setChanged();
				}
			}
		}
	}

	public static boolean hurtEntity(LivingEntity livingEnt) {
		if (livingEnt.level() instanceof ServerLevel serverLevel) {
			return livingEnt.hurtServer(serverLevel, Trashed.getTrashDamageSource(livingEnt), 1.0F);
		}
		return false;
	}

	public static boolean captureItem(Container inv, ItemEntity itemEnt) {
		boolean flag = false;
		ItemStack itemstack = itemEnt.getItem().copy();
		ItemStack itemstack1 = putStackInInventoryAllSlots((Container) null, inv, itemstack, (Direction) null);
		if (itemstack1.isEmpty()) {
			flag = true;
			itemEnt.discard();
		} else {
			itemEnt.setItem(itemstack1);
		}

		return flag;
	}

	public static ItemStack putStackInInventoryAllSlots(@Nullable Container source, Container destination, ItemStack stack, @Nullable Direction direction) {
		if (destination instanceof WorldlyContainer worldlyContainer && direction != null) {
			int[] aint = worldlyContainer.getSlotsForFace(direction);

			for (int k = 0; k < aint.length && !stack.isEmpty(); ++k) {
				stack = insertStack(source, destination, stack, aint[k], direction);
			}
		} else {
			int i = destination.getContainerSize();

			for (int j = 0; j < i && !stack.isEmpty(); ++j) {
				stack = insertStack(source, destination, stack, j, direction);
			}
		}

		return stack;
	}

	private static ItemStack insertStack(@Nullable Container source, Container destination, ItemStack stack, int index, @Nullable Direction direction) {
		ItemStack itemstack = destination.getItem(index);
		if (canInsertItemInSlot(destination, stack, index, direction)) {
			boolean flag = false;
			boolean flag1 = destination.isEmpty();
			if (itemstack.isEmpty()) {
				destination.setItem(index, stack);
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
				if (flag1 && destination instanceof TrashBlockEntity trashBlockEntity) {
					if (!trashBlockEntity.mayDelete()) {
						int k = 0;
						if (source instanceof TrashBlockEntity trashBlockEntity1) {
							if (trashBlockEntity.tickedGameTime >= trashBlockEntity1.tickedGameTime) {
								k = 1;
							}
						}

						trashBlockEntity.setDeletionCooldown(8 - k);
					}
				}

				destination.setChanged();
			}
		}

		return stack;
	}

	private static boolean canCombine(ItemStack stack1, ItemStack stack2) {
		if (stack1.getItem() != stack2.getItem()) {
			return false;
		} else if (stack1.getDamageValue() != stack2.getDamageValue()) {
			return false;
		} else if (stack1.getCount() > stack1.getMaxStackSize()) {
			return false;
		} else {
			return ItemStack.isSameItemSameComponents(stack1, stack2);
		}
	}

	private static boolean canInsertItemInSlot(Container inventoryIn, ItemStack stack, int index, @Nullable Direction side) {
		if (!inventoryIn.canPlaceItem(index, stack)) {
			return false;
		} else {
			return !(inventoryIn instanceof WorldlyContainer) || ((WorldlyContainer) inventoryIn).canPlaceItemThroughFace(index, stack, side);
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.trashContents = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		if (!this.tryLoadLootTable(input)) {
			ContainerHelper.loadAllItems(input, this.trashContents);
		}

		Optional<Integer> cooldown = input.getInt("DeletionCooldown");
		cooldown.ifPresent(integer -> this.deletionCooldown = integer);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (!this.trySaveLootTable(output)) {
			ContainerHelper.saveAllItems(output, this.trashContents);
		}

		output.putInt("DeletionCooldown", this.deletionCooldown);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider lookupProvider) {
		CompoundTag tag = new CompoundTag();
		try (ProblemReporter.ScopedCollector problemreporter$scopedcollector = new ProblemReporter.ScopedCollector(Trashed.LOGGER)) {
			TagValueOutput output = TagValueOutput.createWithContext(problemreporter$scopedcollector, lookupProvider);
			this.saveAdditional(output);
			tag.merge(output.buildResult());
		}
		return tag;
	}

	@Override
	public CompoundTag getPersistentData() {
		CompoundTag tag = new CompoundTag();
		try (ProblemReporter.ScopedCollector problemreporter$scopedcollector = new ProblemReporter.ScopedCollector(Trashed.LOGGER)) {
			HolderLookup.Provider lookupProvider = this.level != null ? this.level.registryAccess() : VanillaRegistries.createLookup();
			TagValueOutput output = TagValueOutput.createWithContext(problemreporter$scopedcollector, lookupProvider);
			this.saveAdditional(output);
			tag.merge(output.buildResult());
		}
		return tag;
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
	public void startOpen(ContainerUser user) {
		if (level != null) {
			level.playSound(user.getLivingEntity(), this.getBlockPos(), SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
		}
		super.startOpen(user);
	}

	public VoxelShape getCollectionArea() {
		if (level != null && this.level.getBlockState(this.worldPosition).getValue(TrashBlock.TYPE) == TrashType.BOTTOM) {
			return Block.box(2.5D, 0.0D, 2.5D, 13.5D, 48.0D, 13.5D);
		} else {
			return Block.box(2.5D, 0.0D, 2.5D, 13.5D, 24.0D, 13.5D);
		}
	}

	public VoxelShape getEntityCollectionArea() {
		return Block.box(2.5D, 0.0D, 2.5D, 13.5D, 24.0D, 13.5D);
	}
}
