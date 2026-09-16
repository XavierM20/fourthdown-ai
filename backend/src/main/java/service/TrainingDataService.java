package com.fourthdown.ai.service;

import com.fourthdown.ai.dto.ml.MatchupFeatureSnapshot;
import com.fourthdown.ai.dto.ml.TeamFeatureSnapshot;
import com.fourthdown.ai.dto.ml.TrainingExampleResponse;
import com.fourthdown.ai.model.Game;
import com.fourthdown.ai.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TrainingDataService {

    private final GameRepository gameRepository;
    private final FeatureEngineeringService featureEngineeringService;

    public TrainingDataService(
            GameRepository gameRepository,
            FeatureEngineeringService featureEngineeringService
    ) {
        this.gameRepository =
                gameRepository;

        this.featureEngineeringService =
                featureEngineeringService;
    }

    // =========================================================
    // BUILD TRAINING DATA
    // =========================================================

    public List<TrainingExampleResponse> buildTrainingData(
            int startSeason,
            int endSeason,
            int minPriorGames
    ) {

        if (startSeason > endSeason) {

            throw new IllegalArgumentException(
                    "startSeason cannot be greater than endSeason"
            );
        }

        if (minPriorGames < 1) {

            throw new IllegalArgumentException(
                    "minPriorGames must be at least 1"
            );
        }

        List<Game> games =
                gameRepository
                        .findBySeasonBetweenAndStatusOrderByGameDateAsc(
                                startSeason,
                                endSeason,
                                "COMPLETED"
                        );

        List<TrainingExampleResponse> examples =
                new ArrayList<>();

        for (Game game : games) {

            // =================================================
            // VALIDATION
            // =================================================

            if (game.getGameDate() == null ||
                    game.getSeason() == null ||
                    game.getHomeScore() == null ||
                    game.getAwayScore() == null ||
                    game.getHomeTeam() == null ||
                    game.getAwayTeam() == null) {

                continue;
            }

            /*
             * College football should not end in a tie.
             *
             * If a malformed historical record exists,
             * do not use it for binary classification.
             */
            if (game.getHomeScore()
                    .equals(game.getAwayScore())) {

                continue;
            }

            // =================================================
            // FEATURE GENERATION
            //
            // This now includes:
            //
            // - current-season features
            // - early-season prior-year fallback
            // - kickoff-time cutoff
            // - opponent-strength metrics
            // =================================================

            MatchupFeatureSnapshot matchup =
                    featureEngineeringService
                            .buildMatchupFeatures(
                                    game.getHomeTeam().getId(),
                                    game.getAwayTeam().getId(),
                                    game.getSeason(),
                                    game.getGameDate()
                            );

            TeamFeatureSnapshot home =
                    matchup.home();

            TeamFeatureSnapshot away =
                    matchup.away();

            // =================================================
            // MINIMUM HISTORY REQUIREMENT
            //
            // gamesPlayed may include usable prior-season
            // history during the early-season fallback period.
            // =================================================

            if (home.gamesPlayed() <
                    minPriorGames ||
                    away.gamesPlayed() <
                            minPriorGames) {

                continue;
            }

            // =================================================
            // BUILD TRAINING EXAMPLE
            // =================================================

            TrainingExampleResponse example =
                    new TrainingExampleResponse();

            // =================================================
            // GAME INFORMATION
            // =================================================

            example.setGameId(
                    game.getId()
            );

            example.setCfbdGameId(
                    game.getCfbdId()
            );

            example.setSeason(
                    game.getSeason()
            );

            example.setWeek(
                    game.getWeek()
            );

            example.setGameDate(
                    game.getGameDate()
            );

            // =================================================
            // TEAM INFORMATION
            // =================================================

            example.setHomeTeamId(
                    game.getHomeTeam().getId()
            );

            example.setHomeTeamName(
                    game.getHomeTeam().getName()
            );

            example.setAwayTeamId(
                    game.getAwayTeam().getId()
            );

            example.setAwayTeamName(
                    game.getAwayTeam().getName()
            );

            // =================================================
            // AVAILABLE HISTORY
            // =================================================

            example.setHomePriorGames(
                    home.gamesPlayed()
            );

            example.setAwayPriorGames(
                    away.gamesPlayed()
            );

            // =================================================
            // HOME TEAM FEATURES
            // =================================================

            example.setHomeAveragePoints(
                    home.averagePoints()
            );

            example.setHomeAverageTotalYards(
                    home.averageTotalYards()
            );

            example.setHomeAveragePassingYards(
                    home.averagePassingYards()
            );

            example.setHomeAverageRushingYards(
                    home.averageRushingYards()
            );

            example.setHomeAverageTurnovers(
                    home.averageTurnovers()
            );

            example.setHomeAverageScoringMargin(
                    home.averageScoringMargin()
            );

            example.setHomeThirdDownRate(
                    home.thirdDownRate()
            );

            example.setHomeRedZoneRate(
                    home.redZoneRate()
            );

            example.setHomeRecentWinRate(
                    home.recentWinRate()
            );

            // =================================================
            // HOME OPPONENT-STRENGTH FEATURES
            // =================================================

            example.setHomeAverageOpponentWinRate(
                    home.averageOpponentWinRate()
            );

            example.setHomeAverageOpponentScoringMargin(
                    home.averageOpponentScoringMargin()
            );

            // =================================================
            // AWAY TEAM FEATURES
            // =================================================

            example.setAwayAveragePoints(
                    away.averagePoints()
            );

            example.setAwayAverageTotalYards(
                    away.averageTotalYards()
            );

            example.setAwayAveragePassingYards(
                    away.averagePassingYards()
            );

            example.setAwayAverageRushingYards(
                    away.averageRushingYards()
            );

            example.setAwayAverageTurnovers(
                    away.averageTurnovers()
            );

            example.setAwayAverageScoringMargin(
                    away.averageScoringMargin()
            );

            example.setAwayThirdDownRate(
                    away.thirdDownRate()
            );

            example.setAwayRedZoneRate(
                    away.redZoneRate()
            );

            example.setAwayRecentWinRate(
                    away.recentWinRate()
            );

            // =================================================
            // AWAY OPPONENT-STRENGTH FEATURES
            // =================================================

            example.setAwayAverageOpponentWinRate(
                    away.averageOpponentWinRate()
            );

            example.setAwayAverageOpponentScoringMargin(
                    away.averageOpponentScoringMargin()
            );

            // =================================================
            // MODEL DIFFERENCE FEATURES
            //
            // These are the actual values used by Python.
            // =================================================

            example.setPointsDifference(
                    matchup.pointsDifference()
            );

            example.setYardsDifference(
                    matchup.yardsDifference()
            );

            example.setTurnoverDifference(
                    matchup.turnoverDifference()
            );

            example.setScoringMarginDifference(
                    matchup.scoringMarginDifference()
            );

            example.setThirdDownDifference(
                    matchup.thirdDownDifference()
            );

            example.setRedZoneDifference(
                    matchup.redZoneDifference()
            );

            example.setRecentFormDifference(
                    matchup.recentFormDifference()
            );

            // =================================================
            // NEW OPPONENT-STRENGTH DIFFERENCE FEATURES
            // =================================================

            example.setOpponentWinRateDifference(
                    matchup.opponentWinRateDifference()
            );

            example.setOpponentScoringMarginDifference(
                    matchup.opponentScoringMarginDifference()
            );

            // =================================================
            // ACTUAL RESULT
            //
            // These values are only labels/evaluation targets.
            // They are NOT available to the model before kickoff.
            // =================================================

            example.setHomeScore(
                    game.getHomeScore()
            );

            example.setAwayScore(
                    game.getAwayScore()
            );

            /*
             * Binary label:
             *
             * 1 = home team won
             * 0 = away team won
             */
            example.setHomeWon(
                    game.getHomeScore() >
                            game.getAwayScore()
                            ? 1
                            : 0
            );

            examples.add(
                    example
            );
        }

        return examples;
    }
}