#!/usr/bin/env python3
"""Генерирует JSON-ресурсы мода: модели, блокстейты, рецепты, локализации."""
import json
import os

ROOT = "src/main/resources/assets/redcyber"
DATA = "src/main/resources/data/redcyber"

ITEMS = [
    "redstone_alloy_ingot", "redstone_circuit", "redstone_servo", "neural_chip", "power_cell",
    "cyber_eye", "cyber_arm", "cyber_legs", "cyber_heart", "cyber_lungs", "cyber_spine",
]


def write(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, ensure_ascii=False, indent=2)
        f.write("\n")


# ---------------------------------------------------------------- модели
for item in ITEMS:
    write(f"{ROOT}/models/item/{item}.json", {
        "parent": "minecraft:item/generated",
        "textures": {"layer0": f"redcyber:item/{item}"},
    })

DISPLAY = {
    "gui": {"rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [0.625, 0.625, 0.625]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [0.25, 0.25, 0.25]},
    "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.5, 0.5, 0.5]},
    "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.375, 0.375, 0.375]},
    "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 0, 0], "scale": [0.4, 0.4, 0.4]},
    "firstperson_lefthand": {"rotation": [0, 225, 0], "translation": [0, 0, 0], "scale": [0.4, 0.4, 0.4]},
}


def table_model(lit=False):
    top = "operating_table_top_on" if lit else "operating_table_top"
    side = "operating_table_side_on" if lit else "operating_table_side"
    elements = [
        {   # столешница (верх на высоте 14/16)
            "from": [0, 12, 0],
            "to": [16, 14, 16],
            "faces": {
                "up": {"texture": f"#top", "cullface": "up"},
                "down": {"texture": "#bottom", "cullface": "down"},
                "north": {"texture": f"#side"},
                "south": {"texture": f"#side"},
                "east": {"texture": f"#side"},
                "west": {"texture": f"#side"},
            },
        },
        {   # ножки
            "from": [1, 0, 1], "to": [3, 12, 3],
            "faces": {k: {"texture": "#side"} for k in ("north", "south", "east", "west", "up", "down")},
        },
        {
            "from": [13, 0, 1], "to": [15, 12, 3],
            "faces": {k: {"texture": "#side"} for k in ("north", "south", "east", "west", "up", "down")},
        },
        {
            "from": [1, 0, 13], "to": [3, 12, 15],
            "faces": {k: {"texture": "#side"} for k in ("north", "south", "east", "west", "up", "down")},
        },
        {
            "from": [13, 0, 13], "to": [15, 12, 15],
            "faces": {k: {"texture": "#side"} for k in ("north", "south", "east", "west", "up", "down")},
        },
        {   # нижняя полка с редстоуновой шиной
            "from": [3, 3, 3], "to": [13, 4, 13],
            "faces": {k: {"texture": "#side"} for k in ("north", "south", "east", "west", "up", "down")},
        },
        {   # подголовник
            "from": [4, 14, 1], "to": [12, 16, 4],
            "faces": {
                "up": {"texture": "#top"},
                "down": {"texture": "#bottom"},
                "north": {"texture": "#side"},
                "south": {"texture": "#side"},
                "east": {"texture": "#side"},
                "west": {"texture": "#side"},
            },
        },
    ]
    return {
        "parent": "block/block",
        "textures": {
            "particle": f"redcyber:block/{top}",
            "top": f"redcyber:block/{top}",
            "side": f"redcyber:block/{side}",
            "bottom": "redcyber:block/operating_table_bottom",
        },
        "elements": elements,
        "display": DISPLAY,
    }


write(f"{ROOT}/models/block/operating_table.json", table_model(False))
write(f"{ROOT}/models/block/operating_table_lit.json", table_model(True))
write(f"{ROOT}/models/item/operating_table.json", {"parent": "redcyber:block/operating_table"})

write(f"{ROOT}/blockstates/operating_table.json", {
    "variants": {
        "lit=false": {"model": "redcyber:block/operating_table"},
        "lit=true": {"model": "redcyber:block/operating_table_lit"},
    }
})

# ---------------------------------------------------------------- рецепты
def shaped(pattern, key, result, count=1, category="misc"):
    return {
        "type": "minecraft:crafting_shaped",
        "category": category,
        "pattern": pattern,
        "key": {k: {"item": v} for k, v in key.items()},
        "result": {"id": f"redcyber:{result}", "count": count},
    }


recipes = {
    "redstone_alloy_ingot": shaped(
        ["RRR", "RIR", "RRR"],
        {"R": "minecraft:redstone", "I": "minecraft:iron_ingot"},
        "redstone_alloy_ingot", 1),
    "redstone_circuit": shaped(
        ["RQR", "QGQ", "RQR"],
        {"R": "minecraft:redstone", "Q": "minecraft:quartz", "G": "minecraft:gold_ingot"},
        "redstone_circuit", 2),
    "redstone_servo": shaped(
        ["IAI", "CPC", "IAI"],
        {"I": "minecraft:iron_ingot", "A": "redcyber:redstone_alloy_ingot",
         "C": "redcyber:redstone_circuit", "P": "minecraft:piston"},
        "redstone_servo", 1),
    "neural_chip": shaped(
        ["RGR", "ECE", "RGR"],
        {"R": "minecraft:redstone", "G": "minecraft:gold_ingot",
         "C": "redcyber:redstone_circuit", "E": "minecraft:ender_pearl"},
        "neural_chip", 1),
    "power_cell": shaped(
        ["AGA", "GRG", "AGA"],
        {"A": "redcyber:redstone_alloy_ingot", "G": "minecraft:glowstone_dust",
         "R": "minecraft:redstone_block"},
        "power_cell", 1),
    "operating_table": shaped(
        ["AAA", "IRI", "I I"],
        {"A": "redcyber:redstone_alloy_ingot", "I": "minecraft:iron_ingot",
         "R": "minecraft:redstone_block"},
        "operating_table", 1, category="misc"),
}

for name, recipe in recipes.items():
    write(f"{DATA}/recipe/{name}.json", recipe)

print("json resources generated")
