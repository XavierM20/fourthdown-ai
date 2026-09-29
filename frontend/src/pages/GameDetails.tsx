import { useEffect, useState } from "react";
import {
  Link,
  useParams,
} from "react-router-dom";

import {
  Brain,
  CalendarDays,
  MapPin,
} from "lucide-react";

import {
  getGameById,
} from "../api/games";

import type {
  Game,
} from "../api/games";

import {
  getStatsByGame,
} from "../api/stats";

import type {
  TeamGameStats,
} from "../api/stats";

export default function GameDetails() {
  const { id } = useParams();

  const gameId = Number(id);

  const [game, setGame] =
    useState<Game | null>(null);

  const [stats, setStats] =
    useState<TeamGameStats[]>([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState("");

  useEffect(() => {
    async function loadGame() {
      if (!gameId) {
        setError("Invalid game.");
        setLoading(false);
        return;
      }

      try {
        const [
          gameData,
          statsData,
        ] = await Promise.all([
          getGameById(gameId),
          getStatsByGame(gameId),
        ]);

        setGame(gameData);
        setStats(statsData);
      } catch (err) {
        console.error(err);

        setError(
          "Unable to load game details."
        );
      } finally {
        setLoading(false);
      }
    }

    loadGame();
  }, [gameId]);

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 p-8 text-white">
        Loading game...
      </div>
    );
  }

  if (error || !game) {
    return (
      <div className="min-h-screen bg-slate-950 p-8 text-white">
        <Link
          to="/games"
          className="mb-6 inline-block text-sm text-blue-400 hover:text-blue-300"
        >
          ← Back to Games
        </Link>

        <p className="text-red-400">
          {error || "Game not found."}
        </p>
      </div>
    );
  }

  const date =
    new Date(game.gameDate);

  const isCompleted =
    game.status?.toUpperCase() ===
    "COMPLETED";

  const isScheduled =
    !isCompleted;

  const homeStats =
    stats.find(
      (stat) =>
        stat.team.id ===
        game.homeTeam.id
    ) ?? null;

  const awayStats =
    stats.find(
      (stat) =>
        stat.team.id ===
        game.awayTeam.id
    ) ?? null;

  return (
    <div className="min-h-screen bg-slate-950 p-8 text-white">
      <div className="mx-auto max-w-7xl">
        {/* NAVIGATION */}

        <div className="mb-6 flex flex-wrap items-center justify-between gap-4">
          <div className="flex flex-wrap gap-4">
            <Link
              to="/games"
              className="text-sm text-blue-400 hover:text-blue-300"
            >
              ← Back to Games
            </Link>

            <Link
              to="/"
              className="text-sm text-slate-400 hover:text-white"
            >
              Back to Home
            </Link>
          </div>

          {isScheduled && (
            <Link
              to={`/predict?gameId=${game.id}`}
              className="inline-flex items-center gap-2 rounded-lg bg-blue-600 px-5 py-3 font-semibold text-white transition hover:bg-blue-500"
            >
              <Brain className="h-5 w-5" />
              Predict This Game
            </Link>
          )}
        </div>

        {/* GAME HEADER */}

        <div className="mb-8 rounded-2xl border border-slate-800 bg-slate-900 p-8">
          <div className="mb-6 text-center">
            <div className="mb-3 flex flex-wrap items-center justify-center gap-3 text-sm text-slate-400">
              <div className="flex items-center gap-2">
                <CalendarDays className="h-4 w-4" />

                <span>
                  {date.toLocaleDateString(
                    undefined,
                    {
                      weekday: "long",
                      month: "long",
                      day: "numeric",
                      year: "numeric",
                    }
                  )}
                </span>
              </div>

              <span className="text-slate-700">
                •
              </span>

              <span>
                {date.toLocaleTimeString(
                  undefined,
                  {
                    hour: "numeric",
                    minute: "2-digit",
                  }
                )}
              </span>
            </div>

            {game.venue && (
              <div className="flex items-center justify-center gap-2 text-sm text-slate-500">
                <MapPin className="h-4 w-4" />
                {game.venue}
              </div>
            )}
          </div>

          {/* MATCHUP */}

          <div className="grid grid-cols-[1fr_auto_1fr] items-center gap-6">
            <TeamDisplay
              team={game.awayTeam}
              score={game.awayScore}
              label="AWAY"
              completed={isCompleted}
            />

            <div className="text-center">
              <p className="text-sm font-semibold text-slate-600">
                {isCompleted
                  ? "FINAL"
                  : "VS"}
              </p>
            </div>

            <TeamDisplay
              team={game.homeTeam}
              score={game.homeScore}
              label="HOME"
              completed={isCompleted}
            />
          </div>

          {/* STATUS */}

          <div className="mt-8 flex justify-center">
            <span
              className={`rounded-full px-4 py-1.5 text-xs font-semibold ${
                isCompleted
                  ? "bg-emerald-500/10 text-emerald-400"
                  : "bg-blue-500/10 text-blue-400"
              }`}
            >
              {game.status}
            </span>
          </div>
        </div>

        {/* PREDICTION CTA */}

        {isScheduled && (
          <div className="mb-8 rounded-2xl border border-blue-500/20 bg-gradient-to-br from-blue-500/10 to-slate-900 p-6">
            <div className="flex flex-col gap-5 md:flex-row md:items-center md:justify-between">
              <div>
                <div className="mb-2 flex items-center gap-2">
                  <Brain className="h-5 w-5 text-blue-400" />

                  <p className="text-sm font-semibold uppercase tracking-wider text-blue-400">
                    FourthDown AI
                  </p>
                </div>

                <h2 className="text-2xl font-bold">
                  Predict This Matchup
                </h2>

                <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-400">
                  Generate an ML prediction using
                  only statistics available before
                  this game's scheduled kickoff.
                </p>
              </div>

              <Link
                to={`/predict?gameId=${game.id}`}
                className="inline-flex shrink-0 items-center justify-center gap-2 rounded-lg bg-blue-600 px-6 py-3 font-semibold text-white transition hover:bg-blue-500"
              >
                <Brain className="h-5 w-5" />
                Predict This Game
              </Link>
            </div>
          </div>
        )}

        {/* GAME ANALYTICS */}

        <div className="mb-8">
          <div className="mb-5">
            <h2 className="text-2xl font-bold">
              Game Analytics
            </h2>

            <p className="mt-1 text-sm text-slate-400">
              Team statistics imported for
              this matchup
            </p>
          </div>

          {homeStats && awayStats ? (
            <div className="rounded-2xl border border-slate-800 bg-slate-900 p-6">
              <div className="mb-6 grid grid-cols-[1fr_auto_1fr] items-center gap-4">
                <div className="text-center">
                  <p className="text-sm text-slate-500">
                    Away
                  </p>

                  <p className="mt-1 text-lg font-semibold">
                    {game.awayTeam.name}
                  </p>
                </div>

                <div className="font-bold text-slate-700">
                  VS
                </div>

                <div className="text-center">
                  <p className="text-sm text-slate-500">
                    Home
                  </p>

                  <p className="mt-1 text-lg font-semibold">
                    {game.homeTeam.name}
                  </p>
                </div>
              </div>

              <div className="space-y-1">
                <StatComparison
                  label="Points"
                  awayValue={awayStats.points}
                  homeValue={homeStats.points}
                />

                <StatComparison
                  label="Total Yards"
                  awayValue={awayStats.totalYards}
                  homeValue={homeStats.totalYards}
                />

                <StatComparison
                  label="Passing Yards"
                  awayValue={awayStats.passingYards}
                  homeValue={homeStats.passingYards}
                />

                <StatComparison
                  label="Rushing Yards"
                  awayValue={awayStats.rushingYards}
                  homeValue={homeStats.rushingYards}
                />

                <StatComparison
                  label="Turnovers"
                  awayValue={awayStats.turnovers}
                  homeValue={homeStats.turnovers}
                />

                <StatComparison
                  label="First Downs"
                  awayValue={awayStats.firstDowns}
                  homeValue={homeStats.firstDowns}
                />

                <StatComparison
                  label="3rd Down"
                  awayValue={formatEfficiency(
                    awayStats.thirdDownConversions,
                    awayStats.thirdDownAttempts
                  )}
                  homeValue={formatEfficiency(
                    homeStats.thirdDownConversions,
                    homeStats.thirdDownAttempts
                  )}
                />

                <StatComparison
                  label="Red Zone"
                  awayValue={formatEfficiency(
                    awayStats.redZoneScores,
                    awayStats.redZoneAttempts
                  )}
                  homeValue={formatEfficiency(
                    homeStats.redZoneScores,
                    homeStats.redZoneAttempts
                  )}
                />

                <StatComparison
                  label="Penalties"
                  awayValue={formatPenalties(
                    awayStats.penalties,
                    awayStats.penaltyYards
                  )}
                  homeValue={formatPenalties(
                    homeStats.penalties,
                    homeStats.penaltyYards
                  )}
                />
              </div>
            </div>
          ) : (
            <div className="rounded-xl border border-slate-800 bg-slate-900 p-8 text-center">
              <p className="text-slate-400">
                No game statistics are available
                for this matchup yet.
              </p>
            </div>
          )}
        </div>

        {/* GAME INFORMATION */}

        <div>
          <h2 className="mb-5 text-2xl font-bold">
            Game Information
          </h2>

          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
            <InfoCard
              label="Status"
              value={game.status}
            />

            <InfoCard
              label="Date"
              value={date.toLocaleDateString()}
            />

            <InfoCard
              label="Kickoff"
              value={date.toLocaleTimeString(
                undefined,
                {
                  hour: "numeric",
                  minute: "2-digit",
                }
              )}
            />

            <InfoCard
              label="Venue"
              value={game.venue || "—"}
            />
          </div>
        </div>
      </div>
    </div>
  );
}

/* =========================================================
   TEAM DISPLAY
   ========================================================= */

interface TeamDisplayProps {
  team: Game["homeTeam"];
  score: number | null;
  label: string;
  completed: boolean;
}

function TeamDisplay({
  team,
  score,
  label,
  completed,
}: TeamDisplayProps) {
  return (
    <Link
      to={`/teams/${team.id}`}
      className="group text-center"
    >
      {team.logoUrl ? (
        <img
          src={team.logoUrl}
          alt={`${team.name} logo`}
          className="mx-auto mb-4 h-24 w-24 object-contain transition group-hover:scale-105"
        />
      ) : (
        <div className="mx-auto mb-4 flex h-24 w-24 items-center justify-center rounded-2xl bg-slate-800 text-3xl font-bold">
          {team.name.charAt(0)}
        </div>
      )}

      <p className="text-xs font-semibold text-slate-500">
        {label}
      </p>

      <h2 className="mt-1 text-xl font-bold transition group-hover:text-blue-400 md:text-2xl">
        {team.name}
      </h2>

      {team.abbreviation && (
        <p className="mt-1 text-sm text-slate-500">
          {team.abbreviation}
        </p>
      )}

      {completed && (
        <p className="mt-4 text-5xl font-bold">
          {score ?? "—"}
        </p>
      )}
    </Link>
  );
}

/* =========================================================
   STAT COMPARISON
   ========================================================= */

interface StatComparisonProps {
  label: string;
  awayValue:
    | string
    | number
    | null
    | undefined;

  homeValue:
    | string
    | number
    | null
    | undefined;
}

function StatComparison({
  label,
  awayValue,
  homeValue,
}: StatComparisonProps) {
  return (
    <div className="grid grid-cols-[1fr_1.2fr_1fr] items-center border-b border-slate-800 py-4 last:border-b-0">
      <div className="text-center text-lg font-semibold">
        {awayValue ?? "—"}
      </div>

      <div className="text-center text-sm text-slate-500">
        {label}
      </div>

      <div className="text-center text-lg font-semibold">
        {homeValue ?? "—"}
      </div>
    </div>
  );
}

// =========================================================
// INFO CARD
// ========================================================= */

interface InfoCardProps {
  label: string;
  value: string;
}

function InfoCard({
  label,
  value,
}: InfoCardProps) {
  return (
    <div className="rounded-xl border border-slate-800 bg-slate-900 p-5">
      <p className="text-sm text-slate-400">
        {label}
      </p>

      <p className="mt-2 font-semibold">
        {value}
      </p>
    </div>
  );
}

// =========================================================
// FORMATTERS
// ========================================================= */

function formatEfficiency(
  conversions:
    | number
    | null
    | undefined,
  attempts:
    | number
    | null
    | undefined
) {
  if (
    conversions == null ||
    attempts == null
  ) {
    return "—";
  }

  return `${conversions}-${attempts}`;
}

function formatPenalties(
  penalties:
    | number
    | null
    | undefined,
  yards:
    | number
    | null
    | undefined
) {
  if (
    penalties == null ||
    yards == null
  ) {
    return "—";
  }

  return `${penalties}-${yards}`;
}