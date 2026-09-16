import json
from datetime import datetime, timezone
from pathlib import Path

import joblib
import numpy as np
import pandas as pd
import requests

from sklearn.ensemble import RandomForestRegressor
from sklearn.linear_model import Ridge
from sklearn.metrics import (
    mean_absolute_error,
    mean_squared_error,
)
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler


# ============================================================
# PATHS
# ============================================================

BASE_DIR = Path(__file__).resolve().parent
MODELS_DIR = BASE_DIR / "models"

MODELS_DIR.mkdir(
    parents=True,
    exist_ok=True,
)

# Keep the current production score model untouched until the
# candidate has been reviewed.
PRODUCTION_MODEL_PATH = (
    MODELS_DIR
    / "fourthdown_score_model.joblib"
)

PRODUCTION_METADATA_PATH = (
    MODELS_DIR
    / "score_model_metadata.json"
)

CANDIDATE_MODEL_PATH = (
    MODELS_DIR
    / "fourthdown_score_model_candidate.joblib"
)

CANDIDATE_METADATA_PATH = (
    MODELS_DIR
    / "score_model_metadata_candidate.json"
)


# ============================================================
# SPRING TRAINING ENDPOINT
# ============================================================

TRAINING_URL = (
    "http://localhost:8080"
    "/api/v1/ml/training-data"
)

PARAMS = {
    "startSeason": 2023,
    "endSeason": 2025,
    "minPriorGames": 3,
}


# ============================================================
# FEATURES
# ============================================================

FEATURES = [
    "pointsDifference",
    "yardsDifference",
    "turnoverDifference",
    "scoringMarginDifference",
    "thirdDownDifference",
    "redZoneDifference",
    "recentFormDifference",
    "opponentWinRateDifference",
    "opponentScoringMarginDifference",
]

TARGETS = [
    "scoreMargin",
    "totalPoints",
]

EVALUATION_TRAIN_SEASONS = [
    2023,
    2024,
]

TEST_SEASON = 2025

DEPLOYMENT_TRAIN_SEASONS = [
    2023,
    2024,
    2025,
]


# ============================================================
# HELPERS
# ============================================================

def load_existing_metadata():
    if not PRODUCTION_METADATA_PATH.exists():
        return None

    try:
        with open(
            PRODUCTION_METADATA_PATH,
            "r",
            encoding="utf-8",
        ) as file:
            return json.load(file)
    except (OSError, json.JSONDecodeError):
        return None


def build_ridge_model():
    return Pipeline(
        steps=[
            (
                "scaler",
                StandardScaler(),
            ),
            (
                "regressor",
                Ridge(
                    alpha=1.0,
                ),
            ),
        ]
    )


def build_random_forest_model():
    return RandomForestRegressor(
        n_estimators=400,
        max_depth=12,
        min_samples_leaf=4,
        random_state=42,
        n_jobs=-1,
    )


def evaluate_model(
    name,
    predictions,
    test_df,
    y_test,
):
    predicted_margin = (
        predictions[:, 0]
    )

    predicted_total = (
        predictions[:, 1]
    )

    actual_margin = (
        y_test["scoreMargin"]
        .to_numpy()
    )

    actual_total = (
        y_test["totalPoints"]
        .to_numpy()
    )

    projected_home = (
        predicted_total
        + predicted_margin
    ) / 2.0

    projected_away = (
        predicted_total
        - predicted_margin
    ) / 2.0

    actual_home = (
        test_df["homeScore"]
        .to_numpy()
    )

    actual_away = (
        test_df["awayScore"]
        .to_numpy()
    )

    margin_mae = (
        mean_absolute_error(
            actual_margin,
            predicted_margin,
        )
    )

    margin_rmse = np.sqrt(
        mean_squared_error(
            actual_margin,
            predicted_margin,
        )
    )

    total_mae = (
        mean_absolute_error(
            actual_total,
            predicted_total,
        )
    )

    home_score_mae = (
        mean_absolute_error(
            actual_home,
            projected_home,
        )
    )

    away_score_mae = (
        mean_absolute_error(
            actual_away,
            projected_away,
        )
    )

    combined_score_mae = (
        home_score_mae
        + away_score_mae
    ) / 2.0

    metrics = {
        "marginMae":
            float(
                margin_mae
            ),

        "marginRmse":
            float(
                margin_rmse
            ),

        "totalPointsMae":
            float(
                total_mae
            ),

        "homeScoreMae":
            float(
                home_score_mae
            ),

        "awayScoreMae":
            float(
                away_score_mae
            ),

        "combinedScoreMae":
            float(
                combined_score_mae
            ),
    }

    print(name)
    print(
        "-" * len(name)
    )
    print(
        f"Margin MAE:       "
        f"{margin_mae:.3f}"
    )
    print(
        f"Margin RMSE:      "
        f"{margin_rmse:.3f}"
    )
    print(
        f"Total Points MAE: "
        f"{total_mae:.3f}"
    )
    print(
        f"Home Score MAE:   "
        f"{home_score_mae:.3f}"
    )
    print(
        f"Away Score MAE:   "
        f"{away_score_mae:.3f}"
    )
    print(
        f"Combined MAE:     "
        f"{combined_score_mae:.3f}"
    )
    print()

    return metrics


# ============================================================
# DOWNLOAD DATA
# ============================================================

print()
print("Downloading training data...")
print()

response = requests.get(
    TRAINING_URL,
    params=PARAMS,
    timeout=300,
)

response.raise_for_status()

data = response.json()

print(
    f"Loaded {len(data):,} "
    "training examples."
)


# ============================================================
# DATAFRAME / VALIDATION
# ============================================================

df = pd.DataFrame(
    data
)

print()
print("Preparing score targets...")
print()

required_columns = (
    FEATURES
    + [
        "season",
        "homeScore",
        "awayScore",
    ]
)

missing_columns = [
    column
    for column in required_columns
    if column not in df.columns
]

if missing_columns:
    raise ValueError(
        "Training dataset is missing "
        f"required columns: {missing_columns}"
    )

for column in FEATURES:
    df[column] = pd.to_numeric(
        df[column],
        errors="coerce",
    )

for column in [
    "season",
    "homeScore",
    "awayScore",
]:
    df[column] = pd.to_numeric(
        df[column],
        errors="coerce",
    )

df = df.dropna(
    subset=(
        FEATURES
        + [
            "season",
            "homeScore",
            "awayScore",
        ]
    )
).copy()

df["scoreMargin"] = (
    df["homeScore"]
    - df["awayScore"]
)

df["totalPoints"] = (
    df["homeScore"]
    + df["awayScore"]
)

print(
    f"{len(df):,} examples "
    "remain after cleaning."
)


# ============================================================
# TIME-BASED HOLDOUT
#
# Evaluate:
# Train = 2023 + 2024
# Test  = 2025
#
# After model selection, refit the chosen algorithm on
# 2023 + 2024 + 2025 for the deployment candidate.
# ============================================================

train_df = df[
    df["season"].isin(
        EVALUATION_TRAIN_SEASONS
    )
].copy()

test_df = df[
    df["season"] == TEST_SEASON
].copy()

deployment_df = df[
    df["season"].isin(
        DEPLOYMENT_TRAIN_SEASONS
    )
].copy()

if train_df.empty:
    raise ValueError(
        "Training split is empty."
    )

if test_df.empty:
    raise ValueError(
        "Testing split is empty."
    )

if deployment_df.empty:
    raise ValueError(
        "Deployment training split is empty."
    )

print()
print("Time-based evaluation split:")
print()
print(
    "Training examples: "
    f"{len(train_df):,}"
)
print(
    "Testing examples:  "
    f"{len(test_df):,}"
)
print(
    "Deployment examples after evaluation: "
    f"{len(deployment_df):,}"
)
print()


# ============================================================
# X / Y
# ============================================================

X_train = train_df[
    FEATURES
]

X_test = test_df[
    FEATURES
]

y_train = train_df[
    TARGETS
]

y_test = test_df[
    TARGETS
]


# ============================================================
# RIDGE REGRESSION
# ============================================================

print(
    "Training Ridge Regression "
    "for holdout evaluation..."
)
print()

ridge_model = (
    build_ridge_model()
)

ridge_model.fit(
    X_train,
    y_train,
)

ridge_predictions = (
    ridge_model.predict(
        X_test
    )
)

ridge_metrics = (
    evaluate_model(
        "Ridge Regression",
        ridge_predictions,
        test_df,
        y_test,
    )
)


# ============================================================
# RANDOM FOREST REGRESSION
# ============================================================

print(
    "Training Random Forest Regressor "
    "for holdout evaluation..."
)
print()

rf_model = (
    build_random_forest_model()
)

rf_model.fit(
    X_train,
    y_train,
)

rf_predictions = (
    rf_model.predict(
        X_test
    )
)

rf_metrics = (
    evaluate_model(
        "Random Forest Regressor",
        rf_predictions,
        test_df,
        y_test,
    )
)


# ============================================================
# MODEL SELECTION
# ============================================================

if (
    ridge_metrics[
        "combinedScoreMae"
    ]
    <=
    rf_metrics[
        "combinedScoreMae"
    ]
):
    best_model_name = (
        "Ridge Regression"
    )

    deployment_model = (
        build_ridge_model()
    )

    best_metrics = (
        ridge_metrics
    )
else:
    best_model_name = (
        "Random Forest Regressor"
    )

    deployment_model = (
        build_random_forest_model()
    )

    best_metrics = (
        rf_metrics
    )

print(
    f"Best evaluated score model: "
    f"{best_model_name}"
)
print()


# ============================================================
# COMPARE WITH CURRENT PRODUCTION METADATA
# ============================================================

existing_metadata = (
    load_existing_metadata()
)

if existing_metadata:
    existing_metrics = (
        existing_metadata.get(
            "metrics",
            {},
        )
    )

    old_combined_mae = (
        existing_metrics.get(
            "combinedScoreMae"
        )
    )

    print(
        "Current production comparison:"
    )

    if old_combined_mae is not None:
        print(
            "Combined MAE delta: "
            f"{best_metrics['combinedScoreMae'] - float(old_combined_mae):+.3f}"
            "  (lower is better)"
        )

    print()


# ============================================================
# REFIT CHOSEN ALGORITHM ON ALL COMPLETED HISTORICAL DATA
# ============================================================

print(
    "Refitting selected algorithm on "
    "2023-2025 for deployment candidate..."
)
print()

X_deployment = deployment_df[
    FEATURES
]

y_deployment = deployment_df[
    TARGETS
]

deployment_model.fit(
    X_deployment,
    y_deployment,
)


# ============================================================
# SAVE CANDIDATE ONLY
# ============================================================

joblib.dump(
    deployment_model,
    CANDIDATE_MODEL_PATH,
)

print(
    "Saved candidate score model to:"
)
print(
    CANDIDATE_MODEL_PATH
)
print()


# ============================================================
# METADATA
# ============================================================

metadata = {
    "modelName":
        best_model_name,

    "status":
        "candidate",

    "generatedAtUtc":
        datetime.now(
            timezone.utc
        ).isoformat(),

    "features":
        FEATURES,

    "featureCount":
        len(FEATURES),

    "targets":
        TARGETS,

    "evaluationTrainingSeasons":
        EVALUATION_TRAIN_SEASONS,

    "testSeason":
        TEST_SEASON,

    "deploymentTrainingSeasons":
        DEPLOYMENT_TRAIN_SEASONS,

    "evaluationTrainingExamples":
        int(
            len(train_df)
        ),

    "testingExamples":
        int(
            len(test_df)
        ),

    "deploymentTrainingExamples":
        int(
            len(deployment_df)
        ),

    "totalExamples":
        int(
            len(df)
        ),

    "metrics":
        best_metrics,

    "ridgeRegression":
        ridge_metrics,

    "randomForestRegressor":
        rf_metrics,
}

with open(
    CANDIDATE_METADATA_PATH,
    "w",
    encoding="utf-8",
) as file:
    json.dump(
        metadata,
        file,
        indent=2,
    )

print(
    "Saved candidate score metadata to:"
)
print(
    CANDIDATE_METADATA_PATH
)
print()
print(
    "Production score model was NOT overwritten."
)
print()