package dev.revere.alley.visual.nametag.model;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.Profile;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * 名字标签上下文，封装观察者和目标的玩家对象及其档案信息。
 * @author Remi
 * @project alley-practice
 * @date 27/06/2025
 */
@Getter
public class NametagContext {
    private final Profile viewerProfile;
    private final Profile targetProfile;
    private final Player viewer;
    private final Player target;

    public NametagContext(Player viewer, Player target) {
        this.viewer = viewer;
        this.target = target;

        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        this.viewerProfile = profileService.getProfile(viewer.getUniqueId());
        this.targetProfile = profileService.getProfile(target.getUniqueId());
    }
}