package dev.revere.alley.feature.arena;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.feature.arena.internal.types.StandAloneArena;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.feature.kit.setting.types.mode.KitSettingBridges;
import dev.revere.alley.feature.kit.setting.types.mode.KitSettingRounds;
import org.bukkit.entity.Player;

/**
 * @author Emmy
 * @project alley-practice
 * @since 07/09/2025
 */
public class ArenaValidator {
    /**
     * Validates if an arena is fully configured before enabling or disabling it.
     * Sends appropriate messages to the player if validation fails.
     * 在启用或禁用竞技场之前验证其是否已完全配置。
     * 如果验证失败，则向玩家发送适当的消息。
     *
     * @param player the player attempting to enable/disable the arena
     *               尝试启用/禁用竞技场的玩家
     * @param arena  the arena to be validated
     *               要验证的竞技场
     * @return true if the arena is fully configured, false otherwise
     *         如果竞技场已完全配置则返回 true，否则返回 false
     */
    public boolean isEligible(Player player, Arena arena) {
        LocaleService localeService = AlleyPlugin.getInstance().getService(LocaleService.class);

        if (arena.getMinimum() == null || arena.getMaximum() == null) {
            player.sendMessage(localeService.getString(GlobalMessagesLocaleImpl.ARENA_NO_SELECTION));
            return false;
        }

        if (arena.getPos1() == null || arena.getPos2() == null) {
            player.sendMessage(localeService.getString(GlobalMessagesLocaleImpl.ARENA_SPAWN_NOT_SET));
            return false;
        }

        if (arena.getCenter() == null) {
            player.sendMessage(localeService.getString(GlobalMessagesLocaleImpl.ARENA_CENTER_NOT_SET));
            return false;
        }

        if (arena.getKits().isEmpty()) {
            player.sendMessage(localeService.getString(GlobalMessagesLocaleImpl.ARENA_MUST_ADD_KIT));
            return false;
        }

        KitService kitService = AlleyPlugin.getInstance().getService(KitService.class);
        for (String kitName : arena.getKits()) {
            Kit kit = kitService.getKit(kitName);
            if (kit == null) {
                player.sendMessage(localeService.getString(GlobalMessagesLocaleImpl.ARENA_ASSIGNED_KIT_NULL).replace("{kit-name}", kitName));
                return false;
            }

            if (arena.getType() == ArenaType.STANDALONE) {
                StandAloneArena standAloneArena = (StandAloneArena) arena;
                if ((kit.isSettingEnabled(KitSettingRounds.class) || kit.isSettingEnabled(KitSettingBridges.class)) && (standAloneArena.getTeam1Portal() == null || standAloneArena.getTeam2Portal() == null)) {
                    player.sendMessage(localeService.getString(GlobalMessagesLocaleImpl.ARENA_STANDALONE_PORTALS_NOT_SET));
                    return false;
                }
            }
        }

        return true;
    }
}