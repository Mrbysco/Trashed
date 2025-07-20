package com.mrbysco.trashed.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class TrashCanItem extends BlockItem {
	public TrashCanItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> componentConsumer, TooltipFlag flag) {
		componentConsumer.accept(Component.translatable("trashed.trash_tooltip").withStyle(ChatFormatting.GOLD));
	}
}
