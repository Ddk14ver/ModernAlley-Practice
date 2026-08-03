package dev.revere.alley.feature.match.internal.types;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.InventoryUtil;
import dev.revere.alley.common.ListenerUtil;
import dev.revere.alley.common.PlayerUtil;
import dev.revere.alley.common.elo.EloCalculator;
import dev.revere.alley.common.elo.EloResult;
import dev.revere.alley.common.elo.OldEloResult;
import dev.revere.alley.common.logger.Logger;
import dev.revere.alley.common.reflect.ReflectionService;
import dev.revere.alley.common.reflect.internal.types.TitleReflectionServiceImpl;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.locale.internal.impl.VisualsLocaleImpl;
import dev.revere.alley.core.locale.internal.impl.message.GameMessagesLocaleImpl;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.progress.PlayerProgress;
import dev.revere.alley.core.profile.progress.ProgressService;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.layout.LayoutService;
import dev.revere.alley.feature.coin.CoinRewardService;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.raiding.BaseRaidingService;
import dev.revere.alley.feature.kit.setting.types.mechanic.KitSettingDropItemsImpl;
import dev.revere.alley.feature.kit.setting.types.mode.KitSettingRaiding;
import dev.revere.alley.feature.kit.setting.types.mode.KitSettingRespawnTimer;
import dev.revere.alley.feature.layout.data.LayoutData;
import dev.revere.alley.feature.match.Match;
import dev.revere.alley.feature.match.MatchState;
import dev.revere.alley.feature.match.model.BaseRaiderRole;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.model.MatchGamePlayerData;
import dev.revere.alley.feature.match.model.TeamGameParticipant;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.match.utility.MatchUtility;
import dev.revere.alley.feature.queue.Queue;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * @author Remi
 * @project Alley
 * @date 5/21/2024
 */
@Getter
@Setter
public class DefaultMatch extends Match {
    private GameParticipant<MatchGamePlayer> participantA;
    private GameParticipant<MatchGamePlayer> participantB;

    public final ChatColor teamAColor;
    public final ChatColor teamBColor;

    private GameParticipant<MatchGamePlayer> winner;
    private GameParticipant<MatchGamePlayer> loser;

    /**
     * Constructor for the MatchRegularImpl class.
     * MatchRegularImpl类的构造函数。
     *
     * @param queue        The queue of the match.
     *                     比赛的队列。
     * @param kit          The kit of the match.
     *                     比赛的工具包。
     * @param arena        The arena of the match.
     *                     比赛的竞技场。
     * @param ranked       Whether the match is ranked or not.
     *                     比赛是否为排位赛。
     * @param participantA The first participant.
     *                     第一个参赛方。
     * @param participantB The second participant.
     *                     第二个参赛方。
     */
    public DefaultMatch(Queue queue, Kit kit, Arena arena, boolean ranked, GameParticipant<MatchGamePlayer> participantA, GameParticipant<MatchGamePlayer> participantB) {
        super(queue, kit, arena, ranked);
        this.participantA = participantA;
        this.participantB = participantB;
        this.teamAColor = ChatColor.BLUE;
        this.teamBColor = ChatColor.RED;
    }

    @Override
    public void setupPlayer(Player player) {
        super.setupPlayer(player);
        this.applyColorKit(player);

        Location spawnLocation = this.getParticipantA().containsPlayer(player.getUniqueId()) ? getArena().getPos1() : getArena().getPos2();
        player.teleport(spawnLocation);

        if (this.getKit().isSettingEnabled(KitSettingRaiding.class)) {
            this.determineRolesAndGiveKit(player);
        }
    }

    @Override
    public List<GameParticipant<MatchGamePlayer>> getParticipants() {
        return Arrays.asList(getParticipantA(), getParticipantB());
    }

    @Override
    protected void replaceParticipant(GameParticipant<MatchGamePlayer> old, TeamGameParticipant<MatchGamePlayer> replacement) {
        if (this.participantA == old) {
            this.participantA = replacement;
        } else if (this.participantB == old) {
            this.participantB = replacement;
        }
    }

    /**
     * Get the team color of a participant.
     * 获取参赛方的队伍颜色。
     *
     * @param participant The participant to get the team color of.
     *                    要获取队伍颜色的参赛方。
     * @return The team color of the participant.
     *         参赛方的队伍颜色。
     */
    public ChatColor getTeamColor(GameParticipant<MatchGamePlayer> participant) {
        return participant == this.getParticipantA() ? this.teamAColor : this.teamBColor;
    }

    /**
     * Applies the wool color to the player based on their team.
     * 根据玩家所属队伍为其应用羊毛颜色。
     *
     * @param player The player to apply the wool color to.
     *               要应用羊毛颜色的玩家。
     */
    public void applyColorKit(Player player) {
        GameParticipant<MatchGamePlayer> participant = this.getParticipant(player);
        if (participant == null) {
            return;
        }

        final InventoryUtil.TeamColor colorToApply = (participant == this.getParticipantA())
                ? InventoryUtil.TeamColor.BLUE
                : InventoryUtil.TeamColor.RED;

        participant.getPlayers().stream()
                .map(MatchGamePlayer::getTeamPlayer)
                .filter(p -> p != null && p.isOnline())
                .forEach(teamPlayer -> InventoryUtil.applyTeamColorToInventory(teamPlayer, colorToApply));
    }

    @Override
    protected boolean shouldHandleRegularRespawn(Player player) {
        return !this.getKit().isSettingEnabled(KitSettingRespawnTimer.class);
    }

    @Override
    public void handleRoundEnd() {
        final boolean teamADead = this.getParticipantA().isAllEliminated() || this.getParticipantA().isAllDead();
        final GameParticipant<MatchGamePlayer> winner = teamADead ? this.getParticipantB() : this.getParticipantA();
        final GameParticipant<MatchGamePlayer> loser = teamADead ? this.getParticipantA() : this.getParticipantB();

        this.winner = winner;
        this.loser = loser;

        broadcastMatchOutcome(winner, loser);
        processStatistics(winner, loser);

        if (!this.getSpectators().isEmpty()) {
            this.broadcastAndStopSpectating();
        }

        super.handleRoundEnd();
    }

    /**
     * Handles all player-facing messages at the end of a match, including titles and results.
     * 处理比赛结束时所有面向玩家的消息，包括标题和结果。
     */
    private void broadcastMatchOutcome(GameParticipant<MatchGamePlayer> winner, GameParticipant<MatchGamePlayer> loser) {
        this.sendVictory(winner);
        this.sendDefeat(loser, winner);

        if (this.isTeamMatch()) {
            MatchUtility.sendConjoinedMatchResult(this, winner, loser);
        } else {
            MatchGamePlayer winnerPlayer = winner.getLeader();
            MatchGamePlayer loserPlayer = loser.getLeader();
            MatchUtility.sendMatchResult(
                    this,
                    winnerPlayer.getUsername(),
                    loserPlayer.getUsername(),
                    winnerPlayer.getUuid(),
                    loserPlayer.getUuid()
            );
        }

        // Start MVP music now, then reveal the MVP after the result title has been visible.
        this.announceMVP(30L);
    }

    /**
     * Processes all backend statistics if the match is configured to affect them.
     * 如果比赛配置为影响统计数据，则处理所有后端统计数据。
     */
    private void processStatistics(GameParticipant<MatchGamePlayer> winner, GameParticipant<MatchGamePlayer> loser) {
        if (!this.isAffectStatistics()) {
            return;
        }

        handleMatchData(winner, loser); // 9 wins, increases to 10, as the player won
        // 9场胜利，增加到10场，因为该玩家获胜了

        if (!this.isRanked()) {
            this.sendProgressToWinner(winner.getLeader().getTeamPlayer());
        }
    }

    /**
     * Routes to the correct statistics handling method based on the match type.
     * 根据比赛类型路由到正确的统计数据处理方法。
     *
     * @param winner The winning participant.
     *               获胜的参赛方。
     * @param loser  The losing participant.
     *               失败的参赛方。
     */
    private void handleMatchData(GameParticipant<MatchGamePlayer> winner, GameParticipant<MatchGamePlayer> loser) {
        if (this.isTeamMatch()) {
            return;
        }

        if (this.isRanked()) {
            updateRankedStats(winner, loser);
        } else {
            updateUnrankedStats(winner, loser);
        }
    }

    /**
     * Updates player profiles and Elo for a ranked match.
     * 为排位赛更新玩家资料和Elo分数。
     */
    private void updateRankedStats(GameParticipant<MatchGamePlayer> winner, GameParticipant<MatchGamePlayer> loser) {
        OldEloResult result = this.getOldEloResult(winner, loser);
        EloResult eloResult = this.getEloResult(result.getOldWinnerElo(), result.getOldLoserElo());

        this.handleWinner(eloResult.getNewWinnerElo(), winner);
        this.handleLoser(eloResult.getNewLoserElo(), loser);

        this.sendEloResult(
                winner.getLeader().getTeamPlayer().getName(),
                loser.getLeader().getTeamPlayer().getName(),
                result.getOldWinnerElo(),
                result.getOldLoserElo(),
                eloResult.getNewWinnerElo(),
                eloResult.getNewLoserElo()
        );
    }

    /**
     * Updates player profiles with wins/losses for an unranked match.
     * 为非排位赛更新玩家资料的胜场/败场记录。
     */
    private void updateUnrankedStats(GameParticipant<MatchGamePlayer> winner, GameParticipant<MatchGamePlayer> loser) {
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);

        Profile winnerProfile = profileService.getProfile(winner.getLeader().getUuid());
        var winnerKitData = winnerProfile.getProfileData().getUnrankedKitData().get(getKit().getName());
        if (winnerKitData != null) winnerKitData.incrementWins();
        winnerProfile.getProfileData().incrementUnrankedWins();
        winnerProfile.getProfileData().determineTitles();

        Profile loserProfile = profileService.getProfile(loser.getLeader().getUuid());
        var loserKitData = loserProfile.getProfileData().getUnrankedKitData().get(getKit().getName());
        if (loserKitData != null) loserKitData.incrementLosses();
        loserProfile.getProfileData().incrementUnrankedLosses();

        // --- Coin rewards ---
        CoinRewardService coinReward = AlleyPlugin.getInstance().getService(CoinRewardService.class);
        coinReward.rewardUnrankedWin(winner.getLeader().getTeamPlayer());
        coinReward.rewardUnrankedLoss(loser.getLeader().getTeamPlayer());
    }

    /**
     * Sends the victory title to the winning participant.
     * 向获胜方发送胜利标题。
     *
     * @param winner The winning participant.
     *               获胜的参赛方。
     */
    private void sendVictory(GameParticipant<MatchGamePlayer> winner) {
        LocaleService localeService = this.plugin.getService(LocaleService.class);


        if (localeService.getBoolean(VisualsLocaleImpl.TITLE_MATCH_VICTORY_ENABLED_BOOLEAN)) {
            String header = localeService.getString(VisualsLocaleImpl.TITLE_MATCH_VICTORY_HEADER).replace("{winner}", winner.getLeader().getUsername());
            String footer = localeService.getString(VisualsLocaleImpl.TITLE_MATCH_VICTORY_FOOTER).replace("{winner}", winner.getLeader().getUsername());

            int fadeIn = localeService.getInt(VisualsLocaleImpl.TITLE_MATCH_VICTORY_FADE_IN);
            int stay = localeService.getInt(VisualsLocaleImpl.TITLE_MATCH_VICTORY_STAY);
            int fadeOut = localeService.getInt(VisualsLocaleImpl.TITLE_MATCH_VICTORY_FADEOUT);

            winner.getPlayers().forEach(matchGamePlayer -> {
                Player player = this.plugin.getServer().getPlayer(matchGamePlayer.getUuid());
                if (player != null && player.isOnline()) {
                    sendResultTitle(player, header, footer, fadeIn, stay, fadeOut);
                }
            });
        }
    }

    /**
     * Sends the defeat title to the losing participant.
     * 向失败方发送失败标题。
     *
     * @param loser The losing participant.
     *              失败的参赛方。
     */
    private void sendDefeat(GameParticipant<MatchGamePlayer> loser, GameParticipant<MatchGamePlayer> winner) {
        LocaleService localeService = this.plugin.getService(LocaleService.class);

        if (localeService.getBoolean(VisualsLocaleImpl.TITLE_MATCH_DEFEAT_ENABLED_BOOLEAN)) {
            String header = localeService.getString(VisualsLocaleImpl.TITLE_MATCH_DEFEAT_HEADER).replace("{winner}", winner.getLeader().getUsername());
            String footer = localeService.getString(VisualsLocaleImpl.TITLE_MATCH_DEFEAT_FOOTER).replace("{winner}", winner.getLeader().getUsername());

            int fadeIn = localeService.getInt(VisualsLocaleImpl.TITLE_MATCH_DEFEAT_FADE_IN);
            int stay = localeService.getInt(VisualsLocaleImpl.TITLE_MATCH_DEFEAT_STAY);
            int fadeOut = localeService.getInt(VisualsLocaleImpl.TITLE_MATCH_DEFEAT_FADEOUT);

            loser.getPlayers().forEach(matchGamePlayer -> {
                Player player = this.plugin.getServer().getPlayer(matchGamePlayer.getUuid());
                if (player != null && player.isOnline()) {
                    sendResultTitle(player, header, footer, fadeIn, stay, fadeOut);
                }
            });
        }
    }

    private void sendResultTitle(Player player, String header, String footer,
                                 int fadeIn, int stay, int fadeOut) {
        Runnable sendTitle = () -> {
            if (!player.isOnline()) return;
            this.plugin.getService(ReflectionService.class)
                    .getReflectionService(TitleReflectionServiceImpl.class)
                    .sendTitle(player, header, footer, fadeIn, stay, fadeOut);
        };

        if (player.isDead()) {
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, sendTitle, 3L);
        } else {
            sendTitle.run();
        }
    }

    /**
     * Sends the elo result message.
     * 发送Elo分数结果消息。
     *
     * @param winnerName   The name of the winner.
     *                     获胜者的名称。
     * @param loserName    The name of the loser.
     *                     失败者的名称。
     * @param oldEloWinner The old elo of the winner.
     *                     获胜者的旧Elo分数。
     * @param oldEloLoser  The old elo of the loser.
     *                     失败者的旧Elo分数。
     * @param newEloWinner The new elo of the winner.
     *                     获胜者的新Elo分数。
     * @param newEloLoser  The new elo of the loser.
     *                     失败者的新Elo分数。
     */
    public void sendEloResult(String winnerName, String loserName, int oldEloWinner, int oldEloLoser, int newEloWinner, int newEloLoser) {
        LocaleService localeService = this.plugin.getService(LocaleService.class);
        if (localeService.getBoolean(GameMessagesLocaleImpl.MATCH_ENDED_MATCH_RESULT_ELO_CHANGES_ENABLED_BOOLEAN)) {
            List<String> list = localeService.getStringList(GameMessagesLocaleImpl.MATCH_ENDED_MATCH_RESULT_ELO_CHANGES_FORMAT);

            list.replaceAll(string -> string
                    .replace("{winner}", winnerName)
                    .replace("{loser}", loserName)
                    .replace("{old-winner-elo}", String.valueOf(oldEloWinner))
                    .replace("{old-loser-elo}", String.valueOf(oldEloLoser))
                    .replace("{new-winner-elo}", String.valueOf(newEloWinner))
                    .replace("{new-loser-elo}", String.valueOf(newEloLoser))
                    .replace("{math-winner-elo}", String.valueOf(Math.abs(oldEloWinner - newEloWinner)))
                    .replace("{math-loser-elo}", String.valueOf(Math.abs(oldEloLoser - newEloLoser)))
            );

            list.forEach(this::notifyParticipants);
        }

    }

    /**
     * Sends the progress of the winner to the player using the ProgressService.
     * 使用ProgressService向玩家发送获胜者的进度信息。
     * The method no longer needs Division or Tier passed in.
     * 该方法不再需要传入Division或Tier。
     *
     * @param winner The winning player.
     *               获胜的玩家。
     */
    public void sendProgressToWinner(Player winner) {

        /*
         * TODO: Fix this retarded calculation its pmo
         * 待办：修复这个智障的计算逻辑，太烦人了
         *  "next thing i know half the core is f---ed" - Titanic Swim Team, Remi (13/07/2025 - 00:37)
         *  "转眼间半个核心就完蛋了" - Titanic Swim Team, Remi (13/07/2025 - 00:37)
         */

        Profile winnerProfile = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(winner.getUniqueId());
        PlayerProgress progress = AlleyPlugin.getInstance().getService(ProgressService.class).calculateProgress(winnerProfile, this.getKit().getName());

        String progressLine;

        if (progress.isMaxRank() && progress.getCurrentWins() >= progress.getWinsForNextTier()) {
            progressLine = " &6&l● &fCONGRATULATIONS! You have reached the maximum rank!";
        } else {
            progressLine = String.format(" &6&l● &fUnlock &6%s &fwith %d more %s!",
                    progress.getNextRankName(),
                    progress.getWinsRequired(),
                    progress.getWinOrWins()
            );
        }

//        Arrays.asList(
//                "&6&lProgress",
//                progressLine,
//                "  &7(" + progress.getProgressBar(12, "■") + "&7) " + progress.getProgressPercentage(),
//                " &6&l● &fDaily Streak: &6" + "N/A" + " &f(Best: " + "N/A" + ")",
//                " &6&l● &fWin Streak: &6" + "N/A" + " &f(Best: " + "N/A" + ")",
//                ""
//        ).forEach(line -> winner.sendMessage(CC.translate(line)));

//        LocaleService localeService = this.plugin.getService(LocaleService.class);
//        if (!localeService.getBoolean(GameMessagesLocaleImpl.MATCH_DIVISION_PROGRESS_ENABLED_BOOLEAN)) {
//            return;
//        }
//
//        Profile winnerProfile = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(winner.getUniqueId());
//        PlayerProgress progress = AlleyPlugin.getInstance().getService(ProgressService.class).calculateProgress(winnerProfile, this.getKit().getName());
//
//        List<String> message;
//
//        DivisionTier reachedTier = AlleyPlugin.getInstance().getService(DivisionService.class).getDivisions().stream()
//                .flatMap(div -> div.getTiers().stream())
//                .filter(tier -> tier.getRequiredWins() == progress.getCurrentWins())
//                .findFirst()
//                .orElse(null);
//
//        if (reachedTier != null) {
//            message = localeService.getMessageList(GameMessagesLocaleImpl.MATCH_DIVISION_PROGRESS_REACHED_FORMAT)
//                    .stream()
//                    .map(line -> line.replace("{reached-new-division}", progress.getNextRankName() + " " + reachedTier.getName()))
//                    .collect(Collectors.toList());
//        } else {
//            message = localeService.getMessageList(GameMessagesLocaleImpl.MATCH_DIVISION_PROGRESS_ONGOING_FORMAT);
//        }
//
//        message.replaceAll(string -> string
//                .replace("{next-division}", Objects.requireNonNull(reachedTier).getName())
//                .replace("{wins-required}", String.valueOf(progress.getWinsRequired()))
//                .replace("{win-or-wins}", progress.getWinOrWins())
//                .replace("{progress-bar}", progress.getProgressBar(12, "■"))
//                .replace("{progress-percentage}", progress.getProgressPercentage())
//                .replace("{daily-streak}", "N/A")
//                .replace("{best-daily-streak}", "N/A")
//                .replace("{win-streak}", String.valueOf(winnerProfile.getProfileData().getUnrankedKitData().get(this.getKit().getName()).getWinstreak()))
//                .replace("{best-win-streak}", "N/A")
//        );
//
//        message.forEach(line -> winner.sendMessage(CC.translate(line)));
    }


    /**
     * Method to get the old elo result.
     * 获取旧Elo结果的方法。
     *
     * @return The old elo result.
     *         旧Elo结果。
     */
    public @NotNull OldEloResult getOldEloResult(GameParticipant<MatchGamePlayer> winner, GameParticipant<MatchGamePlayer> loser) {
        int oldWinnerElo = winner.getLeader().getElo();
        int oldLoserElo = loser.getLeader().getElo();
        return new OldEloResult(oldWinnerElo, oldLoserElo);
    }

    /**
     * Method to get the elo result.
     * 获取Elo结果的方法。
     *
     * @param oldWinnerElo The old elo of the winner.
     *                     获胜者的旧Elo分数。
     * @param oldLoserElo  The old elo of the loser.
     *                     失败者的旧Elo分数。
     * @return The elo result.
     *         Elo结果。
     */
    public @NotNull EloResult getEloResult(int oldWinnerElo, int oldLoserElo) {
        EloCalculator eloCalculator = AlleyPlugin.getInstance().getService(EloCalculator.class);
        int newWinnerElo = eloCalculator.determineNewElo(oldWinnerElo, oldLoserElo, true);
        int newLoserElo = eloCalculator.determineNewElo(oldLoserElo, oldWinnerElo, false);
        return new EloResult(newWinnerElo, newLoserElo);
    }

    /**
     * Method to handle the winner.
     * 处理获胜者的方法。
     *
     * @param elo    The new elo of the winner.
     *               获胜者的新Elo分数。
     * @param winner The winner of the match.
     *               比赛的获胜者。
     */
    public void handleWinner(int elo, GameParticipant<MatchGamePlayer> winner) {
        Profile winnerProfile = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(winner.getLeader().getUuid());
        var winnerKitData = winnerProfile.getProfileData().getRankedKitData().get(getKit().getName());
        if (winnerKitData != null) {
            winnerKitData.setElo(elo);
            winnerKitData.incrementWins();
        }
        winnerProfile.getProfileData().incrementRankedWins();
        winnerProfile.getProfileData().updateElo(winnerProfile);

        AlleyPlugin.getInstance().getService(CoinRewardService.class).rewardRankedWin(winner.getLeader().getTeamPlayer());
    }

    /**
     * Method to handle the loser.
     * 处理失败者的方法。
     *
     * @param elo   The new elo of the loser.
     *              失败者的新Elo分数。
     * @param loser The loser of the match.
     *              比赛的失败者。
     */
    public void handleLoser(int elo, GameParticipant<MatchGamePlayer> loser) {
        Profile loserProfile = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(loser.getLeader().getUuid());
        var loserKitData = loserProfile.getProfileData().getRankedKitData().get(getKit().getName());
        if (loserKitData != null) {
            loserKitData.setElo(elo);
            loserKitData.incrementLosses();
        }
        loserProfile.getProfileData().incrementRankedLosses();
        loserProfile.getProfileData().updateElo(loserProfile);

        AlleyPlugin.getInstance().getService(CoinRewardService.class).rewardRankedLoss(loser.getLeader().getTeamPlayer());
    }

    @Override
    public boolean canStartRound() {
        return false;
    }

    @Override
    public boolean canEndRound() {
        return (this.getParticipantA().isAllDead() || this.getParticipantB().isAllDead())
                || (this.getParticipantA().getAllPlayers().stream().allMatch(MatchGamePlayer::isDisconnected)
                || this.getParticipantB().getAllPlayers().stream().allMatch(MatchGamePlayer::isDisconnected));
    }

    @Override
    public boolean canEndMatch() {
        return true;
    }

    @Override
    public void sendPlayerVersusPlayerMessage() {
        LocaleService localeService = this.plugin.getService(LocaleService.class);

        GameParticipant<MatchGamePlayer> participantA = this.getParticipants().get(0);
        GameParticipant<MatchGamePlayer> participantB = this.getParticipants().get(1);

        if (this.isTeamMatch()) {
            if (localeService.getBoolean(GameMessagesLocaleImpl.MATCH_PLAYER_VS_PLAYER_TEAM_ENABLED_BOOLEAN)) {
                int teamSizeA = participantA.getPlayerSize();
                int teamSizeB = participantB.getPlayerSize();

                List<String> message = localeService.getStringList(GameMessagesLocaleImpl.MATCH_PLAYER_VS_PLAYER_TEAM_FORMAT);
                for (String line : message) {
                    String formatted = line
                            .replace("{teamA-leader}", participantA.getLeader().getUsername())
                            .replace("{teamA-size}", String.valueOf(teamSizeA))
                            .replace("{teamB-leader}", participantB.getLeader().getUsername())
                            .replace("{teamB-size}", String.valueOf(teamSizeB));
                    this.sendMessage(formatted);
                }
            }
        } else {
            if (localeService.getBoolean(GameMessagesLocaleImpl.MATCH_PLAYER_VS_PLAYER_SOLO_ENABLED_BOOLEAN)) {
                List<String> message = localeService.getStringList(GameMessagesLocaleImpl.MATCH_PLAYER_VS_PLAYER_SOLO_FORMAT);
                for (String line : message) {
                    String formatted = line
                            .replace("{playerA}", participantA.getLeader().getUsername())
                            .replace("{playerB}", participantB.getLeader().getUsername());
                    this.sendMessage(formatted);
                }
            }
        }
    }

    @Override
    public void handleRespawn(Player player) {
        player.spigot().respawn();
        PlayerUtil.reset(player, false, true);
    }

    @Override
    public void handleDeathItemDrop(Player player, PlayerDeathEvent event) {
        if (this.getKit().isSettingEnabled(KitSettingDropItemsImpl.class)) {
            ListenerUtil.clearDroppedItemsOnDeath(event, player);
        } else {
            event.getDrops().clear();
        }
    }

    @Override
    public void handleDisconnect(Player player) {
        if (!(this.getState() == MatchState.STARTING || this.getState() == MatchState.RUNNING)) {
            return;
        }

        Profile profile = this.plugin.getService(ProfileService.class).getProfile(player.getUniqueId());
        this.sendMessage(profile.getFancyName() + " &fdisconnected.");

        MatchGamePlayer gamePlayer = this.getFromAllGamePlayers(player);
        if (gamePlayer != null) {
            gamePlayer.setDisconnected(true);
            gamePlayer.setEliminated(true);
            if (!gamePlayer.isDead()) {
                this.handleDeath(player, EntityDamageEvent.DamageCause.CUSTOM);
            }
        }

        if (player.isOnline()) {
            this.finalizePlayer(player);
        }
    }

    /**
     * Gives the base raiding kit to the player based on their team.
     * 根据玩家所属队伍为其发放基础掠夺工具包。
     *
     * @param player The player to give the kit to.
     *               要接收工具包的玩家。
     */
    public void determineRolesAndGiveKit(Player player) {
        if (this.getParticipantA() == null || this.getParticipantB() == null) {
            return;
        }

        Kit parentKit = this.getKit();
        if (parentKit == null) {
            Logger.error("&cCould not determine the parent kit for the raiding match.");
            return;
        }

        BaseRaiderRole role = getParticipantA().containsPlayer(player.getUniqueId())
                ? BaseRaiderRole.TRAPPER
                : BaseRaiderRole.RAIDER;

        Kit kitToGive = AlleyPlugin.getInstance().getService(BaseRaidingService.class).getRaidingKitByRole(parentKit, role);
        if (kitToGive == null) {
            Logger.info("&cNo kit found for role: " + role.name() + " linked to parent kit.");
            return;
        }

        MatchGamePlayerData data = this.getGamePlayer(player).getData();
        data.setRole(role);

        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());
        java.util.List<LayoutData> nonNullLayouts = profile.getProfileData()
                .getLayoutData().getNonNullLayouts(kitToGive.getName());

        if (nonNullLayouts.size() > 1) {
            AlleyPlugin.getInstance().getService(LayoutService.class).giveBooks(player, kitToGive.getName());
        } else if (nonNullLayouts.size() == 1) {
            player.getInventory().setContents(nonNullLayouts.get(0).getItems());
        } else {
            player.getInventory().setContents(kitToGive.getItems());
        }

        player.getInventory().setArmorContents(kitToGive.getArmor());
        player.updateInventory();
    }
}
