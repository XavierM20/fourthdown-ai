package com.fourthdown.ai.controller;

import com.fourthdown.ai.dto.TeamAnalyticsResponse;
import com.fourthdown.ai.service.AnalyticsService;
import org.springframework.web.bind.annotation.*;
import com.fourthdown.ai.dto.TeamComparisonResponse;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(
            AnalyticsService analyticsService
    ) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/teams/{teamId}")
    public TeamAnalyticsResponse getTeamAnalytics(
            @PathVariable Long teamId
    ) {
        return analyticsService.getTeamAnalytics(teamId);
    }

    @GetMapping("/compare")
    public TeamComparisonResponse compareTeams(
            @RequestParam Long team1,
            @RequestParam Long team2
    ) {
        return analyticsService.compareTeams(
                team1,
                team2
        );
    }
}