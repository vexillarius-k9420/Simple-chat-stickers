package vex.simplechatstickers.client.mixin;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import vex.simplechatstickers.StickerRegistry;

import java.util.Map;
import java.util.Objects;

@Mixin(ChatHud.class)
public class ChatListenerMixin {

    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At("HEAD"), argsOnly = true)
    private Text replaceStickerCode(Text message) {
        String content;
        TranslatableTextContent messageContent = null;

        boolean normalMessage = (message.getContent() instanceof TranslatableTextContent
                && Objects.equals(((TranslatableTextContent)message.getContent()).getKey(), "chat.type.text"));
        if (normalMessage) {
            messageContent = (TranslatableTextContent) message.getContent();
            content = messageContent.getArg(1).getString();
        } else  {
            content = message.getString();
        }

        for (Map<String, Identifier> pack : StickerRegistry.PACKS.values()) {
            for (String code : pack.keySet()) {
                int index = content.indexOf(code);
                if (index != -1) {
                    String spacing = "\n\n\n\n";

                    MutableText result = Text.empty();

                    String partBefore = content.substring(0, index).replace("`","*");
                    String partAfter = content.substring(index + code.length()).replace("`","*");

                    result.append(Text.literal(partBefore).setStyle(message.getStyle()));
                    MutableText codeText = Text.literal(spacing +"   "+ code + spacing + (partAfter.isEmpty() ? "" : "\n")).formatted(Formatting.DARK_GRAY);
                    codeText.setStyle(codeText.getStyle()
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, code))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to copy\n "+code))));
                    result.append(codeText);
                    result.append(Text.literal(partAfter).setStyle(message.getStyle()));

                    if (normalMessage) {
                        Object[] args = new Object[]{messageContent.getArg(0), result};
                        TranslatableTextContent newContent = new TranslatableTextContent(messageContent.getKey(), messageContent.getFallback(), args);
                        return MutableText.of(newContent);
                    } else {return result;}
                }
            }
        }

    return message;
    }
}