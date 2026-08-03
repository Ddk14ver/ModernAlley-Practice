package dev.revere.alley.visual.nametag;

import lombok.Getter;

/**
 * 名字标签可见性枚举，定义名字标签的显示行为（始终显示、从不显示、对其他队伍隐藏、对己方队伍隐藏）。
 * @author Remi
 * @project alley-practice
 * @date 22/07/2025
 */
@Getter
public enum NametagVisibility {
    ALWAYS("always"),
    NEVER("never"),
    HIDE_FOR_OTHER_TEAMS("hideForOtherTeams"),
    HIDE_FOR_OWN_TEAM("hideForOwnTeam");

    ;

    private final String value;

    NametagVisibility(String value) {
        this.value = value;
    }

}
