package xyz.mrmelon54.CompactUi.mixin.world;

import net.minecraft.client.gui.screen.world.WorldListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldListWidget.class)
public class WorldListWidgetMixin {
    @ModifyVariable(method = "<init>", at = @At("HEAD"), ordinal = 4, argsOnly = true)
    static private int injectedEntryHeight(int entryHeight) {
        return (entryHeight - 4) / 3 + 4;
    }
}
