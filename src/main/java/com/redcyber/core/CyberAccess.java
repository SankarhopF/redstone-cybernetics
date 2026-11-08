package com.redcyber.core;

import java.util.Map;
import java.util.Set;

/**
 * Доступ к установленным имплантам игрока. Реализуется миксином в PlayerEntity,
 * данные сохраняются в NBT игрока.
 */
public interface CyberAccess {
    Map<CyberSlot, String> redcyber$getParts();

    /**
     * Слоты, у которых игрок отключил дополнительные возможности
     * (меню тумблеров, открывается по удержанию G).
     */
    Set<CyberSlot> redcyber$getDisabled();
}
