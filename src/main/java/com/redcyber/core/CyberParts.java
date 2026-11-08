package com.redcyber.core;

import com.redcyber.registry.ModItems;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Реестр всех кибернетических имплантов и их "стоимости" в компонентах. */
public final class CyberParts {
    private static final List<CyberPart> ALL = new ArrayList<>();
    private static final Map<String, CyberPart> BY_NAME = new HashMap<>();

    private CyberParts() {
    }

    public static void init() {
        if (!ALL.isEmpty()) {
            return;
        }

        register("cyber_eye", CyberSlot.EYE, ModItems.CYBER_EYE, cost(
                ModItems.POWER_CELL, 1,
                ModItems.NEURAL_CHIP, 1,
                ModItems.REDSTONE_CIRCUIT, 2,
                ModItems.REDSTONE_ALLOY_INGOT, 4));

        register("cyber_arm", CyberSlot.ARM, ModItems.CYBER_ARM, cost(
                ModItems.POWER_CELL, 1,
                ModItems.REDSTONE_SERVO, 2,
                ModItems.REDSTONE_CIRCUIT, 1,
                ModItems.REDSTONE_ALLOY_INGOT, 4));

        register("cyber_legs", CyberSlot.LEG, ModItems.CYBER_LEGS, cost(
                ModItems.POWER_CELL, 1,
                ModItems.REDSTONE_SERVO, 4,
                ModItems.REDSTONE_CIRCUIT, 2,
                ModItems.REDSTONE_ALLOY_INGOT, 6));

        register("cyber_heart", CyberSlot.HEART, ModItems.CYBER_HEART, cost(
                ModItems.POWER_CELL, 1,
                ModItems.NEURAL_CHIP, 2,
                ModItems.REDSTONE_CIRCUIT, 2,
                ModItems.REDSTONE_ALLOY_INGOT, 6));

        register("cyber_lungs", CyberSlot.LUNG, ModItems.CYBER_LUNGS, cost(
                ModItems.POWER_CELL, 1,
                ModItems.REDSTONE_CIRCUIT, 2,
                ModItems.REDSTONE_SERVO, 2,
                ModItems.REDSTONE_ALLOY_INGOT, 4));

        register("cyber_spine", CyberSlot.SPINE, ModItems.CYBER_SPINE, cost(
                ModItems.POWER_CELL, 1,
                ModItems.NEURAL_CHIP, 2,
                ModItems.REDSTONE_SERVO, 2,
                ModItems.REDSTONE_ALLOY_INGOT, 4));
    }

    private static Map<Item, Integer> cost(Object... pairs) {
        Map<Item, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((Item) pairs[i], (Integer) pairs[i + 1]);
        }
        return map;
    }

    private static CyberPart register(String name, CyberSlot slot, Item item, Map<Item, Integer> components) {
        CyberPart part = new CyberPart(name, slot, item, components);
        ALL.add(part);
        BY_NAME.put(name, part);
        return part;
    }

    public static Collection<CyberPart> all() {
        return ALL;
    }

    public static CyberPart byName(String name) {
        return BY_NAME.get(name);
    }

    public static CyberPart forSlot(CyberSlot slot) {
        for (CyberPart part : ALL) {
            if (part.slot() == slot) {
                return part;
            }
        }
        return null;
    }
}
