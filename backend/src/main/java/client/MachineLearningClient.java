package com.fourthdown.ai.client;

import com.fourthdown.ai.dto.ml.MlPredictionRequest;
import com.fourthdown.ai.dto.ml.MlPredictionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.fourthdown.ai.dto.ml.ModelMetadataResponse;
import com.fourthdown.ai.dto.ml.ScoreModelMetadataResponse;

@Component
public class MachineLearningClient {

    private final RestClient restClient;

    public MachineLearningClient(
            @Value("${ml.base-url}")
            String baseUrl
    ) {

        this.restClient =
                RestClient.builder()
                        .baseUrl(baseUrl)
                        .build();
    }

    public MlPredictionResponse predict(
            MlPredictionRequest request
    ) {

        return restClient
                .post()
                .uri("/predict")
                .body(request)
                .retrieve()
                .body(
                        MlPredictionResponse.class
                );
    }

    public ModelMetadataResponse getMetadata() {

        return restClient
                .get()
                .uri("/metadata")
                .retrieve()
                .body(
                        ModelMetadataResponse.class
                );
    }

    public ScoreModelMetadataResponse getScoreMetadata() {

        return restClient
                .get()
                .uri("/score-metadata")
                .retrieve()
                .body(
                        ScoreModelMetadataResponse.class
                );
    }
}