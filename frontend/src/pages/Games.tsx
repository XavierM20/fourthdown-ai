import {
  useEffect,
  useMemo,
  useState,
} from "react";

import {
  CalendarDays,
  ChevronLeft,
  ChevronRight,
  Filter,
  MapPin,
  Search,
  Trophy,
  X,
} from "lucide-react";

import {
  Link,
} from "react-router-dom";

import {
  getGames,
} from "../api/games";

import type {
  Game,
} from "../api/games";

type SeasonType =
  | "regular"
  | "postseason";



// =========================================================
// RELEVANT WEEK
// =========================================================

function getRelevantWeek(
  games: Game[],
  season: number | null
): number | null {

  if (season === null) {
    return null;
  }

  const regularGames =
    games.filter(
      (game) =>
        game.season === season &&
        normalizeSeasonType(
          game.seasonType
        ) === "regular"
    );

  const availableWeeks =
    Array.from(
      new Set(
        regularGames
          .map(
            (game) =>
              game.week
          )
          .filter(
            (
              week
            ): week is number =>
              week !== null &&
              week !== undefined
          )
      )
    ).sort(
      (a, b) =>
        a - b
    );

  if (
    availableWeeks.length === 0
  ) {
    return null;
  }

  const now =
    new Date();

  const futureGames =
    regularGames
      .filter(
        (game) => {

          const date =
            new Date(
              game.gameDate
            );

          return (
            !Number.isNaN(
              date.getTime()
            ) &&
            date >= now
          );
        }
      )
      .sort(
        (a, b) =>
          new Date(
            a.gameDate
          ).getTime() -
          new Date(
            b.gameDate
          ).getTime()
      );

  if (
    futureGames.length > 0
  ) {
    return (
      futureGames[0].week ??
      availableWeeks[
        availableWeeks.length - 1
      ]
    );
  }

  return availableWeeks[
    availableWeeks.length - 1
  ];
}

// =========================================================
// GAMES PAGE
// =========================================================

export default function Games() {

  // =======================================================
  // DATA
  // =======================================================

  const [
    games,
    setGames,
  ] = useState<Game[]>([]);

  const [
    loading,
    setLoading,
  ] = useState(true);

  const [
    error,
    setError,
  ] = useState("");

  // =======================================================
  // FILTER STATE
  // =======================================================

  const [
    search,
    setSearch,
  ] = useState("");

  const [
    selectedSeason,
    setSelectedSeason,
  ] = useState<number | null>(
    null
  );

  const [
    selectedSeasonType,
    setSelectedSeasonType,
  ] = useState<SeasonType>(
    "regular"
  );

  const [
    selectedWeek,
    setSelectedWeek,
  ] = useState<number | null>(
    null
  );

  const [
    selectedConference,
    setSelectedConference,
  ] = useState("ALL");

  const [
    selectedStatus,
    setSelectedStatus,
  ] = useState("ALL");

  // =======================================================
  // LOAD GAMES
  // =======================================================

  useEffect(() => {

    let cancelled =
      false;

    async function loadInitialGames() {

      try {

        const data =
          await getGames();

        if (cancelled) {
          return;
        }

        setGames(
          data
        );

        const availableSeasons =
          Array.from(
            new Set(
              data
                .map(
                  (game) =>
                    game.season
                )
                .filter(
                  (
                    season
                  ): season is number =>
                    season !== null &&
                    season !== undefined
                )
            )
          ).sort(
            (a, b) =>
              b - a
          );

        const currentYear =
          new Date()
            .getFullYear();

        const defaultSeason =
          availableSeasons.includes(
            currentYear
          )
            ? currentYear
            : availableSeasons[0] ??
              null;

        setSelectedSeason(
          defaultSeason
        );

        setSelectedWeek(
          getRelevantWeek(
            data,
            defaultSeason
          )
        );

        setError("");

      } catch (err) {

        if (cancelled) {
          return;
        }

        console.error(
          "Unable to load games:",
          err
        );

        setError(
          "Unable to load games."
        );

      } finally {

        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    async function refreshGames() {

      try {

        const data =
          await getGames();

        if (cancelled) {
          return;
        }

        
        setGames(
          data
        );

        setError("");

      } catch (err) {

        console.error(
          "Unable to refresh games:",
          err
        );
      }
    }

    loadInitialGames();

    const refreshInterval =
      window.setInterval(
        refreshGames,
        120000
      );

    function handleWindowFocus() {
      refreshGames();
    }

    window.addEventListener(
      "focus",
      handleWindowFocus
    );

    return () => {

      cancelled =
        true;

      window.clearInterval(
        refreshInterval
      );

      window.removeEventListener(
        "focus",
        handleWindowFocus
      );
    };

  }, []);

  // =======================================================
  // AVAILABLE SEASONS
  // =======================================================

  const seasons =
    useMemo(() => {

      const values =
        new Set<number>();

      games.forEach(
        (game) => {

          if (
            game.season !== null &&
            game.season !== undefined
          ) {

            values.add(
              game.season
            );
          }
        }
      );

      return Array
        .from(values)
        .sort(
          (a, b) =>
            b - a
        );

    }, [games]);

  // =======================================================
  // GAMES FOR SELECTED SEASON
  // =======================================================

  const seasonGames =
    useMemo(() => {

      if (
        selectedSeason === null
      ) {
        return [];
      }

      return games.filter(
        (game) =>
          game.season ===
          selectedSeason
      );

    }, [
      games,
      selectedSeason,
    ]);

  // =======================================================
  // REGULAR-SEASON GAMES
  // =======================================================

  const regularSeasonGames =
    useMemo(() => {

      return seasonGames.filter(
        (game) =>
          normalizeSeasonType(
            game.seasonType
          ) ===
          "regular"
      );

    }, [
      seasonGames,
    ]);

  // =======================================================
  // POSTSEASON GAMES
  // =======================================================

  const postseasonGames =
    useMemo(() => {

      return seasonGames.filter(
        (game) =>
          normalizeSeasonType(
            game.seasonType
          ) ===
          "postseason"
      );

    }, [
      seasonGames,
    ]);

  // =======================================================
  // AVAILABLE WEEKS
  // =======================================================

  const weeks =
    useMemo(() => {

      const values =
        new Set<number>();

      regularSeasonGames.forEach(
        (game) => {

          if (
            game.week !== null &&
            game.week !== undefined
          ) {

            values.add(
              game.week
            );
          }
        }
      );

      return Array
        .from(values)
        .sort(
          (a, b) =>
            a - b
        );

    }, [
      regularSeasonGames,
    ]);

  // =======================================================
  // ACTIVE SEASON TYPE GAMES
  // =======================================================

  const typeGames =
    selectedSeasonType ===
    "postseason"
      ? postseasonGames
      : regularSeasonGames;

  // =======================================================
  // CONFERENCES
  // =======================================================

  const conferences =
    useMemo(() => {

      const values =
        new Set<string>();

      typeGames.forEach(
        (game) => {

          const homeConference =
            game.homeTeam
              ?.conference
              ?.trim();

          const awayConference =
            game.awayTeam
              ?.conference
              ?.trim();

          if (
            homeConference
          ) {

            values.add(
              homeConference
            );
          }

          if (
            awayConference
          ) {

            values.add(
              awayConference
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
      typeGames,
    ]);

  // =======================================================
  // STATUSES
  // =======================================================

  const statuses =
    useMemo(() => {

      const values =
        new Set<string>();

      typeGames.forEach(
        (game) => {

          if (
            game.status
              ?.trim()
          ) {

            values.add(
              game.status
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
      typeGames,
    ]);

  // =======================================================
  // FILTERED GAMES
  // =======================================================

  const filteredGames =
    useMemo(() => {

      const normalizedSearch =
        search
          .trim()
          .toLowerCase();

      return typeGames
        .filter(
          (game) => {

            // -----------------------------------------------
            // WEEK
            // -----------------------------------------------

            const matchesWeek =
              selectedSeasonType ===
                "postseason" ||
              selectedWeek ===
                null ||
              game.week ===
                selectedWeek;

            // -----------------------------------------------
            // SEARCH
            // -----------------------------------------------

            const searchableText = [
              game.homeTeam
                ?.name,

              game.homeTeam
                ?.abbreviation,

              game.homeTeam
                ?.conference,

              game.awayTeam
                ?.name,

              game.awayTeam
                ?.abbreviation,

              game.awayTeam
                ?.conference,

              game.venue,

              game.status,

              game.gameName,

              game.playoffRound,
            ]
              .filter(Boolean)
              .join(" ")
              .toLowerCase();

            const matchesSearch =
              !normalizedSearch ||
              searchableText.includes(
                normalizedSearch
              );

            // -----------------------------------------------
            // CONFERENCE
            // -----------------------------------------------

            const matchesConference =
              selectedConference ===
                "ALL" ||
              game.homeTeam
                ?.conference ===
                selectedConference ||
              game.awayTeam
                ?.conference ===
                selectedConference;

            // -----------------------------------------------
            // STATUS
            // -----------------------------------------------

            const matchesStatus =
              selectedStatus ===
                "ALL" ||
              game.status ===
                selectedStatus;

            return (
              matchesWeek &&
              matchesSearch &&
              matchesConference &&
              matchesStatus
            );
          }
        )
        .sort(
          (a, b) =>
            new Date(
              a.gameDate
            ).getTime() -
            new Date(
              b.gameDate
            ).getTime()
        );

    }, [
      typeGames,
      selectedSeasonType,
      selectedWeek,
      search,
      selectedConference,
      selectedStatus,
    ]);

  // =======================================================
  // ACTIVE FILTER CHECK
  // =======================================================

  const hasExtraFilters =
    search.trim() !== "" ||
    selectedConference !==
      "ALL" ||
    selectedStatus !==
      "ALL";

  // =======================================================
  // CLEAR FILTERS
  // =======================================================

  function clearFilters() {

    setSearch("");

    setSelectedConference(
      "ALL"
    );

    setSelectedStatus(
      "ALL"
    );
  }

  // =======================================================
  // CHANGE SEASON
  // =======================================================

  function changeSeason(
    season: number
  ) {

    setSelectedSeason(
      season
    );

    setSelectedSeasonType(
      "regular"
    );

    setSelectedWeek(
      getRelevantWeek(
        games,
        season
      )
    );

    clearFilters();
  }

  // =======================================================
  // CHANGE SEASON TYPE
  // =======================================================

  function changeSeasonType(
    type: SeasonType
  ) {

    setSelectedSeasonType(
      type
    );

    clearFilters();

    if (
      type ===
      "regular"
    ) {

      setSelectedWeek(
        getRelevantWeek(
          games,
          selectedSeason
        )
      );
    }
  }

  // =======================================================
  // WEEK NAVIGATION
  // =======================================================

  const selectedWeekIndex =
    selectedWeek === null
      ? -1
      : weeks.indexOf(
          selectedWeek
        );

  function previousWeek() {

    if (
      selectedWeekIndex <=
      0
    ) {
      return;
    }

    setSelectedWeek(
      weeks[
        selectedWeekIndex - 1
      ]
    );
  }

  function nextWeek() {

    if (
      selectedWeekIndex < 0 ||
      selectedWeekIndex >=
        weeks.length - 1
    ) {
      return;
    }

    setSelectedWeek(
      weeks[
        selectedWeekIndex + 1
      ]
    );
  }

  // =======================================================
  // LOADING
  // =======================================================

  if (loading) {

    return (
      <div className="min-h-screen bg-slate-950 p-8 text-white">
        Loading games...
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

        {// =================================================
         // HEADER
         // ================================================= */}

        <div className="mb-8">

          <Link
            to="/"
            className="mb-5 inline-block text-sm text-blue-400 transition hover:text-blue-300"
          >
            ← Back to Home
          </Link>

          <div className="flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">

            <div>

              <div className="flex items-center gap-3">

                <CalendarDays className="h-7 w-7 text-blue-400" />

                <h1 className="text-3xl font-bold">
                  College Football Games
                </h1>

              </div>

              <p className="mt-2 text-slate-400">
                Browse regular-season,
                bowl, and College Football
                Playoff games.
              </p>

            </div>

            {/* SEASON */}

            <div>

              <label
                htmlFor="season"
                className="mb-2 block text-xs font-semibold uppercase tracking-wider text-slate-500"
              >
                Season
              </label>

              <select
                id="season"
                value={
                  selectedSeason ??
                  ""
                }
                onChange={
                  (event) =>
                    changeSeason(
                      Number(
                        event.target
                          .value
                      )
                    )
                }
                className="rounded-lg border border-slate-700 bg-slate-900 px-5 py-2.5 font-semibold text-white outline-none transition focus:border-blue-500"
              >

                {seasons.map(
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

          </div>

        </div>

        {// =================================================
         // REGULAR / POSTSEASON TABS
         // ================================================= */}

        <div className="mb-6 grid grid-cols-2 gap-3 rounded-2xl border border-slate-800 bg-slate-900 p-2">

          {/* REGULAR SEASON */}

          <button
            type="button"
            onClick={
              () =>
                changeSeasonType(
                  "regular"
                )
            }
            className={[
              "rounded-xl px-5 py-4 text-left transition",

              selectedSeasonType ===
              "regular"
                ? "bg-blue-600 text-white shadow-lg"
                : "text-slate-400 hover:bg-slate-800 hover:text-white",
            ].join(" ")}
          >

            <div className="flex items-center gap-3">

              <CalendarDays className="h-5 w-5" />

              <div>

                <p className="font-semibold">
                  Regular Season
                </p>

                <p
                  className={[
                    "mt-1 text-xs",

                    selectedSeasonType ===
                    "regular"
                      ? "text-blue-100"
                      : "text-slate-500",
                  ].join(" ")}
                >
                  {
                    regularSeasonGames
                      .length
                  }{" "}
                  games
                </p>

              </div>

            </div>

          </button>

          {/* POSTSEASON */}

          <button
            type="button"
            onClick={
              () =>
                changeSeasonType(
                  "postseason"
                )
            }
            className={[
              "rounded-xl px-5 py-4 text-left transition",

              selectedSeasonType ===
              "postseason"
                ? "bg-amber-500 text-slate-950 shadow-lg"
                : "text-slate-400 hover:bg-slate-800 hover:text-white",
            ].join(" ")}
          >

            <div className="flex items-center gap-3">

              <Trophy className="h-5 w-5" />

              <div>

                <p className="font-semibold">
                  Postseason
                </p>

                <p
                  className={[
                    "mt-1 text-xs",

                    selectedSeasonType ===
                    "postseason"
                      ? "text-amber-950/70"
                      : "text-slate-500",
                  ].join(" ")}
                >
                  {
                    postseasonGames
                      .length
                  }{" "}
                  games
                </p>

              </div>

            </div>

          </button>

        </div>

        {// =================================================
         // REGULAR-SEASON WEEK NAVIGATION
         // ================================================= */}

        {selectedSeasonType ===
          "regular" && (

          <div className="mb-6 rounded-2xl border border-slate-800 bg-slate-900 p-5">

            <div className="mb-4 flex items-center justify-between">

              <div>

                <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">
                  Schedule
                </p>

                <h2 className="mt-1 text-xl font-bold">

                  {selectedWeek ===
                  null
                    ? "All Weeks"
                    : `Week ${selectedWeek}`}

                </h2>

              </div>

              <div className="flex gap-2">

                <button
                  type="button"
                  onClick={
                    previousWeek
                  }
                  disabled={
                    selectedWeek ===
                      null ||
                    selectedWeekIndex <=
                      0
                  }
                  className="rounded-lg border border-slate-700 bg-slate-950 p-2.5 text-slate-300 transition hover:border-slate-600 hover:text-white disabled:cursor-not-allowed disabled:opacity-30"
                  aria-label="Previous week"
                >
                  <ChevronLeft className="h-5 w-5" />
                </button>

                <button
                  type="button"
                  onClick={
                    nextWeek
                  }
                  disabled={
                    selectedWeek ===
                      null ||
                    selectedWeekIndex ===
                      -1 ||
                    selectedWeekIndex >=
                      weeks.length - 1
                  }
                  className="rounded-lg border border-slate-700 bg-slate-950 p-2.5 text-slate-300 transition hover:border-slate-600 hover:text-white disabled:cursor-not-allowed disabled:opacity-30"
                  aria-label="Next week"
                >
                  <ChevronRight className="h-5 w-5" />
                </button>

              </div>

            </div>

            {/* WEEK BUTTONS */}

            <div className="flex gap-2 overflow-x-auto pb-2">

              <button
                type="button"
                onClick={
                  () =>
                    setSelectedWeek(
                      null
                    )
                }
                className={[
                  "shrink-0 rounded-lg px-4 py-2 text-sm font-medium transition",

                  selectedWeek ===
                  null
                    ? "bg-blue-600 text-white"
                    : "bg-slate-950 text-slate-400 hover:bg-slate-800 hover:text-white",
                ].join(" ")}
              >
                All Weeks
              </button>

              {weeks.map(
                (week) => (

                  <button
                    key={
                      week
                    }
                    type="button"
                    onClick={
                      () =>
                        setSelectedWeek(
                          week
                        )
                    }
                    className={[
                      "shrink-0 rounded-lg px-4 py-2 text-sm font-medium transition",

                      selectedWeek ===
                      week
                        ? "bg-blue-600 text-white"
                        : "bg-slate-950 text-slate-400 hover:bg-slate-800 hover:text-white",
                    ].join(" ")}
                  >
                    Week {week}
                  </button>

                )
              )}

            </div>

          </div>

        )}

        {// =================================================
         // POSTSEASON HEADER
         //================================================= */}

        {selectedSeasonType ===
          "postseason" && (

          <div className="mb-6 rounded-2xl border border-amber-500/20 bg-amber-500/5 p-6">

            <div className="flex items-center gap-3">

              <div className="rounded-xl bg-amber-500/10 p-3">

                <Trophy className="h-6 w-6 text-amber-400" />

              </div>

              <div>

                <h2 className="text-xl font-bold">
                  {selectedSeason} Postseason
                </h2>

                <p className="mt-1 text-sm text-slate-400">
                  Bowl games and College
                  Football Playoff matchups
                  shown chronologically.
                </p>

              </div>

            </div>

          </div>

        )}

        {// =================================================
         // FILTERS
         // ================================================= */}

        <div className="mb-8 rounded-2xl border border-slate-800 bg-slate-900 p-6">

          <div className="mb-5 flex items-center justify-between">

            <div className="flex items-center gap-2">

              <Filter className="h-5 w-5 text-blue-400" />

              <h2 className="font-semibold">
                Filter Games
              </h2>

            </div>

            {hasExtraFilters && (

              <button
                type="button"
                onClick={
                  clearFilters
                }
                className="flex items-center gap-2 text-sm text-slate-400 transition hover:text-white"
              >
                <X className="h-4 w-4" />

                Clear Filters
              </button>

            )}

          </div>

          <div className="grid gap-4 md:grid-cols-3">

            {/* SEARCH */}

            <div>

              <label
                htmlFor="game-search"
                className="mb-2 block text-sm font-medium text-slate-400"
              >
                Search
              </label>

              <div className="relative">

                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" />

                <input
                  id="game-search"
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
                  placeholder="Team, bowl, CFP, venue..."
                  className="w-full rounded-lg border border-slate-700 bg-slate-950 py-2.5 pl-10 pr-4 text-sm text-white outline-none transition placeholder:text-slate-600 focus:border-blue-500"
                />

              </div>

            </div>

            {/* CONFERENCE */}

            <div>

              <label
                htmlFor="conference"
                className="mb-2 block text-sm font-medium text-slate-400"
              >
                Conference
              </label>

              <select
                id="conference"
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

            {/* STATUS */}

            <div>

              <label
                htmlFor="status"
                className="mb-2 block text-sm font-medium text-slate-400"
              >
                Status
              </label>

              <select
                id="status"
                value={
                  selectedStatus
                }
                onChange={
                  (event) =>
                    setSelectedStatus(
                      event.target
                        .value
                    )
                }
                className="w-full rounded-lg border border-slate-700 bg-slate-950 px-4 py-2.5 text-sm text-white outline-none transition focus:border-blue-500"
              >

                <option value="ALL">
                  All Statuses
                </option>

                {statuses.map(
                  (status) => (

                    <option
                      key={
                        status
                      }
                      value={
                        status
                      }
                    >
                      {formatStatus(
                        status
                      )}
                    </option>

                  )
                )}

              </select>

            </div>

          </div>

          {/* RESULT COUNT */}

          <div className="mt-5 border-t border-slate-800 pt-4">

            <p className="text-sm text-slate-500">

              <span className="font-semibold text-slate-300">
                {filteredGames.length}
              </span>

              {" "}games

              {selectedSeason !==
                null && (
                <>
                  {" "}in{" "}

                  <span className="font-semibold text-slate-300">
                    {selectedSeason}
                  </span>
                </>
              )}

              {" • "}

              <span className="font-semibold text-slate-300">

                {selectedSeasonType ===
                "regular"
                  ? selectedWeek ===
                    null
                    ? "Regular Season"
                    : `Regular Season • Week ${selectedWeek}`
                  : "Postseason"}

              </span>

            </p>

          </div>

        </div>

        {// =================================================
         // GAME LIST
         // ================================================= */}

        {filteredGames.length ===
        0 ? (

          <div className="rounded-xl border border-slate-800 bg-slate-900 p-10 text-center">

            <CalendarDays className="mx-auto mb-4 h-10 w-10 text-slate-600" />

            <h2 className="font-semibold">
              No games found
            </h2>

            <p className="mt-2 text-sm text-slate-500">
              No games match the selected
              season and filters.
            </p>

          </div>

        ) : (

          <div className="grid gap-6 lg:grid-cols-2">

            {filteredGames.map(
              (game) => (

                <GameCard
                  key={
                    game.id
                  }
                  game={
                    game
                  }
                  postseason={
                    selectedSeasonType ===
                    "postseason"
                  }
                />

              )
            )}

          </div>

        )}

      </div>

    </div>
  );
}

// =========================================================
// GAME CARD
// =========================================================

function GameCard({
  game,
  postseason,
}: {
  game: Game;
  postseason: boolean;
}) {

  return (
    <Link
      to={`/games/${game.id}`}
      className="block rounded-xl border border-slate-800 bg-slate-900 p-6 transition hover:border-blue-500/40 hover:bg-slate-800/80"
    >

      {// ===================================================
       // CARD HEADER
       // =================================================== */}

      <div className="mb-5 flex items-center justify-between gap-4">

        <div className="flex flex-wrap items-center gap-2">

          <StatusBadge
            status={
              game.status
            }
          />

          {postseason ? (

            <span className="flex items-center gap-1 rounded-full bg-amber-500/10 px-3 py-1 text-xs font-medium text-amber-400">

              <Trophy className="h-3 w-3" />

              Postseason

            </span>

          ) : (

            <span className="rounded-full bg-slate-950 px-3 py-1 text-xs font-medium text-slate-400">
              Week {game.week}
            </span>

          )}

        </div>

        <span className="shrink-0 text-sm text-slate-400">
          {formatGameDate(
            game.gameDate
          )}
        </span>

      </div>

      {// ===================================================
       // BOWL / CFP TITLE
       // =================================================== */}

      {postseason &&
        (
          game.gameName ||
          game.playoffRound
        ) && (

          <div className="mb-6 rounded-xl border border-amber-500/20 bg-amber-500/5 px-4 py-4 text-center">

            {game.gameName && (

              <div className="flex items-center justify-center gap-2">

                <Trophy className="h-5 w-5 shrink-0 text-amber-400" />

                <h2 className="text-lg font-bold text-amber-300">
                  {game.gameName}
                </h2>

              </div>

            )}

            {game.playoffRound && (

              <p className="mt-1 text-sm font-medium text-slate-400">
                {game.playoffRound}
              </p>

            )}

          </div>

        )}

      {// ===================================================
       // MATCHUP
       // =================================================== */}

      <div className="grid grid-cols-3 items-center gap-4">

        {/* AWAY TEAM */}

        <GameTeam
          team={
            game.awayTeam
          }
          score={
            game.awayScore
          }
        />

        {/* VS */}

        <div className="text-center">

          <p className="text-xs font-semibold uppercase tracking-wider text-slate-600">
            VS
          </p>

        </div>

        {/* HOME TEAM */}

        <GameTeam
          team={
            game.homeTeam
          }
          score={
            game.homeScore
          }
        />

      </div>

      {// ===================================================
       // VENUE
       // =================================================== */}

      <div className="mt-6 flex items-center gap-2 border-t border-slate-800 pt-4 text-sm text-slate-400">

        <MapPin className="h-4 w-4 shrink-0 text-slate-500" />

        <p className="truncate">
          {game.venue ||
            "Venue unavailable"}
        </p>

      </div>

    </Link>
  );
}

// =========================================================
// GAME TEAM
// =========================================================

function GameTeam({
  team,
  score,
}: {
  team: Game["homeTeam"];
  score: number | null;
}) {

  const name =
    team?.name?.trim() ||
    "Unknown Team";

  return (
    <div className="min-w-0 text-center">

      {/* LOGO */}

      {team?.logoUrl ? (

        <img
          src={
            team.logoUrl
          }
          alt={`${name} logo`}
          className="mx-auto mb-3 h-16 w-16 object-contain"
        />

      ) : (

        <div className="mx-auto mb-3 flex h-16 w-16 items-center justify-center rounded-full bg-slate-800 text-xl font-bold text-slate-400">

          {name
            .charAt(0)
            .toUpperCase()}

        </div>

      )}

      {/* NAME */}

      <h2 className="truncate font-semibold">
        {name}
      </h2>

      {/* ABBREVIATION */}

      {team?.abbreviation && (

        <p className="mt-1 text-sm text-slate-400">
          {team.abbreviation}
        </p>

      )}

      {/* CONFERENCE */}

      {team?.conference && (

        <p className="mt-1 truncate text-xs text-slate-600">
          {team.conference}
        </p>

      )}

      {/* SCORE */}

      {score !== null && (

        <p className="mt-3 text-2xl font-bold">
          {score}
        </p>

      )}

    </div>
  );
}

// =========================================================
// STATUS BADGE
// =========================================================

function StatusBadge({
  status,
}: {
  status: string;
}) {

  const normalized =
    status
      ?.toUpperCase() ||
    "";

  let classes =
    "bg-blue-500/10 text-blue-400";

  if (
    normalized ===
    "COMPLETED"
  ) {

    classes =
      "bg-emerald-500/10 text-emerald-400";

  } else if (
    normalized ===
      "LIVE" ||
    normalized ===
      "IN_PROGRESS"
  ) {

    classes =
      "bg-red-500/10 text-red-400";

  } else if (
    normalized ===
    "SCHEDULED"
  ) {

    classes =
      "bg-blue-500/10 text-blue-400";
  }

  return (
    <span
      className={`rounded-full px-3 py-1 text-xs font-medium ${classes}`}
    >
      {formatStatus(
        status
      )}
    </span>
  );
}

// =========================================================
// NORMALIZE SEASON TYPE
// =========================================================

function normalizeSeasonType(
  seasonType?: string | null
): SeasonType {

  const normalized =
    seasonType
      ?.trim()
      .toLowerCase();

  if (
    normalized ===
    "postseason"
  ) {
    return "postseason";
  }

  return "regular";
}

// =========================================================
// FORMAT STATUS
// =========================================================

function formatStatus(
  status: string
) {

  if (!status) {
    return "Unknown";
  }

  return status
    .replaceAll(
      "_",
      " "
    )
    .toLowerCase()
    .replace(
      /\b\w/g,
      (character) =>
        character.toUpperCase()
    );
}

// =========================================================
// FORMAT DATE
// =========================================================

function formatGameDate(
  value: string
) {

  const date =
    new Date(
      value
    );

  if (
    Number.isNaN(
      date.getTime()
    )
  ) {

    return "Date unavailable";
  }

  return date.toLocaleString(
    undefined,
    {
      month: "short",
      day: "numeric",
      year: "numeric",
      hour: "numeric",
      minute: "2-digit",
    }
  );
}