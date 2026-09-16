package com.fourthdown.ai.controller;

import com.fourthdown.ai.client.CollegeFootballDataClient;
import com.fourthdown.ai.dto.cfbd.CfbdGameResponse;
import org.springframework.web.bind.annotation.*;
import com.fourthdown.ai.dto.cfbd.CfbdTeamGameStatsResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cfbd")
public class CfbdController {

    private final CollegeFootballDataClient cfbdClient;

    public CfbdController(
            CollegeFootballDataClient cfbdClient
    ) {
        this.cfbdClient = cfbdClient;
    }

    @GetMapping("/games")
    public List<CfbdGameResponse> getGames(
            @RequestParam int year,
            @RequestParam String team
    ) {
        return cfbdClient.getGames(year, team);
    }

    @GetMapping("/team-stats")
    public List<CfbdTeamGameStatsResponse> getTeamStats(
            @RequestParam int year,
            @RequestParam(required = false) Integer week
    ) {
        return cfbdClient.getTeamGameStats(year, week);
    }

    @GetMapping("/team-stats/team")
    public List<CfbdTeamGameStatsResponse> getTeamStatsByTeam(
            @RequestParam int year,
            @RequestParam String team
    ) {
        return cfbdClient.getTeamGameStats(year, team);
    }
}