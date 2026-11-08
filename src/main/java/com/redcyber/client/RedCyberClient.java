package com.redcyber.client;

import com.redcyber.client.screen.OperatingTableScreen;
import com.redcyber.client.screen.SurgeryScreen;
import com.redcyber.net.ModPackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class RedCyberClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Игрок лёг на стол: третье лицо + меню с блюром.
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.OpenTableS2C.ID, (payload, context) ->
                context.client().execute(() -> {
                    CameraRig.enterThirdPerson();
                    context.client().setScreen(new OperatingTableScreen(payload.pos()));
                }));

        // Синхронизация установленных имплантов.
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.SyncCyberS2C.ID, (payload, context) ->
                context.client().execute(() -> ClientCyberData.update(payload.data())));

        // Началась операция: закрываем меню и запускаем анимацию.
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.SurgeryStartS2C.ID, (payload, context) ->
                context.client().execute(() -> {
                    ClientSurgeryState.start(payload.part(), payload.duration());
                    if (context.client().currentScreen instanceof OperatingTableScreen table) {
                        table.onSurgeryStarted(payload.part());
                    }
                    context.client().setScreen(new SurgeryScreen(payload.part(), payload.duration()));
                }));

        // Операция закончилась (успешно или нет).
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.SurgeryEndS2C.ID, (payload, context) ->
                context.client().execute(() -> {
                    ClientSurgeryState.clear();
                    if (context.client().currentScreen instanceof SurgeryScreen) {
                        context.client().setScreen(null);
                        CameraRig.restore();
                    } else if (!payload.success()
                            && context.client().currentScreen instanceof OperatingTableScreen table) {
                        table.onInstallRejected();
                    }
                }));

        ClientTickEvents.END_CLIENT_TICK.register(client -> ClientSurgeryState.clientTick());
    }
}
