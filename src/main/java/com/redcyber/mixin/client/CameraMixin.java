package com.redcyber.mixin.client;

import com.redcyber.client.CameraRig;
import com.redcyber.client.ClientTableState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Жёстко ставит камеру у операционного стола, полностью игнорируя то, куда смотрит
 * игрок: со стороны ног, слева и чуть выше, с прицелом на лежащего пациента.
 */
@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    protected abstract void setPos(double x, double y, double z);

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V",
            at = @At("TAIL"))
    private void redcyber$tableCamera(BlockView area, Entity focusedEntity, boolean thirdPerson,
                                      boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (!ClientTableState.active()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || focusedEntity != client.player) {
            return;
        }

        Vec3d focus = focusedEntity.getLerpedPos(tickDelta);
        float bodyYaw = ClientTableState.sessionYaw();
        Vec3d cam = CameraRig.cameraPos(focus, bodyYaw);
        Vec3d aim = CameraRig.aimPos(focus, bodyYaw);

        Vec3d dir = aim.subtract(cam);
        double len = dir.length();
        if (len < 1.0E-4) {
            return;
        }
        dir = dir.multiply(1.0 / len);

        float yaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
        float pitch = (float) -Math.toDegrees(Math.asin(dir.y));

        this.setPos(cam.x, cam.y, cam.z);
        this.setRotation(yaw, pitch);
    }
}
