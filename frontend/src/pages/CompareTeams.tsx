import { useEffect, useMemo, useState } from "react";
import { BarChart3, Search } from "lucide-react";

import { getTeams } from "../api/teams";
import { compareTeams } from "../api/stats";

import type { Team } from "../api/teams";
import type { TeamComparison } from "../api/stats";

export default function CompareTeams() {
  const [teams, setTeams] = useState<Team[]>([]);

  const [teamOneId, setTeamOneId] = useState<number | null>(null);
  const [teamTwoId, setTeamTwoId] = useState<number | null>(null);

  const [teamOneSearch, setTeamOneSearch] = useState("");
  const [teamTwoSearch, setTeamTwoSearch] = useState("");

  const [comparison, setComparison] =
    useState<TeamComparison | null>(null);

  const [loadingTeams, setLoadingTeams] = useState(true);
  const [comparing, setComparing] = useState(false);

  const [error, setError] = useState("");

  useEffect(() => {
    async function loadTeams() {
      try {
        const data = await getTeams();

        const sortedTeams = [...data].sort((a, b) =>
          a.name.localeCompare(b.name, undefined, {
            sensitivity: "base",
          })
        );

        setTeams(sortedTeams);

        if (sortedTeams.length >= 2) {
          setTeamOneId(sortedTeams[0].id);
          setTeamTwoId(sortedTeams[1].id);
        }
      } catch {
        setError("Unable to load teams.");
      } finally {
        setLoadingTeams(false);
      }
    }

    loadTeams();
  }, []);

  const filteredTeamOne = useMemo(() => {
    const query = teamOneSearch.trim().toLowerCase();

    if (!query) {
      return teams;
    }

    return teams.filter((team) =>
      [
        team.name,
        team.abbreviation,
        team.conference,
        team.city,
        team.state,
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase()
        .includes(query)
    );
  }, [teams, teamOneSearch]);

  const filteredTeamTwo = useMemo(() => {
    const query = teamTwoSearch.trim().toLowerCase();

    if (!query) {
      return teams;
    }

    return teams.filter((team) =>
      [
        team.name,
        team.abbreviation,
        team.conference,
        team.city,
        team.state,
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase()
        .includes(query)
    );
  }, [teams, teamTwoSearch]);

  async function handleCompare() {
    if (!teamOneId || !teamTwoId) {
      setError("Select two teams to compare.");
      return;
    }

    if (teamOneId === teamTwoId) {
      setError("Teams must be different.");
      return;
    }

    try {
      setError("");
      setComparing(true);

      const data = await compareTeams(
        teamOneId,
        teamTwoId
      );

      setComparison(data);
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError("Unable to compare teams.");
      }
    } finally {
      setComparing(false);
    }
  }

  if (loadingTeams) {
    return (
      <div className="min-h-screen bg-slate-950 p-8 text-white">
        Loading teams...
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-950 p-8 text-white">
      <div className="mx-auto max-w-6xl">
        <div className="mb-8">
          <div className="mb-3 flex items-center gap-3">
            <div className="rounded-lg bg-blue-500/10 p-2">
              <BarChart3 className="h-5 w-5 text-blue-400" />
            </div>

            <p className="text-sm font-medium uppercase tracking-wider text-blue-400">
              FourthDown AI Analytics
            </p>
          </div>

          <h1 className="text-3xl font-bold">
            Compare Teams
          </h1>

          <p className="mt-2 text-slate-400">
            Search for two teams and compare their recorded performance.
          </p>
        </div>

        <div className="mb-8 rounded-xl border border-slate-800 bg-slate-900 p-6">
          <div className="grid gap-6 md:grid-cols-3 md:items-end">
            <div>
              <label className="mb-2 block text-sm font-medium text-slate-300">
                Team One
              </label>

              <div className="relative mb-3">
                <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" />

                <input
                  type="text"
                  value={teamOneSearch}
                  onChange={(event) =>
                    setTeamOneSearch(event.target.value)
                  }
                  placeholder="Search team..."
                  className="w-full rounded-lg border border-slate-700 bg-slate-950 py-3 pl-10 pr-4 text-white outline-none transition placeholder:text-slate-600 focus:border-blue-500"
                />
              </div>

              <select
                value={teamOneId ?? ""}
                onChange={(event) =>
                  setTeamOneId(Number(event.target.value))
                }
                className="w-full rounded-lg border border-slate-700 bg-slate-950 px-4 py-3 text-white outline-none transition focus:border-blue-500"
              >
                {filteredTeamOne.map((team) => (
                  <option
                    key={team.id}
                    value={team.id}
                  >
                    {team.name}
                  </option>
                ))}
              </select>

              {filteredTeamOne.length === 0 && (
                <p className="mt-2 text-sm text-amber-400">
                  No teams match that search.
                </p>
              )}
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-slate-300">
                Team Two
              </label>

              <div className="relative mb-3">
                <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" />

                <input
                  type="text"
                  value={teamTwoSearch}
                  onChange={(event) =>
                    setTeamTwoSearch(event.target.value)
                  }
                  placeholder="Search team..."
                  className="w-full rounded-lg border border-slate-700 bg-slate-950 py-3 pl-10 pr-4 text-white outline-none transition placeholder:text-slate-600 focus:border-blue-500"
                />
              </div>

              <select
                value={teamTwoId ?? ""}
                onChange={(event) =>
                  setTeamTwoId(Number(event.target.value))
                }
                className="w-full rounded-lg border border-slate-700 bg-slate-950 px-4 py-3 text-white outline-none transition focus:border-blue-500"
              >
                {filteredTeamTwo.map((team) => (
                  <option
                    key={team.id}
                    value={team.id}
                  >
                    {team.name}
                  </option>
                ))}
              </select>

              {filteredTeamTwo.length === 0 && (
                <p className="mt-2 text-sm text-amber-400">
                  No teams match that search.
                </p>
              )}
            </div>

            <button
              onClick={handleCompare}
              disabled={comparing}
              className="rounded-lg bg-blue-600 px-5 py-3 font-semibold text-white transition hover:bg-blue-500 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {comparing
                ? "Comparing..."
                : "Compare Teams"}
            </button>
          </div>

          {error && (
            <div className="mt-4 rounded-lg border border-red-500/20 bg-red-500/10 p-4 text-sm text-red-400">
              {error}
            </div>
          )}
        </div>

        {!comparison ? (
          <div className="rounded-xl border border-slate-800 bg-slate-900 p-12 text-center">
            <BarChart3 className="mx-auto mb-4 h-10 w-10 text-slate-600" />

            <h2 className="text-lg font-semibold">
              Select two teams
            </h2>

            <p className="mt-2 text-sm text-slate-500">
              Search for the teams above and click Compare Teams to view
              their analytics.
            </p>
          </div>
        ) : (
          <ComparisonResults comparison={comparison} />
        )}
      </div>
    </div>
  );
}

function ComparisonResults({
  comparison,
}: {
  comparison: TeamComparison;
}) {
  const { teamOne, teamTwo } = comparison;

  const rows = [
    {
      label: "Games Played",
      teamOne: teamOne.gamesPlayed,
      teamTwo: teamTwo.gamesPlayed,
      lowerIsBetter: false,
    },
    {
      label: "Avg Points",
      teamOne: teamOne.averagePoints,
      teamTwo: teamTwo.averagePoints,
      lowerIsBetter: false,
    },
    {
      label: "Avg Total Yards",
      teamOne: teamOne.averageTotalYards,
      teamTwo: teamTwo.averageTotalYards,
      lowerIsBetter: false,
    },
    {
      label: "Avg Passing Yards",
      teamOne: teamOne.averagePassingYards,
      teamTwo: teamTwo.averagePassingYards,
      lowerIsBetter: false,
    },
    {
      label: "Avg Rushing Yards",
      teamOne: teamOne.averageRushingYards,
      teamTwo: teamTwo.averageRushingYards,
      lowerIsBetter: false,
    },
    {
      label: "Avg Turnovers",
      teamOne: teamOne.averageTurnovers,
      teamTwo: teamTwo.averageTurnovers,
      lowerIsBetter: true,
    },
  ];

  return (
    <div className="overflow-hidden rounded-xl border border-slate-800 bg-slate-900">
      <div className="grid grid-cols-3 border-b border-slate-800 p-6">
        <div className="text-center">
          <p className="text-xl font-bold">
            {teamOne.teamName}
          </p>
        </div>

        <div className="text-center">
          <p className="text-sm font-semibold uppercase tracking-wider text-slate-500">
            Comparison
          </p>
        </div>

        <div className="text-center">
          <p className="text-xl font-bold">
            {teamTwo.teamName}
          </p>
        </div>
      </div>

      {rows.map((row) => {
        const firstWins = row.lowerIsBetter
          ? row.teamOne < row.teamTwo
          : row.teamOne > row.teamTwo;

        const secondWins = row.lowerIsBetter
          ? row.teamTwo < row.teamOne
          : row.teamTwo > row.teamOne;

        return (
          <div
            key={row.label}
            className="grid grid-cols-3 items-center border-b border-slate-800 p-5 last:border-b-0"
          >
            <div className="text-center">
              <p
                className={`text-lg font-semibold ${
                  firstWins
                    ? "text-emerald-400"
                    : "text-white"
                }`}
              >
                {formatValue(row.label, row.teamOne)}
              </p>
            </div>

            <div className="text-center">
              <p className="text-sm text-slate-400">
                {row.label}
              </p>
            </div>

            <div className="text-center">
              <p
                className={`text-lg font-semibold ${
                  secondWins
                    ? "text-emerald-400"
                    : "text-white"
                }`}
              >
                {formatValue(row.label, row.teamTwo)}
              </p>
            </div>
          </div>
        );
      })}
    </div>
  );
}

function formatValue(
  label: string,
  value: number
) {
  if (label === "Games Played") {
    return value;
  }

  return value.toFixed(1);
}
