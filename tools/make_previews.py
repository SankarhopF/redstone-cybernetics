#!/usr/bin/env python3
"""Рендерит превью интерфейса мода (меню стола и анимация операции).

Координаты взяты 1:1 из OperatingTableScreen/SurgeryScreen, масштаб x2.
Фон — условно "размытый мир" (в игре это настоящий блюр от Minecraft).
"""
import os
from PIL import Image, ImageDraw, ImageFilter, ImageFont

S = 2  # масштаб: 1 GUI-пиксель -> 2 px
W, H = 640 * S, 360 * S
FONT = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
FONT_B = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
TEX = "src/main/resources/assets/redcyber/textures/item"
TEXB = "src/main/resources/assets/redcyber/textures/block"

COL_PANEL = (27, 32, 39, 240)
COL_PANEL_DARK = (20, 23, 28, 240)
COL_BORDER = (57, 66, 77, 255)
COL_ACCENT = (224, 34, 49, 255)
COL_TEXT = (242, 245, 247, 255)
COL_MUTED = (151, 161, 172, 255)
COL_OK = (76, 224, 122, 255)
COL_BAD = (224, 74, 74, 255)
COL_HEADER = (179, 18, 27, 255)


def font(size, bold=False):
    return ImageFont.truetype(FONT_B if bold else FONT, size * S)


def fake_world_blurred():
    """Имитация мира за меню (в игре — настоящий vanilla-блюр)."""
    img = Image.new("RGB", (W, H), (110, 160, 200))
    d = ImageDraw.Draw(img)
    # небо
    for y in range(0, H // 2):
        t = y / (H / 2)
        d.line([(0, y), (W, y)], fill=(int(120 + 40 * t), int(170 + 20 * t), int(215 - 20 * t)))
    # земля
    d.rectangle([0, H // 2, W, H], fill=(84, 64, 44))
    # трава
    d.rectangle([0, H // 2 - 40, W, H // 2], fill=(72, 118, 56))
    # каменные блоки / стол
    d.rectangle([W // 2 - 220, H // 2 + 40, W // 2 + 220, H // 2 + 150], fill=(96, 104, 112))
    d.rectangle([W // 2 - 200, H // 2 + 20, W // 2 + 200, H // 2 + 55], fill=(70, 78, 86))
    # деревья
    for x in range(80, W, 260):
        d.rectangle([x, H // 2 - 220, x + 40, H // 2], fill=(74, 54, 38))
        d.ellipse([x - 70, H // 2 - 330, x + 110, H // 2 - 120], fill=(52, 104, 52))
    return img.filter(ImageFilter.GaussianBlur(14))


def icon(name, folder=TEX):
    im = Image.open(os.path.join(folder, name + ".png")).convert("RGBA")
    return im.resize((16 * S, 16 * S), Image.NEAREST)


def text(d, xy, s, f, color=COL_TEXT, shadow=True):
    x, y = xy
    if shadow:
        d.text((x + S, y + S), s, font=f, fill=(0, 0, 0, 140))
    d.text((x, y), s, font=f, fill=color)


def button(d, x, y, w, h, label, active=True, accent=False):
    x, y, w, h = x * S, y * S, w * S, h * S
    d.rectangle([x, y, x + w, y + h], fill=(20, 22, 26, 255))
    top = accent and (170, 30, 40) or ((110, 116, 124) if active else (74, 78, 84))
    bot = accent and (130, 20, 30) or ((88, 94, 102) if active else (58, 62, 68))
    for i in range(h - 2):
        t = i / max(1, h - 3)
        col = tuple(int(top[c] + (bot[c] - top[c]) * t) for c in range(3)) + (255,)
        d.line([(x + 2, y + 1 + i), (x + w - 2, y + 1 + i)], fill=col)
    d.rectangle([x, y, x + w, y + h], outline=(16, 18, 22, 255))
    f = font(7, True)
    tw = d.textlength(label, font=f)
    tx, ty = x + w / 2 - tw / 2, y + h / 2 - 7 * S
    d.text((tx + S, ty + S), label, font=f, fill=(0, 0, 0, 160))
    d.text((tx, ty), label, font=f, fill=COL_TEXT if active else (150, 155, 160, 255))


# ------------------------------------------------------------------ меню
def menu():
    img = fake_world_blurred().convert("RGBA")
    # затемнение как в коде (fillGradient 0xB0000000 -> 0xE0000000)
    dark = Image.new("RGBA", (W, H))
    dd = ImageDraw.Draw(dark)
    for y in range(H):
        a = int(0xB0 + (0xE0 - 0xB0) * (y / H))
        dd.line([(0, y), (W, y)], fill=(0, 0, 0, a))
    img.alpha_composite(dark)

    d = ImageDraw.Draw(img)
    PX, PY, PW, PH = (W - 384 * S) // 2, (H - 240 * S) // 2, 384 * S, 240 * S

    def R(x0, y0, x1, y1, col):
        d.rectangle([PX + x0 * S, PY + y0 * S, PX + x1 * S, PY + y1 * S], fill=col)

    R(-3, -3, 387, 243, (0, 0, 0, 128))
    R(0, 0, 384, 240, COL_PANEL)
    d.rectangle([PX, PY, PX + PW, PY + PH], outline=COL_BORDER, width=S)
    R(0, 0, 384, 3, (179, 18, 27, 255))
    R(8, 32, 196, 204, COL_PANEL_DARK)
    R(196, 32, 376, 204, COL_PANEL_DARK)
    R(10, 28, 374, 29, COL_BORDER)

    text(d, (PX + 12 * S, PY + 10 * S), "ОПЕРАЦИОННЫЙ СТОЛ", font(6, True))
    sub = "Редстоуновая кибернетика"
    f = font(5)
    text(d, (PX + PW - 12 * S - d.textlength(sub, font=f), PY + 10 * S), sub, f, COL_MUTED)

    parts = [
        ("cyber_eye", "Кибернетический глаз", "Глаз", False),
        ("cyber_arm", "Кибернетическая рука", "Рука", False),
        ("cyber_legs", "Кибернетические ноги", "Ноги", False),
        ("cyber_heart", "Кибернетическое сердце", "Сердце", False),
        ("cyber_lungs", "Кибернетические лёгкие", "Лёгкие", False),
        ("cyber_spine", "Кибернетический позвоночник", "Позвоночник", True),
    ]
    for i, (tex, name, slot, installed) in enumerate(parts):
        x, y = PX + 12 * S, PY + (36 + i * 28) * S
        w, h = 178 * S, 26 * S
        sel = i == 1
        d.rectangle([x, y, x + w, y + h], fill=(43, 52, 64, 255) if sel else (26, 31, 38, 255))
        d.rectangle([x, y, x + w, y + h], outline=COL_ACCENT if sel else COL_BORDER, width=S)
        img.alpha_composite(icon(tex), (x + 5 * S, y + 5 * S))
        text(d, (x + 26 * S, y + 3 * S), name, font(6), COL_MUTED if installed else COL_TEXT)
        text(d, (x + 26 * S, y + 14 * S), slot, font(5), COL_MUTED)
        if installed:
            text(d, (x + w - 16 * S, y + 8 * S), "✔", font(9, True), COL_OK)

    # правая колонка
    rx = PX + 200 * S
    ry = PY + 38 * S
    text(d, (rx, ry), "Кибернетическая рука", font(6, True))
    text(d, (rx, ry + 13 * S), "Слот тела: Рука", font(5), COL_MUTED)
    text(d, (rx, ry + 29 * S), "+2 к урону, быстрее копает", font(7), COL_OK)
    text(d, (rx, ry + 49 * S), "НУЖНЫЕ КОМПОНЕНТЫ", font(6, True), COL_HEADER)

    comps = [
        ("power_cell", "Энергоячейка", 1, 1),
        ("redstone_servo", "Редстоуновое серво", 3, 2),
        ("redstone_circuit", "Редстоуновая схема", 1, 1),
        ("redstone_alloy_ingot", "Редстоуновый сплав", 7, 4),
    ]
    for i, (tex, name, have, need) in enumerate(comps):
        y = PY + (100 + i * 20) * S
        img.alpha_composite(icon(tex), (rx, y - 4 * S))
        ok = have >= need
        text(d, (rx + 20 * S, y - 1 * S), name, font(6), COL_TEXT if ok else COL_BAD)
        cnt = f"{have}/{need}"
        text(d, (rx + 172 * S - d.textlength(cnt, font=font(6)), y - 1 * S), cnt, font(6), COL_OK if ok else COL_BAD)

    text(d, (rx, PY + 194 * S), "Компоненты в сборе — можно начинать", font(5), COL_MUTED)

    button(d, (PX + 12 * S) / S, (PY + 208 * S) / S, 120, 20, "Встать со стола", active=True)
    button(d, (PX + 200 * S) / S, (PY + 208 * S) / S, 172, 20, "Начать операцию", active=True, accent=True)
    return img.convert("RGB")


# ------------------------------------------------------------------ операция
def surgery():
    img = fake_world_blurred().convert("RGBA")
    d = ImageDraw.Draw(img)
    bar = 32 * S
    d.rectangle([0, 0, W, bar], fill=(0, 0, 0, 196))
    d.rectangle([0, H - bar, W, H], fill=(0, 0, 0, 196))
    # красное пульсирующее свечение (полупрозрачный слой, как fillGradient в игре)
    glow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    for i in range(70 * S):
        a = int(70 * (1 - i / (70 * S)))
        gd.line([(0, bar + i), (W, bar + i)], fill=(179, 18, 27, a))
        gd.line([(0, H - bar - i), (W, H - bar - i)], fill=(179, 18, 27, a))
    img.alpha_composite(glow)

    title = "ИДЁТ ОПЕРАЦИЯ"
    f = font(8, True)
    tw = d.textlength(title, font=f)
    text(d, ((W - tw) / 2, 8 * S), title, f, (224, 34, 49, 255))
    sub = "Замена: Кибернетическая рука"
    fs = font(8)
    sw = d.textlength(sub, font=fs)
    text(d, ((W - sw) / 2, 19 * S), sub, fs, COL_TEXT)
    img.alpha_composite(icon("cyber_arm"), (int((W - tw) / 2 - 24 * S), 6 * S))

    # прогресс
    pbW, pbX = 220 * S, (W - 220 * S) // 2
    pbY = H - bar - 40 * S
    d.rectangle([pbX - S, pbY - S, pbX + pbW + S, pbY + 9 * S], fill=(0, 0, 0, 255))
    d.rectangle([pbX, pbY, pbX + pbW, pbY + 8 * S], fill=(42, 49, 56, 255))
    p = 0.62
    d.rectangle([pbX, pbY, pbX + int(pbW * p), pbY + 8 * S], fill=(224, 34, 49, 255))
    d.rectangle([pbX, pbY, pbX + int(pbW * p), pbY + 2 * S], fill=(255, 107, 74, 255))

    phase = "Установка модуля"
    fp = font(8)
    pw = d.textlength(phase, font=fp)
    text(d, ((W - pw) / 2, pbY - 14 * S), phase, fp, COL_TEXT)
    pc = "62%"
    text(d, ((W - d.textlength(pc, font=fp)) / 2, pbY + 13 * S), pc, fp, COL_MUTED)
    hs = "Не двигайтесь..."
    text(d, ((W - d.textlength(hs, font=fp)) / 2, H - bar + 11 * S), hs, fp, (242, 245, 247, 160))
    return img.convert("RGB")



# ------------------------------------------------------------------ тумблеры имплантов
def abilities():
    """Меню отключения возможностей (AbilityScreen): блюр мира + панель с тумблерами."""
    img = fake_world_blurred().convert("RGBA")
    dark = Image.new("RGBA", (W, H))
    dd = ImageDraw.Draw(dark)
    for y in range(H):
        a = int(0xA0 + (0xD0 - 0xA0) * (y / H))
        dd.line([(0, y), (W, y)], fill=(0, 0, 0, a))
    img.alpha_composite(dark)

    d = ImageDraw.Draw(img)
    PW_GUI, ROW_H = 320, 26
    rows = [
        ("cyber_eye", "Кибернетический глаз", "Глаз", True),
        ("cyber_arm", "Кибернетическая рука", "Рука", True),
        ("cyber_legs", "Кибернетические ноги", "Ноги", False),
    ]
    panel_h_gui = 64 + max(1, len(rows)) * ROW_H + 28
    PX = (W - PW_GUI * S) // 2
    PY = (H - panel_h_gui * S) // 2
    PH = panel_h_gui * S

    d.rectangle([PX - 3 * S, PY - 3 * S, PX + PW_GUI * S + 3 * S, PY + PH + 3 * S], fill=(0, 0, 0, 128))
    d.rectangle([PX, PY, PX + PW_GUI * S, PY + PH], fill=COL_PANEL)
    d.rectangle([PX, PY, PX + PW_GUI * S, PY + PH], outline=COL_BORDER, width=S)
    d.rectangle([PX, PY, PX + PW_GUI * S, PY + 3 * S], fill=COL_ACCENT)

    text(d, (PX + 12 * S, PY + 12 * S), "ВОЗМОЖНОСТИ КИБЕРНЕТИКИ", font(6, True))
    text(d, (PX + 12 * S, PY + 26 * S), "Удерживайте G, чтобы открыть · включение и отключение имплантов",
         font(5), COL_MUTED)
    d.rectangle([PX + 12 * S, PY + 40 * S, PX + (PW_GUI - 12) * S, PY + 41 * S], fill=COL_BORDER)

    for i, (tex, name, slot, enabled) in enumerate(rows):
        ry = PY + (52 + i * ROW_H) * S
        d.rectangle([PX + 12 * S, ry, PX + (PW_GUI - 12) * S, ry + (ROW_H - 2) * S], fill=(26, 31, 38, 255))
        img.alpha_composite(icon(tex), (PX + 16 * S, ry + 5 * S))
        text(d, (PX + 38 * S, ry + 3 * S), name, font(6), COL_TEXT)
        text(d, (PX + 38 * S, ry + 14 * S), slot, font(5), COL_MUTED)
        button(d, (PX + (PW_GUI - 96) * S) / S, (ry + 3 * S) / S, 84, 20,
               "ВКЛ" if enabled else "ВЫКЛ", active=enabled,
               accent=False)

    button(d, (PX + (PW_GUI - 120) / 2 * S) / S, (PY + PH - 26 * S) / S, 120, 20, "Закрыть", active=True)
    return img.convert("RGB")


# ------------------------------------------------------------------ визор кибернетического глаза
def eye():
    """HUD кибернетического глаза (CyberEyeOverlay): виньетка, сканлайны, прицел, телеметрия."""
    img = fake_world_blurred().filter(ImageFilter.GaussianBlur(1.2)).convert("RGBA")
    GREEN = (111, 247, 200)
    GREEN_DIM = (58, 217, 166)
    CYAN = (127, 228, 255)

    # полупрозрачные слои рисуем отдельно и только потом накладываем —
    # иначе ImageDraw просто затирает пиксели вместо смешивания
    ov = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    od = ImageDraw.Draw(ov)

    edge = max(16, 360 // 10) * S
    for i in range(edge):
        a_top = int(0xE0 * (1 - i / edge))
        od.line([(0, i), (W, i)], fill=(4, 18, 12, a_top))
        od.line([(0, H - 1 - i), (W, H - 1 - i)], fill=(4, 18, 12, a_top))
    od.rectangle([0, edge, 12 * S, H - edge], fill=(4, 18, 12, 0x30))
    od.rectangle([W - 12 * S, edge, W, H - edge], fill=(4, 18, 12, 0x30))

    for y in range(4 * S, H, 4 * S):
        a = 0x1E if (y // (4 * S)) % 8 == 0 else 0x10
        od.line([(0, y), (W, y)], fill=(11, 255, 180, a))

    sweep = H // 3
    for i in range(10 * S):
        a = int(0x22 * (i / (10 * S)))
        od.line([(0, sweep - 10 * S + i), (W, sweep - 10 * S + i)], fill=(0, 255, 200, a))
    od.line([(0, sweep), (W, sweep)], fill=(0, 255, 200, 0x66))
    img.alpha_composite(ov)

    d = ImageDraw.Draw(img)
    pad = edge - 6 * S
    arm = 22 * S
    th = 2 * S
    for (bx, by, sx, sy) in [(pad, pad, 1, 1), (W - pad, pad, -1, 1), (pad, H - pad, 1, -1), (W - pad, H - pad, -1, -1)]:
        d.rectangle([min(bx, bx + sx * arm), min(by, by + sy * th),
                     max(bx, bx + sx * arm), max(by, by + sy * th)], fill=GREEN_DIM + (255,))
        d.rectangle([min(bx, bx + sx * th), min(by, by + sy * arm),
                     max(bx, bx + sx * th), max(by, by + sy * arm)], fill=GREEN_DIM + (255,))

    cx, cy = W // 2, H // 2
    gap, ra = 5 * S, 12 * S
    for (x0, y0, x1, y1) in [
        (cx - gap - ra, cy, cx - gap, cy + S), (cx + gap, cy, cx + gap + ra, cy + S),
        (cx, cy - gap - ra, cx + S, cy - gap), (cx, cy + gap, cx + S, cy + gap + ra),
    ]:
        d.rectangle([x0, y0, x1, y1], fill=GREEN + (255,))
    box = 10 * S
    d.rectangle([cx - box, cy - box, cx + box, cy - box + S], fill=GREEN_DIM + (255,))
    d.rectangle([cx - box, cy + box, cx + box, cy + box + S], fill=GREEN_DIM + (255,))
    d.rectangle([cx - box, cy - box, cx - box + S, cy + box], fill=GREEN_DIM + (255,))
    d.rectangle([cx + box, cy - box, cx + box + S, cy + box], fill=GREEN_DIM + (255,))
    line_y = H // 2 + H // 6
    d.rectangle([cx - 40 * S, line_y, cx - 8 * S, line_y + S], fill=(69, 242, 190, 0x80))
    d.rectangle([cx + 8 * S, line_y, cx + 40 * S, line_y + S], fill=(69, 242, 190, 0x80))

    tx, ty = edge + 6 * S, edge + 4 * S
    text(d, (tx, ty), "OPTIC // RC-EYE 1.1", font(6, True), GREEN + (255,))
    text(d, (tx, ty + 11 * S), "SCAN MODE: ACTIVE", font(5), GREEN_DIM + (255,))
    text(d, (tx, ty + 24 * S), "Камень  3.4м  [128 64 -37]", font(6), CYAN + (255,))
    pos = "POS 128 64 -37"
    hp = "VIT 86%"
    f = font(5)
    text(d, (W - edge - 6 * S - d.textlength(pos, font=f), ty), pos, f, GREEN_DIM + (255,))
    text(d, (W - edge - 6 * S - d.textlength(hp, font=f), ty + 11 * S), hp, f, GREEN_DIM + (255,))
    return img.convert("RGB")


if __name__ == "__main__":
    os.makedirs("docs", exist_ok=True)
    menu().save("docs/preview_menu.png")
    surgery().save("docs/preview_surgery.png")
    abilities().save("docs/preview_abilities.png")
    eye().save("docs/preview_eye.png")
    print("previews generated")
