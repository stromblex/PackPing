package com.stromblex.packping;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UpdateChecker {
    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
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
            URI.create(updateUrl);
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
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(updateUrl))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                parseResponse(response.body());
            } else {
                PackPing.LOGGER.error("Update check failed, HTTP {}", response.statusCode());
                retryIfPossible(updateUrl, attempt);
            }
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
            String currentMcVersion = SharedConstants.getCurrentVersion().getName();

            JsonObject entry = null;
            for (int i = 0; i < entries.size(); i++) {
                JsonObject obj = entries.get(i).getAsJsonObject();
                if (obj.has("minecraft") && obj.get("minecraft").getAsString().equals(currentMcVersion)) {
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
            String downloadUrl = entry.get("download").getAsString();
            String changelog = entry.has("changelog") ? entry.get("changelog").getAsString() : "Update available";

            String currentVersion = PackPingConfig.getLocalVersion();
            PackPing.LOGGER.info("Current: {}, Latest: {}", currentVersion, latestVersion);

            if (!latestVersion.equals(currentVersion)) {
                PackPing.LOGGER.info("Update available: {} -> {}", currentVersion, latestVersion);

                // Toast notification (only if outdated)
                if (!shownToast && PackPingConfig.shouldShowToast() && entry.has("toast")) {
                    JsonObject toast = entry.getAsJsonObject("toast");
                    String title = toast.has("title") ? toast.get("title").getAsString() : "New version available!";
                    String subtitle = toast.has("subtitle") ? toast.get("subtitle").getAsString() : "";
                    Minecraft.getInstance().execute(() ->
                            MinecraftVersionToast.show(Minecraft.getInstance(), title, subtitle));
                    shownToast = true;
                }

                Minecraft.getInstance().execute(() -> showNotification(latestVersion, downloadUrl, changelog));
            } else {
                PackPing.LOGGER.info("Up to date ({})", currentVersion);
            }
        } catch (Exception e) {
            PackPing.LOGGER.error("Error parsing update response", e);
        }
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

        String sep = "\u00a76\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501";
        String local = PackPingConfig.getLocalVersion();

        client.player.sendSystemMessage(Component.literal(""));
        client.player.sendSystemMessage(Component.literal(sep));
        client.player.sendSystemMessage(Component.literal(PackPingConfig.getChatTitle()));
        client.player.sendSystemMessage(Component.literal(
                PackPingConfig.getChatVersionText().replace("%current%", local).replace("%latest%", version)));
        client.player.sendSystemMessage(Component.literal(
                PackPingConfig.getChatChangesText().replace("%changelog%", changelog)));
        client.player.sendSystemMessage(Component.literal(PackPingConfig.getChatDownloadText()));
        client.player.sendSystemMessage(Component.literal(downloadUrl));
        client.player.sendSystemMessage(Component.literal(sep));
        client.player.sendSystemMessage(Component.literal(""));
    }
}
