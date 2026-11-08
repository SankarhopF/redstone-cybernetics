package com.redcyber.core;

import net.minecraft.entity.player.PlayerEntity;

import java.util.Locale;
import java.util.Map;

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
