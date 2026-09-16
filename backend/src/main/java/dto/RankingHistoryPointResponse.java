package com.fourthdown.ai.dto;

public record RankingHistoryPointResponse(
        int week,
        String poll,
        Integer rank,
        Integer pollPoints,
        Integer firstPlaceVotes
) {
}