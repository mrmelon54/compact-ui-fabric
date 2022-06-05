package xyz.mrmelon54.CompactUi.mixin.world;

import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import xyz.mrmelon54.CompactUi.duck.SingleplayerScreenDuckProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SelectWorldScreen.class)
public class SelectWorldScreenMixin implements SingleplayerScreenDuckProvider {
    @Shadow
    private WorldListWidget levelList;

    @Override
    public WorldListWidget getWorldListWidget() {
        return levelList;
    }
}
