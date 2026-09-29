package com.fourthdown.ai.service;

import com.fourthdown.ai.dto.ml.MatchupFeatureSnapshot;
import com.fourthdown.ai.dto.ml.TeamFeatureSnapshot;
import com.fourthdown.ai.model.Game;
import com.fourthdown.ai.model.TeamGameStats;
import com.fourthdown.ai.repository.TeamGameStatsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class FeatureEngineeringService {

    private static final int CURRENT_SEASON_FULL_WEIGHT_GAMES = 4;

    private final TeamGameStatsRepository statsRepository;

    /*
     * =========================================================
     * SEASON CACHE
     * =========================================================
     *
     * Structure:
     *
     * season
     *   -> teamId
     *       -> list of that team's TeamGameStats
     *
     * Example:
     *
     * 2025
     *   -> Auburn ID
     *       -> Auburn 2025 game stats
     *
     *   -> Florida ID
     *       -> Florida 2025 game stats
     *
     * This eliminates thousands of repeated PostgreSQL queries
     * while generating training data.
     */
    private final Map<
            Integer,
            Map<Long, List<TeamGameStats>>
            > seasonStatsCache =
            new ConcurrentHashMap<>();


    public FeatureEngineeringService(
            TeamGameStatsRepository statsRepository
    ) {
        this.statsRepository =
                statsRepository;
    }


    // =========================================================
    // MATCHUP FEATURES
    // =========================================================

    public MatchupFeatureSnapshot buildMatchupFeatures(
            Long homeTeamId,
            Long awayTeamId,
            Integer season,
            LocalDateTime cutoff
    ) {

        TeamFeatureSnapshot home =
                buildTeamFeatures(
                        homeTeamId,
                        season,
                        cutoff
                );

        TeamFeatureSnapshot away =
                buildTeamFeatures(
                        awayTeamId,
                        season,
                        cutoff
                );

        return new MatchupFeatureSnapshot(
                home,
                away,

                home.averagePoints() -
                        away.averagePoints(),

                home.averageTotalYards() -
                        away.averageTotalYards(),

                /*
                 * Lower turnovers are better.
                 *
                 * Positive value =
                 * advantage home team.
                 */
                away.averageTurnovers() -
                        home.averageTurnovers(),

                home.averageScoringMargin() -
                        away.averageScoringMargin(),

                home.thirdDownRate() -
                        away.thirdDownRate(),

                home.redZoneRate() -
                        away.redZoneRate(),

                home.recentWinRate() -
                        away.recentWinRate(),

                home.averageOpponentWinRate() -
                        away.averageOpponentWinRate(),

                home.averageOpponentScoringMargin() -
                        away.averageOpponentScoringMargin()
        );
    }


    // =========================================================
    // TEAM FEATURES
    // =========================================================

    public TeamFeatureSnapshot buildTeamFeatures(
            Long teamId,
            Integer season,
            LocalDateTime cutoff
    ) {

        if (teamId == null) {

            throw new IllegalArgumentException(
                    "Team ID is required"
            );
        }

        if (season == null) {

            throw new IllegalArgumentException(
                    "Season is required"
            );
        }

        if (cutoff == null) {

            throw new IllegalArgumentException(
                    "Feature cutoff is required"
            );
        }


        // =====================================================
        // CURRENT-SEASON STATS BEFORE CUTOFF
        // =====================================================

        List<TeamGameStats> currentSeasonStats =
                getTeamSeasonStats(
                        teamId,
                        season
                )
                        .stream()
                        .filter(
                                this::isCompletedGame
                        )
                        .filter(stat ->
                                isBeforeCutoff(
                                        stat,
                                        cutoff
                                )
                        )
                        .toList();


        // =====================================================
        // PRIOR-SEASON STATS
        // =====================================================

        Integer priorSeason =
                season - 1;

        List<TeamGameStats> priorSeasonStats =
                getTeamSeasonStats(
                        teamId,
                        priorSeason
                )
                        .stream()
                        .filter(
                                this::isCompletedGame
                        )
                        .toList();


        // =====================================================
        // NO HISTORY
        // =====================================================

        if (currentSeasonStats.isEmpty() &&
                priorSeasonStats.isEmpty()) {

            return TeamFeatureSnapshot.empty();
        }


        // =====================================================
        // CALCULATE CURRENT-SEASON SNAPSHOT
        // =====================================================

        TeamFeatureSnapshot currentSnapshot =
                calculateSnapshot(
                        currentSeasonStats,
                        teamId,
                        season,
                        cutoff
                );


        // =====================================================
        // CALCULATE PRIOR-SEASON SNAPSHOT
        // =====================================================

        TeamFeatureSnapshot priorSnapshot =
                calculateSnapshot(
                        priorSeasonStats,
                        teamId,
                        priorSeason,
                        endOfSeasonCutoff(
                                priorSeason
                        )
                );


        int currentGames =
                currentSeasonStats.size();

        int priorGames =
                priorSeasonStats.size();


        // =====================================================
        // NO PRIOR-SEASON DATA
        // =====================================================

        if (priorGames == 0) {

            return currentSnapshot;
        }


        // =====================================================
        // CURRENT SEASON HAS NOT STARTED
        //
        // 100% previous season.
        // =====================================================

        if (currentGames == 0) {

            return priorSnapshot;
        }


        // =====================================================
        // FOUR OR MORE CURRENT-SEASON GAMES
        //
        // 100% current season.
        // =====================================================

        if (currentGames >=
                CURRENT_SEASON_FULL_WEIGHT_GAMES) {

            return currentSnapshot;
        }


        // =====================================================
        // EARLY-SEASON WEIGHTING
        //
        // 1 game:
        // 25% current / 75% prior
        //
        // 2 games:
        // 50% current / 50% prior
        //
        // 3 games:
        // 75% current / 25% prior
        // =====================================================

        double currentWeight =
                (double) currentGames /
                        CURRENT_SEASON_FULL_WEIGHT_GAMES;

        double priorWeight =
                1.0 -
                        currentWeight;

        return blendSnapshots(
                currentSnapshot,
                priorSnapshot,
                currentGames,
                priorGames,
                currentWeight,
                priorWeight
        );
    }


    // =========================================================
    // CALCULATE TEAM SNAPSHOT
    // =========================================================

    private TeamFeatureSnapshot calculateSnapshot(
            List<TeamGameStats> stats,
            Long teamId,
            Integer season,
            LocalDateTime cutoff
    ) {

        if (stats == null ||
                stats.isEmpty()) {

            return TeamFeatureSnapshot.empty();
        }


        double points =
                0;

        double totalYards =
                0;

        double passingYards =
                0;

        double rushingYards =
                0;

        double turnovers =
                0;

        double scoringMargin =
                0;


        int thirdDownConversions =
                0;

        int thirdDownAttempts =
                0;

        int redZoneScores =
                0;

        int redZoneAttempts =
                0;

        int validMarginGames =
                0;


        List<Long> opponentIds =
                new ArrayList<>();


        for (TeamGameStats stat :
                stats) {

            points +=
                    safe(
                            stat.getPoints()
                    );

            totalYards +=
                    safe(
                            stat.getTotalYards()
                    );

            passingYards +=
                    safe(
                            stat.getPassingYards()
                    );

            rushingYards +=
                    safe(
                            stat.getRushingYards()
                    );

            turnovers +=
                    safe(
                            stat.getTurnovers()
                    );


            thirdDownConversions +=
                    safe(
                            stat.getThirdDownConversions()
                    );

            thirdDownAttempts +=
                    safe(
                            stat.getThirdDownAttempts()
                    );


            redZoneScores +=
                    safe(
                            stat.getRedZoneScores()
                    );

            redZoneAttempts +=
                    safe(
                            stat.getRedZoneAttempts()
                    );


            Game game =
                    stat.getGame();


            Integer teamScore =
                    getTeamScore(
                            game,
                            teamId
                    );

            Integer opponentScore =
                    getOpponentScore(
                            game,
                            teamId
                    );


            if (teamScore != null &&
                    opponentScore != null) {

                scoringMargin +=
                        teamScore -
                                opponentScore;

                validMarginGames++;
            }


            Long opponentId =
                    getOpponentTeamId(
                            game,
                            teamId
                    );


            if (opponentId != null) {

                opponentIds.add(
                        opponentId
                );
            }
        }


        int gamesPlayed =
                stats.size();


        double averageScoringMargin =
                validMarginGames == 0
                        ? 0
                        : scoringMargin /
                        validMarginGames;


        double thirdDownRate =
                thirdDownAttempts == 0
                        ? 0
                        : (
                        (double) thirdDownConversions /
                                thirdDownAttempts
                ) * 100.0;


        double redZoneRate =
                redZoneAttempts == 0
                        ? 0
                        : (
                        (double) redZoneScores /
                                redZoneAttempts
                ) * 100.0;


        double recentWinRate =
                calculateRecentWinRate(
                        stats,
                        teamId
                );


        /*
         * Calculate both opponent-strength values
         * at the same time.
         *
         * This avoids evaluating every opponent twice.
         */
        OpponentStrengthSnapshot opponentStrength =
                calculateOpponentStrength(
                        opponentIds,
                        season,
                        cutoff
                );


        return new TeamFeatureSnapshot(
                gamesPlayed,

                points /
                        gamesPlayed,

                totalYards /
                        gamesPlayed,

                passingYards /
                        gamesPlayed,

                rushingYards /
                        gamesPlayed,

                turnovers /
                        gamesPlayed,

                averageScoringMargin,

                thirdDownRate,

                redZoneRate,

                recentWinRate,

                opponentStrength.averageWinRate(),

                opponentStrength.averageScoringMargin()
        );
    }


    // =========================================================
    // OPPONENT STRENGTH
    //
    // Calculates BOTH:
    //
    // - average opponent win rate
    // - average opponent scoring margin
    //
    // in a single pass.
    //
    // No PostgreSQL query occurs inside this loop.
    // =========================================================

    private OpponentStrengthSnapshot calculateOpponentStrength(
            List<Long> opponentIds,
            Integer season,
            LocalDateTime cutoff
    ) {

        if (opponentIds == null ||
                opponentIds.isEmpty()) {

            return OpponentStrengthSnapshot.neutral();
        }


        double totalOpponentWinRate =
                0;

        double totalOpponentScoringMargin =
                0;

        int validOpponents =
                0;


        for (Long opponentId :
                opponentIds) {


            // -------------------------------------------------
            // GET FROM IN-MEMORY SEASON CACHE
            // -------------------------------------------------

            List<TeamGameStats> opponentStats =
                    getTeamSeasonStats(
                            opponentId,
                            season
                    )
                            .stream()
                            .filter(
                                    this::isCompletedGame
                            )
                            .filter(stat ->
                                    isBeforeCutoff(
                                            stat,
                                            cutoff
                                    )
                            )
                            .toList();


            if (opponentStats.isEmpty()) {

                continue;
            }


            int wins =
                    0;

            int losses =
                    0;


            double marginTotal =
                    0;

            int marginGames =
                    0;


            for (TeamGameStats stat :
                    opponentStats) {

                Game game =
                        stat.getGame();


                Integer opponentTeamScore =
                        getTeamScore(
                                game,
                                opponentId
                        );

                Integer otherTeamScore =
                        getOpponentScore(
                                game,
                                opponentId
                        );


                if (opponentTeamScore == null ||
                        otherTeamScore == null) {

                    continue;
                }


                if (opponentTeamScore >
                        otherTeamScore) {

                    wins++;

                } else if (
                        opponentTeamScore <
                                otherTeamScore
                ) {

                    losses++;
                }


                marginTotal +=
                        opponentTeamScore -
                                otherTeamScore;

                marginGames++;
            }


            int recordGames =
                    wins +
                            losses;


            if (recordGames == 0 ||
                    marginGames == 0) {

                continue;
            }


            double opponentWinRate =
                    (double) wins /
                            recordGames;


            double opponentScoringMargin =
                    marginTotal /
                            marginGames;


            totalOpponentWinRate +=
                    opponentWinRate;


            totalOpponentScoringMargin +=
                    opponentScoringMargin;


            validOpponents++;
        }


        if (validOpponents == 0) {

            return OpponentStrengthSnapshot.neutral();
        }


        return new OpponentStrengthSnapshot(

                totalOpponentWinRate /
                        validOpponents,

                totalOpponentScoringMargin /
                        validOpponents
        );
    }


    // =========================================================
    // BLEND CURRENT + PRIOR SEASON
    // =========================================================

    private TeamFeatureSnapshot blendSnapshots(
            TeamFeatureSnapshot current,
            TeamFeatureSnapshot prior,
            int currentGames,
            int priorGames,
            double currentWeight,
            double priorWeight
    ) {

       
        int availableGames =
                currentGames +
                        priorGames;


        return new TeamFeatureSnapshot(
                availableGames,

                weightedAverage(
                        current.averagePoints(),
                        prior.averagePoints(),
                        currentWeight,
                        priorWeight
                ),

                weightedAverage(
                        current.averageTotalYards(),
                        prior.averageTotalYards(),
                        currentWeight,
                        priorWeight
                ),

                weightedAverage(
                        current.averagePassingYards(),
                        prior.averagePassingYards(),
                        currentWeight,
                        priorWeight
                ),

                weightedAverage(
                        current.averageRushingYards(),
                        prior.averageRushingYards(),
                        currentWeight,
                        priorWeight
                ),

                weightedAverage(
                        current.averageTurnovers(),
                        prior.averageTurnovers(),
                        currentWeight,
                        priorWeight
                ),

                weightedAverage(
                        current.averageScoringMargin(),
                        prior.averageScoringMargin(),
                        currentWeight,
                        priorWeight
                ),

                weightedAverage(
                        current.thirdDownRate(),
                        prior.thirdDownRate(),
                        currentWeight,
                        priorWeight
                ),

                weightedAverage(
                        current.redZoneRate(),
                        prior.redZoneRate(),
                        currentWeight,
                        priorWeight
                ),

                weightedAverage(
                        current.recentWinRate(),
                        prior.recentWinRate(),
                        currentWeight,
                        priorWeight
                ),

                weightedAverage(
                        current.averageOpponentWinRate(),
                        prior.averageOpponentWinRate(),
                        currentWeight,
                        priorWeight
                ),

                weightedAverage(
                        current.averageOpponentScoringMargin(),
                        prior.averageOpponentScoringMargin(),
                        currentWeight,
                        priorWeight
                )
        );
    }


    // =========================================================
    // WEIGHTED AVERAGE
    // =========================================================

    private double weightedAverage(
            double currentValue,
            double priorValue,
            double currentWeight,
            double priorWeight
    ) {

        return (
                currentValue *
                        currentWeight
        ) +
                (
                        priorValue *
                                priorWeight
                );
    }


    // =========================================================
    // RECENT WIN RATE
    // =========================================================

    private double calculateRecentWinRate(
            List<TeamGameStats> stats,
            Long teamId
    ) {

        if (stats == null ||
                stats.isEmpty()) {

            return 0.5;
        }


        List<TeamGameStats> recent =
                stats.stream()
                        .filter(
                                this::isCompletedGame
                        )
                        .sorted(
                                Comparator.comparing(
                                        (TeamGameStats stat) ->
                                                stat.getGame()
                                                        .getGameDate()
                                ).reversed()
                        )
                        .limit(5)
                        .toList();


        int wins =
                0;

        int losses =
                0;


        for (TeamGameStats stat :
                recent) {

            Game game =
                    stat.getGame();


            Integer teamScore =
                    getTeamScore(
                            game,
                            teamId
                    );

            Integer opponentScore =
                    getOpponentScore(
                            game,
                            teamId
                    );


            if (teamScore == null ||
                    opponentScore == null) {

                continue;
            }


            if (teamScore >
                    opponentScore) {

                wins++;

            } else if (
                    teamScore <
                            opponentScore
            ) {

                losses++;
            }
        }


        int total =
                wins +
                        losses;


        if (total == 0) {

            return 0.5;
        }


        return (double) wins /
                total;
    }


    // =========================================================
    // LOAD SEASON INTO CACHE
    // =========================================================

    private Map<Long, List<TeamGameStats>> getSeasonStats(
            Integer season
    ) {

        return seasonStatsCache
                .computeIfAbsent(
                        season,
                        this::loadSeasonStats
                );
    }


    // =========================================================
    // ACTUAL DATABASE LOAD
    // =========================================================

    private Map<Long, List<TeamGameStats>> loadSeasonStats(
            Integer season
    ) {

        System.out.println(
                "Loading team game stats into memory for season "
                        + season
        );


        List<TeamGameStats> seasonStats =
                statsRepository
                        .findByGameSeasonOrderByGameGameDateAsc(
                                season
                        );


        Map<Long, List<TeamGameStats>> grouped =
                seasonStats
                        .stream()
                        .filter(stat ->
                                stat != null &&
                                        stat.getTeam() != null &&
                                        stat.getTeam().getId() != null
                        )
                        .collect(
                                Collectors.groupingBy(
                                        stat ->
                                                stat.getTeam()
                                                        .getId()
                                )
                        );


        /*
         * Ensure each team list is chronological.
         */
        grouped.values()
                .forEach(list ->
                        list.sort(
                                Comparator.comparing(
                                        stat ->
                                                stat.getGame()
                                                        .getGameDate()
                                )
                        )
                );


        System.out.println(
                "Season "
                        + season
                        + " cache loaded: "
                        + grouped.size()
                        + " teams, "
                        + seasonStats.size()
                        + " stat rows"
        );


        return grouped;
    }


    // =========================================================
    // GET TEAM STATS FROM CACHED SEASON
    // =========================================================

    private List<TeamGameStats> getTeamSeasonStats(
            Long teamId,
            Integer season
    ) {

        if (teamId == null ||
                season == null) {

            return List.of();
        }


        Map<Long, List<TeamGameStats>> seasonStats =
                getSeasonStats(
                        season
                );


        return seasonStats
                .getOrDefault(
                        teamId,
                        List.of()
                );
    }


    // =========================================================
    // CACHE MANAGEMENT
    //
    // Useful after importing new CFBD data.
    // =========================================================

    public void clearCache() {

        seasonStatsCache.clear();
    }


    public void clearSeasonCache(
            Integer season
    ) {

        if (season == null) {
            return;
        }

        seasonStatsCache.remove(
                season
        );
    }


    // =========================================================
    // DATE FILTER
    // =========================================================

    private boolean isBeforeCutoff(
            TeamGameStats stat,
            LocalDateTime cutoff
    ) {

        if (stat == null ||
                stat.getGame() == null ||
                stat.getGame().getGameDate() == null ||
                cutoff == null) {

            return false;
        }


       
        return stat.getGame()
                .getGameDate()
                .isBefore(
                        cutoff
                );
    }


    // =========================================================
    // GET OPPONENT TEAM ID
    // =========================================================

    private Long getOpponentTeamId(
            Game game,
            Long teamId
    ) {

        if (game == null ||
                teamId == null) {

            return null;
        }


        if (game.getHomeTeam() != null &&
                game.getHomeTeam()
                        .getId()
                        .equals(teamId)) {

            return game.getAwayTeam() == null
                    ? null
                    : game.getAwayTeam()
                    .getId();
        }


        if (game.getAwayTeam() != null &&
                game.getAwayTeam()
                        .getId()
                        .equals(teamId)) {

            return game.getHomeTeam() == null
                    ? null
                    : game.getHomeTeam()
                    .getId();
        }


        return null;
    }


    // =========================================================
    // END-OF-SEASON CUTOFF
    // =========================================================

    private LocalDateTime endOfSeasonCutoff(
            Integer season
    ) {

        return LocalDateTime.of(
                season + 1,
                3,
                1,
                0,
                0
        );
    }


    // =========================================================
    // COMPLETED GAME FILTER
    // =========================================================

    private boolean isCompletedGame(
            TeamGameStats stat
    ) {

        if (stat == null ||
                stat.getGame() == null) {

            return false;
        }


        return "COMPLETED"
                .equalsIgnoreCase(
                        stat.getGame()
                                .getStatus()
                );
    }


    // =========================================================
    // TEAM SCORE
    // =========================================================

    private Integer getTeamScore(
            Game game,
            Long teamId
    ) {

        if (game == null ||
                teamId == null) {

            return null;
        }


        if (game.getHomeTeam() != null &&
                game.getHomeTeam()
                        .getId()
                        .equals(teamId)) {

            return game.getHomeScore();
        }


        if (game.getAwayTeam() != null &&
                game.getAwayTeam()
                        .getId()
                        .equals(teamId)) {

            return game.getAwayScore();
        }


        return null;
    }


    // =========================================================
    // OPPONENT SCORE
    // =========================================================

    private Integer getOpponentScore(
            Game game,
            Long teamId
    ) {

        if (game == null ||
                teamId == null) {

            return null;
        }


        if (game.getHomeTeam() != null &&
                game.getHomeTeam()
                        .getId()
                        .equals(teamId)) {

            return game.getAwayScore();
        }


        if (game.getAwayTeam() != null &&
                game.getAwayTeam()
                        .getId()
                        .equals(teamId)) {

            return game.getHomeScore();
        }


        return null;
    }


    // =========================================================
    // NULL-SAFE INTEGER
    // =========================================================

    private int safe(
            Integer value
    ) {

        return value == null
                ? 0
                : value;
    }


    // =========================================================
    // INTERNAL OPPONENT-STRENGTH RECORD
    // =========================================================

    private record OpponentStrengthSnapshot(
            double averageWinRate,
            double averageScoringMargin
    ) {

        private static OpponentStrengthSnapshot neutral() {

            return new OpponentStrengthSnapshot(
                    0.5,
                    0
            );
        }
    }
}