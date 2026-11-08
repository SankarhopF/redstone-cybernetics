package com.redcyber.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;

/** Переключение камеры в третье лицо на время работы со столом. */
public final class CameraRig {
    private static Perspective saved = null;

    private CameraRig() {
    }

    public static void enterThirdPerson() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (saved == null) {
            saved = client.options.getPerspective();
        }
        client.options.setPerspective(Perspective.THIRD_PERSON_FRONT);
    }

    public static void restore() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.options.setPerspective(saved == null ? Perspective.FIRST_PERSON : saved);
        saved = null;
    }
}
