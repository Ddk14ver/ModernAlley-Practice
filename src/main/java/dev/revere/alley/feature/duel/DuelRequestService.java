package dev.revere.alley.feature.duel;

import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.bootstrap.lifecycle.Service;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Remi
 * 作者：Remi
 * @project alley-practice
 * 项目：alley-practice
 * @date 2/07/2025
 * 日期：2025年7月2日
 */
public interface DuelRequestService extends Service {
    /**
     * Gets a list of all currently pending duel requests.
     * 获取所有当前待处理的决斗请求列表。
     *
     * @return A list of DuelRequest objects.
     *         DuelRequest对象列表。
     */
    List<DuelRequest> getDuelRequests();

    /**
     * The primary method for creating and sending a duel request from one player to another.
     * This handles validation, arena selection, and sending the invitation.
     * 创建并发送从一个玩家到另一个玩家的决斗请求的主要方法。
     * 此方法处理验证、竞技场选择和发送邀请。
     *
     * @param sender        The player initiating the request.
     *                      发起请求的玩家。
     * @param initialTarget The player being challenged.
     *                      被挑战的玩家。
     * @param kit           The kit for the duel.
     *                      决斗使用的套件。
     * @param arena         The specific arena chosen, or null to select a random one.
     *                      选择的特定竞技场，或为null以随机选择一个。
     */
    void createAndSendRequest(Player sender, Player initialTarget, Kit kit, @Nullable Arena arena);

    /**
     * Accepts a pending duel request, leading to the creation of a match.
     * 接受一个待处理的决斗请求，从而创建一场比赛。
     *
     * @param duelRequest The duel request to accept.
     *                    要接受的决斗请求。
     */
    void acceptPendingRequest(DuelRequest duelRequest);

    /**
     * Finds a pending duel request between two players.
     * The order of sender/target does not matter.
     * 查找两个玩家之间待处理的决斗请求。
     * 发送者/目标的顺序无关紧要。
     *
     * @param playerOne The first player.
     *                  第一个玩家。
     * @param playerTwo The second player.
     *                  第二个玩家。
     * @return The DuelRequest object, or null if none is pending.
     *         如果存在待处理的请求则返回DuelRequest对象，否则返回null。
     */
    DuelRequest getDuelRequest(Player playerOne, Player playerTwo);
}