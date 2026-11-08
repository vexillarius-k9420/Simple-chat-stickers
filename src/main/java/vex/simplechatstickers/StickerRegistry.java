package vex.simplechatstickers;

import net.minecraft.util.Identifier;
import java.util.LinkedHashMap;
import java.util.Map;

public class StickerRegistry {
    // LinkedHashMap, чтобы сохранить порядок добавления
    public static final Map<String, Map<String, Identifier>> PACKS = new LinkedHashMap<>();

    static {
        // формат: register("Название папки", "код", "путь к файлу");

        // Smilies
        register("Smilies","=)", "textures/stickers/smilies/winking.png");
        register("Smilies","XD", "textures/stickers/smilies/laughing.png");
        register("Smilies","=P", "textures/stickers/smilies/tongue.png");
        register("Smilies","=D", "textures/stickers/smilies/funny.png");
        register("Smilies",">=(", "textures/stickers/smilies/wicked.png");
        register("Smilies","8(", "textures/stickers/smilies/scared.png");
        register("Smilies","='(", "textures/stickers/smilies/cry.png");

        // Memes
        register("Memes","cat_=P", "textures/stickers/memes/cat_tongue.png");
        register("Memes","cat_Y", "textures/stickers/memes/yeah.png");
        register("Memes","Yeah", "textures/stickers/memes/yeaah.png");
        register("Memes","heheh", "textures/stickers/memes/heheh.png");
        register("Memes","cat_=3", "textures/stickers/memes/cat_happy.png");
        register("Memes","cake", "textures/stickers/memes/cake.png");
        register("Memes","smirk", "textures/stickers/memes/smirk.png");
        register("Memes",">=P", "textures/stickers/memes/smirk_2.png");
        register("Memes","military", "textures/stickers/memes/military.png");
        register("Memes","suspicion", "textures/stickers/memes/suspicion.png");
    }

    private static void register(String pack, String code, String path) {
        // если пака нет, создаем для него новую группу
        PACKS.computeIfAbsent(pack, k -> new LinkedHashMap<>())
                .put("`" + code + "`", Identifier.of("simple-chat-stickers", path));
    }
}
