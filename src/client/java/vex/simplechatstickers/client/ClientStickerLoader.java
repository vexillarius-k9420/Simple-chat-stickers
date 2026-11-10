package vex.simplechatstickers.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import vex.simplechatstickers.StickerRegistry;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ClientStickerLoader {

    public static void loadExternalPacks(MinecraftClient client) {
        // путь к корню игры
        Path packsDir = FabricLoader.getInstance().getGameDir().resolve("sticker-packs");

        // создание папки "sticker-packs" если её нет в корне
        if (!Files.exists(packsDir)) {
            try {
                Files.createDirectories(packsDir);
            } catch (Exception e) {
                System.err.println("[SimpleChatStickers] Failed to create sticker folder: " + e.getMessage());
                return;
            }
        }

        File[] zipFiles = packsDir.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".zip"));
        if (zipFiles == null) return;

        for (File zipFile : zipFiles) {
            try (ZipFile zip = new ZipFile(zipFile)) {
                ZipEntry jsonEntry = zip.getEntry("pack.json");
                if (jsonEntry == null) {
                    System.err.println("[SimpleChatStickers] Missing archive " + zipFile.getName() + ": missing pack.json");
                    continue;
                }

                try (InputStreamReader reader = new InputStreamReader(zip.getInputStream(jsonEntry))) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    String packName = json.get("name").getAsString();
                    JsonObject stickers = json.getAsJsonObject("stickers");

                    String safePackNamespace = packName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_");

                    for (Map.Entry<String, JsonElement> entry : stickers.entrySet()) {
                        String stickerCode = entry.getKey();
                        String imagePath = entry.getValue().getAsString();

                        ZipEntry imgEntry = zip.getEntry(imagePath);
                        if (imgEntry == null) {
                            System.err.println("[SimpleChatStickers] File " + imagePath + " not found in archive " + zipFile.getName());
                            continue;
                        }

                        try (InputStream imgStream = zip.getInputStream(imgEntry)) {
                            NativeImage nativeImage = NativeImage.read(imgStream);
                            NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);

                            String safeCodePath = stickerCode.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_");
                            Identifier dynamicIdentifier = Identifier.of("simple-chat-stickers", "external/" + safePackNamespace + "/" + safeCodePath);

                            // регистрация текстуры в клиенте
                            client.getTextureManager().registerTexture(dynamicIdentifier, texture);

                            // добавляем данные в общую карту, которая лежит в main
                            StickerRegistry.PACKS.computeIfAbsent(packName, k -> new LinkedHashMap<>())
                                    .put("`" + stickerCode + "`", dynamicIdentifier);
                        }
                    }
                    System.out.println("[SimpleChatStickers] The package was successfully downloaded.: " + packName);
                }
            } catch (Exception e) {
                System.err.println("[SimpleChatStickers] Error reading archive " + zipFile.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}