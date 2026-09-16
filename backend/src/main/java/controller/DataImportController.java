package com.fourthdown.ai.controller;

import com.fourthdown.ai.service.DataImportService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/import")
public class DataImportController {

    private final DataImportService dataImportService;

    public DataImportController(
            DataImportService dataImportService
    ) {
        this.dataImportService =
                dataImportService;
    }

    // =========================================================
    // TEAM METADATA
    // =========================================================

    @PostMapping("/teams/{classification}/{year}")
    public DataImportService.TeamMetadataImportResult
    importTeams(
            @PathVariable String classification,
            @PathVariable int year
    ) {

        validateClassification(
                classification
        );

        return dataImportService
                .importTeamMetadata(
                        year,
                        classification
                );
    }

    // =========================================================
    // SEASON GAMES
    // =========================================================

    @PostMapping("/season/{classification}/{year}")
    public DataImportService.ImportResult
    importSeason(
            @PathVariable String classification,
            @PathVariable int year
    ) {

        validateClassification(
                classification
        );

        return dataImportService
                .importSeason(
                        year,
                        classification
                );
    }

    // =========================================================
    // SINGLE TEAM STATS
    // =========================================================

    @PostMapping("/stats/{year}")
    public DataImportService.StatsImportResult
    importStats(
            @PathVariable int year,
            @RequestParam String team
    ) {

        return dataImportService
                .importTeamStats(
                        year,
                        team
                );
    }

    // =========================================================
    // SEASON STATS
    // =========================================================

    @PostMapping("/stats/season/{classification}/{year}")
    public DataImportService.SeasonStatsImportResult
    importSeasonStats(
            @PathVariable String classification,
            @PathVariable int year
    ) {

        validateClassification(
                classification
        );

        return dataImportService
                .importSeasonStats(
                        year,
                        classification
                );
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateClassification(
            String classification
    ) {

        if (!classification.equalsIgnoreCase("fbs") &&
                !classification.equalsIgnoreCase("fcs")) {

            throw new IllegalArgumentException(
                    "Classification must be fbs or fcs"
            );
        }
    }
}