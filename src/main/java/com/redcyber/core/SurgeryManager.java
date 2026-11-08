package com.redcyber.core;

import com.redcyber.block.OperatingTableBlock;
import com.redcyber.net.ModPackets;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Серверная логика операционного стола: игрок лежит, меню открыто, затем идёт
 * "операция" (анимацию проигрывает клиент), по завершении имплант устанавливается.
 */
public final class SurgeryManager {
    /** Длительность операции в тиках (7 секунд). */
    public static final int SURGERY_DURATION = 140;

    /** Активная сессия у стола. */
    public static final class Session {
        public final UUID playerId;
        public final BlockPos tablePos;
        public final double x;
        public final double y;
        public final double z;
        public final float yaw;

        public String partName;
        public int duration;
        public int elapsed;
        public boolean operating;

        Session(UUID playerId, BlockPos tablePos, double x, double y, double z, float yaw) {
            this.playerId = playerId;
            this.tablePos = tablePos;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
        }
    }

    private static final Map<UUID, Session> SESSIONS = new HashMap<>();

    private SurgeryManager() {
    }

    // ------------------------------------------------------------------
    //  Лечь на стол / встать
    // ------------------------------------------------------------------

    public static boolean startSession(ServerPlayerEntity player, BlockPos pos) {
        if (SESSIONS.containsKey(player.getUuid())) {
            return false;
        }
        // Стол занимает только один пациент за раз.
        for (Session other : SESSIONS.values()) {
            if (other.tablePos.equals(pos)) {
                player.sendMessage(Text.translatable("redcyber.msg.table_busy"), true);
                return false;
            }
        }
        ServerWorld world = player.getServerWorld();
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof OperatingTableBlock)) {
            return false;
        }

        double x = pos.getX() + 0.5;
        double y = pos.getY() + OperatingTableBlock.SURFACE_HEIGHT;
        double z = pos.getZ() + 0.5;
        // Ось тела = направление взгляда в момент использования стола.
        // Клиент ставит по ней фиксированную камеру операции (со стороны ног).
        float yaw = player.getYaw();
        float cameraPitch = 0.0f;

        Session session = new Session(player.getUuid(), pos.toImmutable(), x, y, z, yaw);
        SESSIONS.put(player.getUuid(), session);

        player.stopRiding();
        player.setNoGravity(true);
        player.setVelocity(Vec3d.ZERO);
        player.teleport(world, x, y, z, java.util.Set.of(), yaw, cameraPitch);
        player.setPose(EntityPose.SWIMMING);
        player.fallDistance = 0.0f;


        ModPackets.sendOpenTable(player, session.tablePos, session.yaw);
        ModPackets.sendSync(player);
        return true;
    }

    /** Игрок встаёт со стола (закрыл меню / отмена). */
    public static void standUp(ServerPlayerEntity player) {
        Session session = SESSIONS.remove(player.getUuid());
        if (session == null) {
            return;
        }
        release(player, session);
        OperatingTableBlock.setLit(player.getServerWorld(), session.tablePos, false);
    }

    private static void release(ServerPlayerEntity player, Session session) {
        player.setNoGravity(false);
        player.setPose(EntityPose.STANDING);
        if (!player.isRemoved() && !player.isSpectator()) {
            player.teleport(player.getServerWorld(), session.x, session.y, session.z,
                    java.util.Set.of(), session.yaw, 0.0f);
        }
    }

    // ------------------------------------------------------------------
    //  Операция
    // ------------------------------------------------------------------

    public static void requestInstall(ServerPlayerEntity player, BlockPos pos, String partName) {
        Session session = SESSIONS.get(player.getUuid());
        if (session == null || !session.tablePos.equals(pos) || session.operating) {
            ModPackets.sendSurgeryEnd(player, "", false);
            return;
        }
        CyberPart part = CyberParts.byName(partName);
        if (part == null) {
            ModPackets.sendSurgeryEnd(player, "", false);
            return;
        }
        if (CyberData.has(player, part.slot())) {
            player.sendMessage(Text.translatable("redcyber.msg.already_installed"), true);
            ModPackets.sendSurgeryEnd(player, "", false);
            return;
        }
        if (!hasComponents(player, part)) {
            player.sendMessage(Text.translatable("redcyber.msg.missing_components"), true);
            ModPackets.sendSurgeryEnd(player, "", false);
            return;
        }

        consumeComponents(player, part);

        session.partName = part.name();
        session.operating = true;
        session.duration = SURGERY_DURATION;
        session.elapsed = 0;

        ServerWorld world = player.getServerWorld();
        OperatingTableBlock.setLit(world, pos, true);
        world.playSound(null, pos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 0.9f, 0.6f);
        world.playSound(null, pos, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 0.7f, 0.8f);

        ModPackets.sendSurgeryStart(player, part.name(), session.duration);
    }

    /** Тик сервера: отсчёт операции и проверка валидности сессий. */
    public static void tick(MinecraftServer server) {
        if (SESSIONS.isEmpty()) {
            return;
        }
        List<UUID> toFinish = new ArrayList<>();
        Iterator<Map.Entry<UUID, Session>> it = SESSIONS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Session> entry = it.next();
            Session session = entry.getValue();
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(session.playerId);

            if (player == null || player.isRemoved() || player.isDead()) {
                it.remove();
                continue;
            }
            ServerWorld world = player.getServerWorld();
            BlockState tableState = world.getBlockState(session.tablePos);
            if (!(tableState.getBlock() instanceof OperatingTableBlock)) {
                // Стол сломали — прекращаем операцию.
                it.remove();
                if (session.operating) {
                    ModPackets.sendSurgeryEnd(player, "", false);
                }
                player.setNoGravity(false);
                player.setPose(EntityPose.STANDING);
                continue;
            }

            if (!session.operating) {
                continue;
            }

            session.elapsed++;
            tickSurgeryEffects(world, session, player);

            if (session.elapsed >= session.duration) {
                toFinish.add(session.playerId);
            }
        }

        for (UUID id : toFinish) {
            finishSurgery(server, id);
        }
    }

    private static void tickSurgeryEffects(ServerWorld world, Session session, ServerPlayerEntity player) {
        int e = session.elapsed;
        double x = session.x;
        double y = session.y + 0.35;
        double z = session.z;

        if (e % 12 == 0) {
            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 6, 0.35, 0.12, 0.35, 0.02);
        }
        if (e % 20 == 10) {
            world.playSound(null, session.tablePos, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT,
                    SoundCategory.BLOCKS, 0.45f, 1.6f);
        }
        if (e == 20) {
            world.playSound(null, session.tablePos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 0.7f, 0.7f);
        }
        if (e == 60) {
            world.playSound(null, session.tablePos, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE,
                    SoundCategory.BLOCKS, 0.8f, 1.3f);
        }
        if (e == session.duration - 25) {
            world.playSound(null, session.tablePos, SoundEvents.BLOCK_BEACON_POWER_SELECT,
                    SoundCategory.BLOCKS, 0.9f, 1.1f);
        }
        if (e % 4 == 0) {
            player.setVelocity(Vec3d.ZERO);
        }
    }

    private static void finishSurgery(MinecraftServer server, UUID playerId) {
        Session session = SESSIONS.remove(playerId);
        if (session == null) {
            return;
        }
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
        if (player == null) {
            return;
        }
        CyberPart part = CyberParts.byName(session.partName);
        if (part == null) {
            release(player, session);
            return;
        }

        CyberData.install(player, part);
        CyberEffects.refresh(player);

        ServerWorld world = player.getServerWorld();
        OperatingTableBlock.setLit(world, session.tablePos, false);
        world.playSound(null, session.tablePos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1.0f, 1.4f);
        world.playSound(null, session.tablePos, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.6f, 1.6f);
        world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, session.x, session.y + 0.4, session.z, 40, 0.5, 0.4, 0.5, 0.08);

        release(player, session);
        ModPackets.sendSurgeryEnd(player, part.name(), true);
        ModPackets.sendSync(player);
        player.sendMessage(Text.translatable("redcyber.msg.installed", part.item().getName()), false);
    }

    // ------------------------------------------------------------------
    //  Тик игрока (вызывается из миксина PlayerEntity)
    // ------------------------------------------------------------------

    public static void onPlayerTick(PlayerEntity player) {
        Session session = SESSIONS.get(player.getUuid());
        if (session == null) {
            return;
        }
        player.setPose(EntityPose.SWIMMING);
        player.setNoGravity(true);
        player.setVelocity(Vec3d.ZERO);
        if (player.squaredDistanceTo(session.x, session.y, session.z) > 0.0025) {
            player.requestTeleport(session.x, session.y, session.z);
        }
    }

    /** Игрок вышел — сессию закрываем без телепорта. */
    public static void onDisconnect(PlayerEntity player) {
        Session session = SESSIONS.remove(player.getUuid());
        if (session != null) {
            player.setNoGravity(false);
            player.setPose(EntityPose.STANDING);
        }
    }

    public static boolean isBusy(PlayerEntity player) {
        return SESSIONS.containsKey(player.getUuid());
    }

    // ------------------------------------------------------------------
    //  Компоненты
    // ------------------------------------------------------------------

    public static int count(PlayerEntity player, Item item) {
        int total = 0;
        for (ItemStack stack : player.getInventory().main) {
            if (stack.isOf(item)) {
                total += stack.getCount();
            }
        }
        for (ItemStack stack : player.getInventory().offHand) {
            if (stack.isOf(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    public static boolean hasComponents(PlayerEntity player, CyberPart part) {
        for (Map.Entry<Item, Integer> entry : part.components().entrySet()) {
            if (count(player, entry.getKey()) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    private static void consumeComponents(PlayerEntity player, CyberPart part) {
        for (Map.Entry<Item, Integer> entry : part.components().entrySet()) {
            removeItems(player, entry.getKey(), entry.getValue());
        }
        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.currentScreenHandler.syncState();
            serverPlayer.playerScreenHandler.syncState();
        }
    }

    private static void removeItems(PlayerEntity player, Item item, int amount) {
        int left = amount;
        for (ItemStack stack : player.getInventory().main) {
            if (left <= 0) {
                break;
            }
            if (stack.isOf(item)) {
                int take = Math.min(left, stack.getCount());
                stack.decrement(take);
                left -= take;
            }
        }
        for (ItemStack stack : player.getInventory().offHand) {
            if (left <= 0) {
                break;
            }
            if (stack.isOf(item)) {
                int take = Math.min(left, stack.getCount());
                stack.decrement(take);
                left -= take;
            }
        }
    }
}
