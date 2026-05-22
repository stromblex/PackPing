package com.stromblex.packping.neoforge;

import com.stromblex.packping.PackPing;
import com.stromblex.packping.Platform;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;

@Mod(PackPing.MOD_ID)
public class PackPingNeoForge {

    public PackPingNeoForge(IEventBus modBus, ModContainer container) {
        Platform.init(FMLPaths.CONFIGDIR.get(), container.getModInfo().getVersion().toString());
        PackPing.LOGGER.info("PackPing initialized! (NeoForge)");

        if (FMLEnvironment.dist.isClient()) {
            PackPingNeoForgeClient.init();
        }
    }
}
