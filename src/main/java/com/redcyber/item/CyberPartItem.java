package com.redcyber.item;

import com.redcyber.core.CyberSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Кибернетический имплант. Сам предмет — это "спецификация": установить его
 * можно только на операционном столе, потратив редстоуновые компоненты.
 */
public class CyberPartItem extends Item {
    private final CyberSlot slot;

    public CyberPartItem(CyberSlot slot, Settings settings) {
        super(settings);
        this.slot = slot;
    }

    public CyberSlot slot() {
        return this.slot;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        String base = this.getTranslationKey();
        tooltip.add(Text.translatable("redcyber.tooltip.slot", this.slot.displayName()).formatted(Formatting.DARK_GRAY));
        tooltip.add(Text.translatable(base + ".effect").formatted(Formatting.GREEN));
        tooltip.add(Text.empty());
        tooltip.add(Text.translatable(base + ".desc").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("redcyber.tooltip.install_hint").formatted(Formatting.DARK_RED, Formatting.ITALIC));
        super.appendTooltip(stack, context, tooltip, type);
    }
}
