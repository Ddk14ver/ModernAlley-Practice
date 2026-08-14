package dev.revere.alley.feature.coin;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.bootstrap.AlleyContext;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

/**
 * @author Ddk1
 * @project Alley
 * @since 03/07/2025
 *
 * Reads coin reward config from settings.yml and handles rewarding players.
 * 从settings.yml读取金币奖励配置并处理玩家奖励。
 */
@Service(provides = CoinRewardService.class, priority = 300)
public class CoinRewardService implements dev.revere.alley.bootstrap.lifecycle.Service {
    private int unrankedWin, unrankedLoss;
    private int rankedWin, rankedLoss;
    private int ffaKill;
    private int tournamentWin, tournamentLoss;

    @Override
    public void initialize(AlleyContext context) {
        loadConfig();
    }

    @Override
    public void shutdown(AlleyContext context) {}

    private void loadConfig() {
        File configFile = new File(AlleyPlugin.getInstance().getDataFolder(), "settings.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        String path = "coin-rewards.";
        this.unrankedWin = config.getInt(path + "unranked.win", 30);
        this.unrankedLoss = config.getInt(path + "unranked.loss", 10);
        this.rankedWin = config.getInt(path + "ranked.win", 50);
        this.rankedLoss = config.getInt(path + "ranked.loss", 15);
        this.ffaKill = config.getInt(path + "ffa.kill", 10);
        this.tournamentWin = config.getInt(path + "tournament.win", 50);
        this.tournamentLoss = config.getInt(path + "tournament.loss", 15);
    }

    public void rewardUnrankedWin(Player player) {
        if (unrankedWin <= 0) return;
        addCoins(player, unrankedWin);
        sendMessage(player, "+" + unrankedWin + " coins", "&aUnranked Win");
    }

    public void rewardUnrankedLoss(Player player) {
        if (unrankedLoss <= 0) return;
        addCoins(player, unrankedLoss);
        sendMessage(player, "+" + unrankedLoss + " coins", "&7Unranked Loss");
    }

    public void rewardRankedWin(Player player) {
        if (rankedWin <= 0) return;
        addCoins(player, rankedWin);
        sendMessage(player, "+" + rankedWin + " coins", "&eRanked Win");
    }

    public void rewardRankedLoss(Player player) {
        if (rankedLoss <= 0) return;
        addCoins(player, rankedLoss);
        sendMessage(player, "+" + rankedLoss + " coins", "&7Ranked Loss");
    }

    public void rewardFFAKill(Player player) {
        if (ffaKill <= 0) return;
        addCoins(player, ffaKill);
        sendMessage(player, "+" + ffaKill + " coins", "&cFFA Kill");
    }

    public void rewardTournamentWin(Player player) {
        if (tournamentWin <= 0) return;
        addCoins(player, tournamentWin);
        sendMessage(player, "+" + tournamentWin + " coins", "&6Tournament Win");
    }

    public void rewardTournamentLoss(Player player) {
        if (tournamentLoss <= 0) return;
        addCoins(player, tournamentLoss);
        sendMessage(player, "+" + tournamentLoss + " coins", "&7Tournament Loss");
    }

    /** Adds a challenge reward without sending the generic match reward message. */
    public void rewardChallenge(Player player, int amount) {
        if (amount < 0) return;
        addCoins(player, amount);
    }

    private void addCoins(Player player, int amount) {
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());
        if (profile != null) {
            profile.getProfileData().incrementCoins(amount);
        }
    }

    private void sendMessage(Player player, String amount, String reason) {
        player.sendMessage(CC.translate("&6" + amount + " &7| " + reason));
    }
}
