package com.stromblex.packping;

import java.nio.file.Path;

public class Platform {
    private static Path configDir;
    private static String modVersion = "unknown";
    private static String loader = "unknown";

    public static void init(Path configDir, String modVersion, String loader) {
        Platform.configDir = configDir;
        Platform.modVersion = modVersion;
        Platform.loader = loader;
    }

    public static Path getConfigDir() {
        return configDir;
    }

    public static String getModVersion() {
        return modVersion;
    }

    public static String getLoader() {
        return loader;
    }
}
