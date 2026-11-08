package com.redcyber.client.anim;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Мост к PlayerAnimator. Все обращения к классам библиотеки спрятаны за проверкой
 * {@link #available()}, поэтому мод работает и без установленного PlayerAnimator
 * (тогда персонаж просто лежит в ванильной позе, без плавных анимаций).
 */
public final class AnimationBridge {
    private static Boolean available = null;

    private AnimationBridge() {
    }

    public static boolean available() {
        if (available == null) {
            available = FabricLoader.getInstance().isModLoaded("playeranimator");
        }
        return available;
    }

    public static void install() {
        if (available()) {
            CyberAnimations.install();
        }
    }

    public static void tick() {
        if (available()) {
            CyberAnimations.tick();
        }
    }
}
