package com.redcyber.core;

import java.util.Map;

/**
 * Доступ к установленным имплантам игрока. Реализуется миксином в PlayerEntity,
 * данные сохраняются в NBT игрока.
 */
public interface CyberAccess {
    Map<CyberSlot, String> redcyber$getParts();
}
