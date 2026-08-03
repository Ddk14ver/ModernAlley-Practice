package dev.revere.alley.feature.tournament.formation.distribution;

import java.util.List;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 10/07/2026
 */
public interface DistributionOptimizer {
    List<Integer> findOptimalDistribution(List<List<Integer>> possibleDistributions);
}