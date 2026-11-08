package com.redcyber.client;

import com.redcyber.client.anim.AnimationBridge;
import com.redcyber.client.screen.AbilityScreen;
import com.redcyber.client.screen.OperatingTableScreen;
import com.redcyber.client.screen.SurgeryScreen;
import com.redcyber.net.ModPackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public class RedCyberClient implements ClientModInitializer {
    /** Сколько тиков нужно удержать клавишу, чтобы открылось меню тумблеров. */
    private static final int HOLD_TICKS = 8;

    private static int holdTicks = 0;

    @Override
    public void onInitializeClient() {
        ModKeybinds.init();
        AnimationBridge.install();
        CyberEyeOverlay.register();

        // Игрок лёг на стол: третье лицо, фиксированная камера и меню с блюром.
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.OpenTableS2C.ID, (payload, context) ->
                context.client().execute(() -> {
                    ClientTableState.begin(payload.pos(), payload.yaw());
                    CameraRig.enterThirdPerson();
                    context.client().setScreen(new OperatingTableScreen(payload.pos()));
                }));

        // Синхронизация установленных имплантов и выключенных возможностей.
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.SyncCyberS2C.ID, (payload, context) ->
                context.client().execute(() -> ClientCyberData.update(payload.data(), payload.disabled())));

        // Началась операция: закрываем меню и запускаем анимацию.
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.SurgeryStartS2C.ID, (payload, context) ->
                context.client().execute(() -> {
                    ClientSurgeryState.start(payload.part(), payload.duration());
                    if (context.client().currentScreen instanceof OperatingTableScreen table) {
                        table.onSurgeryStarted(payload.part());
                    }
                    context.client().setScreen(new SurgeryScreen(payload.part(), payload.duration()));
                }));

        // Операция закончилась (успешно, отменена или запрос отклонён).
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.SurgeryEndS2C.ID, (payload, context) ->
                context.client().execute(() -> {
                    ClientSurgeryState.clear();
                    boolean wasSurgery = context.client().currentScreen instanceof SurgeryScreen;
                    if (wasSurgery) {
                        context.client().setScreen(null);
                    }
                    if (payload.success() || wasSurgery) {
                        // пациент встаёт со стола — возвращаем обычную камеру
                        ClientTableState.end();
                        CameraRig.restore();
                    } else if (context.client().currentScreen instanceof OperatingTableScreen table) {
                        table.onInstallRejected();
                    }
                }));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientSurgeryState.clientTick();
            AnimationBridge.tick();
            tickAbilityKey(client);
        });
    }

    /** Удержание клавиши открывает меню отключения возможностей кибернетики. */
    private static void tickAbilityKey(MinecraftClient client) {
        if (client.player == null || !ModKeybinds.ABILITIES.isPressed()) {
            holdTicks = 0;
            return;
        }
        holdTicks++;
        if (holdTicks != HOLD_TICKS) {
            return;
        }
        if (client.currentScreen != null || ClientTableState.active() || ClientSurgeryState.active()) {
            return;
        }
        client.setScreen(new AbilityScreen());
    }
}
