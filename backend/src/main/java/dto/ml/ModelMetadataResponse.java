package com.fourthdown.ai.dto.ml;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;

public record ModelMetadataResponse(
        String modelName,
        List<String> features,
        Integer featureCount,

        @JsonAlias("evaluationTrainingSeasons")
        List<Integer> trainingSeasons,

        Integer testSeason,

        @JsonAlias("evaluationTrainingExamples")
        Integer trainingExamples,

        Integer testingExamples,
        Integer totalExamples,
        Double accuracy,
        Double rocAuc,
        Double logLoss
) {
}