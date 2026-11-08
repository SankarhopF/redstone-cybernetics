package com.redcyber.block;

import com.redcyber.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

/** Блок-энтити операционного стола (держит состояние стола). */
public class OperatingTableBlockEntity extends BlockEntity {
    public OperatingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OPERATING_TABLE, pos, state);
    }
}
