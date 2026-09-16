package com.fourthdown.ai.dto;

import java.util.List;

public record PollRankingsResponse(

        int season,

        int week,

        String poll,

        String classification,

        List<PollTeamRankingResponse> rankings

) {
}