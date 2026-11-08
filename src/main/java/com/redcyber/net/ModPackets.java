package com.redcyber.net;

import com.redcyber.RedCyber;
import com.redcyber.core.CyberData;
import com.redcyber.core.SurgeryManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

/** Все сетевые пакеты мода. */
public final class ModPackets {
    private ModPackets() {
    }

    // ------------------------------------------------------------------
    //  S2C
    // ------------------------------------------------------------------

    /** Сервер -> клиент: игрок лёг на стол, открываем меню имплантации. */
    public record OpenTableS2C(BlockPos pos) implements CustomPayload {
        public static final Id<OpenTableS2C> ID = new Id<>(RedCyber.id("open_table"));
        public static final PacketCodec<RegistryByteBuf, OpenTableS2C> CODEC =
                PacketCodec.tuple(BlockPos.PACKET_CODEC, OpenTableS2C::pos, OpenTableS2C::new);

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    /** Сервер -> клиент: синхронизация установленных имплантов ("EYE=cyber_eye,..."). */
    public record SyncCyberS2C(String data) implements CustomPayload {
        public static final Id<SyncCyberS2C> ID = new Id<>(RedCyber.id("sync_cyber"));
        public static final PacketCodec<RegistryByteBuf, SyncCyberS2C> CODEC =
                PacketCodec.tuple(PacketCodecs.STRING, SyncCyberS2C::data, SyncCyberS2C::new);

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

        ServerPlayNetworking.registerGlobalReceiver(InstallPartC2S.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> SurgeryManager.requestInstall(player, payload.pos(), payload.part()));
        });

        ServerPlayNetworking.registerGlobalReceiver(CloseTableC2S.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> SurgeryManager.standUp(player));
        });
    }

    public static void sendOpenTable(ServerPlayerEntity player, BlockPos pos) {
        ServerPlayNetworking.send(player, new OpenTableS2C(pos));
    }

    public static void sendSync(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new SyncCyberS2C(CyberData.encode(CyberData.parts(player))));
    }

    public static void sendSurgeryStart(ServerPlayerEntity player, String part, int duration) {
        ServerPlayNetworking.send(player, new SurgeryStartS2C(part, duration));
    }

    public static void sendSurgeryEnd(ServerPlayerEntity player, String part, boolean success) {
        ServerPlayNetworking.send(player, new SurgeryEndS2C(part, success));
    }
}
