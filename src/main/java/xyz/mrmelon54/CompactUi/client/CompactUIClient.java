package xyz.mrmelon54.CompactUi.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public class CompactUIClient implements ClientModInitializer {
    private static CompactUIClient instance;

    @Override
    public void onInitializeClient() {
        instance = this;
    }

    public static CompactUIClient getInstance() {
        return instance;
    }

    public boolean hasNRPW() {
        return FabricLoader.getInstance().isModLoaded("no-resource-pack-warnings");
    }
}
