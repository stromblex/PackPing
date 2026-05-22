package com.stromblex.packping.fabric;

import com.stromblex.packping.PackPing;
import com.stromblex.packping.Platform;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class PackPingFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        String version = FabricLoader.getInstance()
                .getModContainer(PackPing.MOD_ID)
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");

        Platform.init(FabricLoader.getInstance().getConfigDir(), version);
        PackPing.LOGGER.info("PackPing initialized! (Fabric)");
    }
}
