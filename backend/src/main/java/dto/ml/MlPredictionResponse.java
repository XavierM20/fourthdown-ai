package com.fourthdown.ai.dto.ml;

public record MlPredictionResponse(
        double homeWinProbability,
        double awayWinProbability,
        boolean predictedHomeWin,
        int projectedHomeScore,
        int projectedAwayScore,
        double projectedMargin,
        double projectedTotalPoints
) {
}