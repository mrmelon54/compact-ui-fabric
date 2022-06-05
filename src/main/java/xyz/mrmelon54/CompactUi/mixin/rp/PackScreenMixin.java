package xyz.mrmelon54.CompactUi.mixin.rp;

import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.client.gui.screen.pack.PackScreen;
import xyz.mrmelon54.CompactUi.duck.ResourcePackScreenDuckProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PackScreen.class)
public class PackScreenMixin implements ResourcePackScreenDuckProvider {
    @Shadow
    private PackListWidget selectedPackList;

    @Shadow
    private PackListWidget availablePackList;

    @Override
    public PackListWidget getSelectedPackListWidget() {
        return selectedPackList;
    }

    @Override
    public PackListWidget getAvailablePackList() {
        return availablePackList;
    }
}
