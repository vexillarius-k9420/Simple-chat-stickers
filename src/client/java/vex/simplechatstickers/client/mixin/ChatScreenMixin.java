package vex.simplechatstickers.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vex.simplechatstickers.StickerRegistry;

import java.util.Map;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {

    @Shadow protected TextFieldWidget chatField;

    @Unique private boolean stickerMenuOpen = false;
    @Unique private String currentPack = null; // текущая открытая вкладка

    protected ChatScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        int buttonX = this.width - 24;
        int buttonY = this.height - 38;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("☻"), button -> {
            this.stickerMenuOpen = !this.stickerMenuOpen;
            // возврат курсора в строку ввода
            this.setFocused(this.chatField);
        }).dimensions(buttonX, buttonY, 20, 20).build());

        // установка первого пака по умолчанию (если он есть)
        if (!StickerRegistry.PACKS.isEmpty()) {
            this.currentPack = StickerRegistry.PACKS.keySet().iterator().next();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!this.stickerMenuOpen) return;


        int menuWidth = 230; // 220
        int menuHeight = 170; // 150
        int startX = this.width - menuWidth - 4;
        int startY = this.height - 40 - menuHeight;

        // фон меню
        context.fill(startX, startY, startX + menuWidth, startY + menuHeight, 0xBB000000);

        // 1. РИСУЕМ ВКЛАДКИ (НАЗВАНИЯ ПАКОВ)
        int tabX = startX + 5;
        int tabY = startY + 5;

        for (String packName : StickerRegistry.PACKS.keySet()) {
            // для выбраного пака желтый, остальные серые
            int color = packName.equals(this.currentPack) ? 0xFFFFAA : 0xAAAAAA;
            context.drawTextWithShadow(this.textRenderer, packName, tabX, tabY, color);
            tabX += this.textRenderer.getWidth(packName) + 10; // Отступ между вкладками
        }

        // Разделительная линия под вкладками
        context.fill(startX + 4, startY + 16, startX + menuWidth - 4, startY + 17, 0x55FFFFFF);

        // 2. РИСУЕМ СТИКЕРЫ ТЕКУЩЕГО ПАКА
        if (this.currentPack != null && StickerRegistry.PACKS.containsKey(this.currentPack)) {
            int xOffset = 5;
            int yOffset = 22; // сдвиг вниз, что-бы не перекрывать вкладки
            int iconSize = 32;

            Map<String, Identifier> currentStickers = StickerRegistry.PACKS.get(this.currentPack);

            for (Map.Entry<String, Identifier> entry : currentStickers.entrySet()) {
                context.drawTexture(entry.getValue(), startX + xOffset, startY + yOffset, 0, 0, iconSize, iconSize, iconSize, iconSize);

                xOffset += iconSize + 5;
                if (xOffset + iconSize > menuWidth) {
                    xOffset = 5;
                    yOffset += iconSize + 5;
                }
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!this.stickerMenuOpen || button != 0) return;

        int menuWidth = 230; // 220
        int menuHeight = 170; // 150
        int startX = this.width - menuWidth - 4;
        int startY = this.height - 40 - menuHeight;

        if (mouseX < startX || mouseX > startX + menuWidth || mouseY < startY || mouseY > startY + menuHeight) {
            this.stickerMenuOpen = false;
            return;
        }

        // 1. КЛИК ПО ВКЛАДКАМ
        int tabX = startX + 5;
        int tabY = startY + 5;
        for (String packName : StickerRegistry.PACKS.keySet()) {
            int tabWidth = this.textRenderer.getWidth(packName);
            if (mouseX >= tabX && mouseX <= tabX + tabWidth && mouseY >= tabY && mouseY <= tabY + 10) {
                this.currentPack = packName;
                cir.setReturnValue(true);
                return; // Переключили вкладку и вышли
            }
            tabX += tabWidth + 10;
        }

        // 2. КЛИК ПО СТИКЕРУ
        if (this.currentPack != null && StickerRegistry.PACKS.containsKey(this.currentPack)) {
            int xOffset = 5;
            int yOffset = 22;
            int iconSize = 32;

            for (Map.Entry<String, Identifier> entry : StickerRegistry.PACKS.get(this.currentPack).entrySet()) {
                int iconX = startX + xOffset;
                int iconY = startY + yOffset;

                if (mouseX >= iconX && mouseX <= iconX + iconSize && mouseY >= iconY && mouseY <= iconY + iconSize) {
                    String stickerCode = entry.getKey();
                    String currentText = this.chatField.getText(); // берём "подпись" к стикеру

                    // склеиваем текст со стикером
                    String fullMessage;
                    if (currentText.isEmpty()) {
                        fullMessage = stickerCode;
                    } else if (currentText.endsWith(" ")) {
                        fullMessage = currentText + stickerCode;
                    } else {
                        fullMessage = currentText + " " + stickerCode; // пробел между текстом и кодом
                    }

                    // ручная отправка с полной поддержкой команд и истории чата
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client != null && client.player != null) {

                        // добавление сообщения в историю
                        client.inGameHud.getChatHud().addToMessageHistory(fullMessage);

                        // начинается ли текст со слэша?
                        if (fullMessage.startsWith("/")) {
                            // sendChatCommand требует строку без слэша, поэтому отрезаем первый символ
                            String commandWithoutSlash = fullMessage.substring(1);
                            client.player.networkHandler.sendChatCommand(commandWithoutSlash);
                        } else {
                            // для обычного текста в чат
                            client.player.networkHandler.sendChatMessage(fullMessage);
                        }

                        // закрытие чата после отправки
                        client.setScreen(null);
                    }

                    cir.setReturnValue(true);
                    return;
                }

                xOffset += iconSize + 5;
                if (xOffset + iconSize > menuWidth) {
                    xOffset = 5;
                    yOffset += iconSize + 5;
                }
            }
        }

        cir.setReturnValue(true);
    }
}
