package dev.revere.alley.adapter.core.internal;

import dev.revere.alley.adapter.core.Core;
import dev.revere.alley.adapter.core.CoreType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import services.plasma.helium.api.HeliumAPI;

/**
 * @author Emmy
 * @project Alley
 * @since 26/04/2025
 */
public class HeliumCoreImpl implements Core {
    protected final HeliumAPI heliumAPI;

    /**
     * Constructor for the HeliumCoreImpl class.
     * HeliumCoreImpl 类的构造函数。
     *
     * @param heliumAPI The HeliumAPI instance.
     *                  HeliumAPI 实例。
     */
    public HeliumCoreImpl(HeliumAPI heliumAPI) {
        this.heliumAPI = heliumAPI;
    }

    @Override
    public CoreType getType() {
        return CoreType.HELIUM;
    }

    @Override
    public ChatColor getPlayerColor(Player player) {
        return ChatColor.valueOf(this.heliumAPI.getRankColor(player.getUniqueId()));
    }

    @Override
    public String getRankPrefix(Player player) {
        return this.heliumAPI.getRankPrefix(player.getUniqueId());
    }

    @Override
    public String getRankName(Player player) {
        return this.heliumAPI.getRankName(player.getUniqueId());
    }

    @Override
    public String getRankSuffix(Player player) {
        return this.heliumAPI.getRankSuffix(player.getUniqueId());
    }

    @Override
    public ChatColor getRankColor(Player player) {
        return ChatColor.valueOf(this.heliumAPI.getRankColor(player.getUniqueId()));
    }

    @Override
    public String getTagPrefix(Player player) {
        return this.heliumAPI.getTagDisplayName(player.getUniqueId());
    }

    @Override
    public ChatColor getTagColor(Player player) {
        return ChatColor.WHITE;
    }
}