import json
from datetime import datetime, timezone
from pathlib import Path

import joblib
import pandas as pd
import requests

from sklearn.base import clone
from sklearn.ensemble import RandomForestClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import (
    accuracy_score,
    log_loss,
    roc_auc_score,
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

# Keep the current production model untouched until the
# candidate has been reviewed.
PRODUCTION_MODEL_PATH = (
    MODELS_DIR /
    "fourthdown_model.joblib"
)

PRODUCTION_METADATA_PATH = (
    MODELS_DIR /
    "model_metadata.json"
)

CANDIDATE_MODEL_PATH = (
    MODELS_DIR /
    "fourthdown_model_candidate.joblib"
)

CANDIDATE_METADATA_PATH = (
    MODELS_DIR /
    "model_metadata_candidate.json"
)


# ============================================================
# SPRING BOOT TRAINING ENDPOINT
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
# MODEL FEATURES
#
# These names MUST exactly match:
#
# - Spring TrainingExampleResponse
# - MlPredictionRequest
# - FastAPI prediction request
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

TARGET = "homeWon"

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

# If ROC-AUC values are essentially tied, prefer the model
# with better probability calibration / lower log loss.
AUC_TOLERANCE = 0.002


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


def build_logistic_model():
    return Pipeline(
        steps=[
            (
                "scaler",
                StandardScaler(),
            ),
            (
                "classifier",
                LogisticRegression(
                    max_iter=2000,
                    random_state=42,
                ),
            ),
        ]
    )


def build_random_forest_model():
    return RandomForestClassifier(
        n_estimators=400,
        max_depth=10,
        min_samples_leaf=4,
        random_state=42,
        n_jobs=-1,
        class_weight="balanced",
    )


def evaluate_classifier(
    model,
    X_test,
    y_test,
):
    predictions = model.predict(
        X_test
    )

    probabilities = (
        model.predict_proba(
            X_test
        )[:, 1]
    )

    return {
        "accuracy": float(
            accuracy_score(
                y_test,
                predictions,
            )
        ),
        "rocAuc": float(
            roc_auc_score(
                y_test,
                probabilities,
            )
        ),
        "logLoss": float(
            log_loss(
                y_test,
                probabilities,
            )
        ),
    }


def print_metrics(
    name,
    metrics,
):
    print(name)
    print("-" * len(name))
    print(
        f"Accuracy: {metrics['accuracy']:.4f}"
    )
    print(
        f"ROC-AUC:  {metrics['rocAuc']:.4f}"
    )
    print(
        f"Log Loss: {metrics['logLoss']:.4f}"
    )
    print()


def choose_best_model(
    logistic_metrics,
    rf_metrics,
):
    auc_difference = (
        logistic_metrics["rocAuc"]
        - rf_metrics["rocAuc"]
    )

    if abs(auc_difference) > AUC_TOLERANCE:
        if auc_difference > 0:
            return (
                "Logistic Regression",
                build_logistic_model(),
                logistic_metrics,
            )

        return (
            "Random Forest",
            build_random_forest_model(),
            rf_metrics,
        )

    if (
        logistic_metrics["logLoss"]
        <= rf_metrics["logLoss"]
    ):
        return (
            "Logistic Regression",
            build_logistic_model(),
            logistic_metrics,
        )

    return (
        "Random Forest",
        build_random_forest_model(),
        rf_metrics,
    )


# ============================================================
# DOWNLOAD TRAINING DATA
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
print("Cleaning dataset...")
print()

required_columns = (
    FEATURES
    + [
        TARGET,
        "season",
        "gameDate",
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

df[TARGET] = pd.to_numeric(
    df[TARGET],
    errors="coerce",
)

df["season"] = pd.to_numeric(
    df["season"],
    errors="coerce",
)

df = df.dropna(
    subset=(
        FEATURES
        + [
            TARGET,
            "season",
        ]
    )
)

df = df[
    df[TARGET].isin(
        [0, 1]
    )
].copy()

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

y_train = train_df[
    TARGET
].astype(int)

X_test = test_df[
    FEATURES
]

y_test = test_df[
    TARGET
].astype(int)


# ============================================================
# EVALUATE LOGISTIC REGRESSION
# ============================================================

print(
    "Training Logistic Regression "
    "for holdout evaluation..."
)
print()

logistic_model = (
    build_logistic_model()
)

logistic_model.fit(
    X_train,
    y_train,
)

logistic_metrics = (
    evaluate_classifier(
        logistic_model,
        X_test,
        y_test,
    )
)

print_metrics(
    "Logistic Regression",
    logistic_metrics,
)


# ============================================================
# EVALUATE RANDOM FOREST
# ============================================================

print(
    "Training Random Forest "
    "for holdout evaluation..."
)
print()

random_forest_model = (
    build_random_forest_model()
)

random_forest_model.fit(
    X_train,
    y_train,
)

rf_metrics = (
    evaluate_classifier(
        random_forest_model,
        X_test,
        y_test,
    )
)

print_metrics(
    "Random Forest",
    rf_metrics,
)


# ============================================================
# MODEL SELECTION
# ============================================================

(
    best_model_name,
    deployment_model,
    best_metrics,
) = choose_best_model(
    logistic_metrics,
    rf_metrics,
)

print(
    f"Best evaluated model: "
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
    old_auc = existing_metadata.get(
        "rocAuc"
    )

    old_accuracy = existing_metadata.get(
        "accuracy"
    )

    old_log_loss = existing_metadata.get(
        "logLoss"
    )

    print(
        "Current production comparison:"
    )

    if old_auc is not None:
        print(
            "ROC-AUC delta:  "
            f"{best_metrics['rocAuc'] - float(old_auc):+.4f}"
        )

    if old_accuracy is not None:
        print(
            "Accuracy delta: "
            f"{best_metrics['accuracy'] - float(old_accuracy):+.4f}"
        )

    if old_log_loss is not None:
        print(
            "Log Loss delta: "
            f"{best_metrics['logLoss'] - float(old_log_loss):+.4f}"
            "  (lower is better)"
        )

    print()


# ============================================================
# REFIT CHOSEN ALGORITHM ON ALL COMPLETED HISTORICAL DATA
#
# This does NOT change the holdout metrics above. Those metrics
# remain based only on 2023-2024 -> 2025 evaluation.
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
    TARGET
].astype(int)

deployment_model.fit(
    X_deployment,
    y_deployment,
)


# ============================================================
# SAVE CANDIDATE ONLY
#
# Do not overwrite the current production model until the new
# holdout results have been reviewed.
# ============================================================

joblib.dump(
    deployment_model,
    CANDIDATE_MODEL_PATH,
)

print(
    "Saved candidate winner model to:"
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

    "accuracy":
        float(
            best_metrics[
                "accuracy"
            ]
        ),

    "rocAuc":
        float(
            best_metrics[
                "rocAuc"
            ]
        ),

    "logLoss":
        float(
            best_metrics[
                "logLoss"
            ]
        ),

    "logisticRegression":
        logistic_metrics,

    "randomForest":
        rf_metrics,

    "featureEngineering": {
        "seasonAware":
            True,

        "kickoffCutoff":
            True,

        "priorSeasonFallback":
            True,

        "currentSeasonFullWeightAfterGames":
            4,

        "opponentStrength":
            True,
    },
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
    "Saved candidate metadata to:"
)
print(
    CANDIDATE_METADATA_PATH
)
print()
print(
    "Production winner model was NOT overwritten."
)
print()