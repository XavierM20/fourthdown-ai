package com.fourthdown.ai.controller;

import com.fourthdown.ai.dto.MatchupPredictionResponse;
import com.fourthdown.ai.service.PredictionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/predictions")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(
            PredictionService predictionService
    ) {
        this.predictionService =
                predictionService;
    }

    // =========================================================
    // MANUAL / HYPOTHETICAL MATCHUP
    // =========================================================

    @GetMapping("/matchup")
    public MatchupPredictionResponse predictMatchup(
            @RequestParam Long homeTeamId,
            @RequestParam Long awayTeamId
    ) {

        return predictionService
                .predictMatchup(
                        homeTeamId,
                        awayTeamId
                );
    }

    // =========================================================
    // REAL SCHEDULED GAME
    //
    // Uses the game's kickoff time as the feature cutoff.
    // =========================================================

    @GetMapping("/game/{gameId}")
    public MatchupPredictionResponse predictGame(
            @PathVariable Long gameId
    ) {

        return predictionService
                .predictGame(
                        gameId
                );
    }
}