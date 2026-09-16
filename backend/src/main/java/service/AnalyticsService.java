package com.fourthdown.ai.service;

import com.fourthdown.ai.dto.TeamAnalyticsResponse;
import com.fourthdown.ai.dto.TeamComparisonResponse;
import com.fourthdown.ai.model.Game;
import com.fourthdown.ai.model.Team;
import com.fourthdown.ai.model.TeamGameStats;
import com.fourthdown.ai.repository.TeamGameStatsRepository;
import com.fourthdown.ai.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class AnalyticsService {

    private final TeamRepository teamRepository;
    private final TeamGameStatsRepository statsRepository;

    public AnalyticsService(
            TeamRepository teamRepository,
            TeamGameStatsRepository statsRepository
    ) {
        this.teamRepository = teamRepository;
        this.statsRepository = statsRepository;
    }

    public TeamAnalyticsResponse getTeamAnalytics(
            Long teamId
    ) {

        Team team = teamRepository
                .findById(teamId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Team not found"
                        )
                );

        List<TeamGameStats> stats =
                statsRepository.findByTeamId(teamId);

        TeamAnalyticsResponse response =
                new TeamAnalyticsResponse();

        response.setTeamId(team.getId());
        response.setTeamName(team.getName());
        response.setGamesPlayed(stats.size());

        if (stats.isEmpty()) {
            return response;
        }

        int wins = 0;
        int losses = 0;

        double totalPoints = 0;
        double totalYards = 0;
        double passingYards = 0;
        double rushingYards = 0;
        double turnovers = 0;

        double scoringMarginTotal = 0;

        int thirdDownConversions = 0;
        int thirdDownAttempts = 0;

        int redZoneScores = 0;
        int redZoneAttempts = 0;

        for (TeamGameStats stat : stats) {

            totalPoints += safe(stat.getPoints());
            totalYards += safe(stat.getTotalYards());
            passingYards += safe(stat.getPassingYards());
            rushingYards += safe(stat.getRushingYards());
            turnovers += safe(stat.getTurnovers());

            thirdDownConversions +=
                    safe(stat.getThirdDownConversions());

            thirdDownAttempts +=
                    safe(stat.getThirdDownAttempts());

            redZoneScores +=
                    safe(stat.getRedZoneScores());

            redZoneAttempts +=
                    safe(stat.getRedZoneAttempts());

            Game game = stat.getGame();

            Integer teamScore = null;
            Integer opponentScore = null;

            if (game.getHomeTeam().getId().equals(teamId)) {
                teamScore = game.getHomeScore();
                opponentScore = game.getAwayScore();
            } else if (
                    game.getAwayTeam().getId().equals(teamId)
            ) {
                teamScore = game.getAwayScore();
                opponentScore = game.getHomeScore();
            }

            if (teamScore != null &&
                    opponentScore != null) {

                scoringMarginTotal +=
                        teamScore - opponentScore;

                if (teamScore > opponentScore) {
                    wins++;
                } else if (teamScore < opponentScore) {
                    losses++;
                }
            }
        }

        int gamesPlayed = stats.size();

        response.setWins(wins);
        response.setLosses(losses);

        response.setAveragePoints(
                totalPoints / gamesPlayed
        );

        response.setAverageTotalYards(
                totalYards / gamesPlayed
        );

        response.setAveragePassingYards(
                passingYards / gamesPlayed
        );

        response.setAverageRushingYards(
                rushingYards / gamesPlayed
        );

        response.setAverageTurnovers(
                turnovers / gamesPlayed
        );

        response.setAverageScoringMargin(
                scoringMarginTotal / gamesPlayed
        );

        response.setThirdDownConversionRate(
                thirdDownAttempts == 0
                        ? 0
                        : (
                        (double) thirdDownConversions /
                                thirdDownAttempts
                ) * 100
        );

        response.setRedZoneScoringRate(
                redZoneAttempts == 0
                        ? 0
                        : (
                        (double) redZoneScores /
                                redZoneAttempts
                ) * 100
        );

        calculateRecentForm(
                response,
                stats,
                teamId
        );

        return response;
    }

    private void calculateRecentForm(
            TeamAnalyticsResponse response,
            List<TeamGameStats> stats,
            Long teamId
    ) {

        List<TeamGameStats> recentGames =
                stats.stream()
                        .filter(stat ->
                                stat.getGame() != null &&
                                        stat.getGame().getGameDate() != null
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

        int recentWins = 0;
        int recentLosses = 0;

        for (TeamGameStats stat : recentGames) {

            Game game = stat.getGame();

            Integer teamScore;
            Integer opponentScore;

            if (game.getHomeTeam()
                    .getId()
                    .equals(teamId)) {

                teamScore =
                        game.getHomeScore();

                opponentScore =
                        game.getAwayScore();

            } else {

                teamScore =
                        game.getAwayScore();

                opponentScore =
                        game.getHomeScore();
            }

            if (teamScore == null ||
                    opponentScore == null) {
                continue;
            }

            if (teamScore > opponentScore) {
                recentWins++;
            } else if (teamScore < opponentScore) {
                recentLosses++;
            }
        }

        response.setRecentWins(
                recentWins
        );

        response.setRecentLosses(
                recentLosses
        );
    }

    private int safe(
            Integer value
    ) {
        return value == null
                ? 0
                : value;
    }

    public TeamComparisonResponse compareTeams(
            Long team1Id,
            Long team2Id
    ) {

        TeamAnalyticsResponse team1 =
                getTeamAnalytics(team1Id);

        TeamAnalyticsResponse team2 =
                getTeamAnalytics(team2Id);

        return new TeamComparisonResponse(
                team1,
                team2
        );
    }
}