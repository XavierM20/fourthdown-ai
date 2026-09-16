package com.fourthdown.ai.dto;

public class TeamComparisonResponse {

    private TeamAnalyticsResponse teamOne;
    private TeamAnalyticsResponse teamTwo;

    public TeamComparisonResponse(
            TeamAnalyticsResponse teamOne,
            TeamAnalyticsResponse teamTwo
    ) {
        this.teamOne = teamOne;
        this.teamTwo = teamTwo;
    }

    public TeamAnalyticsResponse getTeamOne() {
        return teamOne;
    }

    public TeamAnalyticsResponse getTeamTwo() {
        return teamTwo;
    }
}