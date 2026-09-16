package com.fourthdown.ai.dto;

import java.util.List;

public record TeamRankingHistoryResponse(
        int season,
        String classification,
        Long teamId,
        String teamName,
        Integer currentRank,
        Integer previousRank,
        Integer movement,
        List<RankingHistoryPointResponse> history
) {
}