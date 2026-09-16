package com.fourthdown.ai.controller;

import com.fourthdown.ai.model.TeamGameStats;
import com.fourthdown.ai.service.TeamGameStatsService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stats")
public class TeamGameStatsController {

    private final TeamGameStatsService teamGameStatsService;

    public TeamGameStatsController(
            TeamGameStatsService teamGameStatsService
    ) {
        this.teamGameStatsService = teamGameStatsService;
    }

    @GetMapping
    public List<TeamGameStats> getAllStats() {
        return teamGameStatsService.getAllStats();
    }

    @GetMapping("/{id}")
    public TeamGameStats getStatsById(@PathVariable Long id) {
        return teamGameStatsService.getStatsById(id);
    }

    @GetMapping("/game/{gameId}")
    public List<TeamGameStats> getStatsByGameId(
            @PathVariable Long gameId
    ) {
        return teamGameStatsService.getStatsByGameId(gameId);
    }

    @GetMapping("/team/{teamId}")
    public List<TeamGameStats> getStatsByTeamId(
            @PathVariable Long teamId
    ) {
        return teamGameStatsService.getStatsByTeamId(teamId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamGameStats createStats(
            @RequestBody TeamGameStats stats
    ) {
        return teamGameStatsService.createStats(stats);
    }

    @PutMapping("/{id}")
    public TeamGameStats updateStats(
            @PathVariable Long id,
            @RequestBody TeamGameStats stats
    ) {
        return teamGameStatsService.updateStats(id, stats);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStats(@PathVariable Long id) {
        teamGameStatsService.deleteStats(id);
    }
}