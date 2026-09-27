import { useEffect, useState } from "react";
import {
  Link,
  useLocation,
  useParams,
} from "react-router-dom";
import {
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";

import {
  getTeamById,
  getTeamRecord,
} from "../api/teams";

import type {
  Team,
  TeamRecord,
} from "../api/teams";

import {
  getStatsByTeam,
  getTeamAnalytics,
} from "../api/stats";

import type {
  TeamAnalytics,
  TeamGameStats,
} from "../api/stats";

import {
  getRankingHistory,
  getRankings,
} from "../api/rankings";

import type {
  PollRankingsResponse,
  TeamRankingHistoryResponse,
} from "../api/rankings";

export default function TeamDetails() {
  const { id } = useParams();
  const teamId = Number(id);

  const [team, setTeam] =
    useState<Team | null>(null);

  const [record, setRecord] =
    useState<TeamRecord | null>(null);

  const [analytics, setAnalytics] =
    useState<TeamAnalytics | null>(null);

  const [stats, setStats] =
    useState<TeamGameStats[]>([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState("");

  const [
    rankingData,
    setRankingData,
  ] =
    useState<PollRankingsResponse | null>(
      null
    );

  const [
    rankingHistory,
    setRankingHistory,
  ] =
    useState<TeamRankingHistoryResponse | null>(
      null
    );

  const location =
    useLocation();

  const navigationState =
    location.state as
      | {
          from?: string;
          backLabel?: string;
        }
      | null;

  const backPath =
    navigationState?.from ??
    "/teams";

  const backLabel =
    navigationState?.backLabel ??
    "Back to Teams";

  /*
   * If the page was opened from Rankings, preserve
   * the selected ranking season.
   */
  const rankingSeason = (() => {
    if (
      !navigationState?.from?.startsWith(
        "/rankings"
      )
    ) {
      return null;
    }

    const queryString =
      navigationState.from.split("?")[1];

    if (!queryString) {
      return null;
    }

    const params =
      new URLSearchParams(
        queryString
      );

    const value =
      Number(
        params.get(
          "season"
        )
      );

    return Number.isFinite(value) &&
      value > 2000
      ? value
      : null;
  })();

  const season =
    rankingSeason ??
    new Date().getFullYear();

  useEffect(() => {
    async function loadTeam() {
      if (!teamId) {
        setError("Invalid team.");
        setLoading(false);
        return;
      }

      setLoading(true);
      setError("");

      try {
        const [
          teamData,
          statsData,
          analyticsData,
        ] = await Promise.all([
          getTeamById(teamId),
          getStatsByTeam(teamId),
          getTeamAnalytics(teamId),
        ]);

        setTeam(teamData);
        setStats(statsData);
        setAnalytics(analyticsData);

        const normalizedClassification =
          teamData.classification
            ?.toLowerCase();

        if (
          normalizedClassification === "fbs" ||
          normalizedClassification === "fcs"
        ) {
          try {
            const [
              rankings,
              history,
            ] = await Promise.all([
              getRankings(
                season,
                normalizedClassification
              ),
              getRankingHistory(
                season,
                normalizedClassification,
                teamId
              ),
            ]);

            setRankingData(
              rankings
            );

            setRankingHistory(
              history
            );
          } catch (rankingError) {
            console.error(
              "Unable to load team ranking data:",
              rankingError
            );

            setRankingData(
              null
            );

            setRankingHistory(
              null
            );
          }
        } else {
          setRankingData(
            null
          );

          setRankingHistory(
            null
          );
        }

        try {
          const recordData =
            await getTeamRecord(
              teamId,
              season
            );

          setRecord(recordData);
        } catch {
          setRecord(null);
        }
      } catch (err) {
        console.error(err);

        setError(
          "Unable to load team details."
        );
      } finally {
        setLoading(false);
      }
    }

    loadTeam();
  }, [teamId, season]);

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 p-8 text-white">
        Loading team...
      </div>
    );
  }

  if (error || !team) {
    return (
      <div className="min-h-screen bg-slate-950 p-8 text-white">
        <Link
          to={backPath}
          className="mb-6 inline-block text-sm text-blue-400 hover:text-blue-300"
        >
          ← {backLabel}
        </Link>

        <p className="text-red-400">
          {error || "Team not found."}
        </p>
      </div>
    );
  }

  const primaryColor =
    team.primaryColor || "#1e293b";

  const alternateColor =
    team.alternateColor || "#334155";

  const totalWins =
    record?.total?.wins ??
    analytics?.wins ??
    0;

  const totalLosses =
    record?.total?.losses ??
    analytics?.losses ??
    0;

  const totalTies =
    record?.total?.ties ?? 0;

  const conferenceWins =
    record?.conferenceGames?.wins ?? 0;

  const conferenceLosses =
    record?.conferenceGames?.losses ?? 0;

  const conferenceTies =
    record?.conferenceGames?.ties ?? 0;

  const currentRanking =
    rankingData?.rankings.find(
      (ranking) =>
        ranking.teamId === teamId
    ) ?? null;

  const rankingChartData =
    rankingHistory?.history.map(
      (entry) => ({
        week: `Week ${entry.week}`,
        weekNumber: entry.week,
        rank: entry.rank,
        poll: entry.poll,
      })
    ) ?? [];

  function formatRecord(
    wins: number,
    losses: number,
    ties: number
  ) {
    if (ties > 0) {
      return `${wins}-${losses}-${ties}`;
    }

    return `${wins}-${losses}`;
  }

  function formatNumber(
    value: number | null | undefined
  ) {
    if (value == null) {
      return "—";
    }

    return value.toFixed(1);
  }

  function formatPercentage(
    value: number | null | undefined
  ) {
    if (value == null) {
      return "—";
    }

    return `${value.toFixed(1)}%`;
  }

  function formatMargin(
    value: number | null | undefined
  ) {
    if (value == null) {
      return "—";
    }

    if (value > 0) {
      return `+${value.toFixed(1)}`;
    }

    return value.toFixed(1);
  }

  function formatMovement(
    movement: number | null | undefined
  ) {
    if (movement == null) {
      return "—";
    }

    if (movement > 0) {
      return `▲ ${movement}`;
    }

    if (movement < 0) {
      return `▼ ${Math.abs(
        movement
      )}`;
    }

    return "— 0";
  }

  function movementClass(
    movement: number | null | undefined
  ) {
    if (movement == null) {
      return "text-slate-400";
    }

    if (movement > 0) {
      return "text-emerald-400";
    }

    if (movement < 0) {
      return "text-red-400";
    }

    return "text-slate-300";
  }

  return (
    <div className="min-h-screen bg-slate-950 text-white">
      <div className="mx-auto max-w-7xl p-8">
        <div className="mb-6 flex flex-wrap gap-4">
          <Link
            to={backPath}
            className="text-sm text-blue-400 hover:text-blue-300"
          >
            ← {backLabel}
          </Link>

          <Link
            to="/"
            className="text-sm text-slate-400 hover:text-white"
          >
            Back to Home
          </Link>
        </div>

        {/* TEAM HEADER */}

        <div
          className="relative mb-8 overflow-hidden rounded-2xl border border-slate-800"
          style={{
            background: `linear-gradient(
              135deg,
              ${primaryColor},
              ${alternateColor}
            )`,
          }}
        >
          <div className="absolute inset-0 bg-black/40" />

          <div className="relative flex flex-col gap-6 p-8 md:flex-row md:items-center md:justify-between">
            <div className="flex items-center gap-6">
              <TeamLogo team={team} />

              <div>
                <div className="mb-2 flex flex-wrap items-center gap-2">
                  {team.classification && (
                    <span className="rounded-full bg-black/30 px-3 py-1 text-xs font-semibold uppercase backdrop-blur">
                      {team.classification}
                    </span>
                  )}

                  {currentRanking && (
                    <span className="rounded-full bg-amber-400 px-3 py-1 text-xs font-bold text-slate-950 shadow-lg">
                      #{currentRanking.rank}{" "}
                      {rankingData?.poll}
                    </span>
                  )}

                  {rankingHistory?.movement != null && (
                    <span
                      className={`rounded-full bg-black/30 px-3 py-1 text-xs font-bold backdrop-blur ${movementClass(
                        rankingHistory.movement
                      )}`}
                    >
                      {formatMovement(
                        rankingHistory.movement
                      )}
                    </span>
                  )}

                  <span className="rounded-full bg-black/30 px-3 py-1 text-xs font-semibold backdrop-blur">
                    {team.conference}
                  </span>
                </div>

                <h1 className="text-4xl font-bold md:text-5xl">
                  {team.name}
                </h1>

                {team.mascot && (
                  <p className="mt-2 text-lg text-white/80">
                    {team.mascot}
                  </p>
                )}

                {(team.city ||
                  team.state) && (
                  <p className="mt-1 text-sm text-white/70">
                    {[team.city, team.state]
                      .filter(Boolean)
                      .join(", ")}
                  </p>
                )}
              </div>
            </div>

            <div className="rounded-2xl bg-black/30 px-8 py-5 text-center backdrop-blur">
              <p className="text-sm font-medium text-white/70">
                {season} Record
              </p>

              <p className="mt-1 text-4xl font-bold">
                {formatRecord(
                  totalWins,
                  totalLosses,
                  totalTies
                )}
              </p>
            </div>
          </div>
        </div>

        {/* RECORD SUMMARY */}

        <div className="mb-8 grid gap-5 md:grid-cols-2 xl:grid-cols-4">
          <SummaryCard
            label="Overall Record"
            value={formatRecord(
              totalWins,
              totalLosses,
              totalTies
            )}
          />

          <SummaryCard
            label="Conference Record"
            value={
              record?.conferenceGames
                ? formatRecord(
                    conferenceWins,
                    conferenceLosses,
                    conferenceTies
                  )
                : "—"
            }
          />

          <SummaryCard
            label="Games Played"
            value={
              analytics?.gamesPlayed ??
              record?.total?.games ??
              "—"
            }
          />

          <SummaryCard
            label="National Ranking"
            value={
              currentRanking
                ? `#${currentRanking.rank}`
                : "Not Ranked"
            }
          />
        </div>

        {/* RANKING HISTORY */}

        {rankingHistory &&
          rankingHistory.history.length > 0 && (
          <div className="mb-8">
            <div className="mb-5">
              <h2 className="text-2xl font-bold">
                Ranking History
              </h2>

              <p className="mt-1 text-sm text-slate-400">
                Weekly official poll position for the{" "}
                {season} season
              </p>
            </div>

            <div className="mb-5 grid gap-5 sm:grid-cols-3">
              <SummaryCard
                label="Current Rank"
                value={
                  rankingHistory.currentRank != null
                    ? `#${rankingHistory.currentRank}`
                    : "Not Ranked"
                }
              />

              <SummaryCard
                label="Previous Rank"
                value={
                  rankingHistory.previousRank != null
                    ? `#${rankingHistory.previousRank}`
                    : "Not Ranked"
                }
              />

              <div className="rounded-xl border border-slate-800 bg-slate-900 p-6">
                <p className="text-sm text-slate-400">
                  Weekly Movement
                </p>

                <p
                  className={`mt-2 text-3xl font-bold ${movementClass(
                    rankingHistory.movement
                  )}`}
                >
                  {formatMovement(
                    rankingHistory.movement
                  )}
                </p>
              </div>
            </div>

            <div className="rounded-2xl border border-slate-800 bg-slate-900 p-6">
              <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
                <div>
                  <h3 className="font-semibold">
                    Weekly Ranking Trend
                  </h3>

                  <p className="mt-1 text-sm text-slate-500">
                    Lower ranking numbers are better.
                    Gaps indicate an unranked week.
                  </p>
                </div>

                <span className="rounded-full bg-slate-800 px-3 py-1 text-xs font-medium text-slate-300">
                  {
                    rankingHistory.history[
                      rankingHistory.history.length - 1
                    ]?.poll
                  }
                </span>
              </div>

              <div className="h-80 w-full">
                <ResponsiveContainer
                  width="100%"
                  height="100%"
                >
                  <LineChart
                    data={rankingChartData}
                    margin={{
                      top: 10,
                      right: 20,
                      left: 0,
                      bottom: 10,
                    }}
                  >
                    <CartesianGrid
                      strokeDasharray="3 3"
                      stroke="#334155"
                    />

                    <XAxis
                      dataKey="week"
                      stroke="#94a3b8"
                      tick={{
                        fontSize: 12,
                      }}
                    />

                    <YAxis
                      domain={[1, 25]}
                      reversed
                      allowDecimals={false}
                      stroke="#94a3b8"
                      tick={{
                        fontSize: 12,
                      }}
                      tickFormatter={
                        (value) =>
                          `#${value}`
                      }
                    />

                    <Tooltip
                      contentStyle={{
                        backgroundColor:
                          "#0f172a",
                        border:
                          "1px solid #334155",
                        borderRadius:
                          "0.75rem",
                        color:
                          "#ffffff",
                      }}
                      labelStyle={{
                        color:
                          "#cbd5e1",
                      }}
                    />

                    <Line
                      type="monotone"
                      dataKey="rank"
                      name="Rank"
                      stroke="#fbbf24"
                      strokeWidth={3}
                      dot={{
                        r: 5,
                        fill:
                          "#fbbf24",
                      }}
                      activeDot={{
                        r: 7,
                      }}
                      connectNulls={false}
                    />
                  </LineChart>
                </ResponsiveContainer>
              </div>

              <div className="mt-6 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
                {rankingHistory.history.map(
                  (entry) => (
                    <div
                      key={`${entry.week}-${entry.poll}`}
                      className="rounded-xl border border-slate-800 bg-slate-950/70 p-4"
                    >
                      <p className="text-xs font-medium uppercase tracking-wide text-slate-500">
                        Week {entry.week}
                      </p>

                      <p className="mt-1 text-2xl font-bold">
                        {entry.rank != null
                          ? `#${entry.rank}`
                          : "NR"}
                      </p>

                      <p className="mt-1 truncate text-xs text-slate-500">
                        {entry.poll}
                      </p>
                    </div>
                  )
                )}
              </div>
            </div>
          </div>
        )}

        {/* ADVANCED ANALYTICS */}

        <div className="mb-8">
          <div className="mb-5">
            <h2 className="text-2xl font-bold">
              FourthDown Analytics
            </h2>

            <p className="mt-1 text-sm text-slate-400">
              Performance metrics calculated
              from imported game statistics
            </p>
          </div>

          {analytics &&
          analytics.gamesPlayed > 0 ? (
            <>
              <div className="mb-5 grid gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6">
                <MetricCard
                  label="Record"
                  value={`${analytics.wins}-${analytics.losses}`}
                />

                <MetricCard
                  label="Recent Form"
                  value={`${analytics.recentWins}-${analytics.recentLosses}`}
                />

                <MetricCard
                  label="Scoring Margin"
                  value={formatMargin(
                    analytics.averageScoringMargin
                  )}
                />

                <MetricCard
                  label="3rd Down"
                  value={formatPercentage(
                    analytics.thirdDownConversionRate
                  )}
                />

                <MetricCard
                  label="Red Zone"
                  value={formatPercentage(
                    analytics.redZoneScoringRate
                  )}
                />

                <MetricCard
                  label="Turnovers / Game"
                  value={formatNumber(
                    analytics.averageTurnovers
                  )}
                />
              </div>

              <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
                <MetricCard
                  label="Avg Points"
                  value={formatNumber(
                    analytics.averagePoints
                  )}
                />

                <MetricCard
                  label="Avg Total Yards"
                  value={formatNumber(
                    analytics.averageTotalYards
                  )}
                />

                <MetricCard
                  label="Avg Passing Yards"
                  value={formatNumber(
                    analytics.averagePassingYards
                  )}
                />

                <MetricCard
                  label="Avg Rushing Yards"
                  value={formatNumber(
                    analytics.averageRushingYards
                  )}
                />
              </div>
            </>
          ) : (
            <div className="rounded-xl border border-slate-800 bg-slate-900 p-8">
              <p className="text-slate-400">
                No analytics have been imported
                for this team yet.
              </p>
            </div>
          )}
        </div>

        {/* TEAM INFO */}

        <div className="mb-8">
          <h2 className="mb-5 text-2xl font-bold">
            Team Information
          </h2>

          <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-4">
            <InfoCard
              label="Classification"
              value={
                team.classification
                  ? team.classification.toUpperCase()
                  : "Unknown"
              }
            />

            <InfoCard
              label="Conference"
              value={team.conference}
            />

            <InfoCard
              label="Abbreviation"
              value={
                team.abbreviation || "—"
              }
            />

            <InfoCard
              label="Location"
              value={
                [team.city, team.state]
                  .filter(Boolean)
                  .join(", ") || "—"
              }
            />
          </div>
        </div>

        {/* DATA STATUS */}

        <div className="rounded-xl border border-slate-800 bg-slate-900 p-6">
          <div className="flex flex-wrap items-center justify-between gap-4">
            <div>
              <h2 className="text-lg font-semibold">
                Analytics Data
              </h2>

              <p className="mt-1 text-sm text-slate-400">
                FourthDown AI currently has{" "}
                {stats.length} game statistic{" "}
                {stats.length === 1
                  ? "entry"
                  : "entries"}{" "}
                for {team.name}.
              </p>
            </div>

            <span
              className={`rounded-full px-3 py-1 text-xs font-medium ${
                stats.length > 0
                  ? "bg-emerald-500/10 text-emerald-400"
                  : "bg-yellow-500/10 text-yellow-400"
              }`}
            >
              {stats.length > 0
                ? "Analytics Available"
                : "Stats Not Imported"}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}

function TeamLogo({
  team,
}: {
  team: Team;
}) {
  const [imageFailed, setImageFailed] =
    useState(false);

  const initials =
    team.abbreviation?.trim() ||
    team.name
      .split(/\s+/)
      .filter(Boolean)
      .map((word) => word[0])
      .join("")
      .slice(0, 3)
      .toUpperCase();

  const logoUrl =
    team.logoUrl ?? undefined;

  const showImage =
    Boolean(logoUrl) &&
    !imageFailed;

  if (showImage) {
    return (
      <div className="flex h-28 w-28 shrink-0 items-center justify-center rounded-2xl bg-white/95 p-3 shadow-xl">
        <img
          src={logoUrl}
          alt={`${team.name} logo`}
          className="max-h-full max-w-full object-contain"
          onError={() =>
            setImageFailed(true)
          }
        />
      </div>
    );
  }

  return (
    <div className="flex h-28 w-28 shrink-0 items-center justify-center rounded-2xl border border-white/10 bg-black/30 px-3 text-center shadow-xl backdrop-blur">
      <span className="text-3xl font-bold tracking-wide text-white">
        {initials || "—"}
      </span>
    </div>
  );
}

interface SummaryCardProps {
  label: string;
  value: string | number;
}

function SummaryCard({
  label,
  value,
}: SummaryCardProps) {
  return (
    <div className="rounded-xl border border-slate-800 bg-slate-900 p-6">
      <p className="text-sm text-slate-400">
        {label}
      </p>

      <p className="mt-2 text-3xl font-bold">
        {value}
      </p>
    </div>
  );
}

interface MetricCardProps {
  label: string;
  value: string | number;
}

function MetricCard({
  label,
  value,
}: MetricCardProps) {
  return (
    <div className="rounded-xl border border-slate-800 bg-slate-900 p-5">
      <p className="text-sm text-slate-400">
        {label}
      </p>

      <p className="mt-2 text-2xl font-bold">
        {value}
      </p>
    </div>
  );
}

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