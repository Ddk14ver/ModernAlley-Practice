package dev.revere.alley.feature.cosmetic.internal.repository.impl.mvpmusic;

import dev.revere.alley.AlleyPlugin;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 13/07/2026
 *
 * Tiny embedded HTTP server that serves the MVP music resource pack
 * ({@code alley_mvp_music.zip}) so clients can download it via
 * {@link org.bukkit.entity.Player#setResourcePack(String)}.
 * <p>
 * Uses {@code com.sun.net.httpserver.HttpServer} (JDK built-in, zero dependencies).
 */
public final class HttpPackServer {

    private com.sun.net.httpserver.HttpServer server;
    private final String packUrl;

    private HttpPackServer(com.sun.net.httpserver.HttpServer server, String packUrl) {
        this.server = server;
        this.packUrl = packUrl;
    }

    /**
     * Starts the HTTP server serving files from the plugin data folder
     * under {@code /alley/}.
     *
     * @param plugin the Alley plugin instance
     * @return a started server, or {@code null} on failure
     */
    public static HttpPackServer start(AlleyPlugin plugin) {
        dev.revere.alley.core.locale.LocaleService locale =
                plugin.getService(dev.revere.alley.core.locale.LocaleService.class);

        // If user provides a full URL override (e.g. HTTPS hosting), use it directly
        // and skip the local HTTP server entirely.
        String overrideUrl = locale.getString(
                dev.revere.alley.core.locale.internal.impl.SettingsLocaleImpl.MVP_MUSIC_PACK_URL_OVERRIDE);
        if (overrideUrl != null && !overrideUrl.isEmpty()) {
            plugin.getLogger().info("[Alley] MVP pack using override URL: " + overrideUrl);
            return new HttpPackServer(null, overrideUrl);
        }

        // Read config
        String configHost = locale.getString(
                dev.revere.alley.core.locale.internal.impl.SettingsLocaleImpl.MVP_MUSIC_PACK_HOST);
        int startPort = locale.getInt(
                dev.revere.alley.core.locale.internal.impl.SettingsLocaleImpl.MVP_MUSIC_PACK_PORT);

        // Build the public-facing URL base
        String publicUrlBase;
        if (configHost != null && !configHost.isEmpty() && !configHost.equals("127.0.0.1")) {
            if (configHost.contains(":")) {
                publicUrlBase = "http://" + configHost;
            } else {
                publicUrlBase = "http://" + configHost + ":" + startPort;
            }
        } else {
            String ip = plugin.getServer().getIp();
            if (ip == null || ip.isEmpty() || ip.equals("0.0.0.0")) ip = "127.0.0.1";
            publicUrlBase = "http://" + ip + ":" + startPort;
        }

        int maxAttempts = 10;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int tryPort = startPort + attempt;
            try {
                InetSocketAddress addr = new InetSocketAddress(tryPort);
                com.sun.net.httpserver.HttpServer srv =
                        com.sun.net.httpserver.HttpServer.create(addr, 0);
                srv.setExecutor(Executors.newFixedThreadPool(2));

                Path dataFolder = plugin.getDataFolder().toPath();
                srv.createContext("/alley/", exchange -> {
                    String path = exchange.getRequestURI().getPath();
                    String fileName = path.substring(path.lastIndexOf('/') + 1);
                    if (fileName.isEmpty() || fileName.contains("..")) {
                        exchange.sendResponseHeaders(404, -1);
                        return;
                    }
                    Path file = dataFolder.resolve(fileName);
                    if (!Files.exists(file) || !Files.isRegularFile(file)) {
                        exchange.sendResponseHeaders(404, -1);
                        return;
                    }
                    exchange.getResponseHeaders().set("Content-Type", "application/zip");
                    exchange.sendResponseHeaders(200, Files.size(file));
                    try (OutputStream out = exchange.getResponseBody();
                         InputStream in = Files.newInputStream(file)) {
                        in.transferTo(out);
                    }
                });

                srv.start();

                // Use the configured public base if this is the first port attempt,
                // otherwise fall back to building URL from the actual listen port
                String url;
                if (tryPort == startPort) {
                    url = publicUrlBase + "/alley/" + ResourcePackBuilder.PACK_FILE_NAME;
                } else {
                    url = "http://" + (configHost != null && !configHost.isEmpty()
                            ? configHost.split(":")[0] : "127.0.0.1")
                            + ":" + tryPort + "/alley/" + ResourcePackBuilder.PACK_FILE_NAME;
                }

                plugin.getLogger().info("[Alley] HTTP pack server started on port " + tryPort
                        + " (public URL: " + url + ")");
                return new HttpPackServer(srv, url);
            } catch (IOException e) {
                // port busy, try next
            }
        }
        plugin.getLogger().severe("[Alley] HTTP pack server failed to start after "
                + maxAttempts + " attempts");
        return null;
    }

    /** Stops the HTTP server if one is running. */
    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    /** The full URL for downloading the resource pack. */
    public String getPackUrl() {
        return packUrl;
    }
}
