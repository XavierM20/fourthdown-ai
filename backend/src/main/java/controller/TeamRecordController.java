package com.fourthdown.ai.controller;

import com.fourthdown.ai.client.CollegeFootballDataClient;
import com.fourthdown.ai.dto.cfbd.CfbdTeamRecordResponse;
import com.fourthdown.ai.model.Team;
import com.fourthdown.ai.repository.TeamRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
public class TeamRecordController {

    private final TeamRepository teamRepository;
    private final CollegeFootballDataClient cfbdClient;

    public TeamRecordController(
            TeamRepository teamRepository,
            CollegeFootballDataClient cfbdClient
    ) {
        this.teamRepository = teamRepository;
        this.cfbdClient = cfbdClient;
    }

    @GetMapping("/{id}/record")
    public CfbdTeamRecordResponse getTeamRecord(
            @PathVariable Long id,
            @RequestParam(defaultValue = "2026") int year
    ) {

        Team team = teamRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Team not found")
                );

        List<CfbdTeamRecordResponse> records =
                cfbdClient.getTeamRecord(
                        year,
                        team.getName()
                );

        if (records == null || records.isEmpty()) {
            throw new RuntimeException(
                    "Team record not found"
            );
        }

        return records.get(0);
    }
}