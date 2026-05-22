package com.stromblex.packping.neoforge;

import com.stromblex.packping.PackPingConfig;
import com.stromblex.packping.UpdateChecker;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

public class PackPingNeoForgeClient {
    private static boolean hasCheckedForUpdates = false;
    private static boolean shouldShowChatMessage = false;
    private static int tickCounter = 0;

    public static void init() {
        PackPingConfig.init();
        NeoForge.EVENT_BUS.register(PackPingNeoForgeClient.class);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        tickCounter++;
        if (!hasCheckedForUpdates && tickCounter > 1) {
            hasCheckedForUpdates = true;
            if (PackPingConfig.shouldCheckOnStartup()) {
                UpdateChecker.checkForUpdates();
            }
        }

        if (shouldShowChatMessage) {
            Minecraft client = Minecraft.getInstance();
            if (client.player != null) {
                shouldShowChatMessage = false;
                UpdateChecker.sendPendingChatMessage();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        if (UpdateChecker.hasPendingUpdate()) {
            shouldShowChatMessage = true;
        }
    }
}
