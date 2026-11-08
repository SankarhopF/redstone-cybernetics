package com.redcyber.client.anim;

import com.redcyber.RedCyber;
import com.redcyber.client.ClientSurgeryState;
import com.redcyber.client.ClientTableState;
import com.redcyber.core.CyberSlot;
import dev.kosmx.playerAnim.api.IPlayable;
import dev.kosmx.playerAnim.api.layered.IActualAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Анимации операции через PlayerAnimator (не ванильные позы!).
 *
 * На каждого игрока добавляется свой слой анимаций, в который мы подменяем
 * эмоуты из {@code assets/redcyber/player_animations/*.json}: «лежит на столе»
 * и «идёт замена конечности». Переходы — через плавный фейд, поэтому никакого
 * дёргания конечностей, как было с ручной правкой модели.
 */
public final class CyberAnimations {
    private static final Logger LOGGER = LoggerFactory.getLogger("RedCyber/Animations");

    private static final Identifier LIE = RedCyber.id("surgery_lying");
    private static final Identifier OP_ARM = RedCyber.id("surgery_arm");
    private static final Identifier OP_LEG = RedCyber.id("surgery_leg");
    private static final Identifier OP_HEAD = RedCyber.id("surgery_head");

    /** Слой PlayerAnimator на каждого игрока (ключ — сам игрок). */
    private static final Map<AbstractClientPlayerEntity, ModifierLayer<IActualAnimation<?>>> LAYERS =
            new WeakHashMap<>();

    private static Identifier current = null;
    private static boolean warnedMissing = false;

    private CyberAnimations() {
    }

    public static void install() {
        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((player, stack) -> {
            ModifierLayer<IActualAnimation<?>> layer = new ModifierLayer<>();
            stack.addAnimLayer(1000, layer);
            LAYERS.put(player, layer);
        });
    }

    /** Вызывается каждый клиентский тик: решает, какая анимация должна играть. */
    public static void tick() {
        AbstractClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            current = null;
            return;
        }
        ModifierLayer<IActualAnimation<?>> layer = LAYERS.get(player);
        if (layer == null) {
            return;
        }

        Identifier want;
        if (ClientSurgeryState.active()) {
            want = surgeryAnimation(ClientSurgeryState.slot());
        } else if (ClientTableState.active()) {
            want = LIE;
        } else {
            want = null;
        }

        if (want == null) {
            if (current != null) {
                stop(layer);
                current = null;
            }
            return;
        }
        if (want.equals(current)) {
            return;
        }

        boolean fromBase = current == null;
        if (play(layer, want, fromBase)) {
            current = want;
        }
    }

    private static Identifier surgeryAnimation(CyberSlot slot) {
        if (slot == null) {
            return LIE;
        }
        return switch (slot) {
            case ARM -> OP_ARM;
            case LEG -> OP_LEG;
            case EYE -> OP_HEAD;
            default -> LIE;
        };
    }

    private static boolean play(ModifierLayer<IActualAnimation<?>> layer, Identifier id, boolean fromBase) {
        IPlayable playable = PlayerAnimationRegistry.getAnimation(id);
        if (playable == null) {
            if (!warnedMissing) {
                warnedMissing = true;
                LOGGER.warn("Анимация {} не загружена PlayerAnimator: работаем без неё.", id);
            }
            return false;
        }
        IActualAnimation<?> animation = playable.playAnimation();
        layer.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(fromBase ? 10 : 6, Ease.INOUTSINE),
                animation, fromBase);
        return true;
    }

    private static void stop(ModifierLayer<IActualAnimation<?>> layer) {
        layer.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(8, Ease.OUTSINE), null);
    }
}
