package com.redcyber.client.screen;

import com.redcyber.client.ClientCyberData;
import com.redcyber.core.CyberSlot;
import com.redcyber.net.ModPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Меню отключения дополнительных возможностей кибернетики.
 *
 * Открывается удержанием клавиши (по умолчанию G). Фон размывается, панель
 * остаётся чёткой. Каждый установленный имплант можно включить или выключить —
 * состояние сохраняется на сервере и влияет на эффекты и атрибуты.
 */
public class AbilityScreen extends Screen {
    private static final int PANEL_W = 320;
    private static final int ROW_H = 26;

    private static final int COL_PANEL = 0xF01B2027;
    private static final int COL_BORDER = 0xFF39424D;
    private static final int COL_ACCENT = 0xFFE02231;
    private static final int COL_TEXT = 0xFFF2F5F7;
    private static final int COL_MUTED = 0xFF97A1AC;

    private final List<CyberSlot> slots = new ArrayList<>();
    private final Map<CyberSlot, ButtonWidget> buttons = new EnumMap<>(CyberSlot.class);

    private int panelX;
    private int panelY;
    private int panelH;

    public AbilityScreen() {
        super(Text.translatable("redcyber.screen.abilities.title"));
    }

    @Override
    protected void init() {
        this.slots.clear();
        this.slots.addAll(ClientCyberData.all().keySet());
        this.slots.sort(Comparator.comparingInt(Enum::ordinal));
        this.buttons.clear();

        int rows = Math.max(1, this.slots.size());
        this.panelH = 64 + rows * ROW_H + 28;
        this.panelX = (this.width - PANEL_W) / 2;
        this.panelY = (this.height - this.panelH) / 2;

        int rowY = this.panelY + 52;
        for (int i = 0; i < this.slots.size(); i++) {
            CyberSlot slot = this.slots.get(i);
            ButtonWidget button = ButtonWidget
                    .builder(this.labelFor(slot), b -> this.toggle(slot))
                    .dimensions(this.panelX + PANEL_W - 96, rowY + i * ROW_H + 3, 84, 20)
                    .build();
            this.buttons.put(slot, button);
            this.addDrawableChild(button);
        }

        this.addDrawableChild(ButtonWidget
                .builder(Text.translatable("redcyber.screen.abilities.close"), b -> this.close())
                .dimensions(this.panelX + (PANEL_W - 120) / 2, this.panelY + this.panelH - 26, 120, 20)
                .build());
    }

    private Text labelFor(CyberSlot slot) {
        boolean enabled = !ClientCyberData.isDisabled(slot);
        return Text.translatable(enabled ? "redcyber.screen.abilities.on" : "redcyber.screen.abilities.off")
                .formatted(enabled ? Formatting.GREEN : Formatting.RED);
    }

    private void toggle(CyberSlot slot) {
        boolean enable = ClientCyberData.isDisabled(slot);
        ClientCyberData.setLocallyEnabled(slot, enable);
        if (ClientPlayNetworking.canSend(ModPackets.ToggleAbilityC2S.ID)) {
            ClientPlayNetworking.send(new ModPackets.ToggleAbilityC2S(slot.key(), enable));
        }
        ButtonWidget button = this.buttons.get(slot);
        if (button != null) {
            button.setMessage(this.labelFor(slot));
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        this.applyBlur(delta);
        context.fillGradient(0, 0, this.width, this.height, 0xA0000000, 0xD0000000);

        int x = this.panelX;
        int y = this.panelY;
        context.fill(x - 3, y - 3, x + PANEL_W + 3, y + this.panelH + 3, 0x80000000);
        context.fill(x, y, x + PANEL_W, y + this.panelH, COL_PANEL);
        context.drawBorder(x, y, PANEL_W, this.panelH, COL_BORDER);
        context.fill(x, y, x + PANEL_W, y + 3, COL_ACCENT);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        TextRenderer tr = this.textRenderer;
        int x = this.panelX;
        int y = this.panelY;

        context.drawText(tr, Text.translatable("redcyber.screen.abilities.title"), x + 12, y + 12, COL_TEXT, false);
        context.drawText(tr, Text.translatable("redcyber.screen.abilities.subtitle"), x + 12, y + 26, COL_MUTED, false);
        context.fill(x + 12, y + 40, x + PANEL_W - 12, y + 41, COL_BORDER);

        if (this.slots.isEmpty()) {
            context.drawText(tr, Text.translatable("redcyber.screen.abilities.empty"),
                    x + 12, y + 56, COL_MUTED, false);
            return;
        }

        int rowY = y + 52;
        for (int i = 0; i < this.slots.size(); i++) {
            CyberSlot slot = this.slots.get(i);
            int ry = rowY + i * ROW_H;
            context.fill(x + 12, ry, x + PANEL_W - 12, ry + ROW_H - 2, 0xFF1A1F26);

            var part = ClientCyberData.installedIn(slot);
            if (part != null) {
                context.drawItem(part.item().getDefaultStack(), x + 16, ry + 5);
                context.drawText(tr, part.item().getName(), x + 38, ry + 4, COL_TEXT, false);
            } else {
                context.drawText(tr, slot.displayName(), x + 38, ry + 4, COL_TEXT, false);
            }
            context.drawText(tr, slot.displayName(), x + 38, ry + 14, COL_MUTED, false);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(null);
    }
}
