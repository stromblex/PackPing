package com.stromblex.packping;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PackPingConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = "packping.json";
    private static Path configPath;
    private static JsonObject config;
    private static JsonObject remoteSettings;

    public static void init() {
        configPath = Platform.getConfigDir().resolve(CONFIG_FILE_NAME);
        loadConfig();
    }

    private static void loadConfig() {
        try {
            if (Files.exists(configPath)) {
                String content = Files.readString(configPath);
                config = GSON.fromJson(content, JsonObject.class);
                if (config == null) config = new JsonObject();
            } else {
                config = createDefault();
                saveConfig();
                PackPing.LOGGER.info("Created config at: {}", configPath);
            }
        } catch (Exception e) {
            PackPing.LOGGER.error("Failed to load config", e);
            config = createDefault();
            saveConfig();
        }
    }

    private static JsonObject createDefault() {
        JsonObject obj = new JsonObject();
        obj.addProperty("updateUrl", "");
        obj.addProperty("localVersion", "0.0.1");
        obj.addProperty("delay", 3000);
        return obj;
    }

    private static void saveConfig() {
        try {
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, GSON.toJson(config));
        } catch (IOException e) {
            PackPing.LOGGER.error("Failed to save config", e);
        }
    }

    public static void applyRemoteSettings(JsonObject settings) {
        remoteSettings = flatten(settings);
    }

    private static JsonObject flatten(JsonObject nested) {
        JsonObject flat = new JsonObject();
        for (String key : nested.keySet()) {
            if (nested.get(key).isJsonObject()) {
                JsonObject inner = nested.getAsJsonObject(key);
                for (String innerKey : inner.keySet()) {
                    flat.add(innerKey, inner.get(innerKey));
                }
            } else {
                flat.add(key, nested.get(key));
            }
        }
        return flat;
    }

    private static String getString(String key, String fallback) {
        if (remoteSettings != null && remoteSettings.has(key))
            return remoteSettings.get(key).getAsString();
        if (config != null && config.has(key))
            return config.get(key).getAsString();
        return fallback;
    }

    private static boolean getBool(String key, boolean fallback) {
        if (remoteSettings != null && remoteSettings.has(key))
            return remoteSettings.get(key).getAsBoolean();
        if (config != null && config.has(key))
            return config.get(key).getAsBoolean();
        return fallback;
    }

    private static int getInt(String key, int fallback) {
        if (remoteSettings != null && remoteSettings.has(key))
            return remoteSettings.get(key).getAsInt();
        if (config != null && config.has(key))
            return config.get(key).getAsInt();
        return fallback;
    }

    // --- Connection ---
    public static String getUpdateUrl() { return getString("updateUrl", null); }
    public static String getLocalVersion() { return getString("localVersion", "0.0.1"); }

    // --- Notifications (toggles) ---
    public static boolean shouldCheckOnStartup() { return getBool("checkOnStartup", true); }
    public static int getNotificationDelay() { return getInt("delay", 3000); }
    public static boolean shouldShowFullscreen() { return getBool("showFullscreen", true); }
    public static boolean shouldShowChat() { return getBool("showChat", true); }
    public static boolean shouldShowToast() { return getBool("showToast", true); }

    // --- Fullscreen ---
    public static String getFullscreenTitle() { return getString("title", "Update Available"); }
    public static String getDownloadButtonText() { return getString("downloadButton", "Download"); }
    public static String getSkipButtonText() { return getString("skipButton", "Skip"); }
    public static boolean shouldCloseGameAfterDownload() { return getBool("closeGameAfterDownload", false); }

    // --- Chat ---
    public static String getChatTitle() { return getString("chatTitle", "\u00a7e\u25B6 Modpack update available!"); }
    public static String getChatVersionText() { return getString("chatVersionText", "\u00a77Version: \u00a7f%current% \u00a77\u2192 \u00a7a%latest%"); }
    public static String getChatChangesText() { return getString("chatChangesText", "\u00a77Changes: \u00a7f%changelog%"); }
    public static String getChatDownloadText() { return getString("chatDownloadText", "\u00a77Download:"); }
    public static String getChatLinkText() { return getString("chatLinkText", "\u00a7b[Click here]"); }
}
