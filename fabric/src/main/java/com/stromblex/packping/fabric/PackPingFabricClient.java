package com.stromblex.packping.fabric;

import com.stromblex.packping.PackPingConfig;
import com.stromblex.packping.UpdateChecker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class PackPingFabricClient implements ClientModInitializer {
    private boolean hasCheckedForUpdates = false;
    private boolean shouldShowChatMessage = false;
    private int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        PackPingConfig.init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickCounter++;
            if (!hasCheckedForUpdates && tickCounter > 1) {
                hasCheckedForUpdates = true;
                if (PackPingConfig.shouldCheckOnStartup()) {
                    UpdateChecker.checkForUpdates();
                }
            }

            if (shouldShowChatMessage && client.player != null) {
                shouldShowChatMessage = false;
                UpdateChecker.sendPendingChatMessage();
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (UpdateChecker.hasPendingUpdate()) {
                shouldShowChatMessage = true;
            }
        });
    }
}
