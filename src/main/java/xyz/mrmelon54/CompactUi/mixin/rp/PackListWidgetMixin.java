package xyz.mrmelon54.CompactUi.mixin.rp;

import net.minecraft.client.gui.screen.pack.PackListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PackListWidget.class)
public class PackListWidgetMixin {
    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 36))
    private static int injectedItemHeight(int itemHeight) {
        return (itemHeight - 4) / 3 + 4;
    }
}
