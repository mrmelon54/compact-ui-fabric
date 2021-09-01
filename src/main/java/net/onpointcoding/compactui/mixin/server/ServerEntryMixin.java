package net.onpointcoding.compactui.mixin.server;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.util.Util;
import net.onpointcoding.compactui.duck.MultiplayerScreenDuckProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(MultiplayerServerListWidget.ServerEntry.class)
public abstract class ServerEntryMixin {
    private int lastRowLeft = 0;
    private int lastRowTop = 0;
    private float oneThird32 = 32 / 3f;
    private float twoThird32 = 32 * 2 / 3f;

    @Shadow
    protected abstract boolean canConnect();

    @Shadow
    @Final
    private MultiplayerScreen screen;

    @Shadow
    private long time;

    @Shadow
    protected abstract void swapEntries(int i, int j);

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;wrapLines(Lnet/minecraft/text/StringVisitable;I)Ljava/util/List;"))
    private List<OrderedText> injectWrapLines(TextRenderer textRenderer, StringVisitable text, int width) {
        return Collections.emptyList();
    }

    @Redirect(method = "draw", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawableHelper;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIFFIIII)V"))
    private void injectedEditServerIcon(MatrixStack matrices, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        DrawableHelper.drawTexture(matrices, x + (int) twoThird32, y, width / 3, height / 3, u, v, width, height, textureWidth, textureHeight);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawableHelper;fill(Lnet/minecraft/client/util/math/MatrixStack;IIIII)V"))
    private void injectedEditHoverOverlaySize(MatrixStack matrices, int x1, int y1, int x2, int y2, int color) {
        DrawableHelper.fill(matrices, x1 + (int) twoThird32, y1, x2, y2 - (int) twoThird32, color);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawableHelper;fill(Lnet/minecraft/client/util/math/MatrixStack;IIIII)V"), cancellable = true)
    private void injectedEditHoverArrows(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        DrawableHelper.fill(matrices, x + (int) twoThird32, y, x + 32, y + (int) oneThird32, -1601138544);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int v = mouseX - x;

        if (canConnect())
            DrawableHelper.drawTexture(matrices, x + (int) twoThird32 - 2, y, (int) oneThird32, (int) oneThird32, 0, (v < 32 && v > twoThird32) ? 32f : 0, 32, 32, 256, 256);
        if (index > 0)
            DrawableHelper.drawTexture(matrices, x, y + 2, 16, 16, 96f, (v < oneThird32) ? 32f : 0, 32, 32, 256, 256);
        if (index < screen.getServerList().size() - 1)
            DrawableHelper.drawTexture(matrices, x + (int) oneThird32, y - 6, 16, 16, 64f, (v < twoThird32 && v > oneThird32) ? 32f : 0, 32, 32, 256, 256);

        ci.cancel();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void injectedMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (screen instanceof MultiplayerScreenDuckProvider screenDuckProvider) {
            double d = mouseX - (double) screenDuckProvider.getServerListWidget().getRowLeft();
            if (d <= 32.0D) {
                if (d < 32.0D && d > 32 * 2 / 3f && this.canConnect()) {
                    this.screen.select((MultiplayerServerListWidget.ServerEntry) (Object) this);
                    this.screen.connect();
                    cir.setReturnValue(true);
                    cir.cancel();
                    return;
                }

                if (this.screen instanceof MultiplayerScreenDuckProvider duckProvider) {
                    int i = duckProvider.getServerListWidget().children().indexOf((MultiplayerServerListWidget.ServerEntry) (Object) this);
                    if (d < 32 / 3f && i > 0) {
                        swapEntries(i, i - 1);
                        cir.setReturnValue(true);
                        cir.cancel();
                        return;
                    }
                    if (d < 32 * 2 / 3f && d > 32 / 3f && i < this.screen.getServerList().size() - 1) {
                        this.swapEntries(i, i + 1);
                        cir.setReturnValue(true);
                        cir.cancel();
                        return;
                    }
                }

            }
        }

        this.screen.select((MultiplayerServerListWidget.ServerEntry) (Object) this);
        if (Util.getMeasuringTimeMs() - this.time < 250L) {
            this.screen.connect();
        }

        time = Util.getMeasuringTimeMs();
        cir.setReturnValue(false);
        cir.cancel();
    }
}
