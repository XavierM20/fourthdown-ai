package com.fourthdown.ai.dto.ml;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;
import java.util.Map;

public record ScoreModelMetadataResponse(
        String modelName,
        List<String> features,
        Integer featureCount,
        List<String> targets,

        @JsonAlias("evaluationTrainingSeasons")
        List<Integer> trainingSeasons,

        Integer testSeason,

        @JsonAlias("evaluationTrainingExamples")
        Integer trainingExamples,

        Integer testingExamples,
        Integer totalExamples,
        Map<String, Double> metrics
) {
}