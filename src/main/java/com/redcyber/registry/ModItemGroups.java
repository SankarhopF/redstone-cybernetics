package com.redcyber.registry;

import com.redcyber.RedCyber;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public final class ModItemGroups {
    private ModItemGroups() {
    }

    public static final RegistryKey<ItemGroup> MAIN = RegistryKey.of(RegistryKeys.ITEM_GROUP,
            RedCyber.id("main"));

    public static void init() {
        Registry.register(Registries.ITEM_GROUP, MAIN, FabricItemGroup.builder()
                .icon(() -> new ItemStack(ModItems.POWER_CELL))
                .displayName(Text.translatable("itemGroup.redcyber.main"))
                .entries((context, entries) -> {
                    entries.add(ModItems.REDSTONE_ALLOY_INGOT);
                    entries.add(ModItems.REDSTONE_CIRCUIT);
                    entries.add(ModItems.REDSTONE_SERVO);
                    entries.add(ModItems.NEURAL_CHIP);
                    entries.add(ModItems.POWER_CELL);
                    entries.add(ModItems.OPERATING_TABLE);
                    entries.add(ModItems.CYBER_EYE);
                    entries.add(ModItems.CYBER_ARM);
                    entries.add(ModItems.CYBER_LEGS);
                    entries.add(ModItems.CYBER_HEART);
                    entries.add(ModItems.CYBER_LUNGS);
                    entries.add(ModItems.CYBER_SPINE);
                })
                .build());
    }
}
