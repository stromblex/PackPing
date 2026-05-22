package com.stromblex.packping;

import java.nio.file.Path;

public class Platform {
    private static Path configDir;
    private static String modVersion = "unknown";

    public static void init(Path configDir, String modVersion) {
        Platform.configDir = configDir;
        Platform.modVersion = modVersion;
    }

    public static Path getConfigDir() {
        return configDir;
    }

    public static String getModVersion() {
        return modVersion;
    }
}
