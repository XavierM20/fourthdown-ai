package com.fourthdown.ai.dto;

public record TeamStandingResponse(

        int rank,

        Long teamId,

        String teamName,

        String abbreviation,

        String conference,

        String classification,

        String logoUrl,

        int gamesPlayed,

        int wins,

        int losses,

        int ties,

        double winPercentage,

        int pointsFor,

        int pointsAgainst,

        int pointDifferential

) {
}