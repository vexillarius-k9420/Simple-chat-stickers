package vex.simplechatstickers.client.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vex.simplechatstickers.StickerRegistry;

import java.util.List;
import java.util.Map;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

    @Shadow
    @Final
    private List<ChatHudLine.Visible> visibleMessages;
    @Shadow
    private int scrolledLines;
    @Shadow protected abstract boolean isChatFocused();
    @Shadow protected abstract boolean isChatHidden();

    @Inject(method = "render", at = @At("TAIL"))
    //? if 1.20-fabric {
    /*private void onRender(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci) {
    *///?} elif 1.21-fabric {
    private void onRender(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
    //?}

        if (this.visibleMessages == null || this.visibleMessages.isEmpty()) return;

        boolean isChatOpen = this.isChatFocused();
        if (!isChatOpen && this.isChatHidden()) return;

        float opacity = net.minecraft.client.MinecraftClient.getInstance().options.getChatOpacity().getValue().floatValue();
        if (opacity <= 0.0f) return;

        int chatBottomY = context.getScaledWindowHeight() - 40;
        int lineCount = 0;

        for (ChatHudLine.Visible line : this.visibleMessages) {
            int messageAge = currentTick - line.addedTime();

            double ageProgress = isChatOpen ? 0.0 : (double) (messageAge-175) / (200.0-175.0); // примерное время жизни до начала затухания
            // формула расчета прозрачности затухания:
            float fadeOpacity = (float) (1.0 - ageProgress);
            if (fadeOpacity < 0.0f) fadeOpacity = 0.0f;
            if (fadeOpacity > 1.0f) fadeOpacity = 1.0f;
            float finalAlpha = opacity * fadeOpacity;

            if ((!isChatOpen && messageAge >= 200) || finalAlpha <= 0.005f) { // 200
                lineCount++;
                continue;
            }


            // извлекаем текст строки один раз для всех проверок
            StringBuilder sb = new StringBuilder();
            line.content().accept((index, style, codePoint) -> {
                sb.appendCodePoint(codePoint);
                return true;
            });
            String messageText = sb.toString();

            // ПЕРЕБОР РЕЕСТРА
            rc:for (Map<String, Identifier> pack : StickerRegistry.PACKS.values()) {
                // проход по стикерам в паке
                for (Map.Entry<String, Identifier> entry : pack.entrySet()) {
                    String stickerCode = entry.getKey();
                    Identifier texture = entry.getValue();

                    if (messageText.contains(stickerCode)) {
                        int visualLineIndex = lineCount - this.scrolledLines;
                        float chatLineSpacing = net.minecraft.client.MinecraftClient.getInstance().options.getChatLineSpacing().getValue().floatValue();
                        float chatScale = net.minecraft.client.MinecraftClient.getInstance().options.getChatScale().getValue().floatValue();

                        if (visualLineIndex >= 0 && visualLineIndex < 20) {
                            int x = Math.round(5*(1f+chatScale));
                            int y = Math.round(chatBottomY - (visualLineIndex * (9f*(1f+chatLineSpacing)*chatScale)) - (35f*(1f+chatLineSpacing)*chatScale)); // -71

                            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, finalAlpha);

                            context.getMatrices().push();
                            context.getMatrices().translate(0, 0, 110f);

                            // texture из реестра вместо жесткого пути
                            context.drawTexture(
                                    texture,
                                    x, y,
                                    Math.round(70f*(1f+chatLineSpacing)*chatScale), Math.round(70f*(1f+chatLineSpacing)*chatScale),
                                    0f, 0f, 512, 512, 512, 512
                            );

                            context.getMatrices().pop();
                            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                        }
                        // выходим из цикла по реестру и идем к следующей строке чата
                        break rc;
                    }
                }
            }

            lineCount++;
        }
    }
}