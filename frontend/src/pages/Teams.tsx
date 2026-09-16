import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";

import { getTeams } from "../api/teams";
import type { Team } from "../api/teams";

export default function Teams() {
  const [teams, setTeams] = useState<Team[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [search, setSearch] = useState("");
  const [conference, setConference] = useState("ALL");
  const [classification, setClassification] = useState("ALL");

  useEffect(() => {
    async function loadTeams() {
      try {
        const data = await getTeams();
        setTeams(data);
      } catch (err) {
        setError("Unable to load teams.");
      } finally {
        setLoading(false);
      }
    }

    loadTeams();
  }, []);

  const conferences = useMemo(() => {
    return Array.from(
      new Set(
        teams
          .filter((team) => {
            if (classification === "ALL") {
              return true;
            }

            return (
              team.classification?.toUpperCase() ===
              classification
            );
          })
          .map((team) => team.conference)
          .filter(Boolean)
      )
    ).sort((a, b) => a.localeCompare(b));
  }, [teams, classification]);

  const filteredTeams = useMemo(() => {
    return [...teams]
      .filter((team) => {
        const searchValue = search.toLowerCase();

        const matchesSearch =
          team.name
            .toLowerCase()
            .includes(searchValue) ||
          (team.abbreviation ?? "")
            .toLowerCase()
            .includes(searchValue) ||
          (team.mascot ?? "")
            .toLowerCase()
            .includes(searchValue);

        const matchesConference =
          conference === "ALL" ||
          team.conference === conference;

        const matchesClassification =
          classification === "ALL" ||
          team.classification?.toUpperCase() ===
            classification;

        return (
          matchesSearch &&
          matchesConference &&
          matchesClassification
        );
      })
      .sort((a, b) =>
        a.name.localeCompare(b.name)
      );
  }, [
    teams,
    search,
    conference,
    classification,
  ]);

  function handleClassificationChange(
    value: string
  ) {
    setClassification(value);

    // Reset conference when switching between
    // FBS and FCS so an incompatible conference
    // does not remain selected.
    setConference("ALL");
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 p-8 text-white">
        Loading teams...
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-slate-950 p-8 text-red-400">
        {error}
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-950 p-8 text-white">
      <div className="mx-auto max-w-7xl">
        <Link
          to="/"
          className="mb-6 inline-block text-sm text-blue-400 hover:text-blue-300"
        >
          ← Back to Home
        </Link>

        <div className="mb-8">
          <h1 className="text-3xl font-bold">
            College Football Teams
          </h1>

          <p className="mt-2 text-slate-400">
            Browse FBS and FCS teams tracked by
            FourthDown AI
          </p>
        </div>

        <div className="mb-8 grid gap-4 lg:grid-cols-3">
          <div>
            <label className="mb-2 block text-sm text-slate-400">
              Search teams
            </label>

            <input
              type="text"
              value={search}
              onChange={(event) =>
                setSearch(event.target.value)
              }
              placeholder="Search Alabama, Texas, Tigers..."
              className="w-full rounded-lg border border-slate-700 bg-slate-900 px-4 py-3 text-white outline-none transition focus:border-blue-500"
            />
          </div>

          <div>
            <label className="mb-2 block text-sm text-slate-400">
              Classification
            </label>

            <select
              value={classification}
              onChange={(event) =>
                handleClassificationChange(
                  event.target.value
                )
              }
              className="w-full rounded-lg border border-slate-700 bg-slate-900 px-4 py-3 text-white outline-none transition focus:border-blue-500"
            >
              <option value="ALL">
                All Classifications
              </option>

              <option value="FBS">
                FBS
              </option>

              <option value="FCS">
                FCS
              </option>
            </select>
          </div>

          <div>
            <label className="mb-2 block text-sm text-slate-400">
              Conference
            </label>

            <select
              value={conference}
              onChange={(event) =>
                setConference(event.target.value)
              }
              className="w-full rounded-lg border border-slate-700 bg-slate-900 px-4 py-3 text-white outline-none transition focus:border-blue-500"
            >
              <option value="ALL">
                All Conferences
              </option>

              {conferences.map((item) => (
                <option
                  key={item}
                  value={item}
                >
                  {item}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="mb-5 flex flex-wrap items-center justify-between gap-3">
          <p className="text-sm text-slate-500">
            Showing {filteredTeams.length} of{" "}
            {teams.length} teams
          </p>

          {classification !== "ALL" && (
            <span className="rounded-full bg-blue-500/10 px-3 py-1 text-xs font-medium text-blue-400">
              {classification}
            </span>
          )}
        </div>

        {filteredTeams.length === 0 ? (
          <div className="rounded-xl border border-slate-800 bg-slate-900 p-8 text-center">
            <p className="text-slate-400">
              No teams match your filters.
            </p>
          </div>
        ) : (
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {filteredTeams.map((team) => (
              <Link
                key={team.id}
                to={`/teams/${team.id}`}
                className="block rounded-xl border border-slate-800 bg-slate-900 p-6 transition hover:border-slate-700 hover:bg-slate-800/80"
              >
                <div className="mb-4 flex items-center gap-4">
                  {team.logoUrl ? (
                    <img
                      src={team.logoUrl}
                      alt={`${team.name} logo`}
                      className="h-14 w-14 object-contain"
                    />
                  ) : (
                    <div className="flex h-14 w-14 items-center justify-center rounded-lg bg-slate-800 text-lg font-bold text-slate-500">
                      {team.name.charAt(0)}
                    </div>
                  )}

                  <div className="min-w-0">
                    <h2 className="truncate text-xl font-semibold">
                      {team.name}
                    </h2>

                    <div className="mt-1 flex flex-wrap gap-2">
                      {team.abbreviation && (
                        <span className="text-sm text-slate-400">
                          {team.abbreviation}
                        </span>
                      )}

                      {team.classification && (
                        <span className="rounded bg-slate-800 px-2 py-0.5 text-xs font-medium text-slate-400">
                          {team.classification.toUpperCase()}
                        </span>
                      )}
                    </div>
                  </div>
                </div>

                <div className="space-y-2 text-sm">
                  {team.mascot && (
                    <p>
                      <span className="text-slate-400">
                        Mascot:
                      </span>{" "}
                      {team.mascot}
                    </p>
                  )}

                  <p>
                    <span className="text-slate-400">
                      Conference:
                    </span>{" "}
                    {team.conference}
                  </p>

                  {(team.city || team.state) && (
                    <p>
                      <span className="text-slate-400">
                        Location:
                      </span>{" "}
                      {[team.city, team.state]
                        .filter(Boolean)
                        .join(", ")}
                    </p>
                  )}
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}