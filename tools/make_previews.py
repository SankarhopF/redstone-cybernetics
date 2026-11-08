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


if __name__ == "__main__":
    os.makedirs("docs", exist_ok=True)
    menu().save("docs/preview_menu.png")
    surgery().save("docs/preview_surgery.png")
    print("previews generated")
