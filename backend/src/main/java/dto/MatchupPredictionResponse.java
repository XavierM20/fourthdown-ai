package com.fourthdown.ai.dto;

import java.util.List;

public class MatchupPredictionResponse {

    private Long homeTeamId;
    private String homeTeamName;

    private Long awayTeamId;
    private String awayTeamName;

    private Long predictedWinnerId;
    private String predictedWinnerName;

    private double homeWinProbability;
    private double awayWinProbability;
    private double confidence;

    private int projectedHomeScore;
    private int projectedAwayScore;

    private List<String> explanation;

    public MatchupPredictionResponse() {
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

    public Long getPredictedWinnerId() {
        return predictedWinnerId;
    }

    public void setPredictedWinnerId(Long predictedWinnerId) {
        this.predictedWinnerId = predictedWinnerId;
    }

    public String getPredictedWinnerName() {
        return predictedWinnerName;
    }

    public void setPredictedWinnerName(String predictedWinnerName) {
        this.predictedWinnerName = predictedWinnerName;
    }

    public double getHomeWinProbability() {
        return homeWinProbability;
    }

    public void setHomeWinProbability(double homeWinProbability) {
        this.homeWinProbability = homeWinProbability;
    }

    public double getAwayWinProbability() {
        return awayWinProbability;
    }

    public void setAwayWinProbability(double awayWinProbability) {
        this.awayWinProbability = awayWinProbability;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public int getProjectedHomeScore() {
        return projectedHomeScore;
    }

    public void setProjectedHomeScore(int projectedHomeScore) {
        this.projectedHomeScore = projectedHomeScore;
    }

    public int getProjectedAwayScore() {
        return projectedAwayScore;
    }

    public void setProjectedAwayScore(int projectedAwayScore) {
        this.projectedAwayScore = projectedAwayScore;
    }

    public List<String> getExplanation() {
        return explanation;
    }

    public void setExplanation(List<String> explanation) {
        this.explanation = explanation;
    }
}