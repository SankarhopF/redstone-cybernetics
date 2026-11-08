package com.redcyber.registry;

import com.redcyber.RedCyber;
import com.redcyber.core.CyberSlot;
import com.redcyber.item.ComponentItem;
import com.redcyber.item.CyberPartItem;
import com.redcyber.item.OperatingTableItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModItems {
    private ModItems() {
    }

    // ---------- Редстоуновые компоненты ----------
    public static final Item REDSTONE_ALLOY_INGOT = register("redstone_alloy_ingot",
            new ComponentItem(new Item.Settings()));
    public static final Item REDSTONE_CIRCUIT = register("redstone_circuit",
            new ComponentItem(new Item.Settings()));
    public static final Item REDSTONE_SERVO = register("redstone_servo",
            new ComponentItem(new Item.Settings()));
    public static final Item NEURAL_CHIP = register("neural_chip",
            new ComponentItem(new Item.Settings()));
    public static final Item POWER_CELL = register("power_cell",
            new ComponentItem(new Item.Settings()));

    // ---------- Кибернетические импланты ----------
    public static final Item CYBER_EYE = register("cyber_eye",
            new CyberPartItem(CyberSlot.EYE, new Item.Settings().maxCount(1)));
    public static final Item CYBER_ARM = register("cyber_arm",
            new CyberPartItem(CyberSlot.ARM, new Item.Settings().maxCount(1)));
    public static final Item CYBER_LEGS = register("cyber_legs",
            new CyberPartItem(CyberSlot.LEG, new Item.Settings().maxCount(1)));
    public static final Item CYBER_HEART = register("cyber_heart",
            new CyberPartItem(CyberSlot.HEART, new Item.Settings().maxCount(1)));
    public static final Item CYBER_LUNGS = register("cyber_lungs",
            new CyberPartItem(CyberSlot.LUNG, new Item.Settings().maxCount(1)));
    public static final Item CYBER_SPINE = register("cyber_spine",
            new CyberPartItem(CyberSlot.SPINE, new Item.Settings().maxCount(1)));

    // ---------- Блоки ----------
    public static final Item OPERATING_TABLE = register("operating_table",
            new OperatingTableItem(ModBlocks.OPERATING_TABLE, new Item.Settings()));

    private static Item register(String name, Item item) {
        Identifier id = RedCyber.id(name);
        return Registry.register(Registries.ITEM, id, item);
    }

    public static void init() {
        // Static initializer is enough; method exists for explicit ordering in RedCyber.
    }
}
