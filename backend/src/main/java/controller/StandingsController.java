package com.fourthdown.ai.controller;

import com.fourthdown.ai.dto.PollRankingsResponse;
import com.fourthdown.ai.dto.TeamRankingHistoryResponse;
import com.fourthdown.ai.service.PollRankingsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rankings")
public class StandingsController {

    private final PollRankingsService pollRankingsService;

    public StandingsController(
            PollRankingsService pollRankingsService
    ) {

        this.pollRankingsService =
                pollRankingsService;
    }

    @GetMapping
    public PollRankingsResponse getRankings(

            @RequestParam
            int season,

            @RequestParam(
                    defaultValue = "fbs"
            )
            String classification

    ) {

        return pollRankingsService
                .getRankings(
                        season,
                        classification
                );
    }

    @GetMapping("/history")
    public TeamRankingHistoryResponse getRankingHistory(

            @RequestParam
            int season,

            @RequestParam(
                    defaultValue = "fbs"
            )
            String classification,

            @RequestParam
            Long teamId

    ) {

        return pollRankingsService
                .getRankingHistory(
                        season,
                        classification,
                        teamId
                );
    }

}