package com.stromblex.packping;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import com.google.gson.JsonObject;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UpdateChecker {
    private static final Gson GSON = new Gson();
    private static final ScheduledExecutorService SCHEDULER =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "PackPing-Update");
                t.setDaemon(true);
                return t;
            });

    private static final int MAX_RETRIES = 1;
    private static final long RETRY_DELAY_MS = 5000;

    private static String pendingVersion = null;
    private static String pendingDownloadUrl = null;
    private static String pendingChangelog = null;
    private static boolean shownToast = false;
    private static boolean shownChat = false;

    public static void checkForUpdates() {
        String updateUrl = PackPingConfig.getUpdateUrl();
        if (updateUrl == null || updateUrl.isEmpty()) {
            PackPing.LOGGER.warn("Update URL not configured");
            return;
        }

        try {
            URI uri = URI.create(updateUrl);
            if ("http".equalsIgnoreCase(uri.getScheme())) {
                PackPing.LOGGER.warn("Update URL uses HTTP; HTTPS is recommended");
            }
        } catch (IllegalArgumentException e) {
            PackPing.LOGGER.error("Invalid update URL: {}", updateUrl);
            return;
        }

        int delayMs = PackPingConfig.getNotificationDelay();
        SCHEDULER.schedule(() -> performCheck(updateUrl), delayMs, TimeUnit.MILLISECONDS);
    }

    private static void performCheck(String updateUrl) {
        performCheck(updateUrl, 0);
    }

    private static void performCheck(String updateUrl, int attempt) {
        try {
            parseResponse(UpdateJsonFetcher.fetch(updateUrl));
        } catch (UpdateJsonFetcher.UpdateFetchException e) {
            PackPing.LOGGER.error("Update check failed: {}", e.getMessage());
            retryIfPossible(updateUrl, attempt);
        } catch (Exception e) {
            PackPing.LOGGER.error("Error checking for updates", e);
            retryIfPossible(updateUrl, attempt);
        }
    }

    private static void retryIfPossible(String updateUrl, int attempt) {
        if (attempt < MAX_RETRIES) {
            PackPing.LOGGER.info("Retrying update check (attempt {})", attempt + 2);
            SCHEDULER.schedule(() -> performCheck(updateUrl, attempt + 1), RETRY_DELAY_MS, TimeUnit.MILLISECONDS);
        }
    }

    private static void parseResponse(String json) {
        try {
            JsonArray entries = GSON.fromJson(json, JsonArray.class);
            if (entries == null) {
                throw new JsonSyntaxException("root value is null");
            }

            String currentMcVersion = SharedConstants.getCurrentVersion().name();
            String currentLoader = Platform.getLoader();

            JsonObject entry = null;
            for (int i = 0; i < entries.size(); i++) {
                JsonObject obj = entries.get(i).getAsJsonObject();
                if (matchesEntry(obj, currentMcVersion, currentLoader)) {
                    entry = obj;
                    break;
                }
            }

            if (entry == null) {
                PackPing.LOGGER.info("No entry for Minecraft {}", currentMcVersion);
                return;
            }

            // Apply remote settings (flattened from nested categories)
            if (entry.has("settings") && entry.get("settings").isJsonObject()) {
                PackPingConfig.applyRemoteSettings(entry.getAsJsonObject("settings"));
            }

            // Version check
            String latestVersion = entry.get("version").getAsString();
            String currentVersion = PackPingConfig.getLocalVersion();
            PackPing.LOGGER.info("Current: {}, Latest: {}", currentVersion, latestVersion);

            showToastIfConfigured(entry);

            if (!latestVersion.equals(currentVersion)) {
                PackPing.LOGGER.info("Update available: {} -> {}", currentVersion, latestVersion);

                String downloadUrl = entry.get("download").getAsString();
                String changelog = entry.has("changelog") ? entry.get("changelog").getAsString() : "Update available";
                Minecraft.getInstance().execute(() -> showNotification(latestVersion, downloadUrl, changelog));
            } else {
                PackPing.LOGGER.info("Up to date ({})", currentVersion);
            }
        } catch (Exception e) {
            PackPing.LOGGER.error("Invalid update JSON", e);
        }
    }

    private static boolean matchesEntry(JsonObject obj, String currentMcVersion, String currentLoader) {
        if (!obj.has("minecraft") || !obj.get("minecraft").getAsString().equals(currentMcVersion)) {
            return false;
        }
        return !obj.has("loader") || obj.get("loader").getAsString().equalsIgnoreCase(currentLoader);
    }

    private static void showToastIfConfigured(JsonObject entry) {
        if (shownToast || !PackPingConfig.shouldShowToast() || !entry.has("toast")) {
            return;
        }

        if (!entry.get("toast").isJsonObject()) {
            PackPing.LOGGER.warn("Ignoring toast entry because it is not an object");
            return;
        }

        JsonObject toast = entry.getAsJsonObject("toast");
        String title = toast.has("title") ? toast.get("title").getAsString() : "New version available!";
        String subtitle = toast.has("subtitle") ? toast.get("subtitle").getAsString() : "";
        Minecraft.getInstance().execute(() ->
                MinecraftVersionToast.show(Minecraft.getInstance(), title, subtitle));
        shownToast = true;
    }

    private static void showNotification(String version, String downloadUrl, String changelog) {
        Minecraft client = Minecraft.getInstance();
        pendingVersion = version;
        pendingDownloadUrl = downloadUrl;
        pendingChangelog = changelog;

        if (PackPingConfig.shouldShowFullscreen()) {
            Screen currentScreen = client.screen;
            client.setScreen(new UpdateScreen(currentScreen, version, downloadUrl, changelog));
        }

        if (!shownChat && client.player != null && PackPingConfig.shouldShowChat()) {
            sendChatMessage(client, version, downloadUrl, changelog);
            shownChat = true;
        }
    }

    public static boolean hasPendingUpdate() {
        return pendingVersion != null;
    }

    public static void sendPendingChatMessage() {
        if (!shownChat && hasPendingUpdate() && PackPingConfig.shouldShowChat()) {
            sendChatMessage(Minecraft.getInstance(), pendingVersion, pendingDownloadUrl, pendingChangelog);
            shownChat = true;
        }
    }

    private static void sendChatMessage(Minecraft client, String version, String downloadUrl, String changelog) {
        if (client.player == null) return;

        String local = PackPingConfig.getLocalVersion();
        String linkText = PackPingConfig.getChatLinkText();

        MutableComponent downloadComponent = Component.literal(PackPingConfig.getChatDownloadText() + " ")
                .append(Component.literal(linkText)
                        .withStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent.OpenUrl(URI.create(downloadUrl)))
                                .withUnderlined(true)));

        client.player.sendSystemMessage(Component.literal(PackPingConfig.getChatTitle()));
        client.player.sendSystemMessage(Component.literal(
                PackPingConfig.getChatVersionText().replace("%current%", local).replace("%latest%", version)));
        client.player.sendSystemMessage(Component.literal(
                PackPingConfig.getChatChangesText().replace("%changelog%", changelog)));
        client.player.sendSystemMessage(downloadComponent);
    }
}
