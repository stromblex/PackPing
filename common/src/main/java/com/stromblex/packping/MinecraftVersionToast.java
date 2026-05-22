package com.stromblex.packping;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;

public class MinecraftVersionToast {

    public static void show(Minecraft client, String line1, String line2) {
        try {
            SystemToast.addOrUpdate(
                    client.getToasts(),
                    SystemToast.SystemToastId.WORLD_BACKUP,
                    Component.literal(line1),
                    Component.literal(line2));
        } catch (Exception e) {
            PackPing.LOGGER.error("Failed to show toast", e);
        }
    }
}
