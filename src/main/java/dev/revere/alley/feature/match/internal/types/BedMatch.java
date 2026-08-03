package dev.revere.alley.feature.match.internal.types;

import dev.revere.alley.common.ListenerUtil;
import dev.revere.alley.common.PlayerUtil;
import dev.revere.alley.common.reflect.ReflectionService;
import dev.revere.alley.common.reflect.internal.types.TitleReflectionServiceImpl;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.locale.internal.impl.VisualsLocaleImpl;
import dev.revere.alley.core.locale.internal.impl.message.GameMessagesLocaleImpl;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.queue.Queue;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;

/**
 * @author Emmy
 * @project Alley
 * @since 21/05/2025
 */
public class BedMatch extends DefaultMatch {

    /**
     * Constructor for the MatchBedImpl class.
     * MatchBedImpl类的构造函数。
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
    public BedMatch(Queue queue, Kit kit, Arena arena, boolean ranked, GameParticipant<MatchGamePlayer> participantA, GameParticipant<MatchGamePlayer> participantB) {
        super(queue, kit, arena, ranked, participantA, participantB);
    }

    @Override
    public boolean canEndRound() {
        return (this.getParticipantA().isAllEliminated() || this.getParticipantB().isAllEliminated())
                || (this.getParticipantA().getAllPlayers().stream().allMatch(MatchGamePlayer::isDisconnected)
                || this.getParticipantB().getAllPlayers().stream().allMatch(MatchGamePlayer::isDisconnected));
    }

    @Override
    public boolean canStartRound() {
        return !this.getParticipantA().isAllEliminated() && !this.getParticipantB().isAllEliminated();
    }

    @Override
    public void handleParticipant(Player player, MatchGamePlayer gamePlayer) {
        GameParticipant<MatchGamePlayer> gameParticipant = this.getParticipantA().containsPlayer(player.getUniqueId())
                ? this.getParticipantA()
                : this.getParticipantB();

        if (gameParticipant.isBedBroken()) {
            GameParticipant<MatchGamePlayer> participant = getParticipant(player);
            if (participant == null) {
                return;
            }

            gamePlayer.setEliminated(true);
        }

        super.handleParticipant(player, gamePlayer);
    }

    @Override
    public void handleDeathItemDrop(Player player, PlayerDeathEvent event) {
        GameParticipant<MatchGamePlayer> participant = this.getParticipantA().containsPlayer(player.getUniqueId())
                ? this.getParticipantA()
                : this.getParticipantB();

        if (participant.isBedBroken()) {
            ListenerUtil.clearDroppedItemsOnDeath(event, player);
            return;
        }
        super.handleDeathItemDrop(player, event);
    }

    @Override
    protected boolean shouldHandleRegularRespawn(Player player) {
        return false;
    }

    @Override
    public void handleRespawn(Player player) {
        PlayerUtil.reset(player, true, true);

        Location spawnLocation = this.getParticipants().get(0).containsPlayer(player.getUniqueId()) ? this.getArena().getPos1() : this.getArena().getPos2();
        ListenerUtil.teleportAndClearSpawn(player, spawnLocation);

        this.giveLoadout(player, this.getKit());
        this.applyColorKit(player);
    }

    /**
     * Alerts the participants about the bed destruction.
     * 向参赛方通报床被摧毁的消息。
     *
     * @param breaker             The player who broke the bed.
     *                            破坏床的玩家。
     * @param opponentParticipant The opponent whose bed was destroyed.
     *                            床被摧毁的对方参赛方。
     */
    public void alertBedDestruction(Player breaker, GameParticipant<MatchGamePlayer> opponentParticipant) {
        LocaleService localeService = this.plugin.getService(LocaleService.class);
        TitleReflectionServiceImpl titleService = this.plugin.getService(ReflectionService.class).getReflectionService(TitleReflectionServiceImpl.class);

        if (localeService.getBoolean(VisualsLocaleImpl.TITLE_MATCH_BED_DESTROYED_ENABLED_BOOLEAN)) {
            String bedDestroyedHeader = localeService.getString(VisualsLocaleImpl.TITLE_MATCH_BED_DESTROYED_HEADER);
            String bedDestroyedFooter = localeService.getString(VisualsLocaleImpl.TITLE_MATCH_BED_DESTROYED_FOOTER);
            int fadeIn = localeService.getInt(VisualsLocaleImpl.TITLE_MATCH_BED_DESTROYED_FADE_IN);
            int stay = localeService.getInt(VisualsLocaleImpl.TITLE_MATCH_BED_DESTROYED_STAY);
            int fadeOut = localeService.getInt(VisualsLocaleImpl.TITLE_MATCH_BED_DESTROYED_FADEOUT);

            opponentParticipant.getPlayers().forEach(matchGamePlayer -> {
                Player p = this.plugin.getServer().getPlayer(matchGamePlayer.getUuid());
                titleService.sendTitle(p, bedDestroyedHeader, bedDestroyedFooter, fadeIn, stay, fadeOut);
            });
        }

        this.playSound(opponentParticipant, Sound.ENTITY_WITHER_DEATH);

        GameParticipant<MatchGamePlayer> breakerParticipant = this.getParticipant(breaker);
        this.playSound(breakerParticipant, Sound.ENTITY_ENDER_DRAGON_GROWL);

        if (localeService.getBoolean(GameMessagesLocaleImpl.MATCH_BED_DESTRUCTION_MESSAGE_ENABLED_BOOLEAN)) {
            List<String> message = localeService.getStringList(GameMessagesLocaleImpl.MATCH_BED_DESTRUCTION_MESSAGE_FORMAT);
            message.forEach(line -> {
                String formattedLine = line
                        .replace("{bed-color}", String.valueOf(this.getTeamColor(opponentParticipant)))
                        .replace("{breaker-color}", String.valueOf(this.getTeamColor(breakerParticipant)))
                        .replace("{bed}", this.getParticipantA() == opponentParticipant ? "Blue Bed" : "Red Bed")
                        .replace("{breaker}", breaker.getName());
                this.sendMessage(formattedLine);
            });
        }
    }

    /**
     * Checks if a block is near a bed.
     * 检查一个方块是否靠近床。
     *
     * @param block The block to check.
     *              要检查的方块。
     * @return true if the block is near a bed, false otherwise.
     *         如果方块靠近床则返回true，否则返回false。
     */
    public boolean isNearBed(Block block) {
        Location center = block.getLocation();
        for (int x = -8; x <= 1; x++) {
            for (int y = -8; y <= 1; y++) {
                for (int z = -8; z <= 1; z++) {
                    Block relativeBlock = new Location(block.getWorld(), center.getX() + x, center.getY() + y, center.getZ() + z).getBlock();
                    if (relativeBlock.getType() == Material.RED_BED) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
