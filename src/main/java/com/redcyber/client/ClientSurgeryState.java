package com.redcyber.client;

import com.redcyber.core.CyberParts;
import com.redcyber.core.CyberSlot;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

/** Состояние идущей операции на клиенте (для анимации/оверлея). */
public final class ClientSurgeryState {
    private static String activePart = null;
    private static CyberSlot activeSlot = null;
    private static int duration = 1;
    private static int ticks = 0;

    private ClientSurgeryState() {
    }

    public static void start(String part, int durationTicks) {
        activePart = part;
        var def = CyberParts.byName(part);
        activeSlot = def == null ? null : def.slot();
        duration = Math.max(1, durationTicks);
        ticks = 0;
    }

    public static void clear() {
        activePart = null;
        activeSlot = null;
        ticks = 0;
    }

    public static void clientTick() {
        if (activePart != null) {
            ticks++;
        }
    }

    public static boolean active() {
        return activePart != null;
    }

    public static String part() {
        return activePart;
    }

    /** Слот, который сейчас меняют (для выбора анимации операции). */
    public static CyberSlot slot() {
        return activeSlot;
    }

    public static float progress() {
        return MathHelper.clamp(ticks / (float) duration, 0.0f, 1.0f);
    }

    /** Слот, который сейчас меняют у этого существа (для анимации модели). */
    public static CyberSlot animationSlot(LivingEntity entity) {
        if (activePart == null) {
            return null;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.player != entity) {
            return null;
        }
        return activeSlot;
    }
}
