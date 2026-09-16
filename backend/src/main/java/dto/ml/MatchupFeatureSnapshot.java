package com.fourthdown.ai.dto.ml;

public record MatchupFeatureSnapshot(
        TeamFeatureSnapshot home,
        TeamFeatureSnapshot away,
        double pointsDifference,
        double yardsDifference,
        double turnoverDifference,
        double scoringMarginDifference,
        double thirdDownDifference,
        double redZoneDifference,
        double recentFormDifference,
        double opponentWinRateDifference,
        double opponentScoringMarginDifference
) {
}