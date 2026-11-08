package com.redcyber.client.screen;

import com.redcyber.client.CameraRig;
import com.redcyber.client.ClientCyberData;
import com.redcyber.client.ClientTableState;
import com.redcyber.core.CyberPart;
import com.redcyber.core.CyberParts;
import com.redcyber.net.ModPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Меню операционного стола.
 *
 * Фон (мир) размывается настоящим vanilla-блюром, панель меню при этом остаётся
 * полностью чёткой. Слева — список имплантов, справа — что нужно для операции
 * и сколько компонентов уже есть в инвентаре.
 */
public class OperatingTableScreen extends Screen {
    private static final int PANEL_W = 384;
    private static final int PANEL_H = 240;

    private static final int COL_PANEL = 0xF01B2027;
    private static final int COL_PANEL_DARK = 0xF014171C;
    private static final int COL_BORDER = 0xFF39424D;
    private static final int COL_ACCENT = 0xFFE02231;
    private static final int COL_TEXT = 0xFFF2F5F7;
    private static final int COL_MUTED = 0xFF97A1AC;
    private static final int COL_OK = 0xFF4CE07A;
    private static final int COL_BAD = 0xFFE04A4A;
    private static final int COL_HEADER = 0xFFB3121B;

    private final BlockPos tablePos;
    private final List<CyberPart> parts = new ArrayList<>(CyberParts.all());
    private final List<PartRow> rows = new ArrayList<>();

    private int selected = 0;
    private int panelX;
    private int panelY;
    private ButtonWidget installButton;
    private String pendingPart = null;
    private Text status = null;
    private boolean statusBad = false;
    private boolean leavingForSurgery = false;

    public OperatingTableScreen(BlockPos tablePos) {
        super(Text.translatable("redcyber.screen.table.title"));
        this.tablePos = tablePos;
    }

    // ------------------------------------------------------------------
    //  Инициализация виджетов
    // ------------------------------------------------------------------

    @Override
    protected void init() {
        this.panelX = (this.width - PANEL_W) / 2;
        this.panelY = (this.height - PANEL_H) / 2;
        this.rows.clear();

        int rowX = this.panelX + 12;
        int rowY = this.panelY + 36;
        for (int i = 0; i < this.parts.size(); i++) {
            PartRow row = new PartRow(this, i, rowX, rowY + i * 28, 178, 26);
            this.rows.add(row);
            this.addDrawableChild(row);
        }

        this.installButton = ButtonWidget.builder(Text.translatable("redcyber.screen.table.operate"), b -> this.confirm())
                .dimensions(this.panelX + 200, this.panelY + PANEL_H - 32, 172, 20)
                .build();
        this.addDrawableChild(this.installButton);

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("redcyber.screen.table.leave"), b -> this.close())
                .dimensions(this.panelX + 12, this.panelY + PANEL_H - 32, 120, 20)
                .build());

        this.updateState();
    }

    // ------------------------------------------------------------------
    //  Фон: блюр мира + затемнение + панель
    // ------------------------------------------------------------------

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Настоящее размытие мира (тот же эффект, что у ванильных меню).
        this.applyBlur(delta);
        // Затемнение, чтобы панель читалась.
        context.fillGradient(0, 0, this.width, this.height, 0xB0000000, 0xE0000000);

        this.drawPanel(context);
        this.drawDetails(context);
    }

    private void drawPanel(DrawContext context) {
        int x = this.panelX;
        int y = this.panelY;

        context.fill(x - 3, y - 3, x + PANEL_W + 3, y + PANEL_H + 3, 0x80000000);
        context.fill(x, y, x + PANEL_W, y + PANEL_H, COL_PANEL);
        context.drawBorder(x, y, PANEL_W, PANEL_H, COL_BORDER);
        context.fill(x, y, x + PANEL_W, y + 3, COL_HEADER);

        context.fill(x + 8, y + 32, x + 196, y + 204, COL_PANEL_DARK);
        context.fill(x + 196, y + 32, x + PANEL_W - 8, y + 204, COL_PANEL_DARK);
        context.fill(x + 10, y + 28, x + PANEL_W - 10, y + 29, COL_BORDER);

        TextRenderer tr = this.textRenderer;
        context.drawText(tr, Text.translatable("redcyber.screen.table.title").formatted(Formatting.BOLD),
                x + 12, y + 11, 0xFFFFFFFF, false);
        Text sub = Text.translatable("redcyber.screen.table.subtitle");
        context.drawText(tr, sub, x + PANEL_W - 12 - tr.getWidth(sub), y + 11, COL_MUTED, false);
    }

    private void drawDetails(DrawContext context) {
        TextRenderer tr = this.textRenderer;
        CyberPart part = this.parts.get(this.selected);
        int rx = this.panelX + 200;
        int ry = this.panelY + 40;
        boolean installed = ClientCyberData.has(part.slot());

        context.drawText(tr, part.item().getName().copy().formatted(Formatting.BOLD), rx, ry, COL_TEXT, false);
        ry += 13;
        context.drawText(tr, Text.translatable("redcyber.screen.table.slot", part.slot().displayName()),
                rx, ry, COL_MUTED, false);
        ry += 14;

        List<OrderedText> wrapped = tr.wrapLines(Text.translatable(part.effectKey()), 172);
        for (int i = 0; i < Math.min(3, wrapped.size()); i++) {
            context.drawText(tr, wrapped.get(i), rx, ry + i * 10, COL_OK, false);
        }
        ry += Math.min(3, wrapped.size()) * 10 + 8;

        context.drawText(tr, Text.translatable("redcyber.screen.table.components").formatted(Formatting.BOLD),
                rx, ry, COL_HEADER, false);
        ry += 13;

        for (Map.Entry<Item, Integer> entry : part.components().entrySet()) {
            int have = this.countInInventory(entry.getKey());
            int need = entry.getValue();
            boolean enough = have >= need;
            context.drawItem(new ItemStack(entry.getKey()), rx, ry - 4);
            context.drawText(tr, entry.getKey().getName(), rx + 20, ry, enough ? COL_TEXT : COL_BAD, false);
            String counter = have + "/" + need;
            context.drawText(tr, counter, rx + 172 - tr.getWidth(counter), ry, enough ? COL_OK : COL_BAD, false);
            ry += 20;
        }

        if (installed) {
            context.drawText(tr, Text.translatable("redcyber.screen.table.already"), rx, this.panelY + 196, COL_OK, false);
        } else if (this.status != null) {
            context.drawText(tr, this.status, rx, this.panelY + 196, this.statusBad ? COL_BAD : COL_MUTED, false);
        }
    }

    // ------------------------------------------------------------------
    //  Логика
    // ------------------------------------------------------------------

    private void select(int index) {
        if (index >= 0 && index < this.parts.size()) {
            this.selected = index;
            this.status = null;
            this.statusBad = false;
            this.updateState();
        }
    }

    private CyberPart selectedPart() {
        return this.parts.get(this.selected);
    }

    private boolean canAfford(CyberPart part) {
        for (Map.Entry<Item, Integer> entry : part.components().entrySet()) {
            if (this.countInInventory(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    private int countInInventory(Item item) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return 0;
        }
        int total = 0;
        for (ItemStack stack : player.getInventory().main) {
            if (stack.isOf(item)) {
                total += stack.getCount();
            }
        }
        for (ItemStack stack : player.getInventory().offHand) {
            if (stack.isOf(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private void updateState() {
        CyberPart part = this.selectedPart();
        boolean installed = ClientCyberData.has(part.slot());
        boolean affordable = this.canAfford(part);
        boolean pending = this.pendingPart != null;

        if (this.installButton != null) {
            this.installButton.active = !installed && affordable && !pending;
            this.installButton.setMessage(installed
                    ? Text.translatable("redcyber.screen.table.installed")
                    : pending
                    ? Text.translatable("redcyber.screen.table.pending")
                    : Text.translatable("redcyber.screen.table.operate"));
        }
        if (!pending && !installed) {
            if (!affordable) {
                this.status = Text.translatable("redcyber.screen.table.need_more");
                this.statusBad = true;
            } else {
                this.status = Text.translatable("redcyber.screen.table.ready");
                this.statusBad = false;
            }
        }
    }

    private void confirm() {
        if (this.pendingPart != null) {
            return;
        }
        CyberPart part = this.selectedPart();
        if (ClientCyberData.has(part.slot()) || !this.canAfford(part)) {
            return;
        }
        this.pendingPart = part.name();
        this.status = Text.translatable("redcyber.screen.table.pending");
        this.statusBad = false;
        this.updateState();
        if (ClientPlayNetworking.canSend(ModPackets.InstallPartC2S.ID)) {
            ClientPlayNetworking.send(new ModPackets.InstallPartC2S(this.tablePos, part.name()));
        }
    }

    // ------------------------------------------------------------------
    //  События от сетевого слоя
    // ------------------------------------------------------------------

    /** Сервер начал операцию — этот экран уступает место анимации. */
    public void onSurgeryStarted(String partName) {
        this.leavingForSurgery = true;
    }

    /** Сервер отказал (не хватает компонентов и т.п.). */
    public void onInstallRejected() {
        this.pendingPart = null;
        this.status = Text.translatable("redcyber.msg.missing_components");
        this.statusBad = true;
        this.updateState();
    }

    @Override
    public void removed() {
        if (!this.leavingForSurgery) {
            if (ClientPlayNetworking.canSend(ModPackets.CloseTableC2S.ID)) {
                ClientPlayNetworking.send(new ModPackets.CloseTableC2S());
            }
            ClientTableState.end();
            CameraRig.restore();
        }
        super.removed();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    // ------------------------------------------------------------------
    //  Строка списка имплантов
    // ------------------------------------------------------------------

    private static class PartRow extends ClickableWidget {
        private final OperatingTableScreen parent;
        private final int index;

        PartRow(OperatingTableScreen parent, int index, int x, int y, int width, int height) {
            super(x, y, width, height, parent.parts.get(index).item().getName());
            this.parent = parent;
            this.index = index;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            TextRenderer tr = MinecraftClient.getInstance().textRenderer;
            CyberPart part = this.parent.parts.get(this.index);
            boolean selected = this.parent.selected == this.index;
            boolean installed = ClientCyberData.has(part.slot());

            int x = this.getX();
            int y = this.getY();
            int w = this.getWidth();
            int h = this.getHeight();

            int bg = selected ? 0xFF2B3440 : (this.isHovered() ? 0xFF232B34 : 0xFF1A1F26);
            context.fill(x, y, x + w, y + h, bg);
            context.drawBorder(x, y, w, h, selected ? COL_ACCENT : COL_BORDER);
            context.drawItem(part.item().getDefaultStack(), x + 5, y + 5);
            context.drawText(tr, part.item().getName(), x + 26, y + 5, installed ? COL_MUTED : COL_TEXT, false);
            context.drawText(tr, part.slot().displayName(), x + 26, y + 15, COL_MUTED, false);
            if (installed) {
                context.drawText(tr, "\u2714", x + w - 14, y + 9, COL_OK, false);
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            this.parent.select(this.index);
            this.parent.updateState();
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            this.appendDefaultNarrations(builder);
        }
    }
}
