const API_URL = (
  import.meta.env.VITE_API_URL ??
  "http://localhost:8080/api/v1"
).replace(/\/$/, "");

export interface ModelMetadata {
  modelName: string | null;

  features: string[] | null;
  featureCount: number | null;

  trainingSeasons: number[] | null;
  testSeason: number | null;

  trainingExamples: number | null;
  testingExamples: number | null;
  totalExamples: number | null;

  accuracy: number | null;
  rocAuc: number | null;
  logLoss: number | null;
}

export interface ScoreModelMetadata {
  modelName: string | null;

  features: string[] | null;
  featureCount: number | null;

  targets: string[] | null;

  trainingSeasons: number[] | null;
  testSeason: number | null;

  trainingExamples: number | null;
  testingExamples: number | null;
  totalExamples: number | null;

  metrics: {
    marginMae: number | null;
    marginRmse: number | null;
    totalPointsMae: number | null;
    homeScoreMae: number | null;
    awayScoreMae: number | null;
    combinedScoreMae: number | null;
  } | null;
}

export async function getModelMetadata(): Promise<ModelMetadata> {
  const response = await fetch(
    `${API_URL}/ml/model`
  );

  if (!response.ok) {
    throw new Error(
      "Failed to load winner model metadata"
    );
  }

  return response.json();
}

export async function getScoreModelMetadata(): Promise<ScoreModelMetadata> {
  const response = await fetch(
    `${API_URL}/ml/score-model`
  );

  if (!response.ok) {
    throw new Error(
      "Failed to load score model metadata"
    );
  }

  return response.json();
}