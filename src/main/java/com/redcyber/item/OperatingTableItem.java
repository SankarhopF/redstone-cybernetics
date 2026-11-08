package com.redcyber.item;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/** Предмет операционного стола (с подсказкой, как им пользоваться). */
public class OperatingTableItem extends BlockItem {
    public OperatingTableItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable(this.getTranslationKey() + ".desc").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable(this.getTranslationKey() + ".usage").formatted(Formatting.DARK_RED, Formatting.ITALIC));
        super.appendTooltip(stack, context, tooltip, type);
    }
}
