package com.redcyber.client.screen;

import com.redcyber.core.CyberPart;
import com.redcyber.core.CyberParts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

/**
 * Кинематографичная анимация замены части тела.
 *
 * Мир остаётся видимым (камера в третьем лице смотрит на лежащего персонажа),
 * сверху накладываются кинематографические полосы, прогресс операции, искры
 * редстоуна и «судороги» собеседника (анимация модели — в PlayerEntityModelMixin).
 */
public class SurgeryScreen extends Screen {
    private final String partName;
    private final int duration;
    private int ticks = 0;
    private int lastPhase = -1;

    public SurgeryScreen(String partName, int duration) {
        super(Text.translatable("redcyber.screen.surgery.title"));
        this.partName = partName;
        this.duration = Math.max(1, duration);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    /** Фон не рисуем: игрок должен видеть операцию на персонаже. */
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void tick() {
        super.tick();
        this.ticks++;
        this.emitEffects();

        // Страховка: если сервер почему-то не прислал конец операции — выходим сами.
        if (this.ticks > this.duration + 100) {
            MinecraftClient client = MinecraftClient.getInstance();
            client.setScreen(null);
            com.redcyber.client.CameraRig.restore();
            com.redcyber.client.ClientSurgeryState.clear();
        }
    }

    private float progress() {
        return MathHelper.clamp(this.ticks / (float) this.duration, 0.0f, 1.0f);
    }

    private int phase() {
        float p = this.progress();
        if (p < 0.15f) {
            return 0;
        }
        if (p < 0.35f) {
            return 1;
        }
        if (p < 0.68f) {
            return 2;
        }
        if (p < 0.9f) {
            return 3;
        }
        return 4;
    }

    private static final String[] PHASES = {
            "redcyber.screen.surgery.phase.anesthesia",
            "redcyber.screen.surgery.phase.incision",
            "redcyber.screen.surgery.phase.implant",
            "redcyber.screen.surgery.phase.wiring",
            "redcyber.screen.surgery.phase.calibration"
    };

    private void emitEffects() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) {
            return;
        }

        double px = player.getX();
        double py = player.getY() + 0.35;
        double pz = player.getZ();

        if (this.ticks % 2 == 0) {
            for (int i = 0; i < 2; i++) {
                double ox = (player.getRandom().nextDouble() - 0.5) * 1.0;
                double oz = (player.getRandom().nextDouble() - 0.5) * 1.0;
                double oy = (player.getRandom().nextDouble() - 0.5) * 0.35;
                player.getWorld().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        px + ox, py + oy, pz + oz, 0.0, 0.015, 0.0);
            }
        }

        int phase = this.phase();
        if (phase != this.lastPhase) {
            this.lastPhase = phase;
            switch (phase) {
                case 0 -> player.playSound(SoundEvents.BLOCK_BEACON_ACTIVATE, 0.6f, 0.6f);
                case 1 -> player.playSound(SoundEvents.BLOCK_ANVIL_USE, 0.55f, 1.4f);
                case 2 -> player.playSound(SoundEvents.BLOCK_PISTON_EXTEND, 0.8f, 1.1f);
                case 3 -> player.playSound(SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.85f, 1.5f);
                default -> player.playSound(SoundEvents.BLOCK_BEACON_POWER_SELECT, 0.7f, 1.2f);
            }
        }
        if (this.ticks % 10 == 5) {
            player.playSound(SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, 0.25f, 1.9f);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        TextRenderer tr = this.textRenderer;
        int w = this.width;
        int h = this.height;
        int barH = 32;

        // Кинематографические полосы
        context.fill(0, 0, w, barH, 0xC4000000);
        context.fill(0, h - barH, w, h, 0xC4000000);

        // Пульсирующее красное свечение сверху/снизу
        int pulse = (int) (70 + 45 * (0.5 + 0.5 * MathHelper.sin(this.ticks * 0.32f)));
        int glow = (pulse << 24) | 0xB3121B;
        context.fillGradient(0, barH, w, barH + 70, glow, 0x00B3121B);
        context.fillGradient(0, h - barH - 70, w, h - barH, 0x00B3121B, glow);

        // Заголовок и иконка импланта
        CyberPart part = CyberParts.byName(this.partName);
        Text title = Text.translatable("redcyber.screen.surgery.title").formatted(Formatting.BOLD);
        int titleW = tr.getWidth(title);
        int titleY = 8;
        context.drawText(tr, title, (w - titleW) / 2, titleY, 0xFFE02231, true);
        Text sub = Text.translatable("redcyber.screen.surgery.target",
                part == null ? Text.literal(this.partName) : part.item().getName());
        context.drawCenteredTextWithShadow(tr, sub, w / 2, titleY + 11, 0xFFF2F5F7);
        if (part != null) {
            context.drawItem(new ItemStack(part.item()), (w - titleW) / 2 - 24, titleY - 2);
        }

        // Прогресс
        int pbW = 220;
        int pbX = (w - pbW) / 2;
        int pbY = h - barH - 40;
        context.fill(pbX - 1, pbY - 1, pbX + pbW + 1, pbY + 9, 0xFF000000);
        context.fill(pbX, pbY, pbX + pbW, pbY + 8, 0xFF2A3138);
        context.fill(pbX, pbY, pbX + (int) (pbW * this.progress()), pbY + 8, 0xFFE02231);
        context.fill(pbX, pbY, pbX + (int) (pbW * this.progress()), pbY + 2, 0xFFFF6B4A);

        context.drawCenteredTextWithShadow(tr, Text.translatable(PHASES[this.phase()]), w / 2, pbY - 14, 0xFFF2F5F7);
        Text percent = Text.literal((int) (this.progress() * 100) + "%");
        context.drawCenteredTextWithShadow(tr, percent, w / 2, pbY + 13, 0xFF97A1AC);

        // Подсказка
        context.drawCenteredTextWithShadow(tr, Text.translatable("redcyber.screen.surgery.hold_still"),
                w / 2, h - barH + 11, 0x9AF2F5F7);
    }
}
