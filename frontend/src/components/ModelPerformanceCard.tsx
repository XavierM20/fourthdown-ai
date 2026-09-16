import {
  Activity,
  Brain,
  Database,
  Target,
  TrendingUp,
} from "lucide-react";

import type {
  ModelMetadata,
  ScoreModelMetadata,
} from "../api/ml";

interface ModelPerformanceCardProps {
  winnerMetadata:
    | ModelMetadata
    | null;

  scoreMetadata:
    | ScoreModelMetadata
    | null;

  loading: boolean;
}

export default function ModelPerformanceCard({
  winnerMetadata,
  scoreMetadata,
  loading,
}: ModelPerformanceCardProps) {
  if (loading) {
    return (
      <div className="rounded-2xl border border-slate-800 bg-slate-900 p-6">
        <p className="text-sm text-slate-400">
          Loading model performance...
        </p>
      </div>
    );
  }

  if (
    !winnerMetadata &&
    !scoreMetadata
  ) {
    return (
      <div className="rounded-2xl border border-slate-800 bg-slate-900 p-6">
        <p className="text-sm text-slate-400">
          Model performance data is currently unavailable.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-6">

      {/* =====================================================
          WINNER MODEL
         ===================================================== */}

      {winnerMetadata && (
        <div className="rounded-2xl border border-blue-500/20 bg-gradient-to-br from-blue-500/10 to-slate-900 p-6">

          <div className="mb-6 flex flex-col gap-4 md:flex-row md:items-start md:justify-between">

            <div>
              <div className="mb-2 flex items-center gap-2">
                <Brain className="h-5 w-5 text-blue-400" />

                <span className="text-sm font-semibold uppercase tracking-wider text-blue-400">
                  Winner Model
                </span>
              </div>

              <h2 className="text-2xl font-bold text-white">
                {winnerMetadata.modelName ??
                  "Winner Prediction Model"}
              </h2>

              <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-400">
                Predicts the probability that the home or away team wins.
              </p>
            </div>

            <div className="rounded-full border border-emerald-500/20 bg-emerald-500/10 px-4 py-2 text-sm font-semibold text-emerald-400">
              Production Model
            </div>

          </div>

          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">

            <MetricCard
              icon={
                <Target className="h-5 w-5 text-blue-400" />
              }
              label="Accuracy"
              value={
                formatPercentage(
                  winnerMetadata.accuracy
                )
              }
            />

            <MetricCard
              icon={
                <Activity className="h-5 w-5 text-purple-400" />
              }
              label="ROC-AUC"
              value={
                formatDecimal(
                  winnerMetadata.rocAuc,
                  4
                )
              }
            />

            <MetricCard
              icon={
                <Brain className="h-5 w-5 text-cyan-400" />
              }
              label="Features"
              value={
                formatInteger(
                  winnerMetadata.featureCount
                )
              }
            />

            <MetricCard
              icon={
                <Database className="h-5 w-5 text-emerald-400" />
              }
              label="Historical Games"
              value={
                formatInteger(
                  winnerMetadata.totalExamples,
                  true
                )
              }
            />

          </div>

          <div className="mt-6 grid gap-4 md:grid-cols-3">

            <DetailCard
              label="Training Seasons"
              value={
                formatSeasons(
                  winnerMetadata.trainingSeasons
                )
              }
            />

            <DetailCard
              label="Holdout Season"
              value={
                formatInteger(
                  winnerMetadata.testSeason
                )
              }
            />

            <DetailCard
              label="Log Loss"
              value={
                formatDecimal(
                  winnerMetadata.logLoss,
                  4
                )
              }
            />

          </div>

        </div>
      )}

      {/* =====================================================
          SCORE MODEL
         ===================================================== */}

      {scoreMetadata && (
        <div className="rounded-2xl border border-purple-500/20 bg-gradient-to-br from-purple-500/10 to-slate-900 p-6">

          <div className="mb-6 flex flex-col gap-4 md:flex-row md:items-start md:justify-between">

            <div>
              <div className="mb-2 flex items-center gap-2">
                <TrendingUp className="h-5 w-5 text-purple-400" />

                <span className="text-sm font-semibold uppercase tracking-wider text-purple-400">
                  Score Model
                </span>
              </div>

              <h2 className="text-2xl font-bold text-white">
                {scoreMetadata.modelName ??
                  "Score Prediction Model"}
              </h2>

              <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-400">
                Predicts scoring margin and total points, which are converted into projected final scores.
              </p>
            </div>

            <div className="rounded-full border border-purple-500/20 bg-purple-500/10 px-4 py-2 text-sm font-semibold text-purple-300">
              Regression Model
            </div>

          </div>

          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">

            <MetricCard
              icon={
                <Target className="h-5 w-5 text-purple-400" />
              }
              label="Score MAE"
              value={
                formatDecimal(
                  scoreMetadata.metrics
                    ?.combinedScoreMae,
                  2
                )
              }
            />

            <MetricCard
              icon={
                <Activity className="h-5 w-5 text-cyan-400" />
              }
              label="Margin MAE"
              value={
                formatDecimal(
                  scoreMetadata.metrics
                    ?.marginMae,
                  2
                )
              }
            />

            <MetricCard
              icon={
                <TrendingUp className="h-5 w-5 text-blue-400" />
              }
              label="Total Points MAE"
              value={
                formatDecimal(
                  scoreMetadata.metrics
                    ?.totalPointsMae,
                  2
                )
              }
            />

            <MetricCard
              icon={
                <Brain className="h-5 w-5 text-emerald-400" />
              }
              label="Features"
              value={
                formatInteger(
                  scoreMetadata.featureCount
                )
              }
            />

          </div>

          <div className="mt-6 grid gap-4 md:grid-cols-3">

            <DetailCard
              label="Training Seasons"
              value={
                formatSeasons(
                  scoreMetadata.trainingSeasons
                )
              }
            />

            <DetailCard
              label="Holdout Season"
              value={
                formatInteger(
                  scoreMetadata.testSeason
                )
              }
            />

            <DetailCard
              label="Test Games"
              value={
                formatInteger(
                  scoreMetadata.testingExamples,
                  true
                )
              }
            />

          </div>

        </div>
      )}

    </div>
  );
}

// =========================================================
// FORMATTERS
// =========================================================

function formatSeasons(
  seasons: number[] | null | undefined
): string {
  if (
    !seasons ||
    seasons.length === 0
  ) {
    return "Unavailable";
  }

  return seasons.join(" + ");
}

function formatPercentage(
  value: number | null | undefined
): string {
  if (value == null) {
    return "Unavailable";
  }

  return `${(
    value * 100
  ).toFixed(2)}%`;
}

function formatDecimal(
  value: number | null | undefined,
  digits: number
): string {
  if (value == null) {
    return "Unavailable";
  }

  return value.toFixed(
    digits
  );
}

function formatInteger(
  value: number | null | undefined,
  locale = false
): string {
  if (value == null) {
    return "Unavailable";
  }

  return locale
    ? value.toLocaleString()
    : value.toString();
}

// =========================================================
// METRIC CARD
// =========================================================

interface MetricCardProps {
  icon: React.ReactNode;
  label: string;
  value: string;
}

function MetricCard({
  icon,
  label,
  value,
}: MetricCardProps) {
  return (
    <div className="rounded-xl border border-slate-800 bg-slate-950/70 p-5">

      <div className="mb-3 flex items-center gap-2">
        {icon}

        <p className="text-sm text-slate-400">
          {label}
        </p>
      </div>

      <p className="text-2xl font-bold text-white">
        {value}
      </p>

    </div>
  );
}

// =========================================================
// DETAIL CARD
// =========================================================

interface DetailCardProps {
  label: string;
  value: string;
}

function DetailCard({
  label,
  value,
}: DetailCardProps) {
  return (
    <div className="rounded-xl border border-slate-800 bg-slate-950/40 p-4">

      <p className="text-xs uppercase tracking-wider text-slate-500">
        {label}
      </p>

      <p className="mt-2 font-semibold text-white">
        {value}
      </p>

    </div>
  );
}