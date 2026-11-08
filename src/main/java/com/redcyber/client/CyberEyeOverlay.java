package com.redcyber.client;

import com.redcyber.core.CyberSlot;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/**
 * «Визор» кибернетического глаза.
 *
 * Это не ночное зрение: поверх мира рисуется интерфейс оптики — тёмная виньетка,
 * сканлайны, бегущая строка развёртки, уголки-рамки, прицел и телеметрия по цели.
 * Включается только когда имплант глаза установлен и не отключён в меню тумблеров.
 */
public final class CyberEyeOverlay implements HudRenderCallback {
    private static final int GREEN = 0xFF6FF7C8;
    private static final int GREEN_DIM = 0xB03AD9A6;
    private static final int CYAN = 0xFF7FE4FF;
    private static final int DARK = 0xFF04120C;

    private CyberEyeOverlay() {
    }

    public static void register() {
        HudRenderCallback.EVENT.register(new CyberEyeOverlay());
    }

    @Override
    public void onHudRender(DrawContext context, net.minecraft.client.render.RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.options.hudHidden) {
            return;
        }
        if (client.currentScreen != null) {
            return;
        }
        if (!ClientCyberData.isEnabled(CyberSlot.EYE)) {
            return;
        }

        int w = context.getScaledWindowWidth();
        int h = context.getScaledWindowHeight();
        long time = client.world.getTime();

        drawVignette(context, w, h);
        drawScanlines(context, w, h, time);
        drawSweep(context, w, h, time);
        drawReticle(context, w, h, time);
        drawTelemetry(client, context, w, h);
    }

    // ------------------------------------------------------------------
    //  Элементы визора
    // ------------------------------------------------------------------

    private static void drawVignette(DrawContext context, int w, int h) {
        int edge = Math.max(16, h / 10);
        context.fillGradient(0, 0, w, edge, 0xE004120C, 0x0004120C);
        context.fillGradient(0, h - edge, w, h, 0x0004120C, 0xE004120C);
        context.fill(0, edge, 12, h - edge, 0x3004120C);
        context.fill(w - 12, edge, w, h - edge, 0x3004120C);

        // уголки-рамки
        int arm = 22;
        int thick = 2;
        int pad = edge - 6;
        // левый верхний
        context.fill(pad, pad, pad + arm, pad + thick, GREEN_DIM);
        context.fill(pad, pad, pad + thick, pad + arm, GREEN_DIM);
        // правый верхний
        context.fill(w - pad - arm, pad, w - pad, pad + thick, GREEN_DIM);
        context.fill(w - pad - thick, pad, w - pad, pad + arm, GREEN_DIM);
        // левый нижний
        context.fill(pad, h - pad - thick, pad + arm, h - pad, GREEN_DIM);
        context.fill(pad, h - pad - arm, pad + thick, h - pad, GREEN_DIM);
        // правый нижний
        context.fill(w - pad - arm, h - pad - thick, w - pad, h - pad, GREEN_DIM);
        context.fill(w - pad - thick, h - pad - arm, w - pad, h - pad, GREEN_DIM);
    }

    private static void drawScanlines(DrawContext context, int w, int h, long time) {
        for (int y = 4; y < h; y += 4) {
            // лёгкое «дыхание» интенсивности, чтобы картинка не была статичной
            int alpha = (((y / 4) + (int) (time / 3)) % 8 == 0) ? 0x1E : 0x10;
            context.fill(0, y, w, y + 1, (alpha << 24) | 0x0BFFB4);
        }
    }

    private static void drawSweep(DrawContext context, int w, int h, long time) {
        int y = (int) ((time * 6) % (h + 60)) - 30;
        if (y < 0 || y > h) {
            return;
        }
        context.fillGradient(0, Math.max(0, y - 10), w, y, 0x0000FFC8, 0x2200FFC8);
        context.fill(0, y, w, y + 1, 0x6600FFC8);
    }

    private static void drawReticle(DrawContext context, int w, int h, long time) {
        int cx = w / 2;
        int cy = h / 2;
        int gap = 5;
        int arm = 12;
        int pulse = (int) (0.5 + 0.5 * Math.sin(time * 0.15));

        // крест с разрывом
        context.fill(cx - gap - arm, cy, cx - gap, cy + 1, GREEN);
        context.fill(cx + gap, cy, cx + gap + arm, cy + 1, GREEN);
        context.fill(cx, cy - gap - arm, cx + 1, cy - gap, GREEN);
        context.fill(cx, cy + gap, cx + 1, cy + gap + arm, GREEN);

        // пульсирующая рамка вокруг цели
        int box = 9 + pulse;
        context.fill(cx - box, cy - box, cx + box, cy - box + 1, GREEN_DIM);
        context.fill(cx - box, cy + box, cx + box, cy + box + 1, GREEN_DIM);
        context.fill(cx - box, cy - box, cx - box + 1, cy + box, GREEN_DIM);
        context.fill(cx + box, cy - box, cx + box + 1, cy + box, GREEN_DIM);

        // «уровень» по горизонту
        int lineY = h / 2 + h / 6;
        context.fill(cx - 40, lineY, cx - 8, lineY + 1, 0x8045F2BE);
        context.fill(cx + 8, lineY, cx + 40, lineY + 1, 0x8045F2BE);
    }

    private static void drawTelemetry(MinecraftClient client, DrawContext context, int w, int h) {
        TextRenderer tr = client.textRenderer;
        int edge = Math.max(16, h / 10);
        int x = edge + 6;
        int y = edge + 4;

        context.drawTextWithShadow(tr, Text.literal("OPTIC // RC-EYE 1.1"), x, y, GREEN);
        context.drawTextWithShadow(tr, Text.literal("SCAN MODE: ACTIVE"), x, y + 11, GREEN_DIM);

        String target = targetInfo(client);
        if (target != null) {
            context.drawTextWithShadow(tr, Text.literal(target), x, y + 24, CYAN);
        }

        if (client.player != null) {
            String pos = String.format("POS %.0f %.0f %.0f",
                    client.player.getX(), client.player.getY(), client.player.getZ());
            context.drawTextWithShadow(tr, Text.literal(pos), w - tr.getWidth(pos) - edge - 6, edge + 4, GREEN_DIM);
            String hp = String.format("VIT %d%%",
                    Math.round(100.0f * client.player.getHealth() / Math.max(1.0f, client.player.getMaxHealth())));
            context.drawTextWithShadow(tr, Text.literal(hp), w - tr.getWidth(hp) - edge - 6, edge + 15, GREEN_DIM);
        }
    }

    /** Что в прицеле: блок или существо, и на какой дистанции. */
    private static String targetInfo(MinecraftClient client) {
        HitResult hit = client.crosshairTarget;
        if (client.player == null || hit == null) {
            return null;
        }
        double dist = hit.getPos().distanceTo(client.player.getEyePos());
        if (hit instanceof BlockHitResult blockHit) {
            if (client.world == null) {
                return null;
            }
            var state = client.world.getBlockState(blockHit.getBlockPos());
            String name = state.getBlock().getName().getString();
            return String.format("%s  %.1fm  [%d %d %d]", name, dist,
                    blockHit.getBlockPos().getX(), blockHit.getBlockPos().getY(), blockHit.getBlockPos().getZ());
        }
        if (hit instanceof EntityHitResult entityHit) {
            Entity entity = entityHit.getEntity();
            String name = entity.getName().getString();
            return String.format("%s  %.1fm", name, dist);
        }
        if (hit.getType() == HitResult.Type.BLOCK) {
            return String.format("BLOCK  %.1fm", dist);
        }
        return null;
    }

    /** Не используется, но оставлено для проверки регистрации предметов в телеметрии. */
    @SuppressWarnings("unused")
    private static String itemName(ItemStack stack) {
        return Registries.ITEM.getId(stack.getItem()).getPath();
    }
}
