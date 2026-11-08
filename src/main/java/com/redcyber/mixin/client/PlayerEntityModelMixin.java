package com.redcyber.mixin.client;

import com.redcyber.client.ClientSurgeryState;
import com.redcyber.core.CyberSlot;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Анимация во время операции: у лежащего игрока подёргивается та часть тела,
 * которую меняют (рука, нога, голова и т.д.), плюс лёгкие «судороги» корпуса.
 */
@Mixin(PlayerEntityModel.class)
public abstract class PlayerEntityModelMixin extends BipedEntityModel<LivingEntity> {

    public PlayerEntityModelMixin(ModelPart root, boolean thinArms) {
        super(root);
    }

    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void redcyber$animateSurgery(LivingEntity entity, float limbAngle, float limbDistance,
                                         float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
        CyberSlot slot = ClientSurgeryState.animationSlot(entity);
        if (slot == null) {
            return;
        }

        float t = animationProgress;
        float twitch = MathHelper.sin(t * 1.35f);
        float slow = MathHelper.sin(t * 0.35f);

        switch (slot) {
            case ARM -> {
                this.rightArm.pitch = -1.35f + twitch * 0.45f;
                this.rightArm.roll = 0.35f * MathHelper.sin(t * 0.9f);
                this.leftArm.pitch = -0.25f + slow * 0.08f;
            }
            case LEG -> {
                this.rightLeg.pitch = 0.55f * twitch;
                this.rightLeg.roll = 0.2f * slow;
                this.leftLeg.pitch = -0.25f + slow * 0.06f;
            }
            case HEART -> {
                this.body.pitch += 0.07f * MathHelper.sin(t * 1.1f);
                this.body.roll = 0.05f * slow;
                this.rightArm.pitch = -0.3f + twitch * 0.2f;
                this.leftArm.pitch = -0.3f - twitch * 0.2f;
            }
            case EYE -> {
                this.head.yaw = 0.22f * MathHelper.sin(t * 0.8f);
                this.head.pitch += 0.1f * twitch;
                this.head.roll = 0.08f * slow;
            }
            case LUNG -> {
                this.body.pitch += 0.05f * MathHelper.sin(t * 0.55f);
                this.head.yaw = 0.06f * slow;
            }
            case SPINE -> {
                this.body.roll = 0.12f * MathHelper.sin(t * 0.5f);
                this.body.yaw += 0.05f * slow;
            }
        }

        // Обе руки чуть подрагивают в такт «операции».
        this.rightArm.yaw += 0.04f * twitch;
        this.leftArm.yaw -= 0.04f * twitch;
    }
}
