package com.redcyber.core;

import net.minecraft.entity.player.PlayerEntity;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Утилиты для чтения/записи установленных имплантов. */
public final class CyberData {
    private CyberData() {
    }

    public static Map<CyberSlot, String> parts(PlayerEntity player) {
        return ((CyberAccess) player).redcyber$getParts();
    }

    public static boolean has(PlayerEntity player, CyberSlot slot) {
        return parts(player).containsKey(slot);
    }

    public static String partName(PlayerEntity player, CyberSlot slot) {
        return parts(player).get(slot);
    }

    /** Слоты, у которых игрок выключил дополнительные возможности. */
    public static Set<CyberSlot> disabled(PlayerEntity player) {
        return ((CyberAccess) player).redcyber$getDisabled();
    }

    /**
     * Имплант стоит в слоте и его возможности включены. Все эффекты проверяют
     * именно это, чтобы тумблеры из меню работали.
     */
    public static boolean isEnabled(PlayerEntity player, CyberSlot slot) {
        return has(player, slot) && !disabled(player).contains(slot);
    }

    public static void setEnabled(PlayerEntity player, CyberSlot slot, boolean enabled) {
        Set<CyberSlot> off = disabled(player);
        if (enabled) {
            off.remove(slot);
        } else {
            off.add(slot);
        }
    }

    /** Сериализация выключенных слотов для сети: "EYE,ARM". */
    public static String encodeDisabled(Set<CyberSlot> slots) {
        StringBuilder sb = new StringBuilder();
        for (CyberSlot slot : slots) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(slot.name().toUpperCase(Locale.ROOT));
        }
        return sb.toString();
    }

    public static void decodeDisabled(String data, Set<CyberSlot> target) {
        target.clear();
        if (data == null || data.isEmpty()) {
            return;
        }
        for (String entry : data.split(",")) {
            try {
                target.add(CyberSlot.valueOf(entry.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
                // неизвестный слот — пропускаем
            }
        }
    }

    public static void install(PlayerEntity player, CyberPart part) {
        parts(player).put(part.slot(), part.name());
    }

    /** Сериализация для сети: "EYE=cyber_eye,ARM=cyber_arm". */
    public static String encode(Map<CyberSlot, String> parts) {
        StringBuilder sb = new StringBuilder();
        parts.forEach((slot, part) -> {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(slot.name().toUpperCase(Locale.ROOT)).append('=').append(part);
        });
        return sb.toString();
    }

    public static void decode(String data, Map<CyberSlot, String> target) {
        target.clear();
        if (data == null || data.isEmpty()) {
            return;
        }
        for (String entry : data.split(",")) {
            int idx = entry.indexOf('=');
            if (idx <= 0) {
                continue;
            }
            try {
                CyberSlot slot = CyberSlot.valueOf(entry.substring(0, idx).toUpperCase(Locale.ROOT));
                target.put(slot, entry.substring(idx + 1));
            } catch (IllegalArgumentException ignored) {
                // неизвестный слот — пропускаем (например, при откате версии мода)
            }
        }
    }
}
