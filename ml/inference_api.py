import json
import os
from pathlib import Path

import joblib
import pandas as pd
import uvicorn

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel


# ============================================================
# PATHS
# ============================================================

BASE_DIR = Path(__file__).resolve().parent

MODELS_DIR = (
    BASE_DIR
    / "models"
)

WINNER_MODEL_PATH = (
    MODELS_DIR
    / "fourthdown_model.joblib"
)

WINNER_METADATA_PATH = (
    MODELS_DIR
    / "model_metadata.json"
)

SCORE_MODEL_PATH = (
    MODELS_DIR
    / "fourthdown_score_model.joblib"
)

SCORE_METADATA_PATH = (
    MODELS_DIR
    / "score_model_metadata.json"
)


# ============================================================
# FEATURES
#
# The order MUST match the training scripts.
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


# ============================================================
# LOAD WINNER MODEL
# ============================================================

winner_model = None

try:

    winner_model = joblib.load(
        WINNER_MODEL_PATH
    )

    print(
        "Loaded winner model from:"
    )

    print(
        WINNER_MODEL_PATH
    )

except Exception as exc:

    print(
        "Failed to load winner model:"
    )

    print(exc)


# ============================================================
# LOAD SCORE MODEL
# ============================================================

score_model = None

try:

    score_model = joblib.load(
        SCORE_MODEL_PATH
    )

    print(
        "Loaded score model from:"
    )

    print(
        SCORE_MODEL_PATH
    )

except Exception as exc:

    print(
        "Failed to load score model:"
    )

    print(exc)


# ============================================================
# LOAD WINNER MODEL METADATA
# ============================================================

winner_metadata = None

try:

    with open(
        WINNER_METADATA_PATH,
        "r",
        encoding="utf-8",
    ) as file:

        winner_metadata = (
            json.load(file)
        )

    print(
        "Loaded winner model metadata."
    )

except Exception as exc:

    print(
        "Failed to load winner metadata:"
    )

    print(exc)


# ============================================================
# LOAD SCORE MODEL METADATA
# ============================================================

score_metadata = None

try:

    with open(
        SCORE_METADATA_PATH,
        "r",
        encoding="utf-8",
    ) as file:

        score_metadata = (
            json.load(file)
        )

    print(
        "Loaded score model metadata."
    )

except Exception as exc:

    print(
        "Failed to load score metadata:"
    )

    print(exc)


# ============================================================
# FASTAPI
# ============================================================

app = FastAPI(
    title="FourthDown AI ML Service",
    version="2.0.0",
)


# ============================================================
# REQUEST
# ============================================================

class PredictionRequest(BaseModel):

    pointsDifference: float

    yardsDifference: float

    turnoverDifference: float

    scoringMarginDifference: float

    thirdDownDifference: float

    redZoneDifference: float

    recentFormDifference: float

    opponentWinRateDifference: float

    opponentScoringMarginDifference: float


# ============================================================
# RESPONSE
# ============================================================

class PredictionResponse(BaseModel):

    homeWinProbability: float

    awayWinProbability: float

    predictedHomeWin: bool

    projectedHomeScore: int

    projectedAwayScore: int

    projectedMargin: float

    projectedTotalPoints: float


# ============================================================
# BUILD FEATURE FRAME
# ============================================================

def build_feature_frame(
    request: PredictionRequest
):

    return pd.DataFrame(
        [
            {
                "pointsDifference":
                    request.pointsDifference,

                "yardsDifference":
                    request.yardsDifference,

                "turnoverDifference":
                    request.turnoverDifference,

                "scoringMarginDifference":
                    request.scoringMarginDifference,

                "thirdDownDifference":
                    request.thirdDownDifference,

                "redZoneDifference":
                    request.redZoneDifference,

                "recentFormDifference":
                    request.recentFormDifference,

                "opponentWinRateDifference":
                    request.opponentWinRateDifference,

                "opponentScoringMarginDifference":
                    request.opponentScoringMarginDifference,
            }
        ],
        columns=FEATURES,
    )


# ============================================================
# HEALTH
# ============================================================

@app.get("/health")
def health():

    return {
        "status": "UP",

        "winnerModelLoaded":
            winner_model is not None,

        "scoreModelLoaded":
            score_model is not None,

        "featureCount":
            len(FEATURES),
    }


# ============================================================
# WINNER MODEL METADATA
# ============================================================

@app.get("/metadata")
def get_metadata():

    if winner_metadata is None:

        raise HTTPException(
            status_code=503,
            detail=(
                "Winner model metadata "
                "is not loaded"
            ),
        )

    return winner_metadata


# ============================================================
# SCORE MODEL METADATA
# ============================================================

@app.get("/score-metadata")
def get_score_metadata():

    if score_metadata is None:

        raise HTTPException(
            status_code=503,
            detail=(
                "Score model metadata "
                "is not loaded"
            ),
        )

    return score_metadata


# ============================================================
# PREDICT
# ============================================================

@app.post(
    "/predict",
    response_model=PredictionResponse,
)
def predict(
    request: PredictionRequest
):

    if winner_model is None:

        raise HTTPException(
            status_code=503,
            detail=(
                "Winner model is not loaded"
            ),
        )

    if score_model is None:

        raise HTTPException(
            status_code=503,
            detail=(
                "Score model is not loaded"
            ),
        )

    try:

        frame = build_feature_frame(
            request
        )

        # ====================================================
        # WIN PROBABILITY MODEL
        # ====================================================

        probabilities = (
            winner_model.predict_proba(
                frame
            )[0]
        )

        away_win_probability = float(
            probabilities[0]
        )

        home_win_probability = float(
            probabilities[1]
        )

        predicted_home_win = (
            home_win_probability
            >=
            away_win_probability
        )


        # ====================================================
        # SCORE MODEL
        #
        # Output:
        #
        # [0] = projected scoring margin
        # [1] = projected total points
        # ====================================================

        score_prediction = (
            score_model.predict(
                frame
            )[0]
        )

        projected_margin = float(
            score_prediction[0]
        )

        projected_total_points = float(
            score_prediction[1]
        )


        # ====================================================
        # PROTECT AGAINST IMPOSSIBLE TOTALS
        # ====================================================

        projected_total_points = max(
            0.0,
            projected_total_points,
        )


        # ====================================================
        # RECONSTRUCT TEAM SCORES
        #
        # home = (total + margin) / 2
        # away = (total - margin) / 2
        # ====================================================

        projected_home_score = (
            projected_total_points
            + projected_margin
        ) / 2.0

        projected_away_score = (
            projected_total_points
            - projected_margin
        ) / 2.0


        projected_home_score = max(
            0,
            round(
                projected_home_score
            ),
        )

        projected_away_score = max(
            0,
            round(
                projected_away_score
            ),
        )


        # ====================================================
        # KEEP SCORE CONSISTENT WITH WINNER CLASSIFIER
        # The score model is responsible for score magnitude.
        # ====================================================

        if predicted_home_win:

            if (
                projected_home_score
                <=
                projected_away_score
            ):

                projected_home_score = (
                    projected_away_score
                    + 1
                )

        else:

            if (
                projected_away_score
                <=
                projected_home_score
            ):

                projected_away_score = (
                    projected_home_score
                    + 1
                )


        # ====================================================
        # RESPONSE
        # ====================================================

        return PredictionResponse(

            homeWinProbability=
                home_win_probability,

            awayWinProbability=
                away_win_probability,

            predictedHomeWin=
                predicted_home_win,

            projectedHomeScore=
                projected_home_score,

            projectedAwayScore=
                projected_away_score,

            projectedMargin=
                projected_margin,

            projectedTotalPoints=
                projected_total_points,
        )

    except Exception as exc:

        print(
            "Prediction error:"
        )

        print(exc)

        raise HTTPException(
            status_code=500,
            detail=str(exc),
        )

# ============================================================
# LOCAL / PRODUCTION ENTRY POINT
#
# Local defaults:
#   HOST=0.0.0.0
#   PORT=8000
# ============================================================

if __name__ == "__main__":
    host = os.getenv(
        "HOST",
        "0.0.0.0",
    )

    port = int(
        os.getenv(
            "PORT",
            "8000",
        )
    )

    reload_enabled = (
        os.getenv(
            "RELOAD",
            "false",
        ).lower()
        == "true"
    )

    uvicorn.run(
        "inference_api:app",
        host=host,
        port=port,
        reload=reload_enabled,
    )