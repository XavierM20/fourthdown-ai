package com.fourthdown.ai.dto.ml;

import java.time.LocalDateTime;

public class TrainingExampleResponse {

    private Long gameId;
    private Long cfbdGameId;

    private Integer season;
    private Integer week;
    private LocalDateTime gameDate;

    private Long homeTeamId;
    private String homeTeamName;

    private Long awayTeamId;
    private String awayTeamName;

    private int homePriorGames;
    private int awayPriorGames;

    // =========================================================
    // HOME FEATURES
    // =========================================================

    private double homeAveragePoints;
    private double homeAverageTotalYards;
    private double homeAveragePassingYards;
    private double homeAverageRushingYards;
    private double homeAverageTurnovers;
    private double homeAverageScoringMargin;
    private double homeThirdDownRate;
    private double homeRedZoneRate;
    private double homeRecentWinRate;

    private double homeAverageOpponentWinRate;
    private double homeAverageOpponentScoringMargin;

    // =========================================================
    // AWAY FEATURES
    // =========================================================

    private double awayAveragePoints;
    private double awayAverageTotalYards;
    private double awayAveragePassingYards;
    private double awayAverageRushingYards;
    private double awayAverageTurnovers;
    private double awayAverageScoringMargin;
    private double awayThirdDownRate;
    private double awayRedZoneRate;
    private double awayRecentWinRate;

    private double awayAverageOpponentWinRate;
    private double awayAverageOpponentScoringMargin;

    // =========================================================
    // DIFFERENCE FEATURES USED BY ML MODEL
    // =========================================================

    private double pointsDifference;
    private double yardsDifference;
    private double turnoverDifference;
    private double scoringMarginDifference;
    private double thirdDownDifference;
    private double redZoneDifference;
    private double recentFormDifference;

    private double opponentWinRateDifference;
    private double opponentScoringMarginDifference;

    // =========================================================
    // ACTUAL RESULT / LABEL
    // =========================================================

    private Integer homeScore;
    private Integer awayScore;
    private int homeWon;

    // =========================================================
    // GETTERS / SETTERS
    // =========================================================

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public Long getCfbdGameId() {
        return cfbdGameId;
    }

    public void setCfbdGameId(Long cfbdGameId) {
        this.cfbdGameId = cfbdGameId;
    }

    public Integer getSeason() {
        return season;
    }

    public void setSeason(Integer season) {
        this.season = season;
    }

    public Integer getWeek() {
        return week;
    }

    public void setWeek(Integer week) {
        this.week = week;
    }

    public LocalDateTime getGameDate() {
        return gameDate;
    }

    public void setGameDate(LocalDateTime gameDate) {
        this.gameDate = gameDate;
    }

    public Long getHomeTeamId() {
        return homeTeamId;
    }

    public void setHomeTeamId(Long homeTeamId) {
        this.homeTeamId = homeTeamId;
    }

    public String getHomeTeamName() {
        return homeTeamName;
    }

    public void setHomeTeamName(String homeTeamName) {
        this.homeTeamName = homeTeamName;
    }

    public Long getAwayTeamId() {
        return awayTeamId;
    }

    public void setAwayTeamId(Long awayTeamId) {
        this.awayTeamId = awayTeamId;
    }

    public String getAwayTeamName() {
        return awayTeamName;
    }

    public void setAwayTeamName(String awayTeamName) {
        this.awayTeamName = awayTeamName;
    }

    public int getHomePriorGames() {
        return homePriorGames;
    }

    public void setHomePriorGames(int homePriorGames) {
        this.homePriorGames = homePriorGames;
    }

    public int getAwayPriorGames() {
        return awayPriorGames;
    }

    public void setAwayPriorGames(int awayPriorGames) {
        this.awayPriorGames = awayPriorGames;
    }

    public double getHomeAveragePoints() {
        return homeAveragePoints;
    }

    public void setHomeAveragePoints(double homeAveragePoints) {
        this.homeAveragePoints = homeAveragePoints;
    }

    public double getHomeAverageTotalYards() {
        return homeAverageTotalYards;
    }

    public void setHomeAverageTotalYards(double homeAverageTotalYards) {
        this.homeAverageTotalYards = homeAverageTotalYards;
    }

    public double getHomeAveragePassingYards() {
        return homeAveragePassingYards;
    }

    public void setHomeAveragePassingYards(double homeAveragePassingYards) {
        this.homeAveragePassingYards = homeAveragePassingYards;
    }

    public double getHomeAverageRushingYards() {
        return homeAverageRushingYards;
    }

    public void setHomeAverageRushingYards(double homeAverageRushingYards) {
        this.homeAverageRushingYards = homeAverageRushingYards;
    }

    public double getHomeAverageTurnovers() {
        return homeAverageTurnovers;
    }

    public void setHomeAverageTurnovers(double homeAverageTurnovers) {
        this.homeAverageTurnovers = homeAverageTurnovers;
    }

    public double getHomeAverageScoringMargin() {
        return homeAverageScoringMargin;
    }

    public void setHomeAverageScoringMargin(double homeAverageScoringMargin) {
        this.homeAverageScoringMargin = homeAverageScoringMargin;
    }

    public double getHomeThirdDownRate() {
        return homeThirdDownRate;
    }

    public void setHomeThirdDownRate(double homeThirdDownRate) {
        this.homeThirdDownRate = homeThirdDownRate;
    }

    public double getHomeRedZoneRate() {
        return homeRedZoneRate;
    }

    public void setHomeRedZoneRate(double homeRedZoneRate) {
        this.homeRedZoneRate = homeRedZoneRate;
    }

    public double getHomeRecentWinRate() {
        return homeRecentWinRate;
    }

    public void setHomeRecentWinRate(double homeRecentWinRate) {
        this.homeRecentWinRate = homeRecentWinRate;
    }

    public double getHomeAverageOpponentWinRate() {
        return homeAverageOpponentWinRate;
    }

    public void setHomeAverageOpponentWinRate(double homeAverageOpponentWinRate) {
        this.homeAverageOpponentWinRate = homeAverageOpponentWinRate;
    }

    public double getHomeAverageOpponentScoringMargin() {
        return homeAverageOpponentScoringMargin;
    }

    public void setHomeAverageOpponentScoringMargin(
            double homeAverageOpponentScoringMargin
    ) {
        this.homeAverageOpponentScoringMargin =
                homeAverageOpponentScoringMargin;
    }

    public double getAwayAveragePoints() {
        return awayAveragePoints;
    }

    public void setAwayAveragePoints(double awayAveragePoints) {
        this.awayAveragePoints = awayAveragePoints;
    }

    public double getAwayAverageTotalYards() {
        return awayAverageTotalYards;
    }

    public void setAwayAverageTotalYards(double awayAverageTotalYards) {
        this.awayAverageTotalYards = awayAverageTotalYards;
    }

    public double getAwayAveragePassingYards() {
        return awayAveragePassingYards;
    }

    public void setAwayAveragePassingYards(double awayAveragePassingYards) {
        this.awayAveragePassingYards = awayAveragePassingYards;
    }

    public double getAwayAverageRushingYards() {
        return awayAverageRushingYards;
    }

    public void setAwayAverageRushingYards(double awayAverageRushingYards) {
        this.awayAverageRushingYards = awayAverageRushingYards;
    }

    public double getAwayAverageTurnovers() {
        return awayAverageTurnovers;
    }

    public void setAwayAverageTurnovers(double awayAverageTurnovers) {
        this.awayAverageTurnovers = awayAverageTurnovers;
    }

    public double getAwayAverageScoringMargin() {
        return awayAverageScoringMargin;
    }

    public void setAwayAverageScoringMargin(double awayAverageScoringMargin) {
        this.awayAverageScoringMargin = awayAverageScoringMargin;
    }

    public double getAwayThirdDownRate() {
        return awayThirdDownRate;
    }

    public void setAwayThirdDownRate(double awayThirdDownRate) {
        this.awayThirdDownRate = awayThirdDownRate;
    }

    public double getAwayRedZoneRate() {
        return awayRedZoneRate;
    }

    public void setAwayRedZoneRate(double awayRedZoneRate) {
        this.awayRedZoneRate = awayRedZoneRate;
    }

    public double getAwayRecentWinRate() {
        return awayRecentWinRate;
    }

    public void setAwayRecentWinRate(double awayRecentWinRate) {
        this.awayRecentWinRate = awayRecentWinRate;
    }

    public double getAwayAverageOpponentWinRate() {
        return awayAverageOpponentWinRate;
    }

    public void setAwayAverageOpponentWinRate(double awayAverageOpponentWinRate) {
        this.awayAverageOpponentWinRate = awayAverageOpponentWinRate;
    }

    public double getAwayAverageOpponentScoringMargin() {
        return awayAverageOpponentScoringMargin;
    }

    public void setAwayAverageOpponentScoringMargin(
            double awayAverageOpponentScoringMargin
    ) {
        this.awayAverageOpponentScoringMargin =
                awayAverageOpponentScoringMargin;
    }

    public double getPointsDifference() {
        return pointsDifference;
    }

    public void setPointsDifference(double pointsDifference) {
        this.pointsDifference = pointsDifference;
    }

    public double getYardsDifference() {
        return yardsDifference;
    }

    public void setYardsDifference(double yardsDifference) {
        this.yardsDifference = yardsDifference;
    }

    public double getTurnoverDifference() {
        return turnoverDifference;
    }

    public void setTurnoverDifference(double turnoverDifference) {
        this.turnoverDifference = turnoverDifference;
    }

    public double getScoringMarginDifference() {
        return scoringMarginDifference;
    }

    public void setScoringMarginDifference(double scoringMarginDifference) {
        this.scoringMarginDifference = scoringMarginDifference;
    }

    public double getThirdDownDifference() {
        return thirdDownDifference;
    }

    public void setThirdDownDifference(double thirdDownDifference) {
        this.thirdDownDifference = thirdDownDifference;
    }

    public double getRedZoneDifference() {
        return redZoneDifference;
    }

    public void setRedZoneDifference(double redZoneDifference) {
        this.redZoneDifference = redZoneDifference;
    }

    public double getRecentFormDifference() {
        return recentFormDifference;
    }

    public void setRecentFormDifference(double recentFormDifference) {
        this.recentFormDifference = recentFormDifference;
    }

    public double getOpponentWinRateDifference() {
        return opponentWinRateDifference;
    }

    public void setOpponentWinRateDifference(
            double opponentWinRateDifference
    ) {
        this.opponentWinRateDifference =
                opponentWinRateDifference;
    }

    public double getOpponentScoringMarginDifference() {
        return opponentScoringMarginDifference;
    }

    public void setOpponentScoringMarginDifference(
            double opponentScoringMarginDifference
    ) {
        this.opponentScoringMarginDifference =
                opponentScoringMarginDifference;
    }

    public Integer getHomeScore() {
        return homeScore;
    }

    public void setHomeScore(Integer homeScore) {
        this.homeScore = homeScore;
    }

    public Integer getAwayScore() {
        return awayScore;
    }

    public void setAwayScore(Integer awayScore) {
        this.awayScore = awayScore;
    }

    public int getHomeWon() {
        return homeWon;
    }

    public void setHomeWon(int homeWon) {
        this.homeWon = homeWon;
    }
}