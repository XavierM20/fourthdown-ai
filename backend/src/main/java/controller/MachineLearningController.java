package com.fourthdown.ai.controller;

import com.fourthdown.ai.client.MachineLearningClient;
import com.fourthdown.ai.dto.ml.ModelMetadataResponse;
import com.fourthdown.ai.dto.ml.TrainingExampleResponse;
import com.fourthdown.ai.service.TrainingDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fourthdown.ai.dto.ml.ScoreModelMetadataResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ml")
public class MachineLearningController {

    private final TrainingDataService trainingDataService;
    private final MachineLearningClient machineLearningClient;

    public MachineLearningController(
            TrainingDataService trainingDataService,
            MachineLearningClient machineLearningClient
    ) {
        this.trainingDataService =
                trainingDataService;

        this.machineLearningClient =
                machineLearningClient;
    }

    // =========================================================
    // TRAINING DATA
    // =========================================================

    @GetMapping("/training-data")
    public List<TrainingExampleResponse> getTrainingData(
            @RequestParam(defaultValue = "2023")
            int startSeason,

            @RequestParam(defaultValue = "2025")
            int endSeason,

            @RequestParam(defaultValue = "3")
            int minPriorGames
    ) {

        return trainingDataService
                .buildTrainingData(
                        startSeason,
                        endSeason,
                        minPriorGames
                );
    }

    // =========================================================
    // MODEL METADATA
    // =========================================================

    @GetMapping("/model")
    public ModelMetadataResponse getModelMetadata() {

        return machineLearningClient
                .getMetadata();
    }

    @GetMapping("/score-model")
    public ScoreModelMetadataResponse getScoreModelMetadata() {

        return machineLearningClient
                .getScoreMetadata();
    }
}