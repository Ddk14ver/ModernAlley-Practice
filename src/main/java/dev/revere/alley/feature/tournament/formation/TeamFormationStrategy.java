package dev.revere.alley.feature.tournament.formation;

import dev.revere.alley.feature.tournament.model.TournamentParticipant;

import java.util.List;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 10/07/2026
 */
public interface TeamFormationStrategy {
    /**
     * Forms balanced teams from a pool of participants.
     * 从参与者池中组建平衡的队伍。
     *
     * @param participantPool The available participants (parties/solos)
     *                        可用的参与者（队伍/单人）
     * @param maxTeamSize The maximum size each team can have
     *                    每支队伍的最大人数
     * @return A list of formed teams
     *         组建完成的队伍列表
     */
    List<TournamentParticipant> formTeams(List<TournamentParticipant> participantPool, int maxTeamSize);
}