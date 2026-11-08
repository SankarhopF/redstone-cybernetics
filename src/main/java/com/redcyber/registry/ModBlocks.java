package com.redcyber.registry;

import com.redcyber.RedCyber;
import com.redcyber.block.OperatingTableBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

public final class ModBlocks {
    private ModBlocks() {
    }

    public static final Block OPERATING_TABLE = register("operating_table",
            new OperatingTableBlock(AbstractBlock.Settings.create()
                    .strength(3.5f, 6.0f)
                    .sounds(BlockSoundGroup.METAL)
                    .nonOpaque()
                    .luminance(state -> state.get(OperatingTableBlock.LIT) ? 9 : 0)));

    private static Block register(String name, Block block) {
        return Registry.register(Registries.BLOCK, RedCyber.id(name), block);
    }

    public static void init() {
        // Static initializer is enough; method exists for explicit ordering in RedCyber.
    }
}
