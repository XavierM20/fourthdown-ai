package com.fourthdown.ai.service;

import com.fourthdown.ai.client.MachineLearningClient;
import com.fourthdown.ai.dto.MatchupPredictionResponse;
import com.fourthdown.ai.dto.ml.MatchupFeatureSnapshot;
import com.fourthdown.ai.dto.ml.MlPredictionRequest;
import com.fourthdown.ai.dto.ml.MlPredictionResponse;
import com.fourthdown.ai.dto.ml.TeamFeatureSnapshot;
import com.fourthdown.ai.model.Game;
import com.fourthdown.ai.model.Team;
import com.fourthdown.ai.repository.GameRepository;
import com.fourthdown.ai.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PredictionService {

    private final MachineLearningClient machineLearningClient;
    private final FeatureEngineeringService featureEngineeringService;
    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;

    public PredictionService(
            MachineLearningClient machineLearningClient,
            FeatureEngineeringService featureEngineeringService,
            GameRepository gameRepository,
            TeamRepository teamRepository
    ) {
        this.machineLearningClient =
                machineLearningClient;

        this.featureEngineeringService =
                featureEngineeringService;

        this.gameRepository =
                gameRepository;

        this.teamRepository =
                teamRepository;
    }

    // =========================================================
    // MANUAL / HYPOTHETICAL MATCHUP
    // =========================================================

    public MatchupPredictionResponse predictMatchup(
            Long homeTeamId,
            Long awayTeamId
    ) {

        if (homeTeamId == null ||
                awayTeamId == null) {

            throw new IllegalArgumentException(
                    "Home team and away team are required"
            );
        }

        if (homeTeamId.equals(awayTeamId)) {

            throw new IllegalArgumentException(
                    "Home team and away team must be different"
            );
        }

        Team homeTeam =
                teamRepository
                        .findById(homeTeamId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Home team not found"
                                )
                        );

        Team awayTeam =
                teamRepository
                        .findById(awayTeamId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Away team not found"
                                )
                        );

        LocalDateTime cutoff =
                LocalDateTime.now();

        int season =
                cutoff.getYear();

        return generatePrediction(
                homeTeam,
                awayTeam,
                season,
                cutoff
        );
    }

    // =========================================================
    // REAL SCHEDULED GAME PREDICTION
    //
    // Uses the scheduled game's:
    //
    // - season
    // - kickoff time
    //
    // so no information after kickoff can enter the features.
    // =========================================================

    public MatchupPredictionResponse predictGame(
            Long gameId
    ) {

        if (gameId == null) {

            throw new IllegalArgumentException(
                    "Game ID is required"
            );
        }

        Game game =
                gameRepository
                        .findById(gameId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Game not found"
                                )
                        );

        if (game.getHomeTeam() == null ||
                game.getAwayTeam() == null) {

            throw new IllegalArgumentException(
                    "Game does not have valid home and away teams"
            );
        }

        if (game.getGameDate() == null) {

            throw new IllegalArgumentException(
                    "Game does not have a scheduled kickoff time"
            );
        }

        if (game.getSeason() == null) {

            throw new IllegalArgumentException(
                    "Game does not have a valid season"
            );
        }

        return generatePrediction(
                game.getHomeTeam(),
                game.getAwayTeam(),
                game.getSeason(),
                game.getGameDate()
        );
    }

    // =========================================================
    // SHARED PREDICTION PIPELINE
    //
    // Winner:
    // Logistic Regression
    //
    // Score:
    // Ridge Regression
    //
    // Both models receive the same 9 engineered features.
    // =========================================================

    private MatchupPredictionResponse generatePrediction(
            Team homeTeam,
            Team awayTeam,
            Integer season,
            LocalDateTime cutoff
    ) {

        if (homeTeam == null ||
                awayTeam == null) {

            throw new IllegalArgumentException(
                    "Both teams are required"
            );
        }

        if (season == null) {

            throw new IllegalArgumentException(
                    "Prediction season is required"
            );
        }

        if (cutoff == null) {

            throw new IllegalArgumentException(
                    "Prediction cutoff is required"
            );
        }

        Long homeTeamId =
                homeTeam.getId();

        Long awayTeamId =
                awayTeam.getId();

        // =====================================================
        // FEATURE ENGINEERING
        //
        // Includes:
        //
        // - season-aware stats
        // - early-season prior-year fallback
        // - kickoff cutoff
        // - recent form
        // - opponent strength
        // =====================================================

        MatchupFeatureSnapshot features =
                featureEngineeringService
                        .buildMatchupFeatures(
                                homeTeamId,
                                awayTeamId,
                                season,
                                cutoff
                        );

        TeamFeatureSnapshot homeFeatures =
                features.home();

        TeamFeatureSnapshot awayFeatures =
                features.away();

        if (homeFeatures.gamesPlayed() == 0 ||
                awayFeatures.gamesPlayed() == 0) {

            throw new IllegalArgumentException(
                    "Both teams must have historical data before the prediction cutoff"
            );
        }

        // =====================================================
        // BUILD ML REQUEST
        //
        // These 9 fields must match:
        //
        // - train_model.py
        // - train_score_model.py
        // - inference_api.py
        // =====================================================

        MlPredictionRequest mlRequest =
                new MlPredictionRequest(
                        features.pointsDifference(),
                        features.yardsDifference(),
                        features.turnoverDifference(),
                        features.scoringMarginDifference(),
                        features.thirdDownDifference(),
                        features.redZoneDifference(),
                        features.recentFormDifference(),
                        features.opponentWinRateDifference(),
                        features.opponentScoringMarginDifference()
                );

        // =====================================================
        // CALL FASTAPI
        //
        // FastAPI now runs:
        //
        // Logistic Regression
        //     -> win probability
        //
        // Ridge Regression
        //     -> projected margin
        //     -> projected total points
        //     -> projected team scores
        // =====================================================

        MlPredictionResponse mlPrediction =
                machineLearningClient
                        .predict(
                                mlRequest
                        );

        if (mlPrediction == null) {

            throw new RuntimeException(
                    "Machine learning service returned no prediction"
            );
        }

        // =====================================================
        // WINNER MODEL OUTPUT
        // =====================================================

        double homeProbability =
                mlPrediction
                        .homeWinProbability();

        double awayProbability =
                mlPrediction
                        .awayWinProbability();

        boolean homePredictedWinner =
                mlPrediction
                        .predictedHomeWin();

        // =====================================================
        // SCORE MODEL OUTPUT
        //
        // These values now come directly from Ridge Regression
        // through FastAPI.
        //
        // Spring no longer computes projected scores using
        // handwritten formulas.
        // =====================================================

        int projectedHomeScore =
                mlPrediction
                        .projectedHomeScore();

        int projectedAwayScore =
                mlPrediction
                        .projectedAwayScore();

        // =====================================================
        // BUILD API RESPONSE
        // =====================================================

        MatchupPredictionResponse response =
                new MatchupPredictionResponse();

        response.setHomeTeamId(
                homeTeamId
        );

        response.setHomeTeamName(
                homeTeam.getName()
        );

        response.setAwayTeamId(
                awayTeamId
        );

        response.setAwayTeamName(
                awayTeam.getName()
        );

        // =====================================================
        // PREDICTED WINNER
        // =====================================================

        if (homePredictedWinner) {

            response.setPredictedWinnerId(
                    homeTeamId
            );

            response.setPredictedWinnerName(
                    homeTeam.getName()
            );

        } else {

            response.setPredictedWinnerId(
                    awayTeamId
            );

            response.setPredictedWinnerName(
                    awayTeam.getName()
            );
        }

        // =====================================================
        // WIN PROBABILITIES
        //
        // FastAPI returns 0.0 - 1.0.
        //
        // React displays 0 - 100.
        // =====================================================

        response.setHomeWinProbability(
                round(
                        homeProbability * 100
                )
        );

        response.setAwayWinProbability(
                round(
                        awayProbability * 100
                )
        );

        response.setConfidence(
                round(
                        Math.max(
                                homeProbability,
                                awayProbability
                        ) * 100
                )
        );

        // =====================================================
        // ML-PROJECTED SCORE
        // =====================================================

        response.setProjectedHomeScore(
                projectedHomeScore
        );

        response.setProjectedAwayScore(
                projectedAwayScore
        );

        // =====================================================
        // EXPLANATION
        // =====================================================

        response.setExplanation(
                buildExplanation(
                        homeTeam.getName(),
                        awayTeam.getName(),
                        homeFeatures,
                        awayFeatures
                )
        );

        return response;
    }

    // =========================================================
    // EXPLANATION
    //
    // This still uses the same pregame feature snapshots
    // used by the ML models.
    // =========================================================

    private List<String> buildExplanation(
            String homeTeamName,
            String awayTeamName,
            TeamFeatureSnapshot home,
            TeamFeatureSnapshot away
    ) {

        List<String> explanation =
                new ArrayList<>();

        // -----------------------------------------------------
        // SCORING MARGIN
        // -----------------------------------------------------

        if (home.averageScoringMargin() >
                away.averageScoringMargin()) {

            explanation.add(
                    homeTeamName +
                            " has the stronger average scoring margin."
            );

        } else if (
                away.averageScoringMargin() >
                        home.averageScoringMargin()
        ) {

            explanation.add(
                    awayTeamName +
                            " has the stronger average scoring margin."
            );
        }

        // -----------------------------------------------------
        // SCORING
        // -----------------------------------------------------

        if (home.averagePoints() >
                away.averagePoints()) {

            explanation.add(
                    homeTeamName +
                            " scores more points per game."
            );

        } else if (
                away.averagePoints() >
                        home.averagePoints()
        ) {

            explanation.add(
                    awayTeamName +
                            " scores more points per game."
            );
        }

        // -----------------------------------------------------
        // TOTAL OFFENSE
        // -----------------------------------------------------

        if (home.averageTotalYards() >
                away.averageTotalYards()) {

            explanation.add(
                    homeTeamName +
                            " produces more total offense per game."
            );

        } else if (
                away.averageTotalYards() >
                        home.averageTotalYards()
        ) {

            explanation.add(
                    awayTeamName +
                            " produces more total offense per game."
            );
        }

        // -----------------------------------------------------
        // TURNOVERS
        // -----------------------------------------------------

        if (home.averageTurnovers() <
                away.averageTurnovers()) {

            explanation.add(
                    homeTeamName +
                            " protects the football better."
            );

        } else if (
                away.averageTurnovers() <
                        home.averageTurnovers()
        ) {

            explanation.add(
                    awayTeamName +
                            " protects the football better."
            );
        }

        // -----------------------------------------------------
        // THIRD DOWN
        // -----------------------------------------------------

        if (home.thirdDownRate() >
                away.thirdDownRate()) {

            explanation.add(
                    homeTeamName +
                            " has the stronger third-down conversion rate."
            );

        } else if (
                away.thirdDownRate() >
                        home.thirdDownRate()
        ) {

            explanation.add(
                    awayTeamName +
                            " has the stronger third-down conversion rate."
            );
        }

        // -----------------------------------------------------
        // RED ZONE
        // -----------------------------------------------------

        if (home.redZoneRate() >
                away.redZoneRate()) {

            explanation.add(
                    homeTeamName +
                            " has the stronger red-zone scoring rate."
            );

        } else if (
                away.redZoneRate() >
                        home.redZoneRate()
        ) {

            explanation.add(
                    awayTeamName +
                            " has the stronger red-zone scoring rate."
            );
        }

        // -----------------------------------------------------
        // RECENT FORM
        // -----------------------------------------------------

        if (home.recentWinRate() >
                away.recentWinRate()) {

            explanation.add(
                    homeTeamName +
                            " has the stronger recent form."
            );

        } else if (
                away.recentWinRate() >
                        home.recentWinRate()
        ) {

            explanation.add(
                    awayTeamName +
                            " has the stronger recent form."
            );
        }

        // -----------------------------------------------------
        // OPPONENT WIN RATE / SCHEDULE STRENGTH
        // -----------------------------------------------------

        if (home.averageOpponentWinRate() >
                away.averageOpponentWinRate()) {

            explanation.add(
                    homeTeamName +
                            " has faced opponents with a stronger average win rate."
            );

        } else if (
                away.averageOpponentWinRate() >
                        home.averageOpponentWinRate()
        ) {

            explanation.add(
                    awayTeamName +
                            " has faced opponents with a stronger average win rate."
            );
        }

        // -----------------------------------------------------
        // OPPONENT SCORING MARGIN
        // -----------------------------------------------------

        if (home.averageOpponentScoringMargin() >
                away.averageOpponentScoringMargin()) {

            explanation.add(
                    homeTeamName +
                            " has faced opponents with a stronger average scoring margin."
            );

        } else if (
                away.averageOpponentScoringMargin() >
                        home.averageOpponentScoringMargin()
        ) {

            explanation.add(
                    awayTeamName +
                            " has faced opponents with a stronger average scoring margin."
            );
        }

        return explanation;
    }

    // =========================================================
    // ROUNDING
    // =========================================================

    private double round(
            double value
    ) {

        return Math.round(
                value * 10.0
        ) / 10.0;
    }
}