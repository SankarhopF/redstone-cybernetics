#!/usr/bin/env python3
"""Генератор пиксель-арт текстур для мода Redstone Cybernetics (16x16)."""
import os
from PIL import Image, ImageDraw

OUT_ITEM = "src/main/resources/assets/redcyber/textures/item"
OUT_BLOCK = "src/main/resources/assets/redcyber/textures/block"
OUT_ROOT = "src/main/resources/assets/redcyber"

TRANSPARENT = (0, 0, 0, 0)
OUTLINE = (14, 17, 20, 255)
DARK = (46, 51, 56, 255)
METAL = (74, 82, 89, 255)
LIGHT = (124, 133, 142, 255)
HIGH = (199, 207, 214, 255)
RED_D = (122, 15, 26, 255)
RED = (211, 32, 47, 255)
HOT = (255, 107, 74, 255)
AMBER = (255, 217, 102, 255)
GREEN = (59, 224, 122, 255)
COPPER = (192, 138, 62, 255)
BOARD = (26, 74, 52, 255)
BOARD_L = (38, 104, 72, 255)
GOLD = (222, 178, 74, 255)


def new_img():
    return Image.new("RGBA", (16, 16), TRANSPARENT)


def px(d, x, y, color):
    if 0 <= x < 16 and 0 <= y < 16:
        d.point((x, y), fill=color)


def rect(d, x0, y0, x1, y1, color):
    d.rectangle([x0, y0, x1, y1], fill=color)


def rect_outline(d, x0, y0, x1, y1, color):
    d.rectangle([x0, y0, x1, y1], outline=color)


def save(img, folder, name):
    os.makedirs(folder, exist_ok=True)
    img.save(os.path.join(folder, name + ".png"))


# ----------------------------------------------------------------------------
#  Компоненты
# ----------------------------------------------------------------------------

def alloy_ingot():
    img = new_img()
    d = ImageDraw.Draw(img)
    # трапеция-слиток
    rows = [
        (4, 5, 11), (5, 4, 12), (6, 3, 12), (7, 3, 12),
        (8, 4, 11), (9, 5, 10), (10, 5, 10),
    ]
    for y, x0, x1 in rows:
        rect(d, x0, y, x1, y, METAL)
    for y, x0, x1 in rows:
        px(d, x0, y, LIGHT)
        px(d, x1, y, DARK)
    # верхняя грань
    rect(d, 5, 4, 11, 4, LIGHT)
    # редстоуновая жила
    rect(d, 5, 7, 10, 7, RED)
    rect(d, 5, 8, 10, 8, RED_D)
    px(d, 6, 7, HOT)
    px(d, 8, 7, HOT)
    px(d, 7, 9, RED)
    # обводка
    for x in range(4, 12):
        px(d, x, 3, OUTLINE)
    px(d, 3, 6, OUTLINE)
    px(d, 12, 9, OUTLINE)
    rect(d, 6, 10, 9, 10, OUTLINE)
    return img


def redstone_circuit():
    img = new_img()
    d = ImageDraw.Draw(img)
    rect(d, 1, 1, 14, 14, BOARD)
    rect_outline(d, 1, 1, 14, 14, (16, 48, 34, 255))
    # дорожки
    for x in range(2, 14):
        px(d, x, 3, BOARD_L)
        px(d, x, 12, BOARD_L)
    for y in range(3, 13):
        px(d, 2, y, BOARD_L)
        px(d, 13, y, BOARD_L)
    rect(d, 5, 5, 10, 10, DARK)
    rect_outline(d, 5, 5, 10, 10, GOLD)
    rect(d, 6, 6, 9, 9, (30, 34, 38, 255))
    rect(d, 7, 7, 8, 8, RED)
    px(d, 7, 7, HOT)
    # контактные площадки
    for (x, y) in [(3, 3), (12, 3), (3, 12), (12, 12), (7, 2), (8, 13)]:
        px(d, x, y, GOLD)
    return img


def redstone_servo():
    img = new_img()
    d = ImageDraw.Draw(img)
    rect(d, 2, 4, 13, 12, METAL)
    rect_outline(d, 2, 4, 13, 12, OUTLINE)
    rect(d, 3, 5, 12, 6, LIGHT)
    # двигатель
    d.ellipse([5, 6, 10, 11], fill=DARK, outline=OUTLINE)
    d.ellipse([6, 7, 9, 10], fill=RED_D)
    d.ellipse([7, 8, 8, 9], fill=RED)
    # вал
    rect(d, 7, 1, 8, 4, HIGH)
    rect(d, 7, 1, 8, 1, OUTLINE)
    # клеммы
    rect(d, 3, 12, 4, 13, COPPER)
    rect(d, 11, 12, 12, 13, COPPER)
    px(d, 13, 12, RED)
    return img


def neural_chip():
    img = new_img()
    d = ImageDraw.Draw(img)
    rect(d, 3, 3, 12, 12, (26, 28, 32, 255))
    rect_outline(d, 3, 3, 12, 12, OUTLINE)
    rect(d, 5, 5, 10, 10, (44, 48, 54, 255))
    rect_outline(d, 5, 5, 10, 10, METAL)
    rect(d, 6, 6, 9, 9, RED_D)
    rect(d, 7, 7, 8, 8, HOT)
    # ножки
    for i, x in enumerate(range(4, 12, 2)):
        rect(d, x, 1, x, 2, HIGH)
        rect(d, x, 13, x, 14, HIGH)
        rect(d, 1, x, 2, x, HIGH)
        rect(d, 13, x, 14, x, HIGH)
    px(d, 4, 4, GREEN)
    return img


def power_cell():
    img = new_img()
    d = ImageDraw.Draw(img)
    rect(d, 4, 2, 11, 13, METAL)
    rect_outline(d, 4, 2, 11, 13, OUTLINE)
    rect(d, 5, 3, 7, 12, LIGHT)
    # редстоуновое ядро
    rect(d, 8, 4, 10, 12, RED_D)
    rect(d, 9, 5, 10, 11, RED)
    rect(d, 9, 6, 9, 10, HOT)
    # крышки
    rect(d, 4, 1, 11, 2, HIGH)
    rect(d, 4, 13, 11, 14, HIGH)
    rect(d, 6, 0, 9, 1, COPPER)
    px(d, 7, 0, AMBER)
    # индикатор
    px(d, 5, 4, GREEN)
    return img


# ----------------------------------------------------------------------------
#  Импланты
# ----------------------------------------------------------------------------

def cyber_eye():
    img = new_img()
    d = ImageDraw.Draw(img)
    d.ellipse([1, 3, 14, 12], fill=OUTLINE)
    d.ellipse([2, 4, 13, 11], fill=METAL)
    d.ellipse([3, 5, 12, 10], fill=(24, 27, 31, 255))
    d.ellipse([4, 5, 11, 10], fill=RED_D)
    d.ellipse([5, 6, 10, 9], fill=RED)
    d.ellipse([6, 6, 9, 9], fill=(20, 22, 26, 255))
    d.ellipse([7, 7, 8, 8], fill=HOT)
    # блик
    px(d, 5, 6, HIGH)
    px(d, 6, 6, (255, 220, 210, 255))
    # металлические сегменты
    rect(d, 6, 2, 9, 3, LIGHT)
    rect(d, 6, 12, 9, 13, LIGHT)
    px(d, 2, 7, HIGH)
    px(d, 13, 8, HIGH)
    return img


def cyber_arm():
    img = new_img()
    d = ImageDraw.Draw(img)
    # плечо
    rect(d, 2, 2, 7, 6, METAL)
    rect_outline(d, 2, 2, 7, 6, OUTLINE)
    rect(d, 3, 3, 6, 3, LIGHT)
    # предплечье
    rect(d, 8, 4, 12, 12, METAL)
    rect_outline(d, 8, 4, 12, 12, OUTLINE)
    rect(d, 9, 5, 11, 5, LIGHT)
    # сустав
    d.ellipse([6, 5, 10, 9], fill=DARK, outline=OUTLINE)
    d.ellipse([7, 6, 9, 8], fill=RED)
    px(d, 8, 7, HOT)
    # кисть/пальцы
    rect(d, 9, 12, 11, 15, LIGHT)
    px(d, 10, 13, METAL)
    px(d, 10, 14, METAL)
    # редстоуновая жила
    rect(d, 4, 4, 4, 5, RED)
    rect(d, 11, 8, 11, 11, RED_D)
    return img


def cyber_legs():
    img = new_img()
    d = ImageDraw.Draw(img)
    for x0 in (3, 9):
        rect(d, x0, 2, x0 + 4, 13, METAL)
        rect_outline(d, x0, 2, x0 + 4, 13, OUTLINE)
        rect(d, x0 + 1, 3, x0 + 1, 12, LIGHT)
        # колено
        d.ellipse([x0, 7, x0 + 4, 10], fill=DARK, outline=OUTLINE)
        px(d, x0 + 2, 8, RED)
        # стопа
        rect(d, x0 - 1, 13, x0 + 4, 14, DARK)
    return img


def cyber_heart():
    img = new_img()
    d = ImageDraw.Draw(img)
    # силуэт сердца
    d.ellipse([2, 2, 8, 8], fill=METAL)
    d.ellipse([7, 2, 13, 8], fill=METAL)
    d.polygon([(2, 6), (13, 6), (7, 14)], fill=METAL)
    d.ellipse([3, 3, 7, 7], fill=LIGHT)
    d.ellipse([8, 3, 12, 7], fill=LIGHT)
    # контур
    d.line([(2, 6), (7, 14), (13, 6)], fill=OUTLINE)
    d.arc([2, 2, 8, 8], 180, 360, fill=OUTLINE)
    d.arc([7, 2, 13, 8], 180, 360, fill=OUTLINE)
    # редстоуновое ядро
    d.ellipse([6, 6, 9, 9], fill=RED_D)
    d.ellipse([7, 7, 8, 8], fill=HOT)
    # трубки
    rect(d, 5, 9, 5, 11, RED)
    rect(d, 10, 9, 10, 11, RED)
    px(d, 7, 12, RED)
    px(d, 8, 12, RED_D)
    return img


def cyber_lungs():
    img = new_img()
    d = ImageDraw.Draw(img)
    # бронх
    rect(d, 7, 1, 8, 6, LIGHT)
    rect(d, 6, 6, 9, 7, METAL)
    # доли
    for side in (-1, 1):
        cx = 8 + side * 4
        d.ellipse([cx - 3, 5, cx + 3, 14], fill=METAL)
        d.ellipse([cx - 2, 6, cx + 2, 13], fill=(58, 64, 71, 255))
        d.ellipse([cx - 2, 5, cx + 2, 14], outline=OUTLINE)
        # полосы
        for y in (8, 10, 12):
            rect(d, cx - 2, y, cx + 2, y, LIGHT)
        rect(d, cx, 6, cx, 13, RED_D)
    px(d, 7, 2, HIGH)
    return img


def cyber_spine():
    img = new_img()
    d = ImageDraw.Draw(img)
    # позвонки
    for i, y in enumerate(range(1, 15, 3)):
        w = 5 if i % 2 == 0 else 4
        x0 = 8 - w // 2
        rect(d, x0, y, x0 + w - 1, y + 2, METAL)
        rect_outline(d, x0, y, x0 + w - 1, y + 2, OUTLINE)
        rect(d, x0 + 1, y + 1, x0 + w - 2, y + 1, LIGHT)
        # отростки
        px(d, x0 - 1, y + 1, LIGHT)
        px(d, x0 + w, y + 1, LIGHT)
        # редстоуновый канал
        px(d, 7, y + 2, RED)
        px(d, 8, y + 2, RED_D)
    # кабель
    rect(d, 7, 1, 8, 14, (24, 27, 31, 255))
    for y in range(2, 14, 2):
        px(d, 7, y, RED)
    return img


# ----------------------------------------------------------------------------
#  Операционный стол
# ----------------------------------------------------------------------------

def table_top(lit=False):
    img = new_img()
    d = ImageDraw.Draw(img)
    rect(d, 0, 0, 15, 15, DARK)
    # металлическая плита
    rect(d, 1, 1, 14, 14, METAL)
    rect(d, 1, 1, 14, 2, LIGHT)
    rect(d, 1, 13, 14, 14, (58, 64, 71, 255))
    # панель
    rect(d, 3, 3, 12, 12, (34, 38, 43, 255))
    rect_outline(d, 3, 3, 12, 12, OUTLINE)
    # центральная редстоуновая прорезь
    core = HOT if lit else (150, 26, 36, 255)
    glow = AMBER if lit else RED
    rect(d, 4, 7, 11, 8, core)
    rect(d, 4, 9, 11, 9, glow)
    rect(d, 7, 4, 8, 11, glow)
    px(d, 7, 7, (255, 255, 255, 255) if lit else HIGH)
    px(d, 8, 8, (255, 255, 255, 255) if lit else HIGH)
    # заклёпки
    for (x, y) in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        px(d, x, y, HIGH)
    if lit:
        rect(d, 3, 3, 3, 12, AMBER)
        rect(d, 12, 3, 12, 12, AMBER)
    return img


def table_side(lit=False):
    img = new_img()
    d = ImageDraw.Draw(img)
    rect(d, 0, 0, 15, 15, DARK)
    rect(d, 0, 0, 15, 1, LIGHT)
    rect(d, 0, 14, 15, 14, (28, 31, 35, 255))
    rect(d, 1, 2, 14, 13, (58, 64, 71, 255))
    # горизонтальные рёбра
    for y in (5, 9):
        rect(d, 1, y, 14, y, (40, 44, 49, 255))
    # редстоуновая линия
    line = AMBER if lit else RED_D
    rect(d, 1, 7, 14, 7, line)
    # заклёпки
    for x in (2, 5, 10, 13):
        px(d, x, 4, HIGH)
        px(d, x, 11, HIGH)
    return img


def table_bottom():
    img = new_img()
    d = ImageDraw.Draw(img)
    rect(d, 0, 0, 15, 15, DARK)
    rect(d, 1, 1, 14, 14, (40, 44, 49, 255))
    rect_outline(d, 1, 1, 14, 14, OUTLINE)
    for (x, y) in [(3, 3), (12, 3), (3, 12), (12, 12)]:
        px(d, x, y, METAL)
    return img


def mod_icon():
    """128x128 иконка мода: редстоуновый глаз на плате."""
    base = Image.new("RGBA", (32, 32), (18, 22, 26, 255))
    d = ImageDraw.Draw(base)
    d.rectangle([0, 0, 31, 31], fill=(18, 22, 26, 255))
    # рамка
    d.rectangle([1, 1, 30, 30], outline=(179, 18, 27, 255))
    # дорожки
    for x in range(4, 28, 4):
        d.line([(x, 3), (x, 29)], fill=(30, 66, 48, 255))
    for y in range(4, 28, 4):
        d.line([(3, y), (29, y)], fill=(30, 66, 48, 255))
    # глаз
    d.ellipse([4, 9, 27, 22], fill=OUTLINE)
    d.ellipse([6, 11, 25, 20], fill=METAL)
    d.ellipse([8, 12, 23, 19], fill=RED_D)
    d.ellipse([11, 13, 20, 18], fill=RED)
    d.ellipse([14, 14, 17, 17], fill=(24, 24, 24, 255))
    d.ellipse([15, 15, 16, 16], fill=HOT)
    d.point((11, 13), fill=HIGH)
    return base.resize((128, 128), Image.NEAREST)


def main():
    save(alloy_ingot(), OUT_ITEM, "redstone_alloy_ingot")
    save(redstone_circuit(), OUT_ITEM, "redstone_circuit")
    save(redstone_servo(), OUT_ITEM, "redstone_servo")
    save(neural_chip(), OUT_ITEM, "neural_chip")
    save(power_cell(), OUT_ITEM, "power_cell")

    save(cyber_eye(), OUT_ITEM, "cyber_eye")
    save(cyber_arm(), OUT_ITEM, "cyber_arm")
    save(cyber_legs(), OUT_ITEM, "cyber_legs")
    save(cyber_heart(), OUT_ITEM, "cyber_heart")
    save(cyber_lungs(), OUT_ITEM, "cyber_lungs")
    save(cyber_spine(), OUT_ITEM, "cyber_spine")

    save(table_top(False), OUT_BLOCK, "operating_table_top")
    save(table_top(True), OUT_BLOCK, "operating_table_top_on")
    save(table_side(False), OUT_BLOCK, "operating_table_side")
    save(table_side(True), OUT_BLOCK, "operating_table_side_on")
    save(table_bottom(), OUT_BLOCK, "operating_table_bottom")

    os.makedirs(OUT_ROOT, exist_ok=True)
    mod_icon().save(os.path.join(OUT_ROOT, "icon.png"))
    print("textures generated")


if __name__ == "__main__":
    main()
