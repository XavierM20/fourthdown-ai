package com.fourthdown.ai.dto;

public class TeamAnalyticsResponse {

    private Long teamId;
    private String teamName;

    private int gamesPlayed;
    private int wins;
    private int losses;

    private double averagePoints;
    private double averageTotalYards;
    private double averagePassingYards;
    private double averageRushingYards;
    private double averageTurnovers;

    private double averageScoringMargin;
    private double thirdDownConversionRate;
    private double redZoneScoringRate;

    private int recentWins;
    private int recentLosses;

    public TeamAnalyticsResponse() {
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public double getAveragePoints() {
        return averagePoints;
    }

    public void setAveragePoints(double averagePoints) {
        this.averagePoints = averagePoints;
    }

    public double getAverageTotalYards() {
        return averageTotalYards;
    }

    public void setAverageTotalYards(double averageTotalYards) {
        this.averageTotalYards = averageTotalYards;
    }

    public double getAveragePassingYards() {
        return averagePassingYards;
    }

    public void setAveragePassingYards(double averagePassingYards) {
        this.averagePassingYards = averagePassingYards;
    }

    public double getAverageRushingYards() {
        return averageRushingYards;
    }

    public void setAverageRushingYards(double averageRushingYards) {
        this.averageRushingYards = averageRushingYards;
    }

    public double getAverageTurnovers() {
        return averageTurnovers;
    }

    public void setAverageTurnovers(double averageTurnovers) {
        this.averageTurnovers = averageTurnovers;
    }

    public double getAverageScoringMargin() {
        return averageScoringMargin;
    }

    public void setAverageScoringMargin(double averageScoringMargin) {
        this.averageScoringMargin = averageScoringMargin;
    }

    public double getThirdDownConversionRate() {
        return thirdDownConversionRate;
    }

    public void setThirdDownConversionRate(double thirdDownConversionRate) {
        this.thirdDownConversionRate = thirdDownConversionRate;
    }

    public double getRedZoneScoringRate() {
        return redZoneScoringRate;
    }

    public void setRedZoneScoringRate(double redZoneScoringRate) {
        this.redZoneScoringRate = redZoneScoringRate;
    }

    public int getRecentWins() {
        return recentWins;
    }

    public void setRecentWins(int recentWins) {
        this.recentWins = recentWins;
    }

    public int getRecentLosses() {
        return recentLosses;
    }

    public void setRecentLosses(int recentLosses) {
        this.recentLosses = recentLosses;
    }
}