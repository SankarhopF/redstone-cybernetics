package com.redcyber.block;

import com.mojang.serialization.MapCodec;
import com.redcyber.core.SurgeryManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * Операционный стол. По нажатию игрок ложится на него, камера уходит в третье лицо,
 * а клиенту отправляется пакет на открытие меню имплантации.
 */
public class OperatingTableBlock extends BlockWithEntity {
    public static final MapCodec<OperatingTableBlock> CODEC = createCodec(OperatingTableBlock::new);
    public static final BooleanProperty LIT = BooleanProperty.of("lit");

    /** Высота рабочей поверхности стола (14/16 блока) — на ней лежит игрок. */
    public static final double SURFACE_HEIGHT = 14.0 / 16.0;

    private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 14.0, 16.0);

    public OperatingTableBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(LIT, Boolean.FALSE));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new OperatingTableBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return ActionResult.PASS;
        }
        boolean started = SurgeryManager.startSession(serverPlayer, pos);
        return started ? ActionResult.SUCCESS : ActionResult.CONSUME;
    }

    /** Включает/выключает подсветку стола (во время операции). */
    public static void setLit(World world, BlockPos pos, boolean lit) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof OperatingTableBlock && state.get(LIT) != lit) {
            world.setBlockState(pos, state.with(LIT, lit), Block.NOTIFY_ALL);
        }
    }
}
