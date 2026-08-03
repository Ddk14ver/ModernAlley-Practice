package dev.revere.alley.visual.nametag.internal;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.revere.alley.visual.nametag.NametagAdapter;
import dev.revere.alley.visual.nametag.NametagView;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 名字标签注册表，管理名字标签适配器的缓存和生命周期。
 * @author Remi
 * @project alley-practice
 * @date 27/06/2025
 */
@Getter
public class NametagRegistry {
    private final Cache<String, NametagAdapter> adapterCache;
    private final NametagServiceImpl service;
    private final AtomicInteger teamIdCounter = new AtomicInteger(0);

    public NametagRegistry(NametagServiceImpl service) {
        this.service = service;
        this.adapterCache = CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build();
    }

    /**
     * Gets or creates a NametagAdapter for a given style.
     * 获取或创建指定样式的 NametagAdapter。
     *
     * @param view The nametag view.
     *        名字标签视图。
     * @return The cached or newly created NametagAdapter.
     *         缓存的或新创建的 NametagAdapter。
     */
    public NametagAdapter getAdapter(NametagView view) {
        String key = view.getPrefix() + "|" + view.getSuffix() + "|" + view.getVisibility().name();
        try {
            return adapterCache.get(key, () -> {
                String teamName = "nt" + teamIdCounter.getAndIncrement();
                return new NametagAdapter(service, teamName, view.getPrefix(), view.getSuffix(), view.getVisibility());
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to load nametag adapter from cache", e);
        }
    }

    /**
     * Sends creation packets for all active adapters to a specific player.
     * 将当前所有活动的适配器的创建数据包发送给指定玩家。
     *
     * @param player The player to receive the packets.
     *        接收数据包的玩家。
     */
    public void sendAllAdapters(Player player) {
        for (NametagAdapter adapter : adapterCache.asMap().values()) {
            adapter.sendCreationPacket(player);
        }
    }

    /**
     * Cleans up a player's data from all perspectives when they quit.
     * 在玩家退出时从所有视角清除该玩家的数据。
     *
     * @param player The player who quit.
     *        退出的玩家。
     */
    public void cleanupPlayer(Player player) {
        service.getPlayerPerspectives().values().forEach(p -> p.getDisplayedAdapters().remove(player.getUniqueId()));
    }
}