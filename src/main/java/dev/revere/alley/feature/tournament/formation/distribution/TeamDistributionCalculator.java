package dev.revere.alley.feature.tournament.formation.distribution;

import dev.revere.alley.feature.tournament.formation.model.TeamDistribution;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;

import java.util.List;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 10/07/2026
 */
public interface TeamDistributionCalculator {
    TeamDistribution calculateDistribution(List<TournamentParticipant> participantPool, int maxTeamSize);
}

