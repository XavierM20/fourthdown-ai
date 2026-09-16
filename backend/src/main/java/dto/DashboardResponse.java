package com.fourthdown.ai.dto;

import java.time.LocalDateTime;

public class DashboardResponse {

    private long totalTeams;
    private long totalGames;
    private long upcomingGames;
    private NextGame nextGame;

    public DashboardResponse(
            long totalTeams,
            long totalGames,
            long upcomingGames,
            NextGame nextGame
    ) {
        this.totalTeams = totalTeams;
        this.totalGames = totalGames;
        this.upcomingGames = upcomingGames;
        this.nextGame = nextGame;
    }

    public long getTotalTeams() {
        return totalTeams;
    }

    public long getTotalGames() {
        return totalGames;
    }

    public long getUpcomingGames() {
        return upcomingGames;
    }

    public NextGame getNextGame() {
        return nextGame;
    }

    public static class NextGame {

        private Long id;
        private String homeTeam;
        private String awayTeam;
        private LocalDateTime gameDate;
        private String venue;

        public NextGame(
                Long id,
                String homeTeam,
                String awayTeam,
                LocalDateTime gameDate,
                String venue
        ) {
            this.id = id;
            this.homeTeam = homeTeam;
            this.awayTeam = awayTeam;
            this.gameDate = gameDate;
            this.venue = venue;
        }

        public Long getId() {
            return id;
        }

        public String getHomeTeam() {
            return homeTeam;
        }

        public String getAwayTeam() {
            return awayTeam;
        }

        public LocalDateTime getGameDate() {
            return gameDate;
        }

        public String getVenue() {
            return venue;
        }
    }
}