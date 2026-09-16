package com.fourthdown.ai.dto.ml;

public record TeamFeatureSnapshot(
        int gamesPlayed,
        double averagePoints,
        double averageTotalYards,
        double averagePassingYards,
        double averageRushingYards,
        double averageTurnovers,
        double averageScoringMargin,
        double thirdDownRate,
        double redZoneRate,
        double recentWinRate,
        double averageOpponentWinRate,
        double averageOpponentScoringMargin
) {

    public static TeamFeatureSnapshot empty() {
        return new TeamFeatureSnapshot(
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0.5,
                0.5,
                0
        );
    }
}