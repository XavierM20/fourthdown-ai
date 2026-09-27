import {
  useEffect,
  useMemo,
  useState,
} from "react";

import {
  Search,
  Trophy,
} from "lucide-react";

import {
  Link,
  useSearchParams,
} from "react-router-dom";

import {
  getRankingWeeks,
  getRankings,
} from "../api/rankings";

import type {
  PollRankingsResponse,
  PollTeamRanking,
} from "../api/rankings";

type Classification =
  | "fbs"
  | "fcs";

// =========================================================
// RANKINGS PAGE
// =========================================================

export default function Rankings() {

  const currentYear =
    new Date()
      .getFullYear();

  const [
    searchParams,
    setSearchParams,
  ] = useSearchParams();

  // =======================================================
  // INITIAL URL STATE
  // =======================================================

  const urlSeason =
    Number(
      searchParams.get(
        "season"
      )
    );

  const urlClassification =
    searchParams.get(
      "classification"
    );

  const urlWeek =
    Number(
      searchParams.get(
        "week"
      )
    );

  const initialSeason =
    Number.isFinite(
      urlSeason
    ) &&
    urlSeason > 2000
      ? urlSeason
      : currentYear;

  const initialClassification:
    Classification =
      urlClassification ===
      "fcs"
        ? "fcs"
        : "fbs";

  // =======================================================
  // STATE
  // =======================================================

  const [
    selectedSeason,
    setSelectedSeason,
  ] =
    useState(
      initialSeason
    );

  const [
    classification,
    setClassification,
  ] =
    useState<Classification>(
      initialClassification
    );

  const [
    selectedWeek,
    setSelectedWeek,
  ] =
    useState<number | null>(
      Number.isFinite(urlWeek) &&
      urlWeek > 0
        ? urlWeek
        : null
    );

  const [
    availableWeeks,
    setAvailableWeeks,
  ] =
    useState<number[]>([]);

  const [
    rankingData,
    setRankingData,
  ] =
    useState<PollRankingsResponse | null>(
      null
    );

  const [
    loading,
    setLoading,
  ] =
    useState(true);

  const [
    error,
    setError,
  ] =
    useState("");

  const [
    search,
    setSearch,
  ] =
    useState("");

  const [
    selectedConference,
    setSelectedConference,
  ] =
    useState("ALL");

  // =======================================================
  // SEASONS
  // =======================================================

  const seasonOptions =
    useMemo(
      () => [
        currentYear,
        currentYear - 1,
        currentYear - 2,
        currentYear - 3,
      ],
      [
        currentYear,
      ]
    );

  // =======================================================
  // KEEP URL UPDATED
  // =======================================================

  useEffect(() => {

    const params: Record<string, string> = {
      season:
        selectedSeason.toString(),

      classification,
    };

    if (selectedWeek !== null) {
      params.week =
        selectedWeek.toString();
    }

    setSearchParams(
      params,
      {
        replace: true,
      }
    );

  }, [
    selectedSeason,
    classification,
    selectedWeek,
    setSearchParams,
  ]);

  // =======================================================
  // LOAD AVAILABLE WEEKS
  // =======================================================

  useEffect(() => {

    async function loadWeeks() {

      try {

        setLoading(
          true
        );

        setError("");

        const weeks =
          await getRankingWeeks(
            selectedSeason,
            classification
          );

        const sortedWeeks =
          [...weeks].sort(
            (a, b) => a - b
          );

        setAvailableWeeks(
          sortedWeeks
        );

        setSelectedWeek(
          (currentWeek) => {

            if (
              currentWeek !== null &&
              sortedWeeks.includes(
                currentWeek
              )
            ) {
              return currentWeek;
            }

            return (
              sortedWeeks[
                sortedWeeks.length - 1
              ] ?? null
            );
          }
        );

      } catch (err) {

        console.error(
          "Unable to load ranking weeks:",
          err
        );

        setAvailableWeeks(
          []
        );

        setSelectedWeek(
          null
        );

        setError(
          "Unable to load ranking weeks."
        );

      } finally {

        setLoading(
          false
        );
      }
    }

    loadWeeks();

  }, [
    selectedSeason,
    classification,
  ]);

  // =======================================================
  // LOAD RANKINGS
  // =======================================================

  useEffect(() => {

    async function loadRankings() {

      if (selectedWeek === null) {
        setRankingData(
          null
        );
        return;
      }

      try {

        setLoading(
          true
        );

        setError("");

        const data =
          await getRankings(
            selectedSeason,
            classification,
            selectedWeek
          );

        setRankingData(
          data
        );

      } catch (err) {

        console.error(
          "Unable to load rankings:",
          err
        );

        setError(
          "Unable to load rankings."
        );

      } finally {

        setLoading(
          false
        );
      }
    }

    loadRankings();

  }, [
    selectedSeason,
    classification,
    selectedWeek,
  ]);

  // =======================================================
  // RANKINGS
  // =======================================================

  const rankings =
    rankingData
      ?.rankings
      ?.slice(0, 25) ??
    [];

  // =======================================================
  // CONFERENCES
  // =======================================================

  const conferences =
    useMemo(() => {

      const values =
        new Set<string>();

      rankings.forEach(
        (ranking) => {

          if (
            ranking.conference
              ?.trim()
          ) {

            values.add(
              ranking.conference
            );
          }
        }
      );

      return Array
        .from(values)
        .sort(
          (a, b) =>
            a.localeCompare(
              b
            )
        );

    }, [
      rankings,
    ]);

  // =======================================================
  // FILTER
  // =======================================================

  const displayedRankings =
    useMemo(() => {

      const normalizedSearch =
        search
          .trim()
          .toLowerCase();

      return rankings.filter(
        (ranking) => {

          const matchesSearch =
            !normalizedSearch ||
            [
              ranking.teamName,
              ranking.abbreviation,
              ranking.conference,
            ]
              .filter(Boolean)
              .join(" ")
              .toLowerCase()
              .includes(
                normalizedSearch
              );

          const matchesConference =
            selectedConference ===
              "ALL" ||
            ranking.conference ===
              selectedConference;

          return (
            matchesSearch &&
            matchesConference
          );
        }
      );

    }, [
      rankings,
      search,
      selectedConference,
    ]);

  // =======================================================
  // RETURN PATH FOR TEAM DETAILS
  // =======================================================

  const rankingReturnPath =
    `/rankings?season=${selectedSeason}&classification=${classification}${
      selectedWeek !== null
        ? `&week=${selectedWeek}`
        : ""
    }`;

  // =======================================================
  // CHANGE CLASSIFICATION
  // =======================================================

  function changeClassification(
    value: Classification
  ) {

    setClassification(
      value
    );

    setSelectedWeek(
      null
    );

    setSearch("");

    setSelectedConference(
      "ALL"
    );
  }

  // =======================================================
  // LOADING
  // =======================================================

  if (loading) {

    return (
      <div className="min-h-screen bg-slate-950 p-8 text-white">
        Loading rankings...
      </div>
    );
  }

  // =======================================================
  // ERROR
  // =======================================================

  if (error) {

    return (
      <div className="min-h-screen bg-slate-950 p-8 text-red-400">
        {error}
      </div>
    );
  }

  // =======================================================
  // PAGE
  // =======================================================

  return (

    <div className="min-h-screen bg-slate-950 p-8 text-white">

      <div className="mx-auto max-w-7xl">

        {/* HEADER */}

        <div className="mb-8">

          <Link
            to="/"
            className="mb-5 inline-block text-sm text-blue-400 transition hover:text-blue-300"
          >
            ← Back to Home
          </Link>

          <div className="flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">

            <div>

              <div className="flex items-center gap-3">

                <Trophy className="h-8 w-8 text-amber-400" />

                <h1 className="text-3xl font-bold">
                  College Football Top 25
                </h1>

              </div>

              <p className="mt-2 text-slate-400">

                {classification ===
                "fbs"
                  ? "AP Top 25 during the regular season, automatically switching to CFP rankings when available."
                  : "FCS Coaches Poll Top 25."}

              </p>

            </div>

            {/* SEASON / WEEK */}

            <div className="flex flex-wrap gap-3">

              <div>

                <label
                  htmlFor="ranking-season"
                  className="mb-2 block text-xs font-semibold uppercase tracking-wider text-slate-500"
                >
                  Season
                </label>

                <select
                  id="ranking-season"
                  value={
                    selectedSeason
                  }
                  onChange={
                    (event) => {

                      setSelectedSeason(
                        Number(
                          event.target
                            .value
                        )
                      );

                      setSelectedWeek(
                        null
                      );

                      setSearch("");

                      setSelectedConference(
                        "ALL"
                      );
                    }
                  }
                  className="rounded-lg border border-slate-700 bg-slate-900 px-5 py-2.5 font-semibold text-white outline-none transition focus:border-blue-500"
                >

                  {seasonOptions.map(
                    (season) => (

                      <option
                        key={
                          season
                        }
                        value={
                          season
                        }
                      >
                        {season} Season
                      </option>

                    )
                  )}

                </select>

              </div>

              <div>

                <label
                  htmlFor="ranking-week"
                  className="mb-2 block text-xs font-semibold uppercase tracking-wider text-slate-500"
                >
                  Week
                </label>

                <select
                  id="ranking-week"
                  value={
                    selectedWeek ?? ""
                  }
                  onChange={
                    (event) => {

                      setSelectedWeek(
                        Number(
                          event.target
                            .value
                        )
                      );

                      setSearch("");

                      setSelectedConference(
                        "ALL"
                      );
                    }
                  }
                  disabled={
                    availableWeeks.length ===
                    0
                  }
                  className="rounded-lg border border-slate-700 bg-slate-900 px-5 py-2.5 font-semibold text-white outline-none transition focus:border-blue-500 disabled:cursor-not-allowed disabled:opacity-50"
                >

                  {availableWeeks.length ===
                  0 ? (

                    <option value="">
                      No weeks available
                    </option>

                  ) : (

                    availableWeeks.map(
                      (week) => (

                        <option
                          key={
                            week
                          }
                          value={
                            week
                          }
                        >
                          Week {week}
                        </option>

                      )
                    )

                  )}

                </select>

              </div>

            </div>

          </div>

        </div>

        {/* FBS / FCS */}

        <div className="mb-6 grid grid-cols-2 gap-3 rounded-2xl border border-slate-800 bg-slate-900 p-2">

          <button
            type="button"
            onClick={
              () =>
                changeClassification(
                  "fbs"
                )
            }
            className={[
              "rounded-xl px-5 py-4 text-left transition",

              classification ===
              "fbs"
                ? "bg-blue-600 text-white shadow-lg"
                : "text-slate-400 hover:bg-slate-800 hover:text-white",
            ].join(" ")}
          >

            <p className="font-semibold">
              FBS Top 25
            </p>

            <p
              className={[
                "mt-1 text-xs",

                classification ===
                "fbs"
                  ? "text-blue-100"
                  : "text-slate-500",
              ].join(" ")}
            >
              AP / CFP Rankings
            </p>

          </button>

          <button
            type="button"
            onClick={
              () =>
                changeClassification(
                  "fcs"
                )
            }
            className={[
              "rounded-xl px-5 py-4 text-left transition",

              classification ===
              "fcs"
                ? "bg-amber-500 text-slate-950 shadow-lg"
                : "text-slate-400 hover:bg-slate-800 hover:text-white",
            ].join(" ")}
          >

            <p className="font-semibold">
              FCS Top 25
            </p>

            <p
              className={[
                "mt-1 text-xs",

                classification ===
                "fcs"
                  ? "text-amber-950/70"
                  : "text-slate-500",
              ].join(" ")}
            >
              FCS Coaches Poll
            </p>

          </button>

        </div>

        {/* POLL INFORMATION */}

        {rankingData && (

          <div className="mb-6 rounded-2xl border border-slate-800 bg-slate-900 p-5">

            <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">
              Selected Ranking
            </p>

            <div className="mt-2 flex flex-wrap items-center gap-3">

              <h2 className="text-xl font-bold">
                {rankingData.poll}
              </h2>

              {rankingData.week >
                0 && (

                <span className="rounded-full bg-slate-950 px-3 py-1 text-sm text-slate-400">
                  Week {rankingData.week}
                </span>

              )}

              <span className="rounded-full bg-slate-950 px-3 py-1 text-sm uppercase text-slate-400">
                {classification}
              </span>

            </div>

          </div>

        )}

        {/* FILTERS */}

        <div className="mb-6 rounded-2xl border border-slate-800 bg-slate-900 p-5">

          <div className="grid gap-4 md:grid-cols-2">

            <div>

              <label
                htmlFor="ranking-search"
                className="mb-2 block text-sm font-medium text-slate-400"
              >
                Search Top 25
              </label>

              <div className="relative">

                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" />

                <input
                  id="ranking-search"
                  value={
                    search
                  }
                  onChange={
                    (event) =>
                      setSearch(
                        event.target
                          .value
                      )
                  }
                  placeholder="Team or conference..."
                  className="w-full rounded-lg border border-slate-700 bg-slate-950 py-2.5 pl-10 pr-4 text-sm text-white outline-none transition placeholder:text-slate-600 focus:border-blue-500"
                />

              </div>

            </div>

            <div>

              <label
                htmlFor="ranking-conference"
                className="mb-2 block text-sm font-medium text-slate-400"
              >
                Conference
              </label>

              <select
                id="ranking-conference"
                value={
                  selectedConference
                }
                onChange={
                  (event) =>
                    setSelectedConference(
                      event.target
                        .value
                    )
                }
                className="w-full rounded-lg border border-slate-700 bg-slate-950 px-4 py-2.5 text-sm text-white outline-none transition focus:border-blue-500"
              >

                <option value="ALL">
                  All Conferences
                </option>

                {conferences.map(
                  (conference) => (

                    <option
                      key={
                        conference
                      }
                      value={
                        conference
                      }
                    >
                      {conference}
                    </option>

                  )
                )}

              </select>

            </div>

          </div>

        </div>

        {/* TABLE */}

        <div className="overflow-hidden rounded-2xl border border-slate-800 bg-slate-900">

          <div className="overflow-x-auto">

            <table className="w-full min-w-[850px]">

              <thead className="border-b border-slate-800 bg-slate-950/70">

                <tr>

                  <th className="px-5 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">
                    Rank
                  </th>

                  <th className="px-5 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
                    Team
                  </th>

                  <th className="px-5 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
                    Conference
                  </th>

                  <th className="px-5 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">
                    Record
                  </th>

                  <th className="px-5 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">
                    Poll Points
                  </th>

                  <th className="px-5 py-4 text-center text-xs font-semibold uppercase tracking-wider text-slate-500">
                    1st Place
                  </th>

                </tr>

              </thead>

              <tbody>

                {displayedRankings.map(
                  (ranking) => (

                    <RankingRow
                      key={
                        ranking.teamId
                      }
                      ranking={
                        ranking
                      }
                      returnPath={
                        rankingReturnPath
                      }
                    />

                  )
                )}

              </tbody>

            </table>

          </div>

          {displayedRankings.length ===
            0 && (

            <div className="p-10 text-center">

              <Trophy className="mx-auto mb-4 h-10 w-10 text-slate-600" />

              <h2 className="font-semibold">
                No rankings available
              </h2>

              <p className="mt-2 text-sm text-slate-500">
                Rankings have not been published for this selection yet.
              </p>

            </div>

          )}

        </div>

      </div>

    </div>
  );
}

// =========================================================
// RANKING ROW
// =========================================================

function RankingRow({
  ranking,
  returnPath,
}: {
  ranking: PollTeamRanking;
  returnPath: string;
}) {

  return (

    <tr className="border-b border-slate-800/80 transition last:border-b-0 hover:bg-slate-800/60">

      {/* RANK */}

      <td className="px-5 py-4 text-center">

        <RankBadge
          rank={
            ranking.rank
          }
        />

      </td>

      {/* TEAM */}

      <td className="px-5 py-4">

        <Link
          to={`/teams/${ranking.teamId}`}
          state={{
            from:
              returnPath,

            backLabel:
              "Back to Rankings",
          }}
          className="group flex items-center gap-3"
        >

          {ranking.logoUrl ? (

            <img
              src={
                ranking.logoUrl
              }
              alt={`${ranking.teamName} logo`}
              className="h-10 w-10 shrink-0 object-contain"
            />

          ) : (

            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-slate-800 font-bold text-slate-400">

              {ranking.teamName
                .charAt(0)
                .toUpperCase()}

            </div>

          )}

          <div className="min-w-0">

            <p className="truncate font-semibold text-white transition group-hover:text-blue-400">
              {ranking.teamName}
            </p>

            {ranking.abbreviation && (

              <p className="mt-0.5 text-xs text-slate-500">
                {ranking.abbreviation}
              </p>

            )}

          </div>

        </Link>

      </td>

      {/* CONFERENCE */}

      <td className="px-5 py-4 text-sm text-slate-400">
        {ranking.conference}
      </td>

      {/* RECORD */}

      <td className="px-5 py-4 text-center font-semibold">

        {ranking.wins}
        -
        {ranking.losses}

        {ranking.ties >
          0 &&
          `-${ranking.ties}`}

      </td>

      {/* POLL POINTS */}

      <td className="px-5 py-4 text-center text-slate-300">

        {ranking.pollPoints ??
          "—"}

      </td>

      {/* FIRST PLACE */}

      <td className="px-5 py-4 text-center text-slate-300">

        {ranking.firstPlaceVotes ??
          "—"}

      </td>

    </tr>
  );
}

// =========================================================
// RANK BADGE
// =========================================================

function RankBadge({
  rank,
}: {
  rank: number;
}) {

  if (
    rank === 1
  ) {

    return (
      <div className="mx-auto flex h-9 w-9 items-center justify-center rounded-full bg-amber-500 font-bold text-slate-950">
        1
      </div>
    );
  }

  if (
    rank === 2
  ) {

    return (
      <div className="mx-auto flex h-9 w-9 items-center justify-center rounded-full bg-slate-300 font-bold text-slate-950">
        2
      </div>
    );
  }

  if (
    rank === 3
  ) {

    return (
      <div className="mx-auto flex h-9 w-9 items-center justify-center rounded-full bg-orange-700 font-bold text-white">
        3
      </div>
    );
  }

  return (
    <span className="font-semibold text-slate-400">
      {rank}
    </span>
  );
}