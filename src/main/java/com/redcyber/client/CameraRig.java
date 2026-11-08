package com.redcyber.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.Vec3d;

/**
 * Фиксированная камера операции.
 *
 * Камера <b>не зависит</b> от того, куда смотрит игрок: она всегда стоит со стороны
 * ножного конца стола, слева от пациента и чуть выше его, и смотрит на лежащего.
 * Положение считается от центра лежащего и оси тела (yaw сессии), см.
 * {@code CameraMixin}.
 */
public final class CameraRig {
    /** Насколько камера отходит назад — в сторону ног. */
    private static final double BACK_OFFSET = 1.05;
    /** Смещение влево от пациента (влево = по левую руку смотрящего от ног к голове). */
    private static final double SIDE_OFFSET = 1.30;
    /** Высота камеры над лежащим. */
    private static final double HEIGHT_OFFSET = 1.45;
    /** Точка прицеливания смещена чуть к голове и на уровень груди. */
    private static final double AIM_FORWARD = 0.20;
    private static final double AIM_HEIGHT = 0.45;

    private static Perspective saved = null;

    private CameraRig() {
    }

    public static void enterThirdPerson() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (saved == null) {
            saved = client.options.getPerspective();
        }
        // Камеру всё равно переопределяет CameraMixin; режим нужен только для того,
        // чтобы игра рисовала модель самого игрока.
        client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
    }

    public static void restore() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (saved != null) {
            client.options.setPerspective(saved);
            saved = null;
        }
    }

    /** Позиция камеры в мире. */
    public static Vec3d cameraPos(Vec3d focus, float yawDeg) {
        Vec3d forward = forward(yawDeg);
        Vec3d left = new Vec3d(forward.z, 0.0, -forward.x);
        return focus.add(forward.multiply(-BACK_OFFSET))
                .add(left.multiply(SIDE_OFFSET))
                .add(0.0, HEIGHT_OFFSET, 0.0);
    }

    /** Точка, на которую смотрит камера (грудь пациента). */
    public static Vec3d aimPos(Vec3d focus, float yawDeg) {
        Vec3d forward = forward(yawDeg);
        return focus.add(forward.multiply(AIM_FORWARD)).add(0.0, AIM_HEIGHT, 0.0);
    }

    /** Горизонтальное направление оси тела по yaw игрока. */
    private static Vec3d forward(float yawDeg) {
        double rad = Math.toRadians(yawDeg);
        return new Vec3d(-Math.sin(rad), 0.0, Math.cos(rad));
    }
}
