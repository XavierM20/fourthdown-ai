import { useEffect, useMemo, useState } from "react";
import {
  Link,
  useSearchParams,
} from "react-router-dom";

import {
  Brain,
  Trophy,
  TrendingUp,
  Zap,
} from "lucide-react";

import {
  getTeams,
} from "../api/teams";

import type {
  Team,
} from "../api/teams";

import {
  predictGame,
  predictMatchup,
} from "../api/predictions";

import type {
  MatchupPrediction,
} from "../api/predictions";

export default function PredictMatchup() {

  // =========================================================
  // URL PARAMETERS
  // =========================================================

  const [searchParams] =
    useSearchParams();

  const gameIdParam =
    searchParams.get("gameId");

  const scheduledGameId =
    gameIdParam
      ? Number(gameIdParam)
      : null;

  const isScheduledGame =
    scheduledGameId != null &&
    !Number.isNaN(scheduledGameId);

  // =========================================================
  // STATE
  // =========================================================

  const [teams, setTeams] =
    useState<Team[]>([]);

  const [homeTeamId, setHomeTeamId] =
    useState<number | null>(null);

  const [awayTeamId, setAwayTeamId] =
    useState<number | null>(null);

  const [prediction, setPrediction] =
    useState<MatchupPrediction | null>(null);

  const [loadingTeams, setLoadingTeams] =
    useState(true);

  const [predicting, setPredicting] =
    useState(false);

  const [error, setError] =
    useState("");

  // =========================================================
  // LOAD TEAMS
  // =========================================================

  useEffect(() => {

    async function loadTeams() {

      try {

        const data =
          await getTeams();

        const sortedTeams =
          [...data].sort(
            (a, b) =>
              a.name.localeCompare(
                b.name
              )
          );

        setTeams(
          sortedTeams
        );

      } catch (err) {

        console.error(err);

        setError(
          "Unable to load teams."
        );

      } finally {

        setLoadingTeams(false);
      }
    }

    loadTeams();

  }, []);

  // =========================================================
  // LOAD SCHEDULED GAME PREDICTION
  // =========================================================

  useEffect(() => {

    async function loadScheduledPrediction() {

      if (!isScheduledGame) {
        return;
      }

      setPredicting(true);
      setError("");
      setPrediction(null);

      try {

        const result =
          await predictGame(
            scheduledGameId
          );

        setPrediction(
          result
        );

        /*
         * Populate the team selectors too,
         * so the UI reflects the real matchup.
         */
        setHomeTeamId(
          result.homeTeamId
        );

        setAwayTeamId(
          result.awayTeamId
        );

      } catch (err) {

        console.error(err);

        setError(
          "Unable to generate a prediction for this scheduled game."
        );

      } finally {

        setPredicting(false);
      }
    }

    loadScheduledPrediction();

  }, [
    scheduledGameId,
    isScheduledGame,
  ]);

  // =========================================================
  // SELECTED TEAMS
  // =========================================================

  const homeTeam = useMemo(
    () =>
      teams.find(
        (team) =>
          team.id === homeTeamId
      ) ?? null,
    [
      teams,
      homeTeamId,
    ]
  );

  const awayTeam = useMemo(
    () =>
      teams.find(
        (team) =>
          team.id === awayTeamId
      ) ?? null,
    [
      teams,
      awayTeamId,
    ]
  );

  // =========================================================
  // MANUAL MATCHUP PREDICTION
  // =========================================================

  async function handlePrediction() {

    if (
      homeTeamId == null ||
      awayTeamId == null
    ) {

      setError(
        "Select both a home team and an away team."
      );

      return;
    }

    if (
      homeTeamId ===
      awayTeamId
    ) {

      setError(
        "Home and away teams must be different."
      );

      return;
    }

    setError("");
    setPrediction(null);
    setPredicting(true);

    try {

      const result =
        await predictMatchup(
          homeTeamId,
          awayTeamId
        );

      setPrediction(
        result
      );

    } catch (err) {

      console.error(err);

      setError(
        "Unable to generate a prediction. Make sure both teams have historical analytics data."
      );

    } finally {

      setPredicting(false);
    }
  }

  // =========================================================
  // TEAM SELECTION
  // =========================================================

  function handleHomeChange(
    value: string
  ) {

    const id =
      value === ""
        ? null
        : Number(value);

    setHomeTeamId(id);

    setPrediction(null);

    setError("");
  }

  function handleAwayChange(
    value: string
  ) {

    const id =
      value === ""
        ? null
        : Number(value);

    setAwayTeamId(id);

    setPrediction(null);

    setError("");
  }

  // =========================================================
  // LOADING
  // =========================================================

  if (loadingTeams) {

    return (
      <div className="min-h-screen bg-slate-950 p-8 text-white">
        Loading teams...
      </div>
    );
  }

  // =========================================================
  // PAGE
  // =========================================================

  return (
    <div className="min-h-screen bg-slate-950 p-8 text-white">

      <div className="mx-auto max-w-7xl">

        {/* NAVIGATION */}

        <div className="mb-6 flex flex-wrap gap-4">

          <Link
            to="/"
            className="text-sm text-blue-400 hover:text-blue-300"
          >
            ← Back to Home
          </Link>

          <Link
            to="/games"
            className="text-sm text-slate-400 hover:text-white"
          >
            Browse Games
          </Link>

        </div>

        {/* HEADER */}

        <div className="mb-8">

          <div className="mb-3 flex items-center gap-3">

            <Brain className="h-8 w-8 text-blue-400" />

            <h1 className="text-3xl font-bold">
              Matchup Prediction
            </h1>

          </div>

          <p className="max-w-3xl text-slate-400">

            FourthDown AI uses historical
            college-football data and a trained
            machine-learning model to estimate
            matchup win probabilities.

          </p>

        </div>

        {/* SCHEDULED GAME MODE */}

        {isScheduledGame && (

          <div className="mb-8 rounded-xl border border-blue-500/20 bg-blue-500/10 p-5">

            <div className="flex items-start gap-3">

              <Brain className="mt-0.5 h-5 w-5 shrink-0 text-blue-400" />

              <div>

                <p className="font-semibold text-blue-300">
                  Scheduled Game Prediction
                </p>

                <p className="mt-1 text-sm leading-6 text-slate-400">

                  This prediction uses the
                  scheduled game's kickoff time
                  as the statistical cutoff.
                  FourthDown AI only considers
                  games played before kickoff.

                </p>

              </div>

            </div>

          </div>
        )}

        {/* TEAM SELECTION */}

        <div className="mb-8 rounded-2xl border border-slate-800 bg-slate-900 p-6">

          <div className="grid gap-6 lg:grid-cols-[1fr_auto_1fr] lg:items-end">

            <TeamSelector
              label="Home Team"
              value={homeTeamId}
              teams={teams}
              selectedTeam={homeTeam}
              onChange={
                handleHomeChange
              }
              excludedTeamId={
                awayTeamId
              }
              disabled={
                isScheduledGame
              }
            />

            <div className="hidden pb-4 text-center text-xl font-bold text-slate-600 lg:block">
              VS
            </div>

            <TeamSelector
              label="Away Team"
              value={awayTeamId}
              teams={teams}
              selectedTeam={awayTeam}
              onChange={
                handleAwayChange
              }
              excludedTeamId={
                homeTeamId
              }
              disabled={
                isScheduledGame
              }
            />

          </div>

          {/* MANUAL PREDICT BUTTON */}

          {!isScheduledGame && (

            <div className="mt-6 flex justify-center">

              <button
                onClick={
                  handlePrediction
                }
                disabled={
                  predicting ||
                  homeTeamId == null ||
                  awayTeamId == null
                }
                className="flex items-center gap-2 rounded-lg bg-blue-600 px-6 py-3 font-semibold text-white transition hover:bg-blue-500 disabled:cursor-not-allowed disabled:opacity-50"
              >

                <Zap className="h-5 w-5" />

                {predicting
                  ? "Generating Prediction..."
                  : "Predict Matchup"}

              </button>

            </div>
          )}

          {/* SCHEDULED LOADING */}

          {isScheduledGame &&
            predicting && (

              <div className="mt-6 text-center text-sm text-slate-400">
                Generating scheduled-game prediction...
              </div>
            )}

          {/* ERROR */}

          {error && (

            <div className="mt-5 rounded-lg border border-red-500/20 bg-red-500/10 p-4 text-center text-sm text-red-400">
              {error}
            </div>
          )}

        </div>

        {/* RESULTS */}

        {prediction &&
          homeTeam &&
          awayTeam && (

            <PredictionResults
              prediction={
                prediction
              }
              homeTeam={
                homeTeam
              }
              awayTeam={
                awayTeam
              }
              scheduledGame={
                isScheduledGame
              }
            />
          )}

      </div>
    </div>
  );
}

// =========================================================
// TEAM SELECTOR
// ========================================================= */

interface TeamSelectorProps {

  label: string;

  value:
    | number
    | null;

  teams: Team[];

  selectedTeam:
    | Team
    | null;

  excludedTeamId:
    | number
    | null;

  disabled: boolean;

  onChange:
    (
      value: string
    ) => void;
}

function TeamSelector({
  label,
  value,
  teams,
  selectedTeam,
  excludedTeamId,
  disabled,
  onChange,
}: TeamSelectorProps) {

  return (
    <div>

      <label className="mb-2 block text-sm font-medium text-slate-400">
        {label}
      </label>

      <select
        value={
          value ?? ""
        }
        disabled={
          disabled
        }
        onChange={
          (event) =>
            onChange(
              event.target.value
            )
        }
        className="w-full rounded-lg border border-slate-700 bg-slate-950 px-4 py-3 text-white outline-none transition focus:border-blue-500 disabled:cursor-not-allowed disabled:opacity-60"
      >

        <option value="">
          Select a team
        </option>

        {teams
          .filter(
            (team) =>
              team.id !==
              excludedTeamId
          )
          .map(
            (team) => (

              <option
                key={
                  team.id
                }
                value={
                  team.id
                }
              >

                {team.name}

                {team.classification
                  ? ` (${team.classification.toUpperCase()})`
                  : ""}

              </option>
            )
          )}

      </select>

      {selectedTeam && (

        <div className="mt-4 flex items-center gap-4 rounded-xl border border-slate-800 bg-slate-950 p-4">

          {selectedTeam.logoUrl ? (

            <img
              src={
                selectedTeam.logoUrl
              }
              alt={`${selectedTeam.name} logo`}
              className="h-14 w-14 object-contain"
            />

          ) : (

            <div className="flex h-14 w-14 items-center justify-center rounded-lg bg-slate-800 text-xl font-bold">

              {selectedTeam.name.charAt(
                0
              )}

            </div>
          )}

          <div>

            <p className="font-semibold">
              {selectedTeam.name}
            </p>

            <p className="text-sm text-slate-400">

              {selectedTeam.conference}

              {selectedTeam.classification &&
                ` • ${selectedTeam.classification.toUpperCase()}`}

            </p>

          </div>

        </div>
      )}

    </div>
  );
}

// =========================================================
// PREDICTION RESULTS
// ========================================================= */

interface PredictionResultsProps {

  prediction:
    MatchupPrediction;

  homeTeam:
    Team;

  awayTeam:
    Team;

  scheduledGame:
    boolean;
}

function PredictionResults({
  prediction,
  homeTeam,
  awayTeam,
  scheduledGame,
}: PredictionResultsProps) {

  const homeWinner =
    prediction.predictedWinnerId ===
    homeTeam.id;

  const winner =
    homeWinner
      ? homeTeam
      : awayTeam;

  return (
    <div className="space-y-8">

      {/* WINNER */}

      <div className="rounded-2xl border border-blue-500/20 bg-gradient-to-br from-blue-500/10 to-slate-900 p-8">

        <div className="flex flex-col items-center text-center">

          <Trophy className="mb-4 h-10 w-10 text-yellow-400" />

          <p className="text-sm font-semibold uppercase tracking-wider text-slate-400">
            FourthDown AI Prediction
          </p>

          {winner.logoUrl && (

            <img
              src={
                winner.logoUrl
              }
              alt={`${winner.name} logo`}
              className="my-5 h-24 w-24 object-contain"
            />
          )}

          <h2 className="text-3xl font-bold">
            {
              prediction.predictedWinnerName
            }
          </h2>

          <p className="mt-2 text-slate-400">
            Predicted Winner
          </p>

          <div className="mt-5 rounded-full bg-blue-500/10 px-5 py-2 text-sm font-semibold text-blue-400">

            {prediction.confidence.toFixed(
              1
            )}
            % confidence

          </div>

          {scheduledGame && (

            <p className="mt-4 max-w-xl text-xs leading-5 text-slate-500">

              This prediction was generated using
              only statistical information available
              before the game's scheduled kickoff.

            </p>
          )}

        </div>

      </div>

      {/* PROJECTED SCORE */}

      <div className="rounded-2xl border border-slate-800 bg-slate-900 p-8">

        <p className="mb-6 text-center text-sm font-medium uppercase tracking-wider text-slate-500">
          Projected Score
        </p>

        <div className="grid grid-cols-[1fr_auto_1fr] items-center gap-5">

          <ScoreTeam
            team={
              homeTeam
            }
            score={
              prediction.projectedHomeScore
            }
            label="HOME"
          />

          <div className="text-xl font-bold text-slate-600">
            -
          </div>

          <ScoreTeam
            team={
              awayTeam
            }
            score={
              prediction.projectedAwayScore
            }
            label="AWAY"
          />

        </div>

      </div>

      {/* WIN PROBABILITY */}

      <div className="rounded-2xl border border-slate-800 bg-slate-900 p-6">

        <div className="mb-6 flex items-center gap-2">

          <TrendingUp className="h-5 w-5 text-blue-400" />

          <h2 className="text-xl font-semibold">
            Win Probability
          </h2>

        </div>

        <ProbabilityRow
          team={
            homeTeam
          }
          probability={
            prediction.homeWinProbability
          }
        />

        <div className="my-6 border-t border-slate-800" />

        <ProbabilityRow
          team={
            awayTeam
          }
          probability={
            prediction.awayWinProbability
          }
        />

      </div>

      {/* MODEL EXPLANATION */}

      <div className="rounded-2xl border border-slate-800 bg-slate-900 p-6">

        <h2 className="mb-2 text-xl font-semibold">

          Why FourthDown AI chose{" "}
          {
            prediction.predictedWinnerName
          }

        </h2>

        <p className="mb-6 text-sm text-slate-400">
          Key factors influencing the matchup model
        </p>

        {prediction.explanation.length >
        0 ? (

          <div className="space-y-3">

            {prediction.explanation.map(
              (
                reason,
                index
              ) => (

                <div
                  key={`${reason}-${index}`}
                  className="flex gap-3 rounded-lg bg-slate-950 p-4"
                >

                  <div className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-blue-500/10 text-xs font-bold text-blue-400">

                    {index + 1}

                  </div>

                  <p className="text-sm text-slate-300">
                    {reason}
                  </p>

                </div>
              )
            )}

          </div>

        ) : (

          <p className="text-sm text-slate-500">
            No major statistical advantages were identified.
          </p>
        )}

      </div>

      {/* DISCLAIMER */}

      <div className="rounded-lg border border-slate-800 bg-slate-900/50 p-4 text-center text-xs leading-5 text-slate-500">

        FourthDown AI uses a trained
        machine-learning model based on
        historical football data. Predictions
        represent estimated probabilities and
        are not guaranteed game outcomes.

      </div>

    </div>
  );
}

// =========================================================
// SCORE TEAM
// ========================================================= */

interface ScoreTeamProps {

  team:
    Team;

  score:
    number;

  label:
    string;
}

function ScoreTeam({
  team,
  score,
  label,
}: ScoreTeamProps) {

  return (
    <div className="text-center">

      {team.logoUrl ? (

        <img
          src={
            team.logoUrl
          }
          alt={`${team.name} logo`}
          className="mx-auto mb-3 h-16 w-16 object-contain"
        />

      ) : (

        <div className="mx-auto mb-3 flex h-16 w-16 items-center justify-center rounded-xl bg-slate-800 text-xl font-bold">

          {team.name.charAt(
            0
          )}

        </div>
      )}

      <p className="text-xs font-semibold text-slate-500">
        {label}
      </p>

      <p className="mt-1 font-semibold">
        {team.name}
      </p>

      <p className="mt-3 text-5xl font-bold">
        {score}
      </p>

    </div>
  );
}

// =========================================================
// PROBABILITY ROW
// ========================================================= */

interface ProbabilityRowProps {

  team:
    Team;

  probability:
    number;
}

function ProbabilityRow({
  team,
  probability,
}: ProbabilityRowProps) {

  const safeProbability =
    Math.max(
      0,
      Math.min(
        100,
        probability
      )
    );

  return (
    <div>

      <div className="mb-3 flex items-center justify-between gap-4">

        <div className="flex items-center gap-3">

          {team.logoUrl && (

            <img
              src={
                team.logoUrl
              }
              alt={`${team.name} logo`}
              className="h-8 w-8 object-contain"
            />
          )}

          <span className="font-medium">
            {team.name}
          </span>

        </div>

        <span className="text-xl font-bold">

          {safeProbability.toFixed(
            1
          )}
          %

        </span>

      </div>

      <div className="h-3 overflow-hidden rounded-full bg-slate-800">

        <div
          className="h-full rounded-full bg-blue-500 transition-all duration-700"
          style={{
            width: `${safeProbability}%`,
          }}
        />

      </div>

    </div>
  );
}