package com.redcyber.core;

import net.minecraft.item.Item;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Описание одного кибернетического импланта: слот, предмет-иконка и набор
 * редстоуновых компонентов, нужных для операции.
 */
public final class CyberPart {
    private final String name;
    private final CyberSlot slot;
    private final Item item;
    private final Map<Item, Integer> components;

    CyberPart(String name, CyberSlot slot, Item item, Map<Item, Integer> components) {
        this.name = name;
        this.slot = slot;
        this.item = item;
        this.components = new LinkedHashMap<>(components);
    }

    /** Имя в реестре, например "cyber_eye". */
    public String name() {
        return this.name;
    }

    public CyberSlot slot() {
        return this.slot;
    }

    public Item item() {
        return this.item;
    }

    /** Необходимые компоненты: предмет -> количество. */
    public Map<Item, Integer> components() {
        return this.components;
    }

    public String translationKey() {
        return "item.redcyber." + this.name;
    }

    public String descriptionKey() {
        return "item.redcyber." + this.name + ".desc";
    }

    public String effectKey() {
        return "item.redcyber." + this.name + ".effect";
    }
}
