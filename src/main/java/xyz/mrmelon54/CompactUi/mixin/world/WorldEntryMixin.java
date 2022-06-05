package xyz.mrmelon54.CompactUi.mixin.world;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.LevelSummary;
import xyz.mrmelon54.CompactUi.duck.SingleplayerScreenDuckProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldListWidget.Entry.class)
public abstract class WorldEntryMixin {
    private static final float oneThird32 = 32 / 3f;
    private static final float twoThird32 = 32 * 2 / 3f;

    private static final Text FROM_NEWER_VERSION_FIRST_LINE = (new TranslatableText("selectWorld.tooltip.fromNewerVersion1")).formatted(Formatting.RED);
    private static final Text FROM_NEWER_VERSION_SECOND_LINE = (new TranslatableText("selectWorld.tooltip.fromNewerVersion2")).formatted(Formatting.RED);
    private static final Text SNAPSHOT_FIRST_LINE = (new TranslatableText("selectWorld.tooltip.snapshot1")).formatted(Formatting.GOLD);
    private static final Text SNAPSHOT_SECOND_LINE = (new TranslatableText("selectWorld.tooltip.snapshot2")).formatted(Formatting.GOLD);
    private static final Text LOCKED_TEXT = (new TranslatableText("selectWorld.locked")).formatted(Formatting.RED);
    private static final Text CONVERSION_TOOLTIP = (new TranslatableText("selectWorld.conversion.tooltip")).formatted(Formatting.RED);

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    @Final
    LevelSummary level;

    @Shadow
    @Final
    private SelectWorldScreen screen;

    @Shadow
    public abstract void play();

    @Shadow
    private long time;

    @Inject(method = "render", at = @At("HEAD"))
    private void injectedRenderHead(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        this.client.textRenderer.draw(matrices, this.level.getDisplayName(), (float) (x + oneThird32 + 3), (float) (y + 1), 16777215);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelSummary;getDetails()Lnet/minecraft/text/Text;"))
    private Text injectedRemoveDetails(LevelSummary levelSummary) {
        return new LiteralText("");
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawableHelper;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIFFIIII)V"))
    private void injectedEditWorldIcon(MatrixStack matrices, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        DrawableHelper.drawTexture(matrices, x, y, width / 3, height / 3, u, v, width, height, textureWidth, textureHeight);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/client/util/math/MatrixStack;Ljava/lang/String;FFI)I"))
    private int injectedEditTextPosition(TextRenderer textRenderer, MatrixStack matrices, String text, float x, float y, int color) {
        return textRenderer.draw(matrices, "", x - twoThird32, y, color);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawableHelper;fill(Lnet/minecraft/client/util/math/MatrixStack;IIIII)V"), cancellable = true)
    private void injectedFixArrows(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        DrawableHelper.fill(matrices, x, y, x + (int) oneThird32, y + (int) oneThird32, -1601138544);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = mouseX - x;
        boolean bl = i < oneThird32;
        int j = bl ? 32 : 0;
        if (this.level.isLocked()) {
            DrawableHelper.drawTexture(matrices, x, y, (int) oneThird32, (int) oneThird32, 96.0F, (float) j, 32, 32, 256, 256);
            if (bl) {
                this.screen.setTooltip(this.client.textRenderer.wrapLines(LOCKED_TEXT, 175));
            }
        } else if (this.level.requiresConversion()) {
            DrawableHelper.drawTexture(matrices, x, y, (int) oneThird32, (int) oneThird32, 96.0F, 32.0F, 32, 32, 256, 256);
            if (bl) {
                this.screen.setTooltip(this.client.textRenderer.wrapLines(CONVERSION_TOOLTIP, 175));
            }
        } else if (this.level.isDifferentVersion()) {
            DrawableHelper.drawTexture(matrices, x, y, (int) oneThird32, (int) oneThird32, 32.0F, (float) j, 32, 32, 256, 256);
            if (this.level.isFutureLevel()) {
                DrawableHelper.drawTexture(matrices, x, y, (int) oneThird32, (int) oneThird32, 96.0F, (float) j, 32, 32, 256, 256);
                if (bl) {
                    this.screen.setTooltip(ImmutableList.of(FROM_NEWER_VERSION_FIRST_LINE.asOrderedText(), FROM_NEWER_VERSION_SECOND_LINE.asOrderedText()));
                }
            } else if (!SharedConstants.getGameVersion().isStable()) {
                DrawableHelper.drawTexture(matrices, x, y, (int) oneThird32, (int) oneThird32, 64.0F, (float) j, 32, 32, 256, 256);
                if (bl) {
                    this.screen.setTooltip(ImmutableList.of(SNAPSHOT_FIRST_LINE.asOrderedText(), SNAPSHOT_SECOND_LINE.asOrderedText()));
                }
            }
        } else {
            DrawableHelper.drawTexture(matrices, x, y, (int) oneThird32, (int) oneThird32, 0.0F, (float) j, 32, 32, 256, 256);
        }
        ci.cancel();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void injectedMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (this.level.isUnavailable()) {
            cir.setReturnValue(true);
            cir.cancel();
        } else if (this.screen instanceof SingleplayerScreenDuckProvider duckProvider) {
            duckProvider.getWorldListWidget().setSelected((WorldListWidget.Entry) (Object) this);
            this.screen.worldSelected(duckProvider.getWorldListWidget().getSelectedAsOptional().isPresent());
            if (mouseX - (double) duckProvider.getWorldListWidget().getRowLeft() <= oneThird32) {
                this.play();
                cir.setReturnValue(true);
                cir.cancel();
            } else if (Util.getMeasuringTimeMs() - this.time < 250L) {
                this.play();
                cir.setReturnValue(true);
                cir.cancel();
            } else {
                this.time = Util.getMeasuringTimeMs();
                cir.setReturnValue(true);
                cir.cancel();
            }
        }
        cir.setReturnValue(false);
        cir.cancel();
    }
}
