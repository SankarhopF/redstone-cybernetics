package com.redcyber.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

/**
 * Клиентское состояние «игрок лежит на операционном столе».
 *
 * Пока оно активно: включено третье лицо, стол подсвечен, камера жёстко
 * зафиксирована у стола (см. {@link CameraRig}), а модель персонажа
 * проигрывает анимации из PlayerAnimator.
 */
public final class ClientTableState {
    private static boolean active = false;
    private static BlockPos tablePos = null;
    private static float sessionYaw = 0.0f;

    private ClientTableState() {
    }

    public static void begin(BlockPos pos, float yaw) {
        active = true;
        tablePos = pos == null ? null : pos.toImmutable();
        sessionYaw = yaw;
    }

    public static void end() {
        active = false;
        tablePos = null;
        sessionYaw = 0.0f;
    }

    public static boolean active() {
        return active;
    }

    public static BlockPos tablePos() {
        return tablePos;
    }

    /** Yaw лежащего игрока в момент начала сессии — ось «ноги-голова». */
    public static float sessionYaw() {
        return sessionYaw;
    }

    /** Лежит ли на столе именно это существо (нужно рендереру для масштаба модели). */
    public static boolean isLying(Entity entity) {
        if (!active) {
            return false;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        return client.player != null && client.player == entity;
    }
}
