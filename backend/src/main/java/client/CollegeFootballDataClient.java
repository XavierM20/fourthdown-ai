package com.fourthdown.ai.client;

import com.fourthdown.ai.dto.cfbd.CfbdGameResponse;
import com.fourthdown.ai.dto.cfbd.CfbdTeamGameStatsResponse;
import com.fourthdown.ai.dto.cfbd.CfbdTeamRecordResponse;
import com.fourthdown.ai.dto.cfbd.CfbdTeamResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.fourthdown.ai.dto.cfbd.CfbdRankingResponse;

import java.util.List;

@Component
public class CollegeFootballDataClient {

    private final RestClient restClient;

    public CollegeFootballDataClient(
            @Value("${cfbd.base-url}") String baseUrl,
            @Value("${cfbd.api-key}") String apiKey
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + apiKey
                )
                .build();
    }

    // =========================================================
    // GAMES
    // =========================================================

    public List<CfbdGameResponse> getGames(
            int year,
            String team
    ) {
        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/games")
                        .queryParam(
                                "year",
                                year
                        )
                        .queryParam(
                                "seasonType",
                                "both"
                        )
                        .queryParam(
                                "team",
                                team
                        )
                        .build())
                .retrieve()
                .body(
                        new ParameterizedTypeReference<
                                List<CfbdGameResponse>
                                >() {
                        }
                );
    }

    public List<CfbdGameResponse> getGamesByClassification(
            int year,
            String classification
    ) {
        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/games")
                        .queryParam(
                                "year",
                                year
                        )
                        .queryParam(
                                "seasonType",
                                "both"
                        )
                        .queryParam(
                                "classification",
                                classification.toLowerCase()
                        )
                        .build())
                .retrieve()
                .body(
                        new ParameterizedTypeReference<
                                List<CfbdGameResponse>
                                >() {
                        }
                );
    }

    public List<CfbdGameResponse> getGamesByClassification(
            int year,
            String classification,
            String seasonType
    ) {
        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/games")
                        .queryParam(
                                "year",
                                year
                        )
                        .queryParam(
                                "seasonType",
                                seasonType.toLowerCase()
                        )
                        .queryParam(
                                "classification",
                                classification.toLowerCase()
                        )
                        .build())
                .retrieve()
                .body(
                        new ParameterizedTypeReference<
                                List<CfbdGameResponse>
                                >() {
                        }
                );
    }

    // =========================================================
    // TEAM METADATA
    // =========================================================

    public List<CfbdTeamResponse> getTeams(
            int year
    ) {
        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/teams")
                        .queryParam("year", year)
                        .build())
                .retrieve()
                .body(
                        new ParameterizedTypeReference<
                                List<CfbdTeamResponse>
                                >() {
                        }
                );
    }

    // =========================================================
    // TEAM GAME STATS BY WEEK
    // =========================================================

    public List<CfbdTeamGameStatsResponse> getTeamGameStats(
            int year,
            Integer week
    ) {
        return restClient
                .get()
                .uri(uriBuilder -> {

                    uriBuilder
                            .path("/games/teams")
                            .queryParam("year", year)
                            .queryParam("seasonType", "both");

                    if (week != null) {
                        uriBuilder.queryParam(
                                "week",
                                week
                        );
                    }

                    return uriBuilder.build();
                })
                .retrieve()
                .body(
                        new ParameterizedTypeReference<
                                List<CfbdTeamGameStatsResponse>
                                >() {
                        }
                );
    }

    // =========================================================
    // TEAM GAME STATS BY TEAM
    // =========================================================

    public List<CfbdTeamGameStatsResponse> getTeamGameStats(
            int year,
            String team
    ) {
        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/games/teams")
                        .queryParam("year", year)
                        .queryParam("seasonType", "both")
                        .queryParam("team", team)
                        .build())
                .retrieve()
                .body(
                        new ParameterizedTypeReference<
                                List<CfbdTeamGameStatsResponse>
                                >() {
                        }
                );
    }

    // =========================================================
    // TEAM RECORD
    // =========================================================

    public List<CfbdTeamRecordResponse> getTeamRecord(
            int year,
            String team
    ) {
        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/records")
                        .queryParam("year", year)
                        .queryParam("team", team)
                        .build())
                .retrieve()
                .body(
                        new ParameterizedTypeReference<
                                List<CfbdTeamRecordResponse>
                                >() {
                        }
                );
    }

    public List<CfbdTeamGameStatsResponse> getTeamGameStats(
            int year,
            Integer week,
            String classification
    ) {
        return getTeamGameStats(
                year,
                week,
                classification,
                "both"
        );
    }

    // =========================================================
    // TEAM GAME STATS BY CLASSIFICATION + SEASON TYPE
    //
    // seasonType:
    // regular
    // postseason
    // both
    // =========================================================

    public List<CfbdTeamGameStatsResponse> getTeamGameStats(
            int year,
            Integer week,
            String classification,
            String seasonType
    ) {
        return restClient
                .get()
                .uri(uriBuilder -> {

                    uriBuilder
                            .path("/games/teams")
                            .queryParam(
                                    "year",
                                    year
                            )
                            .queryParam(
                                    "classification",
                                    classification.toLowerCase()
                            )
                            .queryParam(
                                    "seasonType",
                                    seasonType.toLowerCase()
                            );

                    if (week != null) {
                        uriBuilder.queryParam(
                                "week",
                                week
                        );
                    }

                    return uriBuilder.build();
                })
                .retrieve()
                .body(
                        new ParameterizedTypeReference<
                                List<CfbdTeamGameStatsResponse>
                                >() {
                        }
                );
    }


    // =========================================================
// RANKINGS
// =========================================================

    public List<CfbdRankingResponse> getRankings(
            int year
    ) {

        return restClient
                .get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/rankings")
                                .queryParam(
                                        "year",
                                        year
                                )
                                .queryParam(
                                        "seasonType",
                                        "regular"
                                )
                                .build()
                )
                .retrieve()
                .body(
                        new ParameterizedTypeReference<
                                List<CfbdRankingResponse>
                                >() {
                        }
                );
    }
}