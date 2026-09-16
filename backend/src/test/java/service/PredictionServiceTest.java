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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PredictionServiceTest {

    private MachineLearningClient machineLearningClient;
    private FeatureEngineeringService featureEngineeringService;
    private GameRepository gameRepository;
    private TeamRepository teamRepository;

    private PredictionService predictionService;

    @BeforeEach
    void setUp() {
        machineLearningClient =
                mock(MachineLearningClient.class);

        featureEngineeringService =
                mock(FeatureEngineeringService.class);

        gameRepository =
                mock(GameRepository.class);

        teamRepository =
                mock(TeamRepository.class);

        predictionService =
                new PredictionService(
                        machineLearningClient,
                        featureEngineeringService,
                        gameRepository,
                        teamRepository
                );
    }

    // =========================================================
    // MANUAL MATCHUP
    // =========================================================

    @Test
    void predictMatchupReturnsMlWinnerAndScore() {

        Team homeTeam =
                createTeam(
                        1L,
                        "Auburn"
                );

        Team awayTeam =
                createTeam(
                        2L,
                        "Florida"
                );

        when(
                teamRepository.findById(1L)
        ).thenReturn(
                Optional.of(homeTeam)
        );

        when(
                teamRepository.findById(2L)
        ).thenReturn(
                Optional.of(awayTeam)
        );

        MatchupFeatureSnapshot features =
                createMatchupFeatures();

        when(
                featureEngineeringService
                        .buildMatchupFeatures(
                                eq(1L),
                                eq(2L),
                                any(Integer.class),
                                any(LocalDateTime.class)
                        )
        ).thenReturn(
                features
        );

        MlPredictionResponse mlResponse =
                new MlPredictionResponse(
                        0.68,
                        0.32,
                        true,
                        31,
                        24,
                        7.2,
                        55.4
                );

        when(
                machineLearningClient.predict(
                        any(MlPredictionRequest.class)
                )
        ).thenReturn(
                mlResponse
        );

        MatchupPredictionResponse response =
                predictionService.predictMatchup(
                        1L,
                        2L
                );

        assertNotNull(
                response
        );

        assertEquals(
                1L,
                response.getHomeTeamId()
        );

        assertEquals(
                "Auburn",
                response.getHomeTeamName()
        );

        assertEquals(
                2L,
                response.getAwayTeamId()
        );

        assertEquals(
                "Florida",
                response.getAwayTeamName()
        );

        assertEquals(
                1L,
                response.getPredictedWinnerId()
        );

        assertEquals(
                "Auburn",
                response.getPredictedWinnerName()
        );

        assertEquals(
                68.0,
                response.getHomeWinProbability()
        );

        assertEquals(
                32.0,
                response.getAwayWinProbability()
        );

        assertEquals(
                68.0,
                response.getConfidence()
        );

        assertEquals(
                31,
                response.getProjectedHomeScore()
        );

        assertEquals(
                24,
                response.getProjectedAwayScore()
        );
    }

    // =========================================================
    // AWAY WINNER
    // =========================================================

    @Test
    void predictMatchupReturnsAwayWinnerWhenModelPredictsAway() {

        Team homeTeam =
                createTeam(
                        1L,
                        "Auburn"
                );

        Team awayTeam =
                createTeam(
                        2L,
                        "Georgia"
                );

        when(
                teamRepository.findById(1L)
        ).thenReturn(
                Optional.of(homeTeam)
        );

        when(
                teamRepository.findById(2L)
        ).thenReturn(
                Optional.of(awayTeam)
        );

        when(
                featureEngineeringService
                        .buildMatchupFeatures(
                                eq(1L),
                                eq(2L),
                                any(Integer.class),
                                any(LocalDateTime.class)
                        )
        ).thenReturn(
                createMatchupFeatures()
        );

        when(
                machineLearningClient.predict(
                        any(MlPredictionRequest.class)
                )
        ).thenReturn(
                new MlPredictionResponse(
                        0.35,
                        0.65,
                        false,
                        20,
                        28,
                        -8.0,
                        48.0
                )
        );

        MatchupPredictionResponse response =
                predictionService.predictMatchup(
                        1L,
                        2L
                );

        assertEquals(
                2L,
                response.getPredictedWinnerId()
        );

        assertEquals(
                "Georgia",
                response.getPredictedWinnerName()
        );

        assertEquals(
                20,
                response.getProjectedHomeScore()
        );

        assertEquals(
                28,
                response.getProjectedAwayScore()
        );
    }

    // =========================================================
    // SCHEDULED GAME
    // =========================================================

    @Test
    void predictGameUsesGameSeasonAndKickoffAsCutoff() {

        Team homeTeam =
                createTeam(
                        10L,
                        "Alabama"
                );

        Team awayTeam =
                createTeam(
                        20L,
                        "LSU"
                );

        LocalDateTime kickoff =
                LocalDateTime.of(
                        2026,
                        11,
                        7,
                        19,
                        0
                );

        Game game =
                mock(Game.class);

        when(
                game.getHomeTeam()
        ).thenReturn(
                homeTeam
        );

        when(
                game.getAwayTeam()
        ).thenReturn(
                awayTeam
        );

        when(
                game.getSeason()
        ).thenReturn(
                2026
        );

        when(
                game.getGameDate()
        ).thenReturn(
                kickoff
        );

        when(
                gameRepository.findById(
                        100L
                )
        ).thenReturn(
                Optional.of(game)
        );

        when(
                featureEngineeringService
                        .buildMatchupFeatures(
                                10L,
                                20L,
                                2026,
                                kickoff
                        )
        ).thenReturn(
                createMatchupFeatures()
        );

        when(
                machineLearningClient.predict(
                        any(MlPredictionRequest.class)
                )
        ).thenReturn(
                new MlPredictionResponse(
                        0.57,
                        0.43,
                        true,
                        27,
                        23,
                        4.0,
                        50.0
                )
        );

        MatchupPredictionResponse response =
                predictionService.predictGame(
                        100L
                );

        assertEquals(
                "Alabama",
                response.getPredictedWinnerName()
        );

        assertEquals(
                27,
                response.getProjectedHomeScore()
        );

        assertEquals(
                23,
                response.getProjectedAwayScore()
        );

        verify(
                featureEngineeringService
        ).buildMatchupFeatures(
                10L,
                20L,
                2026,
                kickoff
        );
    }

    // =========================================================
    // SAME TEAM VALIDATION
    // =========================================================

    @Test
    void predictMatchupRejectsSameTeam() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                predictionService
                                        .predictMatchup(
                                                1L,
                                                1L
                                        )
                );

        assertEquals(
                "Home team and away team must be different",
                exception.getMessage()
        );
    }

    // =========================================================
    // MISSING HOME TEAM
    // =========================================================

    @Test
    void predictMatchupRejectsMissingHomeTeam() {

        when(
                teamRepository.findById(
                        1L
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                predictionService
                                        .predictMatchup(
                                                1L,
                                                2L
                                        )
                );

        assertEquals(
                "Home team not found",
                exception.getMessage()
        );
    }

    // =========================================================
    // NO HISTORICAL DATA
    // =========================================================

    @Test
    void predictMatchupRejectsTeamsWithoutHistoricalData() {

        Team homeTeam =
                createTeam(
                        1L,
                        "Team A"
                );

        Team awayTeam =
                createTeam(
                        2L,
                        "Team B"
                );

        when(
                teamRepository.findById(1L)
        ).thenReturn(
                Optional.of(homeTeam)
        );

        when(
                teamRepository.findById(2L)
        ).thenReturn(
                Optional.of(awayTeam)
        );

        TeamFeatureSnapshot empty =
                TeamFeatureSnapshot.empty();

        MatchupFeatureSnapshot features =
                new MatchupFeatureSnapshot(
                        empty,
                        empty,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0
                );

        when(
                featureEngineeringService
                        .buildMatchupFeatures(
                                eq(1L),
                                eq(2L),
                                any(Integer.class),
                                any(LocalDateTime.class)
                        )
        ).thenReturn(
                features
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                predictionService
                                        .predictMatchup(
                                                1L,
                                                2L
                                        )
                );

        assertEquals(
                "Both teams must have historical data before the prediction cutoff",
                exception.getMessage()
        );

        verify(
                machineLearningClient,
                never()
        ).predict(
                any()
        );
    }

    // =========================================================
    // VERIFY ALL NINE FEATURES
    // =========================================================

    @Test
    void predictionSendsAllNineFeaturesToMlService() {

        Team homeTeam =
                createTeam(
                        1L,
                        "Home"
                );

        Team awayTeam =
                createTeam(
                        2L,
                        "Away"
                );

        when(
                teamRepository.findById(1L)
        ).thenReturn(
                Optional.of(homeTeam)
        );

        when(
                teamRepository.findById(2L)
        ).thenReturn(
                Optional.of(awayTeam)
        );

        MatchupFeatureSnapshot features =
                createMatchupFeatures();

        when(
                featureEngineeringService
                        .buildMatchupFeatures(
                                eq(1L),
                                eq(2L),
                                any(Integer.class),
                                any(LocalDateTime.class)
                        )
        ).thenReturn(
                features
        );

        when(
                machineLearningClient.predict(
                        any(MlPredictionRequest.class)
                )
        ).thenReturn(
                new MlPredictionResponse(
                        0.60,
                        0.40,
                        true,
                        30,
                        24,
                        6.0,
                        54.0
                )
        );

        predictionService.predictMatchup(
                1L,
                2L
        );

        ArgumentCaptor<MlPredictionRequest> captor =
                ArgumentCaptor.forClass(
                        MlPredictionRequest.class
                );

        verify(
                machineLearningClient
        ).predict(
                captor.capture()
        );

        MlPredictionRequest request =
                captor.getValue();

        assertEquals(
                features.pointsDifference(),
                request.pointsDifference()
        );

        assertEquals(
                features.yardsDifference(),
                request.yardsDifference()
        );

        assertEquals(
                features.turnoverDifference(),
                request.turnoverDifference()
        );

        assertEquals(
                features.scoringMarginDifference(),
                request.scoringMarginDifference()
        );

        assertEquals(
                features.thirdDownDifference(),
                request.thirdDownDifference()
        );

        assertEquals(
                features.redZoneDifference(),
                request.redZoneDifference()
        );

        assertEquals(
                features.recentFormDifference(),
                request.recentFormDifference()
        );

        assertEquals(
                features.opponentWinRateDifference(),
                request.opponentWinRateDifference()
        );

        assertEquals(
                features.opponentScoringMarginDifference(),
                request.opponentScoringMarginDifference()
        );
    }

    // =========================================================
    // NULL GAME ID
    // =========================================================

    @Test
    void predictGameRejectsNullGameId() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                predictionService
                                        .predictGame(
                                                null
                                        )
                );

        assertEquals(
                "Game ID is required",
                exception.getMessage()
        );
    }

    // =========================================================
    // MISSING GAME
    // =========================================================

    @Test
    void predictGameRejectsMissingGame() {

        when(
                gameRepository.findById(
                        999L
                )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                predictionService
                                        .predictGame(
                                                999L
                                        )
                );

        assertEquals(
                "Game not found",
                exception.getMessage()
        );
    }

    // =========================================================
    // GAME WITHOUT TEAMS
    // =========================================================

    @Test
    void predictGameRejectsGameWithoutTeams() {

        Game game =
                mock(Game.class);

        when(
                game.getHomeTeam()
        ).thenReturn(
                null
        );

        when(
                game.getAwayTeam()
        ).thenReturn(
                null
        );

        when(
                gameRepository.findById(
                        100L
                )
        ).thenReturn(
                Optional.of(game)
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                predictionService
                                        .predictGame(
                                                100L
                                        )
                );

        assertEquals(
                "Game does not have valid home and away teams",
                exception.getMessage()
        );
    }

    // =========================================================
    // GAME WITHOUT DATE
    // =========================================================

    @Test
    void predictGameRejectsGameWithoutDate() {

        Team homeTeam =
                createTeam(
                        1L,
                        "Home"
                );

        Team awayTeam =
                createTeam(
                        2L,
                        "Away"
                );

        Game game =
                mock(Game.class);

        when(
                game.getHomeTeam()
        ).thenReturn(
                homeTeam
        );

        when(
                game.getAwayTeam()
        ).thenReturn(
                awayTeam
        );

        when(
                game.getGameDate()
        ).thenReturn(
                null
        );

        when(
                gameRepository.findById(
                        100L
                )
        ).thenReturn(
                Optional.of(game)
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                predictionService
                                        .predictGame(
                                                100L
                                        )
                );

        assertEquals(
                "Game does not have a scheduled kickoff time",
                exception.getMessage()
        );
    }

    // =========================================================
    // GAME WITHOUT SEASON
    // =========================================================

    @Test
    void predictGameRejectsGameWithoutSeason() {

        Team homeTeam =
                createTeam(
                        1L,
                        "Home"
                );

        Team awayTeam =
                createTeam(
                        2L,
                        "Away"
                );

        Game game =
                mock(Game.class);

        when(
                game.getHomeTeam()
        ).thenReturn(
                homeTeam
        );

        when(
                game.getAwayTeam()
        ).thenReturn(
                awayTeam
        );

        when(
                game.getGameDate()
        ).thenReturn(
                LocalDateTime.of(
                        2026,
                        9,
                        14,
                        19,
                        0
                )
        );

        when(
                game.getSeason()
        ).thenReturn(
                null
        );

        when(
                gameRepository.findById(
                        100L
                )
        ).thenReturn(
                Optional.of(game)
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                predictionService
                                        .predictGame(
                                                100L
                                        )
                );

        assertEquals(
                "Game does not have a valid season",
                exception.getMessage()
        );
    }

    // =========================================================
    // NULL ML RESPONSE
    // =========================================================

    @Test
    void predictMatchupRejectsNullMlResponse() {

        Team homeTeam =
                createTeam(
                        1L,
                        "Home"
                );

        Team awayTeam =
                createTeam(
                        2L,
                        "Away"
                );

        when(
                teamRepository.findById(1L)
        ).thenReturn(
                Optional.of(homeTeam)
        );

        when(
                teamRepository.findById(2L)
        ).thenReturn(
                Optional.of(awayTeam)
        );

        when(
                featureEngineeringService
                        .buildMatchupFeatures(
                                eq(1L),
                                eq(2L),
                                any(Integer.class),
                                any(LocalDateTime.class)
                        )
        ).thenReturn(
                createMatchupFeatures()
        );

        when(
                machineLearningClient.predict(
                        any(MlPredictionRequest.class)
                )
        ).thenReturn(
                null
        );

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () ->
                                predictionService
                                        .predictMatchup(
                                                1L,
                                                2L
                                        )
                );

        assertEquals(
                "Machine learning service returned no prediction",
                exception.getMessage()
        );
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private Team createTeam(
            Long id,
            String name
    ) {

        Team team =
                mock(Team.class);

        when(
                team.getId()
        ).thenReturn(
                id
        );

        when(
                team.getName()
        ).thenReturn(
                name
        );

        return team;
    }

    private MatchupFeatureSnapshot createMatchupFeatures() {

        TeamFeatureSnapshot home =
                new TeamFeatureSnapshot(
                        8,
                        31.5,
                        425.0,
                        260.0,
                        165.0,
                        1.1,
                        8.5,
                        0.44,
                        0.83,
                        0.75,
                        0.62,
                        4.8
                );

        TeamFeatureSnapshot away =
                new TeamFeatureSnapshot(
                        8,
                        24.0,
                        360.0,
                        225.0,
                        135.0,
                        1.6,
                        1.5,
                        0.37,
                        0.74,
                        0.50,
                        0.54,
                        0.5
                );

        return new MatchupFeatureSnapshot(
                home,
                away,
                7.5,
                65.0,
                -0.5,
                7.0,
                0.07,
                0.09,
                0.25,
                0.08,
                4.3
        );
    }
}