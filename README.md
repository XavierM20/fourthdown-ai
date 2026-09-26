<p align="center">
  <img src="docs/assets/fourthdown-ai-banner.png" alt="FourthDown AI banner" width="100%" />
</p>

<h1 align="center">FourthDown AI</h1>

<p align="center">
  <strong>Full-stack college football analytics, rankings, and machine-learning predictions.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/React-TypeScript-61DAFB?logo=react&logoColor=white" alt="React" />
  <img src="https://img.shields.io/badge/Spring%20Boot-Java%2021-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/FastAPI-Python-009688?logo=fastapi&logoColor=white" alt="FastAPI" />
  <img src="https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white" alt="PostgreSQL" />
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white" alt="Docker" />
  <img src="https://img.shields.io/badge/ML-scikit--learn-F7931E?logo=scikitlearn&logoColor=white" alt="scikit-learn" />
</p>

<p align="center">
  <a href="#-features">Features</a> •
  <a href="#-architecture">Architecture</a> •
  <a href="#-machine-learning">Machine Learning</a> •
  <a href="#-local-development">Local Development</a> •
  <a href="#-api-overview">API</a> •
  <a href="#-roadmap">Roadmap</a>
</p>

Overview

FourthDown AI is an end-to-end college football analytics application built to explore teams, games, rankings, historical performance, and matchup predictions across FBS and FCS football.

The project combines a modern React frontend with a Spring Boot API, PostgreSQL, and a dedicated FastAPI machine-learning service. Real college football data is imported from CollegeFootballData (CFBD), transformed into pregame features, and used by trained models to estimate win probability and projected scores.

Why this project exists

FourthDown AI was built as a portfolio project to demonstrate practical full-stack and applied machine-learning engineering in one system:

third-party sports-data ingestion

relational database design

REST API development

feature engineering and leakage prevention

model training and evaluation

Java-to-Python service integration

interactive data visualization

production-oriented environment configuration

Docker-based local infrastructure

✨ Features

Area

What it does

Dashboard

Shows team/game counts, upcoming games, and production model performance

Teams

Browse FBS/FCS teams, season records, analytics, rankings, and ranking history

Games

Explore regular-season, bowl, and playoff games with scores and metadata

Rankings

View AP, CFP, and FCS poll data with weekly movement and history

Analytics

Compare scoring, yardage, turnovers, third downs, red-zone efficiency, and form

Predictions

Generate matchup win probabilities and projected scores

ML Monitoring

Surface model metadata and holdout evaluation metrics inside the app

🧭 Application Flow

flowchart LR
    A[React + TypeScript] -->|REST API| B[Spring Boot]
    B --> C[(PostgreSQL)]
    B -->|HTTP| D[FastAPI ML Service]
    D --> E[Logistic Regression]
    D --> F[Ridge Regression]
    G[CollegeFootballData API] --> B

For normal application requests:

Browser → React → Spring Boot → PostgreSQL

For a prediction:

Browser → React → Spring Boot
                       ├── PostgreSQL historical/team data
                       └── FastAPI → trained ML models

The frontend does not call the Python ML service directly. Spring Boot is the public backend API and coordinates database access and ML inference.

🧱 Architecture

fourthdown-ai/
├── frontend/                  React + TypeScript + Vite
│   ├── src/api/               API clients
│   ├── src/components/        Reusable UI components
│   └── src/pages/             Dashboard, Teams, Games, Analytics, Rankings, Predictions
│
├── backend/                   Spring Boot / Java 21
│   └── src/main/java/
│       ├── client/            CFBD + ML service clients
│       ├── config/            CORS / application configuration
│       ├── controller/        REST controllers
│       ├── dto/               API response/request models
│       ├── model/             JPA entities
│       ├── repository/        Spring Data repositories
│       └── service/           Business logic and feature engineering
│
├── ml/                        FastAPI + scikit-learn
│   ├── models/                Production model artifacts + metadata
│   ├── inference_api.py       ML inference service
│   ├── train_model.py         Winner-model training
│   └── train_score_model.py   Score-model training
│
├── docker-compose.yml         PostgreSQL local environment
├── start-dev.sh               One-command local startup
└── README.md

🛠 Tech Stack

Frontend

React

TypeScript

Vite

Tailwind CSS

React Router

TanStack Query

Recharts

Lucide React

Backend

Java 21

Spring Boot 4

Spring Web MVC

Spring Data JPA

Bean Validation

Spring Boot Actuator

Maven

Machine Learning

Python

FastAPI

Uvicorn

pandas

NumPy

scikit-learn

joblib

Infrastructure / Data

PostgreSQL 16

Docker + Docker Compose

CollegeFootballData (CFBD)

🤖 Machine Learning

FourthDown AI uses two production models.

Winner prediction

Algorithm: Logistic Regression

Metric

Holdout Result

Accuracy

72.96%

ROC-AUC

0.7968

Log Loss

0.5291

Score prediction

Algorithm: Ridge Regression

Metric

Holdout Result

Combined Team Score MAE

9.73

Margin MAE

13.87

Margin RMSE

17.76

Total Points MAE

13.21

Home Score MAE

10.22

Away Score MAE

9.24

Evaluation strategy

The winner and score models were evaluated using a season-based holdout:

Evaluation training seasons: 2023 + 2024
Holdout season:             2025
Training examples:          2,749
Testing examples:           1,631
Total examples:             4,380

After evaluation, the production models are refit using the complete 2023–2025 dataset.

Engineered features

The production feature vector contains nine home-vs-away differences:

Points

Total yards

Turnovers

Scoring margin

Third-down efficiency

Red-zone efficiency

Recent form

Opponent win rate

Opponent scoring margin

Leakage prevention

Pregame features are calculated using information available before kickoff of the game being predicted. Future-game data is excluded from each historical training example.

Early-season fallback

When a team has limited current-season data, prior-season information is blended in:

Current-season games

Current season

Prior season

0

0%

100%

1

25%

75%

2

50%

50%

3

75%

25%

4+

100%

0%

🏆 Rankings

FourthDown AI supports weekly ranking data and ranking-history visualization.

FBS

AP Top 25 before College Football Playoff rankings become available

College Football Playoff rankings once available

FCS

FCS Coaches Poll

Ranking views support:

current rank

previous rank

movement between weeks

first-place votes

poll points

weekly ranking history

ranked/unranked transitions

season and classification selection

📥 Data Import

The backend imports real college football data from CFBD.

Supported data includes:

FBS teams

FCS teams

games

regular season

postseason bowls

playoff rounds

team game statistics

season records

rankings

Postseason data is handled using the actual postseason weeks returned by the upstream data source rather than assuming one fixed week range.

Known data limitation

A small number of completed games may not have detailed team-game statistics available from the imported statistics endpoint. FourthDown AI keeps the valid game result and final score rather than inventing missing statistics.

💻 Local Development

Prerequisites

Install:

Git

Docker Desktop

Java 21+

Maven

Node.js / npm

Python 3

1. Clone

git clone https://github.com/XavierM20/fourthdown-ai.git
cd fourthdown-ai

2. Create local environment configuration

cp .env.example .env.local

Then set your local values:

CFBD_API_KEY=replace_with_your_cfbd_api_key

BACKEND_PORT=8080
ML_PORT=8000
FRONTEND_PORT=5173

SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/fourthdown
SPRING_DATASOURCE_USERNAME=fourthdown
SPRING_DATASOURCE_PASSWORD=fourthdown_dev

ML_BASE_URL=http://localhost:8000
FRONTEND_ORIGIN=http://localhost:5173

HIBERNATE_DDL_AUTO=update
JPA_SHOW_SQL=false
JPA_FORMAT_SQL=false

HOST=0.0.0.0
RELOAD=false

Never commit .env.local. The repository only contains safe example environment files.

3. Frontend environment

Create:

frontend/.env.development

with:

VITE_API_URL=http://localhost:8080/api/v1

4. Install frontend dependencies

cd frontend
npm install
cd ..

5. Create the Python environment

cd ml
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
cd ..

6. Start the full stack

chmod +x start-dev.sh
./start-dev.sh

Service

Local address

Frontend

http://localhost:5173

Spring Boot

http://localhost:8080

FastAPI

http://localhost:8000

PostgreSQL

localhost:5432

Press Ctrl+C to stop Spring Boot, FastAPI, and Vite. PostgreSQL remains running in Docker.

❤️ Health Checks

Spring Boot

curl http://localhost:8080/actuator/health

Expected:

{"status":"UP"}

FastAPI

curl http://localhost:8000/health

🔌 API Overview

Representative endpoints:

GET  /api/v1/teams
GET  /api/v1/teams/{id}

GET  /api/v1/games
GET  /api/v1/games/{id}

GET  /api/v1/analytics/teams/{teamId}
GET  /api/v1/analytics/compare

GET  /api/v1/rankings
GET  /api/v1/rankings/history

POST /api/v1/predictions/matchup
GET  /api/v1/predictions/game/{gameId}

GET  /api/v1/ml/model
GET  /api/v1/ml/score-model

CFBD integration and data-import endpoints are also exposed through the Spring Boot API.

🔐 Environment & Security

CFBD credentials stay on the backend.

.env.local is excluded from Git.

VITE_* values are public browser-build variables and must not contain secrets.

Production CORS is controlled with FRONTEND_ORIGIN.

PostgreSQL credentials are provided through environment variables.

Spring Boot communicates with FastAPI server-to-server.

🚀 Production Deployment

The production architecture is designed around separate services:

Public React Frontend
        │
        ▼
Public Spring Boot API
        │
        ├──────────────► Managed PostgreSQL
        │
        └──────────────► FastAPI ML Service

The deployment target for this project is Railway.

Deployment configuration will use:

managed PostgreSQL

environment-based credentials

service-to-service ML communication

production CORS

GitHub-connected automatic deployments

🧪 Frontend Production Build

cd frontend
npm run build

Major application routes use lazy loading/code splitting to reduce the size of the initial production JavaScript bundle.

📸 Screenshots

Real application screenshots will be added here after the final production UI/deployment pass.

Recommended captures:

Dashboard

Rankings

Team Details

Team Comparison

Matchup Prediction

Using real application screenshots here is preferable to mockups because it gives reviewers an immediate view of the actual product.

🗺 Roadmap

FBS and FCS team import

Historical game import

Detailed team-game statistics

Bowl and playoff support

Team analytics

Team comparison

AP / CFP / FCS rankings

Weekly ranking history

Winner prediction model

Score prediction model

FastAPI inference service

React route-level code splitting

Environment-based configuration

One-command local startup

GitHub repository

Railway production deployment

Production database initialization

Public application URL

Real application screenshots

CI/CD validation

👤 Author

Xavier Mathews

Computer Science / Software Engineering

FourthDown AI was built as a portfolio project focused on full-stack engineering, backend systems, sports-data pipelines, and applied machine learning.
