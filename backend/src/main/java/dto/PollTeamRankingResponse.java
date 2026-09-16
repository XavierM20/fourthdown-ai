package com.fourthdown.ai.dto;

public record PollTeamRankingResponse(

        int rank,

        Long teamId,

        String teamName,

        String abbreviation,

        String conference,

        String classification,

        String logoUrl,

        int wins,

        int losses,

        int ties,

        Integer pollPoints,

        Integer firstPlaceVotes

) {
}