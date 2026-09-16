import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

import { getGames } from "../api/games";
import { getStatsByGame } from "../api/stats";

import type { Game } from "../api/games";
import type { TeamGameStats } from "../api/stats";

export default function Analytics() {
  const [games, setGames] = useState<Game[]>([]);
  const [selectedGameId, setSelectedGameId] =
    useState<number | null>(null);

  const [stats, setStats] = useState<TeamGameStats[]>([]);

  const [loadingGames, setLoadingGames] = useState(true);
  const [loadingStats, setLoadingStats] = useState(false);

  const [error, setError] = useState("");

  useEffect(() => {
    async function loadGames() {
      try {
        const data = await getGames();

        const sortedGames = [...data].sort(
          (a, b) =>
            new Date(b.gameDate).getTime() -
            new Date(a.gameDate).getTime()
        );

        setGames(sortedGames);

        if (sortedGames.length > 0) {
          setSelectedGameId(sortedGames[0].id);
        }
      } catch (err) {
        setError("Unable to load games.");
      } finally {
        setLoadingGames(false);
      }
    }

    loadGames();
  }, []);

  useEffect(() => {
    async function loadStats() {
      if (selectedGameId === null) {
        return;
      }

      try {
        setLoadingStats(true);
        setError("");

        const data = await getStatsByGame(selectedGameId);

        setStats(data);
      } catch (err) {
        setError("Unable to load game analytics.");
      } finally {
        setLoadingStats(false);
      }
    }

    loadStats();
  }, [selectedGameId]);

  if (loadingGames) {
    return (
      <div className="min-h-screen bg-slate-950 p-8 text-white">
        Loading games...
      </div>
    );
  }

  const selectedGame = games.find(
    (game) => game.id === selectedGameId
  );

  const teamOne = stats[0];
  const teamTwo = stats[1];

  const thirdDownPercentage = (
    team: TeamGameStats
  ) => {
    if (!team.thirdDownAttempts) {
      return "0%";
    }

    return `${Math.round(
      (team.thirdDownConversions /
        team.thirdDownAttempts) *
        100
    )}%`;
  };

  const redZonePercentage = (
    team: TeamGameStats
  ) => {
    if (!team.redZoneAttempts) {
      return "0%";
    }

    return `${Math.round(
      (team.redZoneScores /
        team.redZoneAttempts) *
        100
    )}%`;
  };

  const rows =
    teamOne && teamTwo
      ? [
          {
            label: "Points",
            teamOne: teamOne.points,
            teamTwo: teamTwo.points,
          },
          {
            label: "Total Yards",
            teamOne: teamOne.totalYards,
            teamTwo: teamTwo.totalYards,
          },
          {
            label: "Passing Yards",
            teamOne: teamOne.passingYards,
            teamTwo: teamTwo.passingYards,
          },
          {
            label: "Rushing Yards",
            teamOne: teamOne.rushingYards,
            teamTwo: teamTwo.rushingYards,
          },
          {
            label: "First Downs",
            teamOne: teamOne.firstDowns,
            teamTwo: teamTwo.firstDowns,
          },
          {
            label: "Turnovers",
            teamOne: teamOne.turnovers,
            teamTwo: teamTwo.turnovers,
          },
          {
            label: "Third Down",
            teamOne:
              thirdDownPercentage(teamOne),
            teamTwo:
              thirdDownPercentage(teamTwo),
          },
          {
            label: "Red Zone",
            teamOne:
              redZonePercentage(teamOne),
            teamTwo:
              redZonePercentage(teamTwo),
          },
          {
            label: "Penalties",
            teamOne: `${teamOne.penalties ?? 0} / ${
              teamOne.penaltyYards ?? 0
            } yds`,
            teamTwo: `${teamTwo.penalties ?? 0} / ${
              teamTwo.penaltyYards ?? 0
            } yds`,
          },
        ]
      : [];

  return (
    <div className="min-h-screen bg-slate-950 p-8 text-white">
      <div className="mx-auto max-w-5xl">
        <Link
          to="/"
          className="mb-6 inline-block text-sm text-blue-400 hover:text-blue-300"
        >
          ← Back to Home
        </Link>

        <div className="mb-8">
          <h1 className="text-3xl font-bold">
            Game Analytics
          </h1>

          <p className="mt-2 text-slate-400">
            Select a real game and compare its recorded
            team statistics.
          </p>
        </div>

        <div className="mb-8 rounded-xl border border-slate-800 bg-slate-900 p-6">
          <label className="mb-2 block text-sm font-medium text-slate-300">
            Select Game
          </label>

          <select
            value={selectedGameId ?? ""}
            onChange={(event) =>
              setSelectedGameId(
                Number(event.target.value)
              )
            }
            className="w-full rounded-lg border border-slate-700 bg-slate-950 px-4 py-3 text-white outline-none transition focus:border-blue-500"
          >
            {games.map((game) => (
              <option
                key={game.id}
                value={game.id}
              >
                {game.awayTeam.name} at{" "}
                {game.homeTeam.name} -{" "}
                {new Date(
                  game.gameDate
                ).toLocaleDateString()}
              </option>
            ))}
          </select>
        </div>

        {loadingStats && (
          <div className="rounded-xl border border-slate-800 bg-slate-900 p-8 text-center text-slate-400">
            Loading analytics...
          </div>
        )}

        {error && (
          <div className="mb-6 rounded-xl border border-red-500/20 bg-red-500/10 p-4 text-red-400">
            {error}
          </div>
        )}

        {!loadingStats &&
          selectedGame &&
          stats.length < 2 && (
            <div className="rounded-xl border border-slate-800 bg-slate-900 p-8 text-center">
              <h2 className="text-lg font-semibold">
                No analytics available yet
              </h2>

              <p className="mt-2 text-sm text-slate-500">
                FourthDown AI does not yet have team
                statistics stored for this game.
              </p>
            </div>
          )}

        {!loadingStats &&
          teamOne &&
          teamTwo && (
            <>
              <div className="mb-6 grid grid-cols-3 items-center rounded-xl border border-slate-800 bg-slate-900 p-6">
                <div className="text-center">
                  {teamOne.team.logoUrl && (
                    <img
                      src={teamOne.team.logoUrl}
                      alt={`${teamOne.team.name} logo`}
                      className="mx-auto mb-3 h-20 w-20 object-contain"
                    />
                  )}

                  <h2 className="text-xl font-bold">
                    {teamOne.team.name}
                  </h2>

                  <p className="text-sm text-slate-400">
                    {teamOne.team.abbreviation ??
                      ""}
                  </p>
                </div>

                <div className="text-center">
                  <p className="text-sm font-semibold text-slate-500">
                    VS
                  </p>
                </div>

                <div className="text-center">
                  {teamTwo.team.logoUrl && (
                    <img
                      src={teamTwo.team.logoUrl}
                      alt={`${teamTwo.team.name} logo`}
                      className="mx-auto mb-3 h-20 w-20 object-contain"
                    />
                  )}

                  <h2 className="text-xl font-bold">
                    {teamTwo.team.name}
                  </h2>

                  <p className="text-sm text-slate-400">
                    {teamTwo.team.abbreviation ??
                      ""}
                  </p>
                </div>
              </div>

              <div className="overflow-hidden rounded-xl border border-slate-800 bg-slate-900">
                {rows.map((row) => (
                  <div
                    key={row.label}
                    className="grid grid-cols-3 border-b border-slate-800 p-4 last:border-b-0"
                  >
                    <div className="text-center text-lg font-semibold">
                      {row.teamOne}
                    </div>

                    <div className="text-center text-sm font-medium text-slate-400">
                      {row.label}
                    </div>

                    <div className="text-center text-lg font-semibold">
                      {row.teamTwo}
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}
      </div>
    </div>
  );
}