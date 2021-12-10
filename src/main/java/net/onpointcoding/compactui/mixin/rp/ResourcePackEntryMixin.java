package net.onpointcoding.compactui.mixin.rp;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackListWidget;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourcePackCompatibility;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.onpointcoding.compactui.duck.ResourcePackScreenDuckProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PackListWidget.ResourcePackEntry.class)
public abstract class ResourcePackEntryMixin {
    private static final Identifier RESOURCE_PACKS_TEXTURE = new Identifier("textures/gui/resource_packs.png");
    private static final Text INCOMPATIBLE = new TranslatableText("pack.incompatible");
    private static final Text INCOMPATIBLE_CONFIRM = new TranslatableText("pack.incompatible.confirm.title");
    private static final float oneThird32 = 32 / 3f;
    private static final float twoThird32 = 32 * 2 / 3f;

    @Shadow
    @Final
    protected MinecraftClient client;
    @Shadow
    @Final
    private ResourcePackOrganizer.Pack pack;

    @Shadow
    protected abstract boolean isSelectable();

    @Shadow
    @Final
    private OrderedText incompatibleText;
    @Shadow
    @Final
    private OrderedText displayName;

    @Shadow
    @Final
    private PackListWidget widget;

    @Shadow
    @Final
    protected Screen screen;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void injectedRender(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        ResourcePackCompatibility resourcePackCompatibility = this.pack.getCompatibility();
        if (!resourcePackCompatibility.isCompatible()) {
            RenderSystem.setShaderColor(1, 1, 1, 1);
            DrawableHelper.fill(matrices, x - 1, y - 1, x + entryWidth - 9, y + entryHeight + 1, -8978432);
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.pack.getIconId());
        RenderSystem.setShaderColor(1, 1, 1, 1);
        DrawableHelper.drawTexture(matrices, x + (int) twoThird32, y, (int) oneThird32, (int) oneThird32, 0.0F, 0.0F, 32, 32, 32, 32);
        OrderedText orderedText = this.displayName;

        if (this.isSelectable() && (this.client.options.touchscreen || hovered)) {
            RenderSystem.setShaderTexture(0, RESOURCE_PACKS_TEXTURE);
            DrawableHelper.fill(matrices, x + (int) twoThird32, y, x + 32 - 1, y + (int) oneThird32, -1601138544);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            int i = mouseX - x;
            int j = mouseY - y;
            if (!this.pack.getCompatibility().isCompatible()) {
                orderedText = this.incompatibleText;
            }

            if (this.pack.canBeEnabled())
                DrawableHelper.drawTexture(matrices, x + (int) twoThird32, y, (int) oneThird32, (int) oneThird32, 0, (i < 32 && i > twoThird32) ? 32 : 0, 32, 32, 256, 256);
            else {
                if (this.pack.canBeDisabled())
                    DrawableHelper.drawTexture(matrices, x + (int) twoThird32 + 2, y, (int) oneThird32, (int) oneThird32, 32, (i < 32 && i > twoThird32) ? 32 : 0, 32, 32, 256, 256);
                if (this.pack.canMoveTowardStart())
                    DrawableHelper.drawTexture(matrices, x - 8, y + 2, 16, 16, 96.0F, (i < oneThird32) ? 32 : 0, 32, 32, 256, 256);
                if (this.pack.canMoveTowardEnd())
                    DrawableHelper.drawTexture(matrices, x + (int) oneThird32 - 8, y - 6, 16, 16, 64, (i < twoThird32 && i > oneThird32) ? 32 : 0, 32, 32, 256, 256);
            }
        }

        this.client.textRenderer.drawWithShadow(matrices, orderedText, (float) (x + 32 + 2), (float) (y + 1), 16777215);
        ci.cancel();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void injectedMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (screen instanceof ResourcePackScreenDuckProvider) {
            double d = mouseX - (double) this.widget.getRowLeft();
            if (this.isSelectable() && d <= 32) {
                if (this.pack.canBeEnabled() && d > twoThird32) {
                    ResourcePackCompatibility resourcePackCompatibility = this.pack.getCompatibility();
                    if (resourcePackCompatibility.isCompatible()) {
                        this.pack.enable();
                    } else {
                        Text text = resourcePackCompatibility.getConfirmMessage();
                        this.client.setScreen(new ConfirmScreen((bl) -> {
                            this.client.setScreen(this.screen);
                            if (bl) {
                                this.pack.enable();
                            }
                        }, INCOMPATIBLE_CONFIRM, text));
                    }

                    cir.setReturnValue(true);
                    cir.cancel();
                    return;
                }

                if (d > twoThird32 && this.pack.canBeDisabled()) {
                    this.pack.disable();
                    cir.setReturnValue(true);
                    cir.cancel();
                    return;
                }

                if (d < oneThird32 && this.pack.canMoveTowardStart()) {
                    this.pack.moveTowardStart();
                    cir.setReturnValue(true);
                    cir.cancel();
                    return;
                }

                if (d < twoThird32 && d > oneThird32 && this.pack.canMoveTowardEnd()) {
                    this.pack.moveTowardEnd();
                    cir.setReturnValue(true);
                    cir.cancel();
                    return;
                }
            }
        }
        cir.setReturnValue(false);
        cir.cancel();
    }
}
