# FourthDown AI

FourthDown AI is a full-stack college football analytics and machine-learning application built to explore teams, games, rankings, historical performance, and matchup predictions across FBS and FCS football.

The application combines a React frontend, a Spring Boot API, PostgreSQL, and a FastAPI machine-learning service. Real college football data is imported from CollegeFootballData (CFBD), transformed into team and game-level features, and used by trained machine-learning models to generate win probabilities and projected scores.

## Features

### Dashboard
- High-level counts for tracked teams and games
- Upcoming-game summary
- Production model performance
- Quick navigation to major application features

### Teams
- Browse FBS and FCS teams
- Team details and metadata
- Season records
- Team analytics
- Ranking information
- Weekly ranking-history visualization
- Conference and classification filtering

### Games
- Browse regular-season and postseason games
- Search and filter by season, week, team, and postseason status
- Bowl and playoff game names
- Final scores and venue information
- Game-level statistics

### Rankings
- FBS Top 25 rankings
  - AP Poll before CFP rankings become available
  - College Football Playoff rankings once available
- FCS Coaches Poll
- Weekly ranking history
- Ranking movement between weeks
- Team-specific ranking details

### Analytics
- Team performance metrics
- Scoring and yardage statistics
- Turnover metrics
- Third-down efficiency
- Red-zone performance
- Recent form
- Team comparison tools

### Matchup Prediction
FourthDown AI uses two production machine-learning models:

- **Logistic Regression** for game winner probabilities
- **Ridge Regression** for projected scoring margin and total points

The score prediction is converted into projected home and away final scores.

## Architecture

```text
                         ┌──────────────────────┐
                         │   React + TypeScript │
                         │   Vite + Tailwind    │
                         └──────────┬───────────┘
                                    │
                                    │ REST
                                    ▼
                         ┌──────────────────────┐
                         │     Spring Boot      │
                         │       Java 21        │
                         │      REST API        │
                         └───────┬───────┬──────┘
                                 │       │
                    PostgreSQL   │       │ ML inference
                                 │       ▼
                                 │  ┌──────────────────────┐
                                 │  │      FastAPI         │
                                 │  │ Python + scikit-learn│
                                 │  └──────────────────────┘
                                 │
                                 ▼
                         ┌──────────────────────┐
                         │     PostgreSQL 16    │
                         │ Teams / Games / Stats│
                         └──────────────────────┘

                                    ▲
                                    │
                         ┌──────────────────────┐
                         │ CollegeFootballData  │
                         │       CFBD API       │
                         └──────────────────────┘
```

### Request flow

For a normal frontend request:

```text
Browser → React → Spring Boot → PostgreSQL
```

For a prediction:

```text
Browser → React → Spring Boot
                       │
                       ├── PostgreSQL
                       │   historical game/team data
                       │
                       ▼
                    FastAPI
                       │
                       ▼
              trained ML models
```

The frontend never calls the Python inference service directly. Spring Boot acts as the application's public backend API and communicates with FastAPI internally.

## Technology Stack

### Frontend
- React
- TypeScript
- Vite
- Tailwind CSS
- React Router
- TanStack Query
- Recharts
- Lucide React

### Backend
- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- PostgreSQL JDBC
- Maven

### Machine Learning
- Python
- FastAPI
- Uvicorn
- pandas
- scikit-learn
- joblib

### Database / Infrastructure
- PostgreSQL 16
- Docker
- Docker Compose

### Data
- CollegeFootballData (CFBD)

## Machine-Learning Pipeline

FourthDown AI uses historical game data to construct pregame features for each matchup.

The production feature vector contains nine engineered differences between the home and away teams:

1. Points difference
2. Total-yards difference
3. Turnover difference
4. Scoring-margin difference
5. Third-down conversion difference
6. Red-zone scoring difference
7. Recent-form difference
8. Opponent win-rate difference
9. Opponent scoring-margin difference

### Leakage prevention

Features are generated using data available **before the scheduled kickoff time** of the game being predicted.

This prevents future-game information from leaking into training examples or predictions.

### Early-season fallback

Early-season teams may not yet have enough games to produce stable current-season statistics.

FourthDown AI blends previous-season information with current-season information:

| Current-season games | Current season | Prior season |
|---:|---:|---:|
| 0 | 0% | 100% |
| 1 | 25% | 75% |
| 2 | 50% | 50% |
| 3 | 75% | 25% |
| 4+ | 100% | 0% |

### Winner model

Production algorithm:

```text
Logistic Regression
```

Holdout evaluation:

| Metric | Result |
|---|---:|
| Accuracy | 72.96% |
| ROC-AUC | 0.7968 |
| Log Loss | 0.5291 |

Evaluation split:

```text
Training seasons: 2023 + 2024
Holdout season:   2025
Training games:   2,749
Testing games:    1,631
Total examples:   4,380
```

After model selection, the production model is refit using the full 2023-2025 dataset.

### Score model

Production algorithm:

```text
Ridge Regression
```

The model predicts:

```text
scoring margin
total points
```

Those outputs are transformed into projected home and away scores.

Holdout metrics:

| Metric | Result |
|---|---:|
| Margin MAE | 13.87 |
| Margin RMSE | 17.76 |
| Total Points MAE | 13.21 |
| Home Score MAE | 10.22 |
| Away Score MAE | 9.24 |
| Combined Team Score MAE | 9.73 |

A Random Forest regressor was also evaluated, but Ridge Regression produced the stronger holdout score error.

## Ranking System

FourthDown AI uses weekly CFBD ranking data.

### FBS

The application uses:

```text
AP Top 25 → before CFP rankings become available
College Football Playoff rankings → after CFP rankings begin
```

### FCS

The application uses:

```text
FCS Coaches Poll
```

The rankings UI supports:

- current rank
- previous rank
- weekly movement
- first-place votes
- poll points
- weekly rank history
- unranked weeks
- season and classification selection

## Data Import

The Spring Boot import layer pulls real college football information from CFBD.

Imported data includes:

- FBS teams
- FCS teams
- games
- regular-season games
- postseason games
- bowl names
- playoff rounds
- team game statistics
- team records
- rankings

Postseason statistics are discovered by querying the actual postseason weeks returned by CFBD instead of assuming a fixed postseason week range.

## Known Data Limitation

A small number of completed 2025 games do not have detailed team-game statistics available from the imported CFBD statistics endpoint.

FourthDown AI preserves their valid game results and final scores but does not fabricate missing statistics.

## Repository Structure

```text
fourthdown-ai/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/fourthdown/ai/
│       │   │       ├── client/
│       │   │       ├── config/
│       │   │       ├── controller/
│       │   │       ├── dto/
│       │   │       ├── model/
│       │   │       ├── repository/
│       │   │       └── service/
│       │   └── resources/
│       │       └── application.properties
│       └── test/
├── frontend/
│   ├── src/
│   │   ├── api/
│   │   ├── components/
│   │   └── pages/
│   ├── package.json
│   └── vite.config.ts
├── ml/
│   ├── models/
│   ├── inference_api.py
│   ├── train_model.py
│   └── train_score_model.py
├── docker-compose.yml
├── start-dev.sh
├── .env.example
└── README.md
```

## Local Development

### Prerequisites

Install:

- Docker Desktop
- Java 21+
- Maven
- Node.js / npm
- Python 3
- Git

### 1. Clone the repository

```bash
git clone https://github.com/XavierM20/fourthdown-ai.git
cd fourthdown-ai
```

### 2. Create local environment configuration

Copy the example file:

```bash
cp .env.example .env.local
```

Add your CFBD API key:

```env
CFBD_API_KEY=your_cfbd_api_key
```

`.env.local` is ignored by Git and must never be committed.

Example local configuration:

```env
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
```

### 3. Frontend environment

Create:

```text
frontend/.env.development
```

with:

```env
VITE_API_URL=http://localhost:8080/api/v1
```

### 4. Install frontend dependencies

```bash
cd frontend
npm install
cd ..
```

### 5. Create the Python virtual environment

```bash
cd ml

python3 -m venv .venv

source .venv/bin/activate
```

Install the required Python dependencies for the ML service and training scripts.

Then return to the project root:

```bash
cd ..
```

### 6. Start the application

Make the startup script executable once:

```bash
chmod +x start-dev.sh
```

Start the full stack:

```bash
./start-dev.sh
```

The script starts:

| Service | URL / Port |
|---|---|
| React frontend | http://localhost:5173 |
| Spring Boot API | http://localhost:8080 |
| FastAPI ML service | http://localhost:8000 |
| PostgreSQL | localhost:5432 |

Press `Ctrl+C` to stop Spring Boot, FastAPI, and Vite.

PostgreSQL remains running in Docker.

## Running Services Individually

### PostgreSQL

```bash
docker compose up -d
```

### Spring Boot

```bash
cd backend

export CFBD_API_KEY="your_key"

mvn spring-boot:run
```

### FastAPI

```bash
cd ml

source .venv/bin/activate

python inference_api.py
```

Or:

```bash
uvicorn inference_api:app --reload --port 8000
```

### React

```bash
cd frontend

npm run dev
```

## Health Checks

### Spring Boot

```bash
curl http://localhost:8080/actuator/health
```

Expected:

```json
{
  "status": "UP"
}
```

### FastAPI

```bash
curl http://localhost:8000/health
```

### Model metadata

Winner model:

```bash
curl http://localhost:8080/api/v1/ml/model
```

Score model:

```bash
curl http://localhost:8080/api/v1/ml/score-model
```

## Important API Routes

Examples of available API routes include:

```text
GET  /api/v1/teams
GET  /api/v1/teams/{id}
GET  /api/v1/games
GET  /api/v1/games/{id}

GET  /api/v1/stats/game/{gameId}
GET  /api/v1/stats/team/{teamId}

GET  /api/v1/analytics/teams/{teamId}
GET  /api/v1/analytics/compare

GET  /api/v1/rankings
GET  /api/v1/rankings/history

POST /api/v1/predictions/matchup
GET  /api/v1/predictions/game/{gameId}

GET  /api/v1/ml/model
GET  /api/v1/ml/score-model
```

Data-import and CFBD integration routes are also available through the Spring Boot API.

## Environment Variables

### Spring Boot

| Variable | Purpose | Local default |
|---|---|---|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/fourthdown` |
| `SPRING_DATASOURCE_USERNAME` | PostgreSQL username | `fourthdown` |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL password | `fourthdown_dev` |
| `CFBD_API_KEY` | CollegeFootballData API key | required |
| `CFBD_BASE_URL` | CFBD base URL | CollegeFootballData |
| `ML_BASE_URL` | FastAPI service URL | `http://localhost:8000` |
| `FRONTEND_ORIGIN` | Allowed browser origin | `http://localhost:5173` |
| `PORT` | Spring server port in production | `8080` |

### Frontend

| Variable | Purpose |
|---|---|
| `VITE_API_URL` | Public Spring Boot API base URL |

Do not place secrets in `VITE_*` variables. Vite environment values are included in the browser bundle.

### FastAPI

| Variable | Purpose |
|---|---|
| `HOST` | Bind address |
| `PORT` | Runtime port |
| `RELOAD` | Enable development reload |

## Production Configuration

The application is designed so deployment values can be provided entirely through environment variables.

Planned deployment architecture:

```text
Public React frontend
        │
        ▼
Public Spring Boot API
        │
        ├──────────────► PostgreSQL
        │
        └──────────────► Private FastAPI ML service
```

The current deployment target is Railway.

Production configuration will include:

- managed PostgreSQL
- private communication between Spring Boot and FastAPI
- environment-based CFBD credentials
- environment-based database credentials
- production CORS configuration
- automatic deployment from GitHub

## Security

- CFBD credentials are stored only in backend environment variables.
- `.env.local` is ignored by Git.
- Frontend `VITE_*` variables must never contain private credentials.
- FastAPI is intended to sit behind the Spring Boot API rather than being called directly from the browser.
- Production CORS is controlled through `FRONTEND_ORIGIN`.

## Frontend Production Build

```bash
cd frontend

npm run build
```

The frontend uses route-level lazy loading so major pages are split into separate production chunks.

## Development Notes

FourthDown AI is designed as an end-to-end software engineering and machine-learning project rather than only a prediction notebook.

The project demonstrates:

- full-stack application architecture
- REST API design
- relational data modeling
- third-party API integration
- scheduled/historical sports-data ingestion
- feature engineering
- data leakage prevention
- classification and regression model evaluation
- model inference through a dedicated Python service
- Java/Python service integration
- frontend data visualization
- environment-based production configuration
- Docker-based local infrastructure

## Roadmap

- [x] FBS and FCS team import
- [x] Historical game import
- [x] Detailed game statistics
- [x] Postseason bowl and playoff support
- [x] Team analytics
- [x] Team comparison
- [x] AP / CFP / FCS ranking support
- [x] Weekly ranking history
- [x] Winner prediction model
- [x] Score prediction model
- [x] FastAPI model inference
- [x] Route-level frontend code splitting
- [x] Environment-based frontend/backend configuration
- [x] One-command local startup
- [ ] Railway production deployment
- [ ] Production database initialization
- [ ] Public application URL
- [ ] Application screenshots
- [ ] CI/CD validation

## Author

**Xavier Mathews**

Software Engineer

FourthDown AI was built as a portfolio project demonstrating full-stack engineering, backend systems, data engineering, and applied machine learning.
