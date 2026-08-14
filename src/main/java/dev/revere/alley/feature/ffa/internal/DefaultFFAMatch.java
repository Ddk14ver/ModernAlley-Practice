package dev.revere.alley.feature.ffa.internal;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.knockback.KnockbackManager;
import dev.revere.alley.common.PlayerUtil;
import dev.revere.alley.common.reflect.ReflectionService;
import dev.revere.alley.common.reflect.internal.types.ActionBarReflectionServiceImpl;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.locale.internal.impl.message.GameMessagesLocaleImpl;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.data.types.ProfileFFAData;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.challenge.ChallengeService;
import dev.revere.alley.feature.challenge.ChallengeType;
import dev.revere.alley.feature.combat.CombatService;
import dev.revere.alley.feature.cooldown.Cooldown;
import dev.revere.alley.feature.cooldown.CooldownService;
import dev.revere.alley.feature.cooldown.CooldownType;
import dev.revere.alley.feature.ffa.FFAMatch;
import dev.revere.alley.feature.ffa.FFAState;
import dev.revere.alley.feature.ffa.model.GameFFAPlayer;
import dev.revere.alley.feature.hotbar.HotbarService;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.match.MatchService;
import dev.revere.alley.feature.match.internal.MatchServiceImpl;
import dev.revere.alley.feature.spawn.SpawnService;
import dev.revere.alley.feature.visibility.VisibilityService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Remi
 * @project Alley
 * @date 5/27/2024
 */
public class DefaultFFAMatch extends FFAMatch {
    protected final AlleyPlugin plugin = AlleyPlugin.getInstance();

    private void applyLegacyCombat(Player player) {
        MatchService matchService = this.plugin.getService(MatchService.class);
        if (matchService instanceof MatchServiceImpl matchServiceImpl
                && matchServiceImpl.getLegacyCombatService() != null) {
            matchServiceImpl.getLegacyCombatService().applyKit(player, this.getKit());
        }
    }

    private void removeLegacyCombat(Player player) {
        MatchService matchService = this.plugin.getService(MatchService.class);
        if (matchService instanceof MatchServiceImpl matchServiceImpl
                && matchServiceImpl.getLegacyCombatService() != null) {
            matchServiceImpl.getLegacyCombatService().removeAll(player);
        }
    }

    /**
     * Constructor for the DefaultFFAMatchImpl class.
     * DefaultFFAMatchImpl 类的构造函数。
     *
     * @param name       The name of the match
     *                   比赛名称
     * @param arena      The arena the match is being played in
     *                   比赛所在的竞技场
     * @param kit        The kit the players are using
     *                   玩家使用的工具包
     * @param maxPlayers The maximum amount of players allowed in the match
     *                   比赛中允许的最大玩家数量
     */
    public DefaultFFAMatch(String name, Arena arena, Kit kit, int maxPlayers) {
        super(name, arena, kit, maxPlayers);
    }

    /**
     * Join a player to the FFA match.
     * 将玩家加入到 FFA 比赛中。
     *
     * @param player The player
     *               玩家
     */
    @Override
    public void join(Player player) {
        if (this.getArena() == null) return;
        if (this.getArena().getPos1() == null) return;

        Profile profile = this.plugin.getService(ProfileService.class).getProfile(player.getUniqueId());
        GameFFAPlayer gameFFAPlayer = new GameFFAPlayer(player.getUniqueId(), player.getName());
        if (this.getPlayers().size() >= this.getMaxPlayers()) return;

        this.getPlayers().add(gameFFAPlayer);

        LocaleService localeService = this.plugin.getService(LocaleService.class);
        boolean ffaPlayerLeftMessageEnabled = localeService.getBoolean(GameMessagesLocaleImpl.FFA_PLAYER_JOIN_MESSAGE_ENABLED_BOOLEAN);
        if (ffaPlayerLeftMessageEnabled) {
            List<String> ffaPlayerLeftMessageFormat = localeService.getStringList(GameMessagesLocaleImpl.FFA_PLAYER_JOIN_MESSAGE_FORMAT);
            for (String line : ffaPlayerLeftMessageFormat) {
                this.getPlayers().forEach(ffaPlayer -> ffaPlayer.getPlayer().sendMessage(
                        CC.translate(line
                                .replace("{name-color}", profile.getNameColor().toString())
                                .replace("{player}", profile.getName())
                        )
                ));
            }
        }

        this.setupPlayer(player);
    }

    /**
     * Force a player to join the FFA match.
     * 强制玩家加入 FFA 比赛。
     *
     * @param player The player
     *               玩家
     */
    public void forceJoin(Player player) {
        if (this.getArena() == null) return;
        if (this.getArena().getPos1() == null) return;

        GameFFAPlayer gameFFAPlayer = new GameFFAPlayer(player.getUniqueId(), player.getName());
        this.getPlayers().add(gameFFAPlayer);
        this.setupPlayer(player);
    }

    /**
     * Leave a player from the FFA match.
     * 将玩家从 FFA 比赛中移除。
     *
     * @param player The player
     *               玩家
     */
    @Override
    public void leave(Player player) {
        ProfileService profileService = this.plugin.getService(ProfileService.class);
        LocaleService localeService = this.plugin.getService(LocaleService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());

        CooldownService cooldownService = this.plugin.getService(CooldownService.class);
        Cooldown pearlCooldown = cooldownService.getCooldown(player.getUniqueId(), CooldownType.ENDER_PEARL);
        if (pearlCooldown != null) {
            pearlCooldown.cancelCooldown();
            cooldownService.removeCooldown(player.getUniqueId(), CooldownType.ENDER_PEARL);
        }

        GameFFAPlayer gameFFAPlayer = this.getGameFFAPlayer(player);
        this.getPlayers().remove(gameFFAPlayer);

        boolean ffaPlayerLeftMessageEnabled = localeService.getBoolean(GameMessagesLocaleImpl.FFA_PLAYER_LEFT_MESSAGE_ENABLED_BOOLEAN);
        if (ffaPlayerLeftMessageEnabled) {
            List<String> ffaPlayerLeftMessageFormat = localeService.getStringList(GameMessagesLocaleImpl.FFA_PLAYER_LEFT_MESSAGE_FORMAT);
            for (String line : ffaPlayerLeftMessageFormat) {
                this.getPlayers().forEach(ffaPlayer -> ffaPlayer.getPlayer().sendMessage(
                        CC.translate(line
                                .replace("{name-color}", profile.getNameColor().toString())
                                .replace("{player}", profile.getName())
                        )
                ));
            }
        }

        profile.setState(ProfileState.LOBBY);
        profile.setFfaMatch(null);
        profile.getProfileData().getFfaData().get(this.getKit().getName()).resetKillstreak();

        this.plugin.getService(VisibilityService.class).updateVisibility(player);

        this.removeLegacyCombat(player);
        PlayerUtil.reset(player, false, true);
        this.plugin.getService(KnockbackManager.class).clearKnockback(player);
        this.plugin.getService(SpawnService.class).teleportToSpawn(player);
        this.plugin.getService(HotbarService.class).applyHotbarItems(player);
    }

    /**
     * Setup a player for the FFA match.
     * 为 FFA 比赛设置玩家。
     *
     * @param player The player
     *               玩家
     */
    @Override
    public void setupPlayer(Player player) {
        GameFFAPlayer gameFFAPlayer = this.getGameFFAPlayer(player);
        gameFFAPlayer.setState(FFAState.SPAWN);

        ProfileService profileService = this.plugin.getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());
        profile.setState(ProfileState.FFA);
        profile.setFfaMatch(this);

        this.plugin.getService(VisibilityService.class).updateVisibility(player);

        PlayerUtil.reset(player, true, true);
        this.plugin.getService(KnockbackManager.class).applyKnockback(player, getKit());

        Arena arena = this.getArena();
        player.teleport(arena.getPos1());

        Kit kit = this.getKit();
        player.getInventory().setArmorContents(kit.getArmor());
        player.getInventory().setContents(kit.getItems());
        this.applyLegacyCombat(player);
    }

    /**
     * Handle the respawn of a player.
     * 处理玩家的重生。
     *
     * @param player The player
     *               玩家
     */
    public void handleRespawn(Player player) {
        ProfileService profileService = this.plugin.getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());
        profile.setState(ProfileState.FFA);
        profile.setFfaMatch(this);

        Arena arena = this.getArena();

        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            Profile currentProfile = profileService.getProfile(player.getUniqueId());
            if (!player.isOnline() || currentProfile == null
                    || currentProfile.getFfaMatch() != this) return;
            player.teleport(arena.getPos1());
            this.plugin.getService(KnockbackManager.class).resetHitDelayState(player);

            Kit kit = this.getKit();
            player.getInventory().clear();
            player.getInventory().setArmorContents(kit.getArmor());
            player.getInventory().setContents(kit.getItems());
            player.updateInventory();
            this.applyLegacyCombat(player);
        }, 1L);

        GameFFAPlayer gameFFAPlayer = this.getGameFFAPlayer(player);
        gameFFAPlayer.setState(FFAState.SPAWN);
    }

    /**
     * Handle the death of a player.
     * 处理玩家的死亡。
     *
     * @param player The player who died.
     *               死亡的玩家。
     * @param killer The killer / last attacker of the player who died.
     *               击杀者 / 死亡玩家的最后攻击者。
     */
    @Override
    public void handleDeath(Player player, Player killer) {
        ProfileService profileService = this.plugin.getService(ProfileService.class);
        LocaleService localeService = this.plugin.getService(LocaleService.class);

        if (killer == null) {
            Profile profile = profileService.getProfile(player.getUniqueId());
            ProfileFFAData ffaData = profile.getProfileData().getFfaData().get(this.getKit().getName());
            ffaData.incrementDeaths();
            ffaData.resetKillstreak();

            boolean suicideDeathMessageEnabled = localeService.getBoolean(GameMessagesLocaleImpl.FFA_PLAYER_DIED_MESSAGE_ENABLED_BOOLEAN);
            if (suicideDeathMessageEnabled) {
                for (String line : localeService.getStringList(GameMessagesLocaleImpl.FFA_PLAYER_DIED_MESSAGE_FORMAT)) {
                    this.getPlayers().forEach(ffaPlayer -> ffaPlayer.getPlayer().sendMessage(
                            CC.translate(line
                                    .replace("{name-color}", profile.getNameColor().toString())
                                    .replace("{player}", profile.getName())
                            )
                    ));
                }
            }

            this.handleRespawn(player);
            return;
        }

        Profile killerProfile = profileService.getProfile(killer.getUniqueId());
        ProfileFFAData killerFfaData = killerProfile.getProfileData().getFfaData().get(getKit().getName());
        if (killerFfaData != null) {
            killerFfaData.incrementKills();
            killerFfaData.incrementKillstreak();
            AlleyPlugin.getInstance().getService(ChallengeService.class)
                    .recordProgress(killerProfile, ChallengeType.KILLS, 1);
        }

        AlleyPlugin.getInstance().getService(dev.revere.alley.feature.coin.CoinRewardService.class).rewardFFAKill(killer);

        Profile profile = profileService.getProfile(player.getUniqueId());
        ProfileFFAData ffaData = profile.getProfileData().getFfaData().get(getKit().getName());
        ffaData.incrementDeaths();
        ffaData.resetKillstreak();

        this.plugin.getService(ReflectionService.class).getReflectionService(ActionBarReflectionServiceImpl.class).sendDeathMessage(killer, player);
        this.plugin.getService(CombatService.class).resetCombatLog(player);

        boolean killDeathMessageEnabled = localeService.getBoolean(GameMessagesLocaleImpl.FFA_PLAYER_KILLED_PLAYER_MESSAGE_ENABLED_BOOLEAN);
        if (killDeathMessageEnabled) {
            for (String line : localeService.getStringList(GameMessagesLocaleImpl.FFA_PLAYER_KILLED_PLAYER_MESSAGE_FORMAT)) {
                this.getPlayers().forEach(ffaPlayer -> ffaPlayer.getPlayer().sendMessage(
                        CC.translate(line
                                .replace("{name-color}", profile.getNameColor().toString())
                                .replace("{player}", profile.getName())
                                .replace("{killer-name-color}", killerProfile.getNameColor().toString())
                                .replace("{killer}", killerProfile.getName())
                        )
                ));
            }
        }

        this.sendKillstreakAlertMessage(killer);
        this.handleRespawn(player);
    }
}
