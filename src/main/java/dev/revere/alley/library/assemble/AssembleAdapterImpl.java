package dev.revere.alley.library.assemble;

import dev.revere.alley.core.config.ConfigService;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.visual.scoreboard.internal.*;
import dev.revere.alley.visual.scoreboard.internal.match.MatchScoreboardImpl;
import dev.revere.alley.common.animation.AnimationService;
import dev.revere.alley.common.animation.AnimationType;
import dev.revere.alley.common.animation.internal.config.ScoreboardTitleAnimation;
import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.text.CC;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Emmy
 * @project Alley
 * @date 27/03/2024 - 14:27
 */
public class AssembleAdapterImpl implements AssembleAdapter {
    private final AnimationService animationService;
    private final ProfileService profileService;
    private final ConfigService configService;

    private final LobbyScoreboardImpl lobbyScoreboardImpl = new LobbyScoreboardImpl();
    private final QueueScoreboardImpl queueScoreboardImpl = new QueueScoreboardImpl();
    private final MatchScoreboardImpl matchScoreboardImpl = new MatchScoreboardImpl();
    private final BotScoreboardImpl botScoreboardImpl = new BotScoreboardImpl();
    private final SpectatorScoreboardImpl spectatorScoreboardImpl = new SpectatorScoreboardImpl();
    private final FFAScoreboardImpl ffaScoreboardImpl = new FFAScoreboardImpl();
    private final TournamentScoreboardImpl tournamentScoreboardImpl = new TournamentScoreboardImpl();

    public AssembleAdapterImpl(AnimationService animationService, ProfileService profileService, ConfigService configService) {
        this.animationService = animationService;
        this.profileService = profileService;
        this.configService = configService;
    }

    @Override
    public String getTitle(Player player) {
        Profile profile = this.profileService.getProfile(player.getUniqueId());
        if (profile == null || profile.getProfileData() == null) return "";
        if (!profile.getProfileData().getSettingData().isScoreboardEnabled()) {
            return "";
        }
        String title = this.animationService.getAnimation(ScoreboardTitleAnimation.class, AnimationType.CONFIG).getText();
        if (AlleyPlugin.getInstance().getService(dev.revere.alley.feature.staff.StaffModeManager.class).isStaff(player)) {
            title += CC.translate(" &7STAFF");
        }
        return title;
    }

    /**
     * Get the lines of the scoreboard.
     * 获取记分板的行内容。
     *
     * @param player The player to get the lines for.
     *        要获取其记分板行的玩家。
     * @return The lines of the scoreboard.
     *         记分板的行列表。
     */
    @Override
    public List<String> getLines(Player player) {
        Profile profile = this.profileService.getProfile(player.getUniqueId());
        if (profile == null || profile.getProfileData() == null) return Collections.emptyList();

        if (profile.getProfileData().getSettingData().isScoreboardEnabled()) {

            if (profile.getState() == ProfileState.EDITING) {
                return Collections.emptyList();
            }

            List<String> lines = new ArrayList<>();

            switch (profile.getState()) {
                case LOBBY:
                    lines.addAll(this.lobbyScoreboardImpl.getLines(profile));
                    break;
                case WAITING:
                    lines.addAll(this.queueScoreboardImpl.getLines(profile));
                    break;
                case PLAYING:
                    lines.addAll(this.matchScoreboardImpl.getLines(profile, player));
                    break;
                case FIGHTING_BOT:
                    lines.addAll(this.botScoreboardImpl.getLines(profile, player));
                    break;
                case TOURNAMENT_LOBBY:
                    lines.addAll(this.tournamentScoreboardImpl.getLines(profile));
                    break;
                case SPECTATING:
                    lines.addAll(this.spectatorScoreboardImpl.getLines(profile));
                    break;
                case FFA:
                    lines.addAll(this.ffaScoreboardImpl.getLines(profile, player));
                    break;
            }

            List<String> footer = this.configService.getScoreboardConfig().getStringList("scoreboard.footer-addition");
            footer.forEach(line -> lines.add(CC.translate(line)));

            lines.replaceAll(line -> line.replace("{sidebar}", this.getScoreboardLines(profile)));
            return lines;
        }
        return null;
    }

    /**
     * Method to either show the scoreboard lines or not.
     * 决定是否显示记分板行的方法。
     *
     * @param profile The profile to get the scoreboard lines for.
     *        要获取记分板行的玩家档案。
     * @return The scoreboard lines.
     *         记分板行格式。
     */
    private String getScoreboardLines(Profile profile) {
        if (profile.getProfileData().getSettingData().isShowScoreboardLines()) {
            return this.configService.getScoreboardConfig().getString("scoreboard.sidebar-format");
        }
        return "";
    }
}
