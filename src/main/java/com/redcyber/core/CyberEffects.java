package com.redcyber.core;

import com.redcyber.RedCyber;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Применяет эффекты установленных имплантов к игроку. */
public final class CyberEffects {
    private CyberEffects() {
    }

    /** Описание одного модификатора атрибута. */
    private record Mod(RegistryEntry<EntityAttribute> attribute, double value) {
    }

    private static final Map<CyberSlot, List<Mod>> MODIFIERS = new EnumMap<>(CyberSlot.class);

    static {
        MODIFIERS.put(CyberSlot.EYE, List.of(
                new Mod(EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE, 1.0),
                new Mod(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE, 1.0)));

        MODIFIERS.put(CyberSlot.ARM, List.of(
                new Mod(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0),
                new Mod(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED, 0.4),
                new Mod(EntityAttributes.PLAYER_MINING_EFFICIENCY, 2.0)));

        MODIFIERS.put(CyberSlot.LEG, List.of(
                new Mod(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.035),
                new Mod(EntityAttributes.GENERIC_STEP_HEIGHT, 0.6),
                new Mod(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE, 2.0),
                new Mod(EntityAttributes.GENERIC_JUMP_STRENGTH, 0.06)));

        MODIFIERS.put(CyberSlot.HEART, List.of(
                new Mod(EntityAttributes.GENERIC_MAX_HEALTH, 6.0),
                new Mod(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.15)));

        MODIFIERS.put(CyberSlot.LUNG, List.of(
                new Mod(EntityAttributes.GENERIC_OXYGEN_BONUS, 4.0),
                new Mod(EntityAttributes.PLAYER_SUBMERGED_MINING_SPEED, 1.0)));

        MODIFIERS.put(CyberSlot.SPINE, List.of(
                new Mod(EntityAttributes.GENERIC_FALL_DAMAGE_MULTIPLIER, -0.9),
                new Mod(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.35),
                new Mod(EntityAttributes.GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE, 0.3)));
    }

    /** Пересчитывает все модификаторы и статус-эффекты по текущему набору имплантов. */
    public static void refresh(ServerPlayerEntity player) {
        Map<CyberSlot, String> installed = CyberData.parts(player);

        for (CyberSlot slot : CyberSlot.values()) {
            List<Mod> mods = MODIFIERS.getOrDefault(slot, List.of());
            for (int i = 0; i < mods.size(); i++) {
                Mod mod = mods.get(i);
                EntityAttributeInstance instance = player.getAttributeInstance(mod.attribute());
                if (instance == null) {
                    continue;
                }
                var id = RedCyber.id("implant_" + slot.lowerName() + "_" + i);
                instance.removeModifier(id);
                if (installed.containsKey(slot)) {
                    instance.addPersistentModifier(new EntityAttributeModifier(id, mod.value(),
                            EntityAttributeModifier.Operation.ADD_VALUE));
                }
            }
        }

        if (installed.containsKey(CyberSlot.EYE)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 500, 0, true, false, false));
        }
        if (installed.containsKey(CyberSlot.LUNG)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 500, 0, true, false, false));
        }
        if (installed.containsKey(CyberSlot.HEART)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 120, 0, true, false, true));
        }
    }

    /** Краткая строка для тултипа/меню. */
    public static List<String> modifierIds(CyberSlot slot) {
        List<String> ids = new ArrayList<>();
        List<Mod> mods = MODIFIERS.getOrDefault(slot, List.of());
        for (int i = 0; i < mods.size(); i++) {
            ids.add("implant_" + slot.lowerName() + "_" + i);
        }
        return ids;
    }
}
