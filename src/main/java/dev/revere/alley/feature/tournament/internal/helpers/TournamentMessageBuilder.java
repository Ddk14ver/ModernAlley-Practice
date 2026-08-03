package dev.revere.alley.feature.tournament.internal.helpers;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
/**
 * @author Remi
 * @project alley-practice
 * @date 6/08/2025
 */
@UtilityClass
public class TournamentMessageBuilder {
    /**
     * Generates a broadcast message for a tournament participant.
     * 为锦标赛参与者生成广播消息。
     * The message is formatted based on the number of team members.
     * 消息将根据队伍成员数量进行格式化。
     *
     * @param participant  The tournament participant.
     *                     锦标赛参与者。
     * @param verbSingular The verb to use if the participant has one member.
     *                     当参与者只有一名成员时使用的动词。
     * @param verbPlural   The verb to use if the participant has multiple members.
     *                     当参与者有多名成员时使用的动词。
     * @return A formatted broadcast message.
     *         格式化后的广播消息。
     */
    public String generateParticipantBroadcast(TournamentParticipant participant, String verbSingular, String verbPlural) {
        String nameList = getNaturalTeamNameListWithProfileColors(participant);
        if (nameList.isEmpty()) return "";

        String verb = participant.getSize() > 1 ? verbPlural : verbSingular;
        return CC.translate("&e" + nameList + " &f" + verb + "!");
    }

    /**
     * Creates a natural-language formatted list of team members with a specified color.
     * 使用指定颜色创建自然语言格式的队伍成员列表。
     * e.g., "&cplayer1 &7and &9player2"
     * 例如："&cplayer1 &7和 &9player2"
     *
     * @param participant The tournament participant.
     *                    锦标赛参与者。
     * @param nameColor   The color to apply to the names.
     *                    应用于名称的颜色。
     * @return A formatted string of team member names.
     *         格式化后的队伍成员名称字符串。
     */
    public String getNaturalTeamNameList(TournamentParticipant participant, String nameColor) {
        List<String> memberNames = participant.getMemberUuids().stream()
                .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return formatNameList(memberNames, nameColor);
    }

    /**
     * Creates a natural-language formatted list of team members with their profile colors.
     * 使用玩家个人资料颜色创建自然语言格式的队伍成员列表。
     * e.g., "&cplayer1 &7and &9player2"
     * 例如："&cplayer1 &7和 &9player2"
     *
     * @param participant The tournament participant.
     *                    锦标赛参与者。
     * @return A formatted string of team member names with profile colors.
     *         带有个人资料颜色的格式化队伍成员名称字符串。
     */
    public String getNaturalTeamNameListWithProfileColors(TournamentParticipant participant) {
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);

        List<String> coloredNames = participant.getMemberUuids().stream()
                .map(uuid -> {
                    Profile profile = profileService.getProfile(uuid);
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

                    if (offlinePlayer.getName() == null) return null;

                    String color = (profile != null) ? profile.getNameColor().toString() : "&7";
                    return color + offlinePlayer.getName();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return formatColoredNameList(coloredNames);
    }

    /**
     * Formats a list of names into a natural-language string with a specified color.
     * 将名称列表格式化为带指定颜色的自然语言字符串。
     * Handles singular and plural cases appropriately.
     * 适当处理单数和复数情况。
     *
     * @param names     The list of names to format.
     *                  要格式化的名称列表。
     * @param nameColor The color to apply to the names.
     *                  应用于名称的颜色。
     * @return A formatted string of names.
     *         格式化后的名称字符串。
     */
    private String formatNameList(List<String> names, String nameColor) {
        int size = names.size();
        if (size == 0) return "An unknown team";
        if (size == 1) return nameColor + names.get(0);

        if (size == 2) {
            return nameColor + names.get(0) + " &fand " + nameColor + names.get(1);
        } else {
            String almostAll = names.subList(0, size - 1).stream()
                    .map(name -> nameColor + name)
                    .collect(Collectors.joining("&f, "));
            return almostAll + " &fand " + nameColor + names.get(size - 1);
        }
    }

    /**
     * Formats a list of colored names into a natural-language string.
     * 将带颜色的名称列表格式化为自然语言字符串。
     * Handles singular and plural cases appropriately.
     * 适当处理单数和复数情况。
     *
     * @param coloredNames The list of colored names to format.
     *                     要格式化的带颜色名称列表。
     * @return A formatted string of colored names.
     *         格式化后的带颜色名称字符串。
     */
    private String formatColoredNameList(List<String> coloredNames) {
        int size = coloredNames.size();
        if (size == 0) return "&fAn unknown team";
        if (size == 1) return coloredNames.get(0);

        if (size == 2) {
            return coloredNames.get(0) + " &fand " + coloredNames.get(1);
        } else {
            String almostAll = String.join("&f, ", coloredNames.subList(0, size - 1));
            return almostAll + " &fand " + coloredNames.get(size - 1);
        }
    }
}
