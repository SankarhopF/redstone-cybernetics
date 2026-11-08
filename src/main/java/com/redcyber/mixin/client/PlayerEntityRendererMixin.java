package com.redcyber.mixin.client;

import com.redcyber.client.ClientTableState;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Лежащий на столе персонаж слегка уменьшается и приподнимается, чтобы тело
 * аккуратно легло на поверхность одного блока стола (а не свисало с него).
 */
@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {

    @Inject(method = "setupTransforms(Lnet/minecraft/client/network/AbstractClientPlayerEntity;"
            + "Lnet/minecraft/client/util/math/MatrixStack;FFFF)V", at = @At("TAIL"))
    private void redcyber$lyingOnTable(AbstractClientPlayerEntity player, MatrixStack matrices,
                                       float animationProgress, float bodyYaw, float tickDelta,
                                       float scale, CallbackInfo ci) {
        if (!ClientTableState.isLying(player)) {
            return;
        }
        matrices.translate(0.0, 0.16, 0.0);
        matrices.scale(0.85f, 0.85f, 0.85f);
    }
}
