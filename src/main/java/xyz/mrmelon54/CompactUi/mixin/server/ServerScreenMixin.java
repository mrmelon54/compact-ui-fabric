package xyz.mrmelon54.CompactUi.mixin.server;

import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import xyz.mrmelon54.CompactUi.duck.MultiplayerScreenDuckProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MultiplayerScreen.class)
public class ServerScreenMixin implements MultiplayerScreenDuckProvider {
    @Shadow
    protected MultiplayerServerListWidget serverListWidget;

    @Override
    public MultiplayerServerListWidget getServerListWidget() {
        return serverListWidget;
    }
}
