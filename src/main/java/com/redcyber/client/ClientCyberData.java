package com.redcyber.client;

import com.redcyber.core.CyberData;
import com.redcyber.core.CyberPart;
import com.redcyber.core.CyberParts;
import com.redcyber.core.CyberSlot;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Клиентская копия установленных имплантов и выключенных возможностей. */
public final class ClientCyberData {
    private static final Map<CyberSlot, String> PARTS = new EnumMap<>(CyberSlot.class);
    private static final Set<CyberSlot> DISABLED = EnumSet.noneOf(CyberSlot.class);

    private ClientCyberData() {
    }

    public static void update(String data, String disabled) {
        CyberData.decode(data, PARTS);
        CyberData.decodeDisabled(disabled, DISABLED);
    }

    public static boolean has(CyberSlot slot) {
        return PARTS.containsKey(slot);
    }

    public static String name(CyberSlot slot) {
        return PARTS.get(slot);
    }

    /** Имплант в слоте включён (стоит и тумблер не выключен). */
    public static boolean isEnabled(CyberSlot slot) {
        return has(slot) && !DISABLED.contains(slot);
    }

    public static boolean isDisabled(CyberSlot slot) {
        return DISABLED.contains(slot);
    }

    /** Оптимистично применяем тумблер, чтобы интерфейс реагировал сразу. */
    public static void setLocallyEnabled(CyberSlot slot, boolean enabled) {
        if (enabled) {
            DISABLED.remove(slot);
        } else {
            DISABLED.add(slot);
        }
    }

    /** Имплант, установленный в слот (или null). */
    public static CyberPart installedIn(CyberSlot slot) {
        String name = PARTS.get(slot);
        return name == null ? null : CyberParts.byName(name);
    }

    public static Map<CyberSlot, String> all() {
        return PARTS;
    }

    public static Set<CyberSlot> disabledSlots() {
        return DISABLED;
    }
}
