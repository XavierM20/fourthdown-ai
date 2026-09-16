package com.fourthdown.ai.dto.ml;

public record MlPredictionRequest(
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