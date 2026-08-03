package dev.revere.alley.feature.tournament.formation.assembly;

import dev.revere.alley.feature.tournament.model.TournamentParticipant;

import java.util.List;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 10/06/2026
 */
public interface PartyMatcher {
    TournamentParticipant createTeamOfSize(List<TournamentParticipant> availableParties,
                                           int targetSize,
                                           int maxTeamSize);
}