package com.fourthdown.ai.service;

import com.fourthdown.ai.dto.ml.MatchupFeatureSnapshot;
import com.fourthdown.ai.dto.ml.TeamFeatureSnapshot;
import com.fourthdown.ai.model.Game;
import com.fourthdown.ai.model.Team;
import com.fourthdown.ai.model.TeamGameStats;
import com.fourthdown.ai.repository.TeamGameStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FeatureEngineeringServiceTest {

    private TeamGameStatsRepository statsRepository;

    private FeatureEngineeringService featureEngineeringService;

    @BeforeEach
    void setUp() {

        statsRepository =
                mock(
                        TeamGameStatsRepository.class
                );

        featureEngineeringService =
                new FeatureEngineeringService(
                        statsRepository
                );
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    @Test
    void buildTeamFeaturesRejectsNullTeamId() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                featureEngineeringService
                                        .buildTeamFeatures(
                                                null,
                                                2026,
                                                LocalDateTime.now()
                                        )
                );

        assertEquals(
                "Team ID is required",
                exception.getMessage()
        );
    }

    @Test
    void buildTeamFeaturesRejectsNullSeason() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                featureEngineeringService
                                        .buildTeamFeatures(
                                                1L,
                                                null,
                                                LocalDateTime.now()
                                        )
                );

        assertEquals(
                "Season is required",
                exception.getMessage()
        );
    }

    @Test
    void buildTeamFeaturesRejectsNullCutoff() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                featureEngineeringService
                                        .buildTeamFeatures(
                                                1L,
                                                2026,
                                                null
                                        )
                );

        assertEquals(
                "Feature cutoff is required",
                exception.getMessage()
        );
    }

    // =========================================================
    // NO HISTORY
    // =========================================================

    @Test
    void returnsEmptySnapshotWhenNoHistoryExists() {

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2026
                        )
        ).thenReturn(
                List.of()
        );

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2025
                        )
        ).thenReturn(
                List.of()
        );

        TeamFeatureSnapshot snapshot =
                featureEngineeringService
                        .buildTeamFeatures(
                                1L,
                                2026,
                                LocalDateTime.of(
                                        2026,
                                        9,
                                        1,
                                        12,
                                        0
                                )
                        );

        assertEquals(
                0,
                snapshot.gamesPlayed()
        );

        assertEquals(
                0.0,
                snapshot.averagePoints(),
                0.0001
        );

        assertEquals(
                0.5,
                snapshot.recentWinRate(),
                0.0001
        );

        assertEquals(
                0.5,
                snapshot.averageOpponentWinRate(),
                0.0001
        );
    }

    // =========================================================
    // STRICT KICKOFF CUTOFF
    // =========================================================

    @Test
    void excludesGamesAtOrAfterCutoff() {

        LocalDateTime cutoff =
                LocalDateTime.of(
                        2026,
                        9,
                        10,
                        19,
                        0
                );

        TeamGameStats beforeCutoff =
                createStat(
                        1L,
                        2L,
                        LocalDateTime.of(
                                2026,
                                9,
                                5,
                                19,
                                0
                        ),
                        "COMPLETED",
                        30,
                        20,
                        400,
                        250,
                        150,
                        1,
                        5,
                        10,
                        4,
                        5
                );

        TeamGameStats exactlyAtCutoff =
                createStat(
                        1L,
                        3L,
                        cutoff,
                        "COMPLETED",
                        50,
                        10,
                        500,
                        300,
                        200,
                        0,
                        8,
                        10,
                        5,
                        5
                );

        TeamGameStats afterCutoff =
                createStat(
                        1L,
                        4L,
                        cutoff.plusHours(2),
                        "COMPLETED",
                        60,
                        7,
                        550,
                        325,
                        225,
                        0,
                        9,
                        10,
                        5,
                        5
                );

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2026
                        )
        ).thenReturn(
                List.of(
                        beforeCutoff,
                        exactlyAtCutoff,
                        afterCutoff
                )
        );

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2025
                        )
        ).thenReturn(
                List.of()
        );

        TeamFeatureSnapshot snapshot =
                featureEngineeringService
                        .buildTeamFeatures(
                                1L,
                                2026,
                                cutoff
                        );

        assertEquals(
                1,
                snapshot.gamesPlayed()
        );

        assertEquals(
                30.0,
                snapshot.averagePoints(),
                0.0001
        );

        assertEquals(
                400.0,
                snapshot.averageTotalYards(),
                0.0001
        );
    }

    // =========================================================
    // INCOMPLETE GAME FILTER
    // =========================================================

    @Test
    void ignoresGamesThatAreNotCompleted() {

        LocalDateTime cutoff =
                LocalDateTime.of(
                        2026,
                        10,
                        1,
                        12,
                        0
                );

        TeamGameStats completed =
                createStat(
                        1L,
                        2L,
                        LocalDateTime.of(
                                2026,
                                9,
                                1,
                                12,
                                0
                        ),
                        "COMPLETED",
                        28,
                        21,
                        400,
                        240,
                        160,
                        1,
                        4,
                        10,
                        3,
                        4
                );

        TeamGameStats scheduled =
                createStat(
                        1L,
                        3L,
                        LocalDateTime.of(
                                2026,
                                9,
                                15,
                                12,
                                0
                        ),
                        "SCHEDULED",
                        99,
                        0,
                        999,
                        500,
                        499,
                        0,
                        10,
                        10,
                        10,
                        10
                );

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2026
                        )
        ).thenReturn(
                List.of(
                        completed,
                        scheduled
                )
        );

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2025
                        )
        ).thenReturn(
                List.of()
        );

        TeamFeatureSnapshot snapshot =
                featureEngineeringService
                        .buildTeamFeatures(
                                1L,
                                2026,
                                cutoff
                        );

        assertEquals(
                1,
                snapshot.gamesPlayed()
        );

        assertEquals(
                28.0,
                snapshot.averagePoints(),
                0.0001
        );
    }

    // =========================================================
    // PRIOR SEASON FALLBACK
    // =========================================================

    @Test
    void usesPriorSeasonWhenCurrentSeasonHasNoGames() {

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2026
                        )
        ).thenReturn(
                List.of()
        );

        TeamGameStats priorOne =
                createStat(
                        1L,
                        2L,
                        LocalDateTime.of(
                                2025,
                                9,
                                1,
                                12,
                                0
                        ),
                        "COMPLETED",
                        30,
                        20,
                        400,
                        250,
                        150,
                        1,
                        5,
                        10,
                        4,
                        5
                );

        TeamGameStats priorTwo =
                createStat(
                        1L,
                        3L,
                        LocalDateTime.of(
                                2025,
                                9,
                                8,
                                12,
                                0
                        ),
                        "COMPLETED",
                        20,
                        24,
                        300,
                        190,
                        110,
                        2,
                        4,
                        10,
                        3,
                        5
                );

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2025
                        )
        ).thenReturn(
                List.of(
                        priorOne,
                        priorTwo
                )
        );

        TeamFeatureSnapshot snapshot =
                featureEngineeringService
                        .buildTeamFeatures(
                                1L,
                                2026,
                                LocalDateTime.of(
                                        2026,
                                        8,
                                        20,
                                        12,
                                        0
                                )
                        );

        assertEquals(
                2,
                snapshot.gamesPlayed()
        );

        assertEquals(
                25.0,
                snapshot.averagePoints(),
                0.0001
        );

        assertEquals(
                350.0,
                snapshot.averageTotalYards(),
                0.0001
        );

        assertEquals(
                3.0,
                snapshot.averageScoringMargin(),
                0.0001
        );
    }

    // =========================================================
    // ONE-GAME BLEND
    //
    // 25% current
    // 75% prior
    // =========================================================

    @Test
    void blendsOneCurrentGameAtTwentyFivePercent() {

        TeamGameStats current =
                createStat(
                        1L,
                        2L,
                        LocalDateTime.of(
                                2026,
                                9,
                                1,
                                12,
                                0
                        ),
                        "COMPLETED",
                        40,
                        20,
                        500,
                        300,
                        200,
                        1,
                        6,
                        10,
                        5,
                        5
                );

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2026
                        )
        ).thenReturn(
                List.of(
                        current
                )
        );

        List<TeamGameStats> prior =
                List.of(
                        createStat(
                                1L,
                                3L,
                                LocalDateTime.of(
                                        2025,
                                        9,
                                        1,
                                        12,
                                        0
                                ),
                                "COMPLETED",
                                20,
                                10,
                                300,
                                200,
                                100,
                                2,
                                4,
                                10,
                                3,
                                5
                        ),
                        createStat(
                                1L,
                                4L,
                                LocalDateTime.of(
                                        2025,
                                        9,
                                        8,
                                        12,
                                        0
                                ),
                                "COMPLETED",
                                20,
                                10,
                                300,
                                200,
                                100,
                                2,
                                4,
                                10,
                                3,
                                5
                        ),
                        createStat(
                                1L,
                                5L,
                                LocalDateTime.of(
                                        2025,
                                        9,
                                        15,
                                        12,
                                        0
                                ),
                                "COMPLETED",
                                20,
                                10,
                                300,
                                200,
                                100,
                                2,
                                4,
                                10,
                                3,
                                5
                        ),
                        createStat(
                                1L,
                                6L,
                                LocalDateTime.of(
                                        2025,
                                        9,
                                        22,
                                        12,
                                        0
                                ),
                                "COMPLETED",
                                20,
                                10,
                                300,
                                200,
                                100,
                                2,
                                4,
                                10,
                                3,
                                5
                        )
                );

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2025
                        )
        ).thenReturn(
                prior
        );

        TeamFeatureSnapshot snapshot =
                featureEngineeringService
                        .buildTeamFeatures(
                                1L,
                                2026,
                                LocalDateTime.of(
                                        2026,
                                        9,
                                        5,
                                        12,
                                        0
                                )
                        );

        /*
         * points:
         *
         * 40 * .25
         * +
         * 20 * .75
         *
         * = 25
         */

        assertEquals(
                25.0,
                snapshot.averagePoints(),
                0.0001
        );

        /*
         * yards:
         *
         * 500 * .25
         * +
         * 300 * .75
         *
         * = 350
         */

        assertEquals(
                350.0,
                snapshot.averageTotalYards(),
                0.0001
        );

        /*
         * gamesPlayed stores current + prior
         */

        assertEquals(
                5,
                snapshot.gamesPlayed()
        );
    }

    // =========================================================
    // TWO-GAME BLEND
    //
    // 50% current
    // 50% prior
    // =========================================================

    @Test
    void blendsTwoCurrentGamesAtFiftyPercent() {

        List<TeamGameStats> current =
                List.of(
                        createStat(
                                1L,
                                2L,
                                date(
                                        2026,
                                        9,
                                        1
                                ),
                                "COMPLETED",
                                40,
                                20,
                                500,
                                300,
                                200,
                                1,
                                6,
                                10,
                                5,
                                5
                        ),
                        createStat(
                                1L,
                                3L,
                                date(
                                        2026,
                                        9,
                                        8
                                ),
                                "COMPLETED",
                                40,
                                20,
                                500,
                                300,
                                200,
                                1,
                                6,
                                10,
                                5,
                                5
                        )
                );

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2026
                        )
        ).thenReturn(
                current
        );

        List<TeamGameStats> prior =
                List.of(
                        createStat(
                                1L,
                                4L,
                                date(
                                        2025,
                                        9,
                                        1
                                ),
                                "COMPLETED",
                                20,
                                10,
                                300,
                                200,
                                100,
                                2,
                                4,
                                10,
                                3,
                                5
                        ),
                        createStat(
                                1L,
                                5L,
                                date(
                                        2025,
                                        9,
                                        8
                                ),
                                "COMPLETED",
                                20,
                                10,
                                300,
                                200,
                                100,
                                2,
                                4,
                                10,
                                3,
                                5
                        )
                );

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2025
                        )
        ).thenReturn(
                prior
        );

        TeamFeatureSnapshot snapshot =
                featureEngineeringService
                        .buildTeamFeatures(
                                1L,
                                2026,
                                date(
                                        2026,
                                        9,
                                        20
                                )
                        );

        assertEquals(
                30.0,
                snapshot.averagePoints(),
                0.0001
        );

        assertEquals(
                400.0,
                snapshot.averageTotalYards(),
                0.0001
        );
    }

    // =========================================================
    // THREE-GAME BLEND
    //
    // 75% current
    // 25% prior
    // =========================================================

    @Test
    void blendsThreeCurrentGamesAtSeventyFivePercent() {

        List<TeamGameStats> current =
                new ArrayList<>();

        for (int i = 0; i < 3; i++) {

            current.add(
                    createStat(
                            1L,
                            10L + i,
                            date(
                                    2026,
                                    9,
                                    1 + i
                            ),
                            "COMPLETED",
                            40,
                            20,
                            500,
                            300,
                            200,
                            1,
                            6,
                            10,
                            5,
                            5
                    )
            );
        }

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2026
                        )
        ).thenReturn(
                current
        );

        List<TeamGameStats> prior =
                List.of(
                        createStat(
                                1L,
                                20L,
                                date(
                                        2025,
                                        9,
                                        1
                                ),
                                "COMPLETED",
                                20,
                                10,
                                300,
                                200,
                                100,
                                2,
                                4,
                                10,
                                3,
                                5
                        )
                );

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2025
                        )
        ).thenReturn(
                prior
        );

        TeamFeatureSnapshot snapshot =
                featureEngineeringService
                        .buildTeamFeatures(
                                1L,
                                2026,
                                date(
                                        2026,
                                        9,
                                        20
                                )
                        );

        assertEquals(
                35.0,
                snapshot.averagePoints(),
                0.0001
        );

        assertEquals(
                450.0,
                snapshot.averageTotalYards(),
                0.0001
        );
    }

    // =========================================================
    // FOUR+ GAMES
    //
    // 100% current season
    // =========================================================

    @Test
    void usesOnlyCurrentSeasonAfterFourGames() {

        List<TeamGameStats> current =
                new ArrayList<>();

        for (int i = 0; i < 4; i++) {

            current.add(
                    createStat(
                            1L,
                            10L + i,
                            date(
                                    2026,
                                    9,
                                    1 + i
                            ),
                            "COMPLETED",
                            40,
                            20,
                            500,
                            300,
                            200,
                            1,
                            6,
                            10,
                            5,
                            5
                    )
            );
        }

        /*
         * Create the prior-season mock BEFORE
         * using it inside thenReturn().
         *
         * createStat() performs Mockito stubbing
         * internally, so it cannot safely be called
         * from inside another unfinished when().
         */
        TeamGameStats priorStat =
                createStat(
                        1L,
                        50L,
                        date(
                                2025,
                                9,
                                1
                        ),
                        "COMPLETED",
                        10,
                        50,
                        100,
                        75,
                        25,
                        5,
                        1,
                        10,
                        1,
                        5
                );

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2026
                        )
        ).thenReturn(
                current
        );

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2025
                        )
        ).thenReturn(
                List.of(
                        priorStat
                )
        );

        TeamFeatureSnapshot snapshot =
                featureEngineeringService
                        .buildTeamFeatures(
                                1L,
                                2026,
                                date(
                                        2026,
                                        10,
                                        1
                                )
                        );

        /*
         * Once four current-season games exist,
         * the prior season should have zero weight.
         */
        assertEquals(
                4,
                snapshot.gamesPlayed()
        );

        assertEquals(
                40.0,
                snapshot.averagePoints(),
                0.0001
        );

        assertEquals(
                500.0,
                snapshot.averageTotalYards(),
                0.0001
        );

        assertEquals(
                300.0,
                snapshot.averagePassingYards(),
                0.0001
        );

        assertEquals(
                200.0,
                snapshot.averageRushingYards(),
                0.0001
        );

        assertEquals(
                1.0,
                snapshot.averageTurnovers(),
                0.0001
        );

        assertEquals(
                20.0,
                snapshot.averageScoringMargin(),
                0.0001
        );
    }

    // =========================================================
    // RECENT WIN RATE
    // =========================================================

    @Test
    void recentWinRateUsesMostRecentFiveGames() {

        List<TeamGameStats> stats =
                List.of(
                        // Oldest loss - should be ignored
                        createStat(
                                1L,
                                2L,
                                date(2026, 8, 20),
                                "COMPLETED",
                                10,
                                20,
                                300,
                                200,
                                100,
                                1,
                                4,
                                10,
                                3,
                                5
                        ),

                        // Most recent five:
                        // W
                        createStat(
                                1L,
                                3L,
                                date(2026, 9, 1),
                                "COMPLETED",
                                30,
                                20,
                                400,
                                250,
                                150,
                                1,
                                5,
                                10,
                                4,
                                5
                        ),

                        // W
                        createStat(
                                1L,
                                4L,
                                date(2026, 9, 8),
                                "COMPLETED",
                                28,
                                21,
                                400,
                                250,
                                150,
                                1,
                                5,
                                10,
                                4,
                                5
                        ),

                        // L
                        createStat(
                                1L,
                                5L,
                                date(2026, 9, 15),
                                "COMPLETED",
                                14,
                                24,
                                400,
                                250,
                                150,
                                1,
                                5,
                                10,
                                4,
                                5
                        ),

                        // W
                        createStat(
                                1L,
                                6L,
                                date(2026, 9, 22),
                                "COMPLETED",
                                35,
                                14,
                                400,
                                250,
                                150,
                                1,
                                5,
                                10,
                                4,
                                5
                        ),

                        // L
                        createStat(
                                1L,
                                7L,
                                date(2026, 9, 29),
                                "COMPLETED",
                                17,
                                24,
                                400,
                                250,
                                150,
                                1,
                                5,
                                10,
                                4,
                                5
                        )
                );

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2026
                        )
        ).thenReturn(
                stats
        );

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2025
                        )
        ).thenReturn(
                List.of()
        );

        TeamFeatureSnapshot snapshot =
                featureEngineeringService
                        .buildTeamFeatures(
                                1L,
                                2026,
                                date(
                                        2026,
                                        10,
                                        5
                                )
                        );

        /*
         * Most recent five:
         *
         * W W L W L
         *
         * 3 / 5 = 0.60
         */

        assertEquals(
                0.60,
                snapshot.recentWinRate(),
                0.0001
        );
    }

    // =========================================================
    // RATE CALCULATIONS
    // =========================================================

    @Test
    void calculatesThirdDownAndRedZoneRates() {

        TeamGameStats stat =
                createStat(
                        1L,
                        2L,
                        date(
                                2026,
                                9,
                                1
                        ),
                        "COMPLETED",
                        30,
                        20,
                        400,
                        250,
                        150,
                        1,

                        // 5 / 10 = 50%
                        5,
                        10,

                        // 4 / 5 = 80%
                        4,
                        5
                );

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2026
                        )
        ).thenReturn(
                List.of(
                        stat
                )
        );

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2025
                        )
        ).thenReturn(
                List.of()
        );

        TeamFeatureSnapshot snapshot =
                featureEngineeringService
                        .buildTeamFeatures(
                                1L,
                                2026,
                                date(
                                        2026,
                                        9,
                                        10
                                )
                        );

        assertEquals(
                50.0,
                snapshot.thirdDownRate(),
                0.0001
        );

        assertEquals(
                80.0,
                snapshot.redZoneRate(),
                0.0001
        );
    }

    // =========================================================
    // OPPONENT STRENGTH
    // =========================================================

    @Test
    void calculatesOpponentWinRateAndScoringMargin() {

        LocalDateTime cutoff =
                date(
                        2026,
                        10,
                        1
                );

        /*
         * Team 1 played Team 2.
         */

        TeamGameStats teamOneVsTwo =
                createStat(
                        1L,
                        2L,
                        date(
                                2026,
                                9,
                                20
                        ),
                        "COMPLETED",
                        28,
                        24,
                        400,
                        250,
                        150,
                        1,
                        5,
                        10,
                        4,
                        5
                );

        /*
         * Team 2's season:
         *
         * W 30-20 = +10
         * L 20-24 = -4
         *
         * win rate = .5
         * average margin = +3
         */

        TeamGameStats teamTwoWin =
                createStat(
                        2L,
                        3L,
                        date(
                                2026,
                                9,
                                1
                        ),
                        "COMPLETED",
                        30,
                        20,
                        400,
                        250,
                        150,
                        1,
                        5,
                        10,
                        4,
                        5
                );

        TeamGameStats teamTwoLoss =
                createStat(
                        2L,
                        4L,
                        date(
                                2026,
                                9,
                                10
                        ),
                        "COMPLETED",
                        20,
                        24,
                        350,
                        220,
                        130,
                        2,
                        4,
                        10,
                        3,
                        5
                );

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2026
                        )
        ).thenReturn(
                List.of(
                        teamOneVsTwo,
                        teamTwoWin,
                        teamTwoLoss
                )
        );

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2025
                        )
        ).thenReturn(
                List.of()
        );

        TeamFeatureSnapshot snapshot =
                featureEngineeringService
                        .buildTeamFeatures(
                                1L,
                                2026,
                                cutoff
                        );

        assertEquals(
                0.5,
                snapshot.averageOpponentWinRate(),
                0.0001
        );

        assertEquals(
                3.0,
                snapshot.averageOpponentScoringMargin(),
                0.0001
        );
    }

    // =========================================================
    // MATCHUP DIFFERENCES
    // =========================================================

    @Test
    void buildsAllMatchupDifferenceFeatures() {

        TeamGameStats homeStat =
                createStat(
                        1L,
                        10L,
                        date(
                                2026,
                                9,
                                1
                        ),
                        "COMPLETED",
                        35,
                        20,
                        450,
                        280,
                        170,
                        1,
                        6,
                        10,
                        5,
                        5
                );

        TeamGameStats awayStat =
                createStat(
                        2L,
                        11L,
                        date(
                                2026,
                                9,
                                2
                        ),
                        "COMPLETED",
                        21,
                        28,
                        350,
                        220,
                        130,
                        2,
                        4,
                        10,
                        3,
                        5
                );

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2026
                        )
        ).thenReturn(
                List.of(
                        homeStat,
                        awayStat
                )
        );

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2025
                        )
        ).thenReturn(
                List.of()
        );

        MatchupFeatureSnapshot matchup =
                featureEngineeringService
                        .buildMatchupFeatures(
                                1L,
                                2L,
                                2026,
                                date(
                                        2026,
                                        9,
                                        20
                                )
                        );

        assertEquals(
                14.0,
                matchup.pointsDifference(),
                0.0001
        );

        assertEquals(
                100.0,
                matchup.yardsDifference(),
                0.0001
        );

        /*
         * Turnover difference intentionally uses:
         *
         * away turnovers - home turnovers
         *
         * because fewer turnovers are better.
         */

        assertEquals(
                1.0,
                matchup.turnoverDifference(),
                0.0001
        );

        /*
         * Home margin = +15
         * Away margin = -7
         *
         * difference = 22
         */

        assertEquals(
                22.0,
                matchup.scoringMarginDifference(),
                0.0001
        );

        /*
         * Third down:
         *
         * Home 60%
         * Away 40%
         */

        assertEquals(
                20.0,
                matchup.thirdDownDifference(),
                0.0001
        );

        /*
         * Red zone:
         *
         * Home 100%
         * Away 60%
         */

        assertEquals(
                40.0,
                matchup.redZoneDifference(),
                0.0001
        );

        /*
         * Both won/lost one game:
         *
         * Home recent = 1
         * Away recent = 0
         */

        assertEquals(
                1.0,
                matchup.recentFormDifference(),
                0.0001
        );

        /*
         * Opponents have no history in this fixture,
         * so both fall back to neutral .5 / 0.
         */

        assertEquals(
                0.0,
                matchup.opponentWinRateDifference(),
                0.0001
        );

        assertEquals(
                0.0,
                matchup.opponentScoringMarginDifference(),
                0.0001
        );
    }

    // =========================================================
    // CACHE REUSE
    // =========================================================

    @Test
    void seasonCachePreventsRepeatedRepositoryQueries() {

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2026
                        )
        ).thenReturn(
                List.of()
        );

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2025
                        )
        ).thenReturn(
                List.of()
        );

        LocalDateTime cutoff =
                date(
                        2026,
                        9,
                        1
                );

        featureEngineeringService
                .buildTeamFeatures(
                        1L,
                        2026,
                        cutoff
                );

        featureEngineeringService
                .buildTeamFeatures(
                        1L,
                        2026,
                        cutoff
                );

        verify(
                statsRepository,
                times(1)
        ).findByGameSeasonOrderByGameGameDateAsc(
                2026
        );

        verify(
                statsRepository,
                times(1)
        ).findByGameSeasonOrderByGameGameDateAsc(
                2025
        );
    }

    // =========================================================
    // SINGLE-SEASON CACHE CLEARING
    // =========================================================

    @Test
    void clearSeasonCacheReloadsOnlySelectedSeason() {

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2026
                        )
        ).thenReturn(
                List.of()
        );

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2025
                        )
        ).thenReturn(
                List.of()
        );

        LocalDateTime cutoff =
                date(
                        2026,
                        9,
                        1
                );

        featureEngineeringService
                .buildTeamFeatures(
                        1L,
                        2026,
                        cutoff
                );

        featureEngineeringService
                .clearSeasonCache(
                        2026
                );

        featureEngineeringService
                .buildTeamFeatures(
                        1L,
                        2026,
                        cutoff
                );

        verify(
                statsRepository,
                times(2)
        ).findByGameSeasonOrderByGameGameDateAsc(
                2026
        );

        /*
         * 2025 remained cached.
         */

        verify(
                statsRepository,
                times(1)
        ).findByGameSeasonOrderByGameGameDateAsc(
                2025
        );
    }

    // =========================================================
    // FULL CACHE CLEARING
    // =========================================================

    @Test
    void clearCacheReloadsAllSeasons() {

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2026
                        )
        ).thenReturn(
                List.of()
        );

        when(
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                2025
                        )
        ).thenReturn(
                List.of()
        );

        LocalDateTime cutoff =
                date(
                        2026,
                        9,
                        1
                );

        featureEngineeringService
                .buildTeamFeatures(
                        1L,
                        2026,
                        cutoff
                );

        featureEngineeringService
                .clearCache();

        featureEngineeringService
                .buildTeamFeatures(
                        1L,
                        2026,
                        cutoff
                );

        verify(
                statsRepository,
                times(2)
        ).findByGameSeasonOrderByGameGameDateAsc(
                2026
        );

        verify(
                statsRepository,
                times(2)
        ).findByGameSeasonOrderByGameGameDateAsc(
                2025
        );
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private LocalDateTime date(
            int year,
            int month,
            int day
    ) {

        return LocalDateTime.of(
                year,
                month,
                day,
                12,
                0
        );
    }

    private TeamGameStats createStat(
            Long teamId,
            Long opponentId,
            LocalDateTime gameDate,
            String status,
            Integer teamScore,
            Integer opponentScore,
            Integer totalYards,
            Integer passingYards,
            Integer rushingYards,
            Integer turnovers,
            Integer thirdDownConversions,
            Integer thirdDownAttempts,
            Integer redZoneScores,
            Integer redZoneAttempts
    ) {

        Team team =
                mock(Team.class);

        Team opponent =
                mock(Team.class);

        when(
                team.getId()
        ).thenReturn(
                teamId
        );

        when(
                opponent.getId()
        ).thenReturn(
                opponentId
        );

        Game game =
                mock(Game.class);

        /*
         * For these fixtures, the tracked team is always
         * treated as the home team.
         */

        when(
                game.getHomeTeam()
        ).thenReturn(
                team
        );

        when(
                game.getAwayTeam()
        ).thenReturn(
                opponent
        );

        when(
                game.getGameDate()
        ).thenReturn(
                gameDate
        );

        when(
                game.getStatus()
        ).thenReturn(
                status
        );

        when(
                game.getHomeScore()
        ).thenReturn(
                teamScore
        );

        when(
                game.getAwayScore()
        ).thenReturn(
                opponentScore
        );

        TeamGameStats stat =
                mock(TeamGameStats.class);

        when(
                stat.getTeam()
        ).thenReturn(
                team
        );

        when(
                stat.getGame()
        ).thenReturn(
                game
        );

        when(
                stat.getPoints()
        ).thenReturn(
                teamScore
        );

        when(
                stat.getTotalYards()
        ).thenReturn(
                totalYards
        );

        when(
                stat.getPassingYards()
        ).thenReturn(
                passingYards
        );

        when(
                stat.getRushingYards()
        ).thenReturn(
                rushingYards
        );

        when(
                stat.getTurnovers()
        ).thenReturn(
                turnovers
        );

        when(
                stat.getThirdDownConversions()
        ).thenReturn(
                thirdDownConversions
        );

        when(
                stat.getThirdDownAttempts()
        ).thenReturn(
                thirdDownAttempts
        );

        when(
                stat.getRedZoneScores()
        ).thenReturn(
                redZoneScores
        );

        when(
                stat.getRedZoneAttempts()
        ).thenReturn(
                redZoneAttempts
        );

        return stat;
    }
}