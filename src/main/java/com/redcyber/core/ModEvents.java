package com.redcyber.core;

import com.redcyber.net.ModPackets;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ModEvents {
    private ModEvents() {
    }

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            SurgeryManager.tick(server);

            // Раз в секунду обновляем эффекты (статус-эффекты ночного зрения и т.п.)
            if (server.getTicks() % 20 == 0) {
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    CyberEffects.refresh(player);
                }
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            ModPackets.sendSync(player);
            CyberEffects.refresh(player);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            SurgeryManager.onDisconnect(handler.getPlayer());
        });
    }
}
