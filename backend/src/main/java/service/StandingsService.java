package com.fourthdown.ai.service;

import com.fourthdown.ai.dto.TeamStandingResponse;
import com.fourthdown.ai.model.Game;
import com.fourthdown.ai.model.Team;
import com.fourthdown.ai.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StandingsService {

    private final GameRepository gameRepository;

    public StandingsService(
            GameRepository gameRepository
    ) {
        this.gameRepository =
                gameRepository;
    }

    // =========================================================
    // GET SEASON STANDINGS
    // =========================================================

    public List<TeamStandingResponse> getStandings(
            int season,
            String classification
    ) {

        return getStandings(
                season,
                classification,
                null
        );
    }

    public List<TeamStandingResponse> getStandings(
            int season,
            String classification,
            Integer rankingWeek
    ) {

        String normalizedClassification =
                classification
                        .trim()
                        .toLowerCase();

        validateClassification(
                normalizedClassification
        );

        /*
         * Only completed games should count toward
         * team records.
         */
        List<Game> games =
                gameRepository
                        .findBySeasonAndStatusOrderByGameDateAsc(
                                season,
                                "COMPLETED"
                        );

        /*
         * teamId -> mutable standing
         */
        Map<Long, MutableStanding> standings =
                new HashMap<>();

        // =====================================================
        // PROCESS EVERY COMPLETED GAME
        // =====================================================

        for (Game game : games) {

            if (
                    game == null ||
                            game.getHomeTeam() == null ||
                            game.getAwayTeam() == null ||
                            game.getHomeScore() == null ||
                            game.getAwayScore() == null
            ) {
                continue;
            }

            /*
             * CFBD poll "Week N" represents the poll entering Week N.
             *
             * Therefore the record shown beside that poll should only
             * include games completed BEFORE Week N. Example:
             *
             * Week 1 poll -> preseason / 0-0 record
             * Week 7 poll -> record through Week 6
             *
             * When rankingWeek is null, keep the original behavior and
             * use every completed game in the season.
             */
            if (rankingWeek != null) {

                /*
                 * Weekly polls are based on regular-season results
                 * available at that point in the season.
                 *
                 * CFBD postseason week numbers restart, so a bowl or
                 * playoff game can have a small week number such as 1
                 * or 2. If we only compare the numeric week, those
                 * postseason games would incorrectly be counted in a
                 * Week 15 poll from the regular season.
                 */
                if (
                        game.getSeasonType() != null &&
                                !game.getSeasonType()
                                        .equalsIgnoreCase(
                                                "regular"
                                        )
                ) {
                    continue;
                }

                /*
                 * Poll Week N represents the rankings entering Week N,
                 * so only games from earlier regular-season weeks count.
                 */
                if (
                        game.getWeek() == null ||
                                game.getWeek() >= rankingWeek
                ) {
                    continue;
                }
            }

            Team homeTeam =
                    game.getHomeTeam();

            Team awayTeam =
                    game.getAwayTeam();

            Integer homeScore =
                    game.getHomeScore();

            Integer awayScore =
                    game.getAwayScore();

            /*
             * A selected FBS team should still get credit
             * for a game against an FCS opponent, and vice
             * versa.
             *
             * Therefore, we filter teams individually rather
             * than throwing away cross-classification games.
             */

            if (
                    matchesClassification(
                            homeTeam,
                            normalizedClassification
                    )
            ) {

                MutableStanding homeStanding =
                        standings.computeIfAbsent(
                                homeTeam.getId(),
                                ignored ->
                                        new MutableStanding(
                                                homeTeam
                                        )
                        );

                homeStanding.recordGame(
                        homeScore,
                        awayScore
                );
            }

            if (
                    matchesClassification(
                            awayTeam,
                            normalizedClassification
                    )
            ) {

                MutableStanding awayStanding =
                        standings.computeIfAbsent(
                                awayTeam.getId(),
                                ignored ->
                                        new MutableStanding(
                                                awayTeam
                                        )
                        );

                awayStanding.recordGame(
                        awayScore,
                        homeScore
                );
            }
        }

        // =====================================================
        // SORT STANDINGS
        // =====================================================

        List<MutableStanding> sorted =
                new ArrayList<>(
                        standings.values()
                );

        sorted.sort(
                Comparator
                        .comparingDouble(
                                MutableStanding::getWinPercentage
                        )
                        .reversed()

                        .thenComparing(
                                Comparator.comparingInt(
                                                MutableStanding::getPointDifferential
                                        )
                                        .reversed()
                        )

                        .thenComparing(
                                Comparator.comparingInt(
                                                MutableStanding::getPointsFor
                                        )
                                        .reversed()
                        )

                        .thenComparing(
                                standing ->
                                        standing.team
                                                .getName(),
                                String.CASE_INSENSITIVE_ORDER
                        )
        );

        // =====================================================
        // CREATE API RESPONSE + RANK
        // =====================================================

        List<TeamStandingResponse> response =
                new ArrayList<>();

        for (
                int index = 0;
                index < sorted.size();
                index++
        ) {

            MutableStanding standing =
                    sorted.get(index);

            Team team =
                    standing.team;

            response.add(
                    new TeamStandingResponse(
                            index + 1,

                            team.getId(),

                            team.getName(),

                            team.getAbbreviation(),

                            team.getConference(),

                            team.getClassification(),

                            team.getLogoUrl(),

                            standing.gamesPlayed,

                            standing.wins,

                            standing.losses,

                            standing.ties,

                            round(
                                    standing.getWinPercentage()
                            ),

                            standing.pointsFor,

                            standing.pointsAgainst,

                            standing.getPointDifferential()
                    )
            );
        }

        return response;
    }

    // =========================================================
    // CLASSIFICATION CHECK
    // =========================================================

    private boolean matchesClassification(
            Team team,
            String classification
    ) {

        if (
                team == null ||
                        team.getClassification() == null
        ) {
            return false;
        }

        return team
                .getClassification()
                .equalsIgnoreCase(
                        classification
                );
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateClassification(
            String classification
    ) {

        if (
                !"fbs".equalsIgnoreCase(
                        classification
                ) &&
                        !"fcs".equalsIgnoreCase(
                                classification
                        )
        ) {

            throw new IllegalArgumentException(
                    "Classification must be fbs or fcs"
            );
        }
    }

    // =========================================================
    // ROUND DOUBLE
    // =========================================================

    private double round(
            double value
    ) {

        return Math.round(
                value * 1000.0
        ) / 1000.0;
    }

    // =========================================================
    // INTERNAL MUTABLE STANDING
    // =========================================================

    private static class MutableStanding {

        private final Team team;

        private int gamesPlayed;

        private int wins;

        private int losses;

        private int ties;

        private int pointsFor;

        private int pointsAgainst;

        private MutableStanding(
                Team team
        ) {
            this.team =
                    team;
        }

        // =====================================================
        // RECORD ONE GAME
        // =====================================================

        private void recordGame(
                int teamScore,
                int opponentScore
        ) {

            gamesPlayed++;

            pointsFor +=
                    teamScore;

            pointsAgainst +=
                    opponentScore;

            if (
                    teamScore >
                            opponentScore
            ) {

                wins++;

            } else if (
                    teamScore <
                            opponentScore
            ) {

                losses++;

            } else {

                ties++;
            }
        }

        // =====================================================
        // WIN PERCENTAGE
        // =====================================================

        private double getWinPercentage() {

            if (
                    gamesPlayed == 0
            ) {
                return 0.0;
            }

            /*
             * A tie counts as half a win.
             */
            return (
                    wins +
                            (ties * 0.5)
            ) / gamesPlayed;
        }

        // =====================================================
        // POINT DIFFERENTIAL
        // =====================================================

        private int getPointDifferential() {

            return pointsFor -
                    pointsAgainst;
        }

        private int getPointsFor() {

            return pointsFor;
        }
    }
}