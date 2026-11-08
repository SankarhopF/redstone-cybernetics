package com.redcyber.net;

import com.redcyber.RedCyber;
import com.redcyber.core.CyberData;
import com.redcyber.core.CyberEffects;
import com.redcyber.core.CyberSlot;
import com.redcyber.core.SurgeryManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Locale;

/** Все сетевые пакеты мода. */
public final class ModPackets {
    private ModPackets() {
    }

    // ------------------------------------------------------------------
    //  S2C
    // ------------------------------------------------------------------

    /**
     * Сервер -> клиент: игрок лёг на стол. Клиент открывает меню, включает третье
     * лицо и фиксирует камеру операции по yaw сессии (камера не зависит от взгляда).
     */
    public record OpenTableS2C(BlockPos pos, float yaw) implements CustomPayload {
        public static final Id<OpenTableS2C> ID = new Id<>(RedCyber.id("open_table"));
        public static final PacketCodec<RegistryByteBuf, OpenTableS2C> CODEC = PacketCodec.tuple(
                BlockPos.PACKET_CODEC, OpenTableS2C::pos,
                PacketCodecs.FLOAT, OpenTableS2C::yaw,
                OpenTableS2C::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    /**
     * Сервер -> клиент: синхронизация имплантов ("EYE=cyber_eye,...") и слотов,
     * у которых игрок выключил дополнительные возможности ("EYE,ARM").
     */
    public record SyncCyberS2C(String data, String disabled) implements CustomPayload {
        public static final Id<SyncCyberS2C> ID = new Id<>(RedCyber.id("sync_cyber"));
        public static final PacketCodec<RegistryByteBuf, SyncCyberS2C> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, SyncCyberS2C::data,
                PacketCodecs.STRING, SyncCyberS2C::disabled,
                SyncCyberS2C::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    /** Сервер -> клиент: операция началась, играем анимацию. */
    public record SurgeryStartS2C(String part, int duration) implements CustomPayload {
        public static final Id<SurgeryStartS2C> ID = new Id<>(RedCyber.id("surgery_start"));
        public static final PacketCodec<RegistryByteBuf, SurgeryStartS2C> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, SurgeryStartS2C::part,
                PacketCodecs.VAR_INT, SurgeryStartS2C::duration,
                SurgeryStartS2C::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    /** Сервер -> клиент: операция завершена (или отменена, если part пустой). */
    public record SurgeryEndS2C(String part, boolean success) implements CustomPayload {
        public static final Id<SurgeryEndS2C> ID = new Id<>(RedCyber.id("surgery_end"));
        public static final PacketCodec<RegistryByteBuf, SurgeryEndS2C> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, SurgeryEndS2C::part,
                PacketCodecs.BOOL, SurgeryEndS2C::success,
                SurgeryEndS2C::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    // ------------------------------------------------------------------
    //  C2S
    // ------------------------------------------------------------------

    /** Клиент -> сервер: "я хочу поставить этот имплант". */
    public record InstallPartC2S(BlockPos pos, String part) implements CustomPayload {
        public static final Id<InstallPartC2S> ID = new Id<>(RedCyber.id("install_part"));
        public static final PacketCodec<RegistryByteBuf, InstallPartC2S> CODEC = PacketCodec.tuple(
                BlockPos.PACKET_CODEC, InstallPartC2S::pos,
                PacketCodecs.STRING, InstallPartC2S::part,
                InstallPartC2S::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    /** Клиент -> сервер: игрок встаёт со стола. */
    public record CloseTableC2S() implements CustomPayload {
        public static final Id<CloseTableC2S> ID = new Id<>(RedCyber.id("close_table"));
        public static final PacketCodec<RegistryByteBuf, CloseTableC2S> CODEC =
                PacketCodec.unit(new CloseTableC2S());

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    /** Клиент -> сервер: включить/выключить возможности импланта в слоте. */
    public record ToggleAbilityC2S(String slot, boolean enabled) implements CustomPayload {
        public static final Id<ToggleAbilityC2S> ID = new Id<>(RedCyber.id("toggle_ability"));
        public static final PacketCodec<RegistryByteBuf, ToggleAbilityC2S> CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, ToggleAbilityC2S::slot,
                PacketCodecs.BOOL, ToggleAbilityC2S::enabled,
                ToggleAbilityC2S::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    // ------------------------------------------------------------------
    //  Регистрация и отправка
    // ------------------------------------------------------------------

    public static void registerServer() {
        PayloadTypeRegistry.playS2C().register(OpenTableS2C.ID, OpenTableS2C.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncCyberS2C.ID, SyncCyberS2C.CODEC);
        PayloadTypeRegistry.playS2C().register(SurgeryStartS2C.ID, SurgeryStartS2C.CODEC);
        PayloadTypeRegistry.playS2C().register(SurgeryEndS2C.ID, SurgeryEndS2C.CODEC);

        PayloadTypeRegistry.playC2S().register(InstallPartC2S.ID, InstallPartC2S.CODEC);
        PayloadTypeRegistry.playC2S().register(CloseTableC2S.ID, CloseTableC2S.CODEC);
        PayloadTypeRegistry.playC2S().register(ToggleAbilityC2S.ID, ToggleAbilityC2S.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(InstallPartC2S.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> SurgeryManager.requestInstall(player, payload.pos(), payload.part()));
        });

        ServerPlayNetworking.registerGlobalReceiver(CloseTableC2S.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> SurgeryManager.standUp(player));
        });

        ServerPlayNetworking.registerGlobalReceiver(ToggleAbilityC2S.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> toggleAbility(player, payload.slot(), payload.enabled()));
        });
    }

    /** Переключает возможности слота и сразу пересчитывает эффекты. */
    private static void toggleAbility(ServerPlayerEntity player, String slotName, boolean enabled) {
        CyberSlot slot;
        try {
            slot = CyberSlot.valueOf(slotName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            sendSync(player);
            return;
        }
        if (!CyberData.has(player, slot)) {
            // импланта нет — просто присылаем актуальное состояние
            sendSync(player);
            return;
        }
        CyberData.setEnabled(player, slot, enabled);
        CyberEffects.refresh(player);
        sendSync(player);
    }

    public static void sendOpenTable(ServerPlayerEntity player, BlockPos pos, float yaw) {
        ServerPlayNetworking.send(player, new OpenTableS2C(pos, yaw));
    }

    public static void sendSync(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new SyncCyberS2C(
                CyberData.encode(CyberData.parts(player)),
                CyberData.encodeDisabled(CyberData.disabled(player))));
    }

    public static void sendSurgeryStart(ServerPlayerEntity player, String part, int duration) {
        ServerPlayNetworking.send(player, new SurgeryStartS2C(part, duration));
    }

    public static void sendSurgeryEnd(ServerPlayerEntity player, String part, boolean success) {
        ServerPlayNetworking.send(player, new SurgeryEndS2C(part, success));
    }
}
