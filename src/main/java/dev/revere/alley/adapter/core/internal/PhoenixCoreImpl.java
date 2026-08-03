package dev.revere.alley.adapter.core.internal;

import dev.revere.alley.adapter.core.Core;
import dev.revere.alley.adapter.core.CoreType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.refinedev.phoenix.SharedAPI;
import xyz.refinedev.phoenix.profile.Profile;

import java.util.Objects;

/**
 * @author Emmy
 * @project Alley
 * @since 26/04/2025
 */
public class PhoenixCoreImpl implements Core {
    protected final SharedAPI phoenixAPI;

    /**
     * Constructor for the PhoenixCoreImpl class.
     * PhoenixCoreImpl 类的构造函数。
     *
     * @param phoenixAPI The Shared Phoenix API instance.
     *                   共享的 Phoenix API 实例。
     */
    public PhoenixCoreImpl(SharedAPI phoenixAPI) {
        this.phoenixAPI = phoenixAPI;
    }

    @Override
    public CoreType getType() {
        return CoreType.PHOENIX;
    }

    @Override
    public ChatColor getPlayerColor(Player player) {
        return ChatColor.valueOf(this.getProfile(player).getHighestRank().getColor());
    }

    @Override
    public String getRankPrefix(Player player) {
        return this.getProfile(player).getHighestRank().getPrefix();
    }

    @Override
    public String getRankName(Player player) {
        return this.getProfile(player).getHighestRank().getName();
    }

    @Override
    public String getRankSuffix(Player player) {
        return this.getProfile(player).getHighestRank().getSuffix();
    }

    @Override
    public ChatColor getRankColor(Player player) {
        return ChatColor.valueOf(this.getProfile(player).getHighestRank().getColor());
    }

    @Override
    public String getTagPrefix(Player player) {
        return Objects.requireNonNull(this.getProfile(player).getTag()).getPrefix();
    }

    @Override
    public ChatColor getTagColor(Player player) {
        return ChatColor.valueOf(Objects.requireNonNull(this.getProfile(player).getTag()).getColor());
    }

    /**
     * Retrieves the Profile object associated with the given player.
     * 获取与给定玩家关联的 Profile 对象。
     *
     * @param player The player whose profile is to be retrieved.
     *               要获取其个人资料的玩家。
     * @return The Profile object associated with the player.
     *         与玩家关联的 Profile 对象。
     */
    private Profile getProfile(Player player) {
        return this.phoenixAPI.getProfileHandler().getProfile(player.getUniqueId());
    }
}