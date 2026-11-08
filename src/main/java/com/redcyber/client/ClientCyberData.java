package com.redcyber.client;

import com.redcyber.core.CyberData;
import com.redcyber.core.CyberPart;
import com.redcyber.core.CyberParts;
import com.redcyber.core.CyberSlot;

import java.util.EnumMap;
import java.util.Map;

/** Клиентская копия установленных имплантов (приходит с сервера пакетом). */
public final class ClientCyberData {
    private static final Map<CyberSlot, String> PARTS = new EnumMap<>(CyberSlot.class);

    private ClientCyberData() {
    }

    public static void update(String data) {
        CyberData.decode(data, PARTS);
    }

    public static boolean has(CyberSlot slot) {
        return PARTS.containsKey(slot);
    }

    public static String name(CyberSlot slot) {
        return PARTS.get(slot);
    }

    /** Имплант, установленный в слот (или null). */
    public static CyberPart installedIn(CyberSlot slot) {
        String name = PARTS.get(slot);
        return name == null ? null : CyberParts.byName(name);
    }

    public static Map<CyberSlot, String> all() {
        return PARTS;
    }
}
