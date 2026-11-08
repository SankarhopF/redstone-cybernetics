package com.redcyber.core;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Locale;

/** Слоты тела, в которые ставятся кибернетические импланты. */
public enum CyberSlot {
    EYE("eye", Formatting.AQUA),
    ARM("arm", Formatting.RED),
    LEG("leg", Formatting.GOLD),
    HEART("heart", Formatting.LIGHT_PURPLE),
    LUNG("lung", Formatting.BLUE),
    SPINE("spine", Formatting.GREEN);

    private final String key;
    private final Formatting color;

    CyberSlot(String key, Formatting color) {
        this.key = key;
        this.color = color;
    }

    public String key() {
        return this.key;
    }

    public Formatting color() {
        return this.color;
    }

    public String translationKey() {
        return "redcyber.slot." + this.key;
    }

    public Text displayName() {
        return Text.translatable(this.translationKey()).formatted(this.color);
    }

    public String lowerName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
