package com.redcyber;

import com.redcyber.core.CyberParts;
import com.redcyber.core.ModEvents;
import com.redcyber.net.ModPackets;
import com.redcyber.registry.ModBlockEntities;
import com.redcyber.registry.ModBlocks;
import com.redcyber.registry.ModItemGroups;
import com.redcyber.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Redstone Cybernetics — кибернетические части тела на редстоуне.
 *
 * Основная идея: игрок ложится на операционный стол, камера уходит в третье лицо,
 * открывается меню с заблюренным фоном, где выбирается имплант + показывается,
 * какие компоненты для него нужны и сколько их есть в инвентаре. После подтверждения
 * проигрывается анимация операции и часть тела устанавливается.
 */
public class RedCyber implements ModInitializer {
    public static final String MOD_ID = "redcyber";
    public static final Logger LOGGER = LoggerFactory.getLogger("Redstone Cybernetics");

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        // Порядок важен: блоки -> предметы (block item) -> блок-энтити -> части -> сеть -> события
        ModBlocks.init();
        ModItems.init();
        ModBlockEntities.init();
        ModItemGroups.init();
        CyberParts.init();
        ModPackets.registerServer();
        ModEvents.init();

        LOGGER.info("Redstone Cybernetics loaded: {} cybernetic implants registered.", CyberParts.all().size());
    }
}
