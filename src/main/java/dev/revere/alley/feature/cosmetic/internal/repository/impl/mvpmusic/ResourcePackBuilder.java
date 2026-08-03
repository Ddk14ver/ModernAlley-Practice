package dev.revere.alley.feature.cosmetic.internal.repository.impl.mvpmusic;

import dev.revere.alley.AlleyPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Builds a static Minecraft resource pack containing all MVP music .ogg files
 * and a {@code sounds.json} mapping each song to a custom sound event.
 * <p>
 * The pack is generated once at startup into the plugin data folder and
 * served to players via {@code server.properties resource-pack=} or
 * {@link org.bukkit.entity.Player#setResourcePack(String)}.
 * <p>
 * Sound event naming convention: {@code alley.mvp.<songKey>}.
 */
public final class ResourcePackBuilder {

    /** Maps sound event name → .ogg asset path inside the pack. */
    private static final Map<String, String> SONGS = new LinkedHashMap<>();

    static {
        // Sound event name             .ogg file inside resources/mvpmusic/
        SONGS.put("alley.mvp.onmyown",    "onmyown.ogg");
        SONGS.put("alley.mvp.ez4ence",    "ez4ence.ogg");
        SONGS.put("alley.mvp.haruhikage", "cry.ogg");
        SONGS.put("alley.mvp.dashstar",   "cjx.ogg");
        SONGS.put("alley.mvp.flashbang",  "flashbang.ogg");
        SONGS.put("alley.mvp.inhuman",    "inhuman.ogg");
        SONGS.put("alley.mvp.boundbylove","qlwh.ogg");
        SONGS.put("alley.mvp.girlbandcry","xxrrwmdcs.ogg");
    }

    public static final String PACK_FILE_NAME = "alley_mvp_music.zip";
    private static final int PACK_FORMAT = 34; // Minecraft 1.21+

    private ResourcePackBuilder() {}

    /**
     * Ensures the MVP music resource pack exists in the plugin data folder.
     * If the pack is already present and up-to-date this is a no-op.
     *
     * @param plugin the Alley plugin instance
     */
    public static void ensurePackExists(AlleyPlugin plugin) {
        Path packFile = plugin.getDataFolder().toPath().resolve(PACK_FILE_NAME);

        // Check whether at least one .ogg source is available
        boolean anySource = false;
        for (String oggFile : SONGS.values()) {
            if (plugin.getResource("mvpmusic/" + oggFile) != null) {
                anySource = true;
                break;
            }
        }
        if (!anySource) {
            plugin.getLogger().warning("[Alley] No .ogg MVP music files found in resources/mvpmusic/ — "
                    + "resource pack will not be generated.");
            return;
        }

        // Skip if pack already exists (delete the file to force regeneration)
        if (Files.exists(packFile)) {
            plugin.getLogger().info("[Alley] MVP music resource pack already exists: " + packFile);
            return;
        }

        try {
            generatePack(plugin, packFile);
            plugin.getLogger().info("[Alley] MVP music resource pack generated: " + packFile
                    + " (" + packFile.toFile().length() / 1024 + " KB)");
        } catch (Exception e) {
            plugin.getLogger().severe("[Alley] Failed to generate MVP music resource pack: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Returns the SHA-1 hash of the generated pack file, or empty string if it doesn't exist. */
    public static String getPackSha1(AlleyPlugin plugin) {
        Path packFile = plugin.getDataFolder().toPath().resolve(PACK_FILE_NAME);
        if (!Files.exists(packFile)) return "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            try (InputStream in = Files.newInputStream(packFile)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) != -1) md.update(buf, 0, n);
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private static void generatePack(AlleyPlugin plugin, Path packFile) throws Exception {
        Path tmpFile = Files.createTempFile("alley_mvp_", ".zip");
        try {
            try (ZipOutputStream zos = new ZipOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(tmpFile)))) {

                // --- pack.mcmeta ---
                zos.putNextEntry(new ZipEntry("pack.mcmeta"));
                writePackMcmeta(zos);
                zos.closeEntry();

                // --- sounds.json ---
                zos.putNextEntry(new ZipEntry("assets/minecraft/sounds.json"));
                writeSoundsJson(zos);
                zos.closeEntry();

                // --- .ogg files ---
                for (Map.Entry<String, String> entry : SONGS.entrySet()) {
                    String resourcePath = "mvpmusic/" + entry.getValue();
                    try (InputStream in = plugin.getResource(resourcePath)) {
                        if (in == null) {
                            plugin.getLogger().warning("[Alley] Missing .ogg resource: " + resourcePath);
                            continue;
                        }
                        // Path inside the pack: assets/minecraft/sounds/alley/mvpmusic/xxx.ogg
                        String packPath = "assets/minecraft/sounds/alley/mvpmusic/" + entry.getValue();
                        zos.putNextEntry(new ZipEntry(packPath));
                        in.transferTo(zos);
                        zos.closeEntry();
                    }
                }
            }
            Files.move(tmpFile, packFile);
        } finally {
            try { Files.deleteIfExists(tmpFile); } catch (IOException ignored) {}
        }
    }

    private static void writePackMcmeta(ZipOutputStream zos) throws IOException {
        String json = "{\"pack\":{\"pack_format\":" + PACK_FORMAT
                + ",\"description\":\"Alley MVP Music\"}}";
        zos.write(json.getBytes(StandardCharsets.UTF_8));
    }

    private static void writeSoundsJson(ZipOutputStream zos) throws IOException {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (String eventName : SONGS.keySet()) {
            if (!first) sb.append(",");
            first = false;
            String oggFile = SONGS.get(eventName);
            String soundPath = "alley/mvpmusic/" + (oggFile != null ? oggFile.replace(".ogg", "") : eventName);
            sb.append("\"").append(eventName).append("\":{\"sounds\":[")
              .append("{\"name\":\"").append(soundPath).append("\",\"stream\":true}")
              .append("]}");
        }
        sb.append("}");
        zos.write(sb.toString().getBytes(StandardCharsets.UTF_8));
    }
}
