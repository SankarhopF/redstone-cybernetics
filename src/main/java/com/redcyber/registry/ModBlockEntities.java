package com.redcyber.registry;

import com.redcyber.RedCyber;
import com.redcyber.block.OperatingTableBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModBlockEntities {
    private ModBlockEntities() {
    }

    public static final BlockEntityType<OperatingTableBlockEntity> OPERATING_TABLE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, RedCyber.id("operating_table"),
                    BlockEntityType.Builder.create(OperatingTableBlockEntity::new, ModBlocks.OPERATING_TABLE)
                            .build(null));

    public static void init() {
        // Static initializer is enough; method exists for explicit ordering in RedCyber.
    }
}
