package dev.revere.alley.feature.queue.internal;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.arena.ArenaService;
import dev.revere.alley.feature.arena.ArenaType;
import dev.revere.alley.feature.queue.QueueService;
import dev.revere.alley.feature.queue.Queue;
import dev.revere.alley.feature.queue.QueueProfile;
import dev.revere.alley.feature.match.MatchService;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.model.TeamGameParticipant;
import dev.revere.alley.feature.party.PartyService;
import dev.revere.alley.feature.party.Party;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.common.logger.Logger;
import dev.revere.alley.common.text.CC;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Remi
 * @project Alley
 * @date 5/21/2024
 */
@RequiredArgsConstructor
public class QueueTask implements Runnable {
    /**
     * Main execution method that processes all active queues.
     * 主执行方法，处理所有活跃队列。
     * Called periodically by the scheduler.
     * 由调度器周期性调用。
     */
    @Override
    public void run() {
        QueueService queueService = AlleyPlugin.getInstance().getService(QueueService.class);
        queueService.getQueues().forEach(this::processQueue);
    }

    /**
     * Processes a single queue by validating players, handling timeouts,
     * 处理单个队列，包括验证玩家、处理超时，
     * and attempting to create matches.
     * 并尝试创建比赛。
     *
     * @param queue The queue to process
     *              要处理的队列
     */
    public void processQueue(Queue queue) {
        validateAndCleanupQueuePlayers(queue);

        if (queue.isDuos()) {
            processDuosQueue(queue);
        } else {
            processSoloQueue(queue);
        }
    }

    /**
     * Validates all players in the queue and removes those who are offline,
     * 验证队列中的所有玩家，移除离线、
     * have changed state, or have exceeded the maximum queue time.
     * 状态变更或超过最大排队时间的玩家。
     *
     * @param queue The queue to validate and cleanup
     *              要验证和清理的队列
     */
    private void validateAndCleanupQueuePlayers(Queue queue) {
        List<QueueProfile> profilesToCheck = new ArrayList<>(queue.getProfiles());

        for (QueueProfile profile : profilesToCheck) {
            Player player = Bukkit.getPlayer(profile.getUuid());

            if (shouldRemovePlayerFromQueue(player, profile)) {
                queue.removePlayer(profile);
                notifyPlayerOfQueueRemoval(player, profile);
            } else if (profile.isReady()) {
                profile.queueRange(player);
            }
        }
    }

    /**
     * Determines if a player should be removed from the queue based on
     * 根据玩家的在线状态、玩家状态和排队时间，
     * their online status, profile state, and queue time.
     * 判断是否应将玩家移出队列。
     *
     * @param player  The player to check
     *                要检查的玩家
     * @param profile The player's queue profile
     *                玩家的队列数据
     * @return true if the player should be removed from the queue
     *         如果应将玩家移出队列则返回 true
     */
    private boolean shouldRemovePlayerFromQueue(Player player, QueueProfile profile) {
        if (player == null || !player.isOnline()) {
            return true;
        }

        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);

        Profile playerProfile = profileService.getProfile(profile.getUuid());
        if (!profile.isReady()) {
            return playerProfile == null
                    || playerProfile.getQueueProfile() != profile
                    || (playerProfile.getState() != ProfileState.PLAYING
                    && playerProfile.getState() != ProfileState.LOBBY);
        }
        if (!playerProfile.getState().equals(ProfileState.WAITING)) {
            return true;
        }

        return profile.getElapsedTime() >= profile.getQueue().getMaxQueueTime();
    }

    /**
     * Sends appropriate notification message to a player being removed from queue.
     * 向被移出队列的玩家发送适当的通知消息。
     *
     * @param player  The player being removed
     *                被移出队列的玩家
     * @param profile The player's queue profile
     *                玩家的队列数据
     */
    private void notifyPlayerOfQueueRemoval(Player player, QueueProfile profile) {
        if (player != null) {
            if (profile.getElapsedTime() >= profile.getQueue().getMaxQueueTime()) {
                player.sendMessage(CC.translate("&cYou have been removed from the queue due to inactivity"));
            } else {
                player.sendMessage(CC.translate("&cYou have been removed from the queue due to being offline or state change."));
            }
        }
    }

    /**
     * Processes solo queue matchmaking by finding two available solo players
     * 处理单人队列的匹配，寻找两名可用的单人玩家，
     * and creating a 1v1 match between them.
     * 并在他们之间创建 1v1 比赛。
     *
     * @param queue The solo queue to process
     *              要处理的单人队列
     */
    private void processSoloQueue(Queue queue) {
        if (queue.getProfiles().size() < 2) {
            return;
        }

        List<QueueProfile> availableSoloPlayers = getAvailableSoloPlayers(queue);

        if (availableSoloPlayers.size() < 2) {
            return;
        }

        attemptSoloMatches(queue, availableSoloPlayers);
    }

    /**
     * Gets a list of available solo players (not in parties) sorted by queue time.
     * 获取可用的单人玩家列表（不在队伍中），按排队时间排序。
     *
     * @param queue The queue to get solo players from
     *              要从中获取单人玩家的队列
     * @return List of available solo players
     *         可用的单人玩家列表
     */
    private List<QueueProfile> getAvailableSoloPlayers(Queue queue) {
        PartyService partyService = AlleyPlugin.getInstance().getService(PartyService.class);

        return queue.getProfiles().stream()
                .filter(QueueProfile::isReady)
                .filter(queueProfile -> partyService.getParty(Bukkit.getPlayer(queueProfile.getUuid())) == null)
                .sorted(Comparator.comparingLong(QueueProfile::getElapsedTime))
                .collect(Collectors.toList());
    }

    /**
     * Attempts to create matches between available solo players.
     * 尝试在可用的单人玩家之间创建比赛。
     *
     * @param queue                The queue being processed
     *                             正在处理的队列
     * @param availableSoloPlayers List of available solo players
     *                             可用的单人玩家列表
     */
    private void attemptSoloMatches(Queue queue, List<QueueProfile> availableSoloPlayers) {
        for (int i = 0; i < availableSoloPlayers.size(); i++) {
            QueueProfile firstProfile = availableSoloPlayers.get(i);
            Player firstPlayer = Bukkit.getPlayer(firstProfile.getUuid());

            if (isPlayerInvalidForMatch(firstPlayer, firstProfile, queue)) {
                continue;
            }

            for (int j = i + 1; j < availableSoloPlayers.size(); j++) {
                QueueProfile secondProfile = availableSoloPlayers.get(j);
                Player secondPlayer = Bukkit.getPlayer(secondProfile.getUuid());

                if (isPlayerInvalidForMatch(secondPlayer, secondProfile, queue)) {
                    continue;
                }

                if (createSoloMatch(queue, firstPlayer, secondPlayer, firstProfile, secondProfile)) {
                    return;
                }
            }
        }
    }

    /**
     * Validates if a player is ready for match creation.
     * 验证玩家是否已准备好创建比赛。
     *
     * @param player  The player to validate
     *                要验证的玩家
     * @param profile The player's queue profile
     *                玩家的队列数据
     * @param queue   The queue being processed
     *                正在处理的队列
     * @return true if the player is valid for match creation
     *         如果玩家可以创建比赛则返回 true
     */
    private boolean isPlayerInvalidForMatch(Player player, QueueProfile profile, Queue queue) {
        if (player == null || !player.isOnline()) {
            queue.removePlayer(profile);
            return true;
        }

        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);

        Profile playerProfile = profileService.getProfile(profile.getUuid());
        if (!profile.isReady() || playerProfile == null
                || !playerProfile.getState().equals(ProfileState.WAITING)) {
            queue.removePlayer(profile);
            return true;
        }

        return false;
    }

    /**
     * Creates a solo match between two players.
     * 在两名单人玩家之间创建比赛。
     *
     * @param queue         The queue
     *                      队列
     * @param firstPlayer   First player
     *                      第一名玩家
     * @param secondPlayer  Second player
     *                      第二名玩家
     * @param firstProfile  First player's profile
     *                      第一名玩家的队列数据
     * @param secondProfile Second player's profile
     *                      第二名玩家的队列数据
     * @return true if match was successfully created
     *         如果比赛成功创建则返回 true
     */
    private boolean createSoloMatch(Queue queue, Player firstPlayer, Player secondPlayer, QueueProfile firstProfile, QueueProfile secondProfile) {
        GamePlayerList gamePlayerList = getGamePlayerList(firstPlayer, secondPlayer, firstProfile, secondProfile);
        GameParticipantList gameParticipantList = getSoloGameParticipantList(gamePlayerList);

        if (!isPingCompatible(firstPlayer, secondPlayer)) {
            return false;
        }

        Arena arena = this.getArena(queue);
        if (!isArenaAvailable(arena, Arrays.asList(firstPlayer, secondPlayer), queue)) {
            return false;
        }

        MatchService matchService = AlleyPlugin.getInstance().getService(MatchService.class);
        matchService.createAndStartMatch(
                queue.getKit(), arena, gameParticipantList.participantA, gameParticipantList.participantB,
                false, true, queue.isRanked()
        );

        this.clearQueueProfiles(queue, Arrays.asList(firstProfile.getUuid(), secondProfile.getUuid()), false);
        return true;
    }

    /**
     * Processes duos queue matchmaking by attempting different team compositions:
     * 处理双人队列的匹配，尝试不同的队伍组合：
     * 1. Two full parties (2v2)
     *    两个满编队伍 (2v2)
     * 2. One full party vs two solo players
     *    一个满编队伍 vs 两名单人玩家
     * 3. Four solo players
     *    四名单人玩家
     *
     * @param queue The duos queue to process
     *              要处理的双人队列
     */
    private void processDuosQueue(Queue queue) {
        if (queue.getProfiles().size() < 2 && queue.getTotalPlayerCount() < 4) {
            return;
        }

        List<QueueProfile> availableProfiles = new ArrayList<>(queue.getProfiles());
        availableProfiles.sort(Comparator.comparingLong(QueueProfile::getElapsedTime));

        List<QueueProfile> fullParties = getFullParties(availableProfiles);
        List<QueueProfile> soloDuosPlayers = getSoloDuosPlayers(availableProfiles);

        attemptDuosMatching(queue, fullParties, soloDuosPlayers);
    }

    /**
     * Gets profiles representing full parties (2 members) from available profiles.
     * 从可用的玩家数据中获取代表满编队伍（2 名成员）的数据。
     *
     * @param availableProfiles List of available profiles
     *                          可用的玩家数据列表
     * @return List of profiles representing full parties
     *         代表满编队伍的玩家数据列表
     */
    private List<QueueProfile> getFullParties(List<QueueProfile> availableProfiles) {
        PartyService partyService = AlleyPlugin.getInstance().getService(PartyService.class);
        return availableProfiles.stream()
                .filter(qp -> {
                    Player leader = Bukkit.getPlayer(qp.getUuid());
                    if (leader == null) return false;
                    Party party = partyService.getPartyByLeader(leader);
                    return party != null && party.getMembers().size() == 2;
                })
                .collect(Collectors.toList());
    }

    /**
     * Gets profiles representing solo players or single-member parties for duos queue.
     * 获取代表双人队列中单人玩家或单成员队伍的数据。
     *
     * @param availableProfiles List of available profiles
     *                          可用的玩家数据列表
     * @return List of solo duos players
     *         双人队列的单人玩家列表
     */
    private List<QueueProfile> getSoloDuosPlayers(List<QueueProfile> availableProfiles) {
        PartyService partyService = AlleyPlugin.getInstance().getService(PartyService.class);
        return availableProfiles.stream()
                .filter(qp -> {
                    Player player = Bukkit.getPlayer(qp.getUuid());
                    if (player == null) return false;
                    Party party = partyService.getPartyByLeader(player);
                    return party == null || party.getMembers().size() == 1;
                })
                .collect(Collectors.toList());
    }

    /**
     * Attempts various duos matching strategies in order of preference.
     * 按优先级顺序尝试各种双人匹配策略。
     *
     * @param queue           The duos queue
     *                        双人队列
     * @param fullParties     List of full parties
     *                        满编队伍列表
     * @param soloDuosPlayers List of solo duos players
     *                        双人队列的单人玩家列表
     */
    private void attemptDuosMatching(Queue queue, List<QueueProfile> fullParties, List<QueueProfile> soloDuosPlayers) {
        // Strategy 1: Match two full parties (2v2)
        // 策略 1：匹配两个满编队伍 (2v2)
        if (fullParties.size() >= 2) {
            QueueProfile team1LeaderProfile = fullParties.get(0);
            QueueProfile team2LeaderProfile = fullParties.get(1);

            if (tryMatchDuos(queue, team1LeaderProfile, team2LeaderProfile)) {
                return;
            }
        }

        // Strategy 2: Match one full party with two solo-duo players
        // 策略 2：匹配一个满编队伍与两名双人队列单人玩家
        if (!fullParties.isEmpty() && soloDuosPlayers.size() >= 2) {
            QueueProfile partyLeaderProfile = fullParties.get(0);
            QueueProfile soloDuo1Profile = soloDuosPlayers.get(0);
            QueueProfile soloDuo2Profile = soloDuosPlayers.get(1);

            if (tryMatchDuos(queue, partyLeaderProfile, soloDuo1Profile, soloDuo2Profile)) {
                return;
            }
        }

        // Strategy 3: Match four solo-duo players
        // 策略 3：匹配四名双人队列单人玩家
        if (soloDuosPlayers.size() >= 4) {
            QueueProfile soloDuo1Profile = soloDuosPlayers.get(0);
            QueueProfile soloDuo2Profile = soloDuosPlayers.get(1);
            QueueProfile soloDuo3Profile = soloDuosPlayers.get(2);
            QueueProfile soloDuo4Profile = soloDuosPlayers.get(3);

            tryMatchDuos(queue, soloDuo1Profile, soloDuo2Profile, soloDuo3Profile, soloDuo4Profile);
        }
    }

    /**
     * Attempts to create a duos match with the given potential players.
     * 尝试使用给定的潜在玩家创建双人比赛。
     * Supports 2-4 potential players for different team compositions.
     * 支持 2-4 名潜在玩家组成不同的队伍组合。
     *
     * @param queue            The duos queue
     *                         双人队列
     * @param potentialPlayers Array of potential players for the match
     *                         比赛潜在玩家的数组
     * @return true if match was successfully created
     *         如果比赛成功创建则返回 true
     */
    private boolean tryMatchDuos(Queue queue, QueueProfile... potentialPlayers) {
        if (potentialPlayers.length < 2 || potentialPlayers.length > 4) {
            Logger.error("Invalid number of potential players for tryMatchDuos: " + potentialPlayers.length);
            return false;
        }

        List<Player> onlinePlayers = new ArrayList<>();
        List<QueueProfile> validQueueProfiles = new ArrayList<>();

        if (!validatePotentialPlayers(potentialPlayers, onlinePlayers, validQueueProfiles)) {
            return false;
        }

        List<Player> allMatchPlayers = buildMatchPlayerList(onlinePlayers, potentialPlayers.length);

        if (allMatchPlayers.size() != 4) {
            Logger.info("Expected exactly 4 players for duos match, but got: " + allMatchPlayers.size());
            return false;
        }

        return createDuosMatch(queue, allMatchPlayers, validQueueProfiles, onlinePlayers, potentialPlayers.length);
    }

    /**
     * Validates that all potential players are online and ready for match creation.
     * 验证所有潜在玩家都在线并准备好创建比赛。
     *
     * @param potentialPlayers   Array of potential players
     *                           潜在玩家数组
     * @param onlinePlayers      Output list for validated online players
     *                           已验证在线玩家的输出列表
     * @param validQueueProfiles Output list for validated queue profiles
     *                           已验证队列数据的输出列表
     * @return true if all players are valid
     *         如果所有玩家都有效则返回 true
     */
    private boolean validatePotentialPlayers(QueueProfile[] potentialPlayers, List<Player> onlinePlayers, List<QueueProfile> validQueueProfiles) {
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        for (QueueProfile queueProfile : potentialPlayers) {
            Player player = Bukkit.getPlayer(queueProfile.getUuid());
            if (player == null || !player.isOnline() || !profileService.getProfile(player.getUniqueId()).getState().equals(ProfileState.WAITING)) {
                Logger.info("One of the potential players is not ready: " + queueProfile.getUuid());
                return false;
            }
            onlinePlayers.add(player);
            validQueueProfiles.add(queueProfile);
        }
        return true;
    }

    /**
     * Builds the complete list of players for the duos match by including
     * 构建双人比赛的完整玩家列表，包括队伍成员并处理不同的队伍组合。
     * party members and handling different team compositions.
     *
     * @param onlinePlayers        List of validated online players
     *                             已验证的在线玩家列表
     * @param potentialPlayerCount Number of potential players
     *                             潜在玩家数量
     * @return Complete list of match players
     *         完整的比赛玩家列表
     */
    private List<Player> buildMatchPlayerList(List<Player> onlinePlayers, int potentialPlayerCount) {
        List<Player> allMatchPlayers = new ArrayList<>();

        Player team1Leader = onlinePlayers.get(0);
        allMatchPlayers.add(team1Leader);

        // Add team 1 party members
        // 添加第一队的队伍成员
        addPartyMembersToMatch(team1Leader, allMatchPlayers);

        // Determine team 2 leader position based on potential player count
        // 根据潜在玩家数量确定第二队队长的位置
        int team2StartIndex = (potentialPlayerCount == 2) ? 1 : 2;
        Player team2Leader = onlinePlayers.get(team2StartIndex);
        allMatchPlayers.add(team2Leader);

        // Add team 2 party members
        // 添加第二队的队伍成员
        addPartyMembersToMatch(team2Leader, allMatchPlayers);

        // Add remaining solo players
        // 添加剩余的单人玩家
        addRemainingSoloPlayers(onlinePlayers, allMatchPlayers, team2StartIndex);

        return allMatchPlayers;
    }

    /**
     * Adds party members of a leader to the match player list.
     * 将队长的队伍成员添加到比赛玩家列表中。
     *
     * @param leader          The party leader
     *                        队长
     * @param allMatchPlayers List to add party members to
     *                        要添加队伍成员的列表
     */
    private void addPartyMembersToMatch(Player leader, List<Player> allMatchPlayers) {
        PartyService partyService = AlleyPlugin.getInstance().getService(PartyService.class);
        Party party = partyService.getPartyByLeader(leader);
        if (party != null && party.getMembers().size() == 2) {
            party.getMembers().stream()
                    .filter(uuid -> !uuid.equals(leader.getUniqueId()))
                    .findFirst()
                    .ifPresent(memberUUID -> {
                        Player memberPlayer = Bukkit.getPlayer(memberUUID);
                        if (memberPlayer != null && memberPlayer.isOnline()) {
                            allMatchPlayers.add(memberPlayer);
                        }
                    });
        }
    }

    /**
     * Adds remaining solo players to the match that aren't already included.
     * 将尚未包含在比赛中的剩余单人玩家添加到比赛中。
     *
     * @param onlinePlayers   List of all online players
     *                        所有在线玩家列表
     * @param allMatchPlayers Current match players list
     *                        当前比赛玩家列表
     * @param team2StartIndex Index where team 2 starts
     *                        第二队起始的索引位置
     */
    private void addRemainingSoloPlayers(List<Player> onlinePlayers, List<Player> allMatchPlayers, int team2StartIndex) {
        for (int i = 1; i < onlinePlayers.size(); i++) {
            if (i == team2StartIndex) continue;

            Player soloPlayer = onlinePlayers.get(i);
            if (!allMatchPlayers.contains(soloPlayer)) {
                allMatchPlayers.add(soloPlayer);
            }
        }
    }

    /**
     * Creates the actual duos match with proper team assignments.
     * 创建实际的双人比赛并分配正确的队伍。
     *
     * @param queue                The duos queue
     *                             双人队列
     * @param allMatchPlayers      All players in the match
     *                             比赛中的所有玩家
     * @param validQueueProfiles   Valid queue profiles
     *                             有效的队列数据
     * @param onlinePlayers        Online players list
     *                             在线玩家列表
     * @param potentialPlayerCount Number of potential players
     *                             潜在玩家数量
     * @return true if match was successfully created
     *         如果比赛成功创建则返回 true
     */
    private boolean createDuosMatch(Queue queue, List<Player> allMatchPlayers, List<QueueProfile> validQueueProfiles, List<Player> onlinePlayers, int potentialPlayerCount) {
        int team2StartIndex = (potentialPlayerCount == 2) ? 1 : 2;
        Player team1Leader = onlinePlayers.get(0);
        Player team2Leader = onlinePlayers.get(team2StartIndex);

        QueueProfile team1LeaderProfile = validQueueProfiles.get(0);
        QueueProfile team2LeaderProfile = validQueueProfiles.get(team2StartIndex);

        GameParticipant<MatchGamePlayer> participantA = createTeamParticipant(team1Leader, team1LeaderProfile);
        GameParticipant<MatchGamePlayer> participantB = createTeamParticipant(team2Leader, team2LeaderProfile);

        assignPlayersToTeams(allMatchPlayers, validQueueProfiles, team1Leader, team2Leader, participantA, participantB);

        if (participantA.getPlayerSize() != 2 || participantB.getPlayerSize() != 2) {
            Logger.info("Teams don't have exactly 2 players each. Team A: " + participantA.getPlayerSize() + " Team B: " + participantB.getPlayerSize());
            return false;
        }

        if (!areParticipantsPingCompatible(participantA, participantB)) {
            return false;
        }

        Arena arena = this.getArena(queue);
        if (!isArenaAvailable(arena, allMatchPlayers, queue)) {
            List<UUID> allUUIDsToRemove = allMatchPlayers.stream()
                    .map(Player::getUniqueId)
                    .collect(Collectors.toList());
            clearQueueProfiles(queue, allUUIDsToRemove, true);
            return false;
        }

        MatchService matchService = AlleyPlugin.getInstance().getService(MatchService.class);
        matchService.createAndStartMatch(
                queue.getKit(), arena, participantA, participantB, true, true, queue.isRanked()
        );

        List<UUID> allUUIDsToRemove = allMatchPlayers.stream()
                .map(Player::getUniqueId)
                .collect(Collectors.toList());
        clearQueueProfiles(queue, allUUIDsToRemove, false);
        return true;
    }

    /**
     * Checks both players' personal queue-ping preferences. A disabled range
     * does not restrict that player; an enabled range must accept the other
     * player's current ping as well.
     */
    private boolean isPingCompatible(Player first, Player second) {
        if (first == null || second == null) {
            return false;
        }

        int difference = Math.abs(first.getPing() - second.getPing());
        return acceptsPingDifference(first.getUniqueId(), difference)
                && acceptsPingDifference(second.getUniqueId(), difference);
    }

    private boolean areParticipantsPingCompatible(GameParticipant<MatchGamePlayer> first,
                                                  GameParticipant<MatchGamePlayer> second) {
        for (MatchGamePlayer firstPlayer : first.getAllPlayers()) {
            for (MatchGamePlayer secondPlayer : second.getAllPlayers()) {
                Player firstBukkitPlayer = Bukkit.getPlayer(firstPlayer.getUuid());
                Player secondBukkitPlayer = Bukkit.getPlayer(secondPlayer.getUuid());
                if (!isPingCompatible(firstBukkitPlayer, secondBukkitPlayer)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean acceptsPingDifference(UUID playerId, int difference) {
        Profile profile = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(playerId);
        if (profile == null) {
            return true;
        }

        int range = profile.getProfileData().getSettingData().getQueuePingRange();
        return range <= 0 || difference <= range;
    }

    /**
     * Creates a team participant with the given leader.
     * 使用给定的队长创建一个队伍参与者。
     *
     * @param leader        The team leader
     *                      队长
     * @param leaderProfile The leader's queue profile
     *                      队长的队列数据
     * @return Team game participant
     *         队伍游戏参与者
     */
    private GameParticipant<MatchGamePlayer> createTeamParticipant(Player leader, QueueProfile leaderProfile) {
        MatchGamePlayer gameLeader = new MatchGamePlayer(leader.getUniqueId(), leader.getName(), leaderProfile.getElo());
        return new TeamGameParticipant<>(gameLeader);
    }

    /**
     * Assigns all match players to their appropriate teams based on party membership.
     * 根据队伍成员关系将所有比赛玩家分配到正确的队伍。
     *
     * @param allMatchPlayers    All players in the match
     *                           比赛中的所有玩家
     * @param validQueueProfiles Valid queue profiles
     *                           有效的队列数据
     * @param team1Leader        Team 1 leader
     *                           第一队队长
     * @param team2Leader        Team 2 leader
     *                           第二队队长
     * @param participantA       Team A participant
     *                           A 队参与者
     * @param participantB       Team B participant
     *                           B 队参与者
     */
    private void assignPlayersToTeams(List<Player> allMatchPlayers, List<QueueProfile> validQueueProfiles, Player team1Leader, Player team2Leader, GameParticipant<MatchGamePlayer> participantA, GameParticipant<MatchGamePlayer> participantB) {
        PartyService partyService = AlleyPlugin.getInstance().getService(PartyService.class);
        Party team1Party = partyService.getPartyByLeader(team1Leader);
        Party team2Party = partyService.getPartyByLeader(team2Leader);

        for (Player player : allMatchPlayers) {
            if (player.equals(team1Leader) || player.equals(team2Leader)) {
                continue;
            }

            int playerElo = getPlayerElo(player, validQueueProfiles);
            MatchGamePlayer gamePlayer = new MatchGamePlayer(player.getUniqueId(), player.getName(), playerElo);

            assignPlayerToTeam(player, gamePlayer, team1Party, team2Party, participantA, participantB);
        }
    }

    /**
     * Gets the ELO rating for a player from the queue profiles.
     * 从队列数据中获取玩家的 ELO 评分。
     *
     * @param player             The player
     *                           玩家
     * @param validQueueProfiles List of valid queue profiles
     *                           有效的队列数据列表
     * @return Player's ELO rating or default value
     *         玩家的 ELO 评分或默认值
     */
    private int getPlayerElo(Player player, List<QueueProfile> validQueueProfiles) {
        return validQueueProfiles.stream()
                .filter(queueProfile -> queueProfile.getUuid().equals(player.getUniqueId()))
                .findFirst()
                .map(QueueProfile::getElo)
                .orElse(1000);
    }

    /**
     * Assigns a single player to the appropriate team based on party membership.
     * 根据队伍成员关系将单个玩家分配到正确的队伍。
     *
     * @param player       The player to assign
     *                      要分配的玩家
     * @param gamePlayer   The game player implementation
     *                      游戏玩家实现
     * @param team1Party   Team 1's party
     *                      第一队的队伍
     * @param team2Party   Team 2's party
     *                      第二队的队伍
     * @param participantA Team A participant
     *                      A 队参与者
     * @param participantB Team B participant
     *                      B 队参与者
     */
    private void assignPlayerToTeam(Player player, MatchGamePlayer gamePlayer, Party team1Party, Party team2Party, GameParticipant<MatchGamePlayer> participantA, GameParticipant<MatchGamePlayer> participantB) {
        if (team1Party != null && team1Party.getMembers().contains(player.getUniqueId())) {
            participantA.addPlayer(gamePlayer);
        } else if (team2Party != null && team2Party.getMembers().contains(player.getUniqueId())) {
            participantB.addPlayer(gamePlayer);
        } else {
            if (participantA.getPlayerSize() < participantB.getPlayerSize()) {
                participantA.addPlayer(gamePlayer);
            } else {
                participantB.addPlayer(gamePlayer);
            }
        }
    }

    /**
     * Checks if an arena is available and suitable for the match.
     * 检查竞技场是否可用且适合该比赛。
     *
     * @param arena   The arena to check
     *                要检查的竞技场
     * @param players List of players for error messaging
     *                用于错误消息的玩家列表
     * @param queue   The queue for removing players on failure
     *                失败时用于移除玩家的队列
     * @return true if arena is available and suitable
     *         如果竞技场可用且适合则返回 true
     */
    private boolean isArenaAvailable(Arena arena, List<Player> players, Queue queue) {
        if (arena == null || arena.getType().equals(ArenaType.FFA)) {
            players.forEach(p -> p.sendMessage(CC.translate("&cThere are no available arenas for this kit")));
            return false;
        }
        return true;
    }

    /**
     * Retrieves a random available arena for the given queue's kit.
     * 为给定队列的装备包随机获取一个可用的竞技场。
     *
     * @param queue The queue requesting an arena
     *              请求竞技场的队列
     * @return An available arena or null if none found
     *         可用的竞技场，如果未找到则返回 null
     */
    private Arena getArena(Queue queue) {
        ArenaService arenaService = AlleyPlugin.getInstance().getService(ArenaService.class);
        return arenaService.getRandomArena(queue.getKit());
    }

    /**
     * Clears queue profiles for multiple players after a match has been created.
     * 比赛创建后清除多个玩家的队列数据。
     * This method handles all cleanup including updating player profiles and
     * 此方法处理所有清理工作，包括更新玩家数据
     * removing entries from the queue list.
     * 并从队列列表中移除条目。
     *
     * @param queue       The queue to clean up
     *                    要清理的队列
     * @param playerUUIDs The UUIDs of all players who were placed in the match
     *                    所有被放入比赛的玩家的 UUID
     * @param removeQueue Whether to call queue.removePlayer() for cleanup
     *                    是否调用 queue.removePlayer() 进行清理
     */
    public void clearQueueProfiles(Queue queue, List<UUID> playerUUIDs, boolean removeQueue) {
        Set<QueueProfile> uniqueProfiles = getUniqueQueueProfiles(playerUUIDs);

        for (QueueProfile queueProfile : uniqueProfiles) {
            if (removeQueue) {
                queue.removePlayer(queueProfile);
            } else {
                performProfileCleanup(queue, queueProfile);
            }
        }
    }

    /**
     * Gets unique queue profiles for the given player UUIDs.
     * 获取给定玩家 UUID 的唯一队列数据。
     *
     * @param playerUUIDs List of player UUIDs
     *                    玩家 UUID 列表
     * @return Set of unique queue profiles
     *         唯一队列数据的集合
     */
    private Set<QueueProfile> getUniqueQueueProfiles(List<UUID> playerUUIDs) {
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Set<QueueProfile> uniqueProfiles = new HashSet<>();
        for (UUID uuid : playerUUIDs) {
            Profile profile = profileService.getProfile(uuid);
            if (profile != null && profile.getQueueProfile() != null) {
                uniqueProfiles.add(profile.getQueueProfile());
            }
        }
        return uniqueProfiles;
    }

    /**
     * Performs cleanup for a specific queue profile, including party members if applicable.
     * 对特定队列数据进行清理，包括队伍成员（如适用）。
     *
     * @param queue        The queue being cleaned up
     *                     正在清理的队列
     * @param queueProfile The queue profile to clean up
     *                     要清理的队列数据
     */
    private void performProfileCleanup(Queue queue, QueueProfile queueProfile) {
        Player leader = Bukkit.getPlayer(queueProfile.getUuid());
        if (leader == null) return;

        PartyService partyService = AlleyPlugin.getInstance().getService(PartyService.class);
        Party party = partyService.getParty(leader);
        List<UUID> membersToClean = getMembersToClean(queue, party, leader);

        cleanupPlayerProfiles(membersToClean);
        queue.getProfiles().remove(queueProfile);
    }

    /**
     * Gets the list of member UUIDs that need profile cleanup.
     * 获取需要进行数据清理的成员 UUID 列表。
     *
     * @param queue  The queue being processed
     *               正在处理的队列
     * @param party  The party (can be null)
     *               队伍（可以为 null）
     * @param leader The leader player
     *               队长玩家
     * @return List of UUIDs to clean up
     *         需要清理的 UUID 列表
     */
    private List<UUID> getMembersToClean(Queue queue, Party party, Player leader) {
        List<UUID> membersToClean = new ArrayList<>();
        if (queue.isDuos() && party != null) {
            membersToClean.addAll(party.getMembers());
        } else {
            membersToClean.add(leader.getUniqueId());
        }
        return membersToClean;
    }

    /**
     * Cleans up profiles for the given member UUIDs by setting their queue profile to null.
     * 清理给定成员 UUID 的玩家数据，将队列数据设置为 null。
     *
     * @param memberUUIDs List of member UUIDs to clean up
     *                    要清理的成员 UUID 列表
     */
    private void cleanupPlayerProfiles(List<UUID> memberUUIDs) {
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        for (UUID memberId : memberUUIDs) {
            Profile memberProfile = profileService.getProfile(memberId);
            if (memberProfile != null) {
                memberProfile.setQueueProfile(null);
            }
        }
    }

    /**
     * Creates game participant list for solo matches.
     * 为单人比赛创建游戏参与者列表。
     *
     * @param gamePlayerList The game player list containing both players
     *                       包含两名玩家的游戏玩家列表
     * @return Game participant list with individual participants
     *         包含独立参与者的游戏参与者列表
     */
    private @NotNull GameParticipantList getSoloGameParticipantList(GamePlayerList gamePlayerList) {
        return new GameParticipantList(
                new GameParticipant<>(gamePlayerList.getFirstMatchGamePlayer()),
                new GameParticipant<>(gamePlayerList.getSecondMatchGamePlayer())
        );
    }

    /**
     * Creates a game player list from two players and their profiles.
     * 从两名玩家及其数据创建游戏玩家列表。
     *
     * @param firstPlayer   The first player
     *                      第一名玩家
     * @param secondPlayer  The second player
     *                      第二名玩家
     * @param firstProfile  The first player's queue profile
     *                      第一名玩家的队列数据
     * @param secondProfile The second player's queue profile
     *                      第二名玩家的队列数据
     * @return Game player list containing both match game players
     *         包含两名比赛游戏玩家的游戏玩家列表
     */
    private @NotNull GamePlayerList getGamePlayerList(Player firstPlayer, Player secondPlayer, QueueProfile firstProfile, QueueProfile secondProfile) {
        return new GamePlayerList(
                new MatchGamePlayer(firstPlayer.getUniqueId(), firstPlayer.getName(), firstProfile.getElo()),
                new MatchGamePlayer(secondPlayer.getUniqueId(), secondPlayer.getName(), secondProfile.getElo())
        );
    }

    /**
     * Data class representing a pair of game participants for match creation.
     * 表示用于创建比赛的一对游戏参与者的数据类。
     * Contains two participants that will compete against each other.
     * 包含两名将相互对抗的参与者。
     */
    @Getter
    private static class GameParticipantList {
        /**
         * The first participant in the match
         * 比赛中的第一位参与者
         */
        public final GameParticipant<MatchGamePlayer> participantA;
        /**
         * The second participant in the match
         * 比赛中的第二位参与者
         */
        public final GameParticipant<MatchGamePlayer> participantB;

        /**
         * Constructor for the GameParticipantList class.
         * GameParticipantList 类的构造函数。
         *
         * @param participantA The first participant
         *                     第一位参与者
         * @param participantB The second participant
         *                     第二位参与者
         */
        public GameParticipantList(GameParticipant<MatchGamePlayer> participantA, GameParticipant<MatchGamePlayer> participantB) {
            this.participantA = participantA;
            this.participantB = participantB;
        }

        /**
         * Gets both participants as a list for easier iteration.
         * 将两位参与者作为列表返回，以便于遍历。
         *
         * @return List containing both participants
         *         包含两位参与者的列表
         */
        public List<GameParticipant<MatchGamePlayer>> getParticipants() {
            return Arrays.asList(this.participantA, this.participantB);
        }
    }

    /**
     * Data class representing a pair of match game players.
     * 表示一对比赛游戏玩家的数据类。
     * Used for organizing player data before creating participants.
     * 用于在创建参与者之前组织玩家数据。
     */
    @Getter
    private static class GamePlayerList {
        /**
         * The first match game player
         * 第一名比赛游戏玩家
         */
        public final MatchGamePlayer firstMatchGamePlayer;
        /**
         * The second match game player
         * 第二名比赛游戏玩家
         */
        public final MatchGamePlayer secondMatchGamePlayer;

        /**
         * Constructor for the GamePlayerList class.
         * GamePlayerList 类的构造函数。
         *
         * @param firstMatchGamePlayer  The first match game player
         *                              第一名比赛游戏玩家
         * @param secondMatchGamePlayer The second match game player
         *                              第二名比赛游戏玩家
         */
        public GamePlayerList(MatchGamePlayer firstMatchGamePlayer, MatchGamePlayer secondMatchGamePlayer) {
            this.firstMatchGamePlayer = firstMatchGamePlayer;
            this.secondMatchGamePlayer = secondMatchGamePlayer;
        }
    }
}
