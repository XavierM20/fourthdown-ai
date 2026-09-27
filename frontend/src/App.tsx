import {
  BrowserRouter,
  Link,
  NavLink,
  Route,
  Routes,
} from "react-router-dom";

import {
  Activity,
  BarChart3,
  Brain,
  CalendarDays,
  Gamepad2,
  Home,
  Shield,
  Trophy,
  Users,
} from "lucide-react";

import {
  lazy,
  Suspense,
  useEffect,
  useState,
} from "react";

const Games = lazy(
  () => import("./pages/Games")
);

const GameDetails = lazy(
  () => import("./pages/GameDetails")
);

const Teams = lazy(
  () => import("./pages/Teams")
);

const TeamDetails = lazy(
  () => import("./pages/TeamDetails")
);

const Analytics = lazy(
  () => import("./pages/Analytics")
);

const CompareTeams = lazy(
  () => import("./pages/CompareTeams")
);

const PredictMatchup = lazy(
  () => import("./pages/PredictMatchup")
);

const Rankings = lazy(
  () => import("./pages/Rankings")
);

import {
  getDashboardData,
} from "./api/dashboard";

import type {
  DashboardData,
} from "./api/dashboard";

import {
  getModelMetadata,
  getScoreModelMetadata,
} from "./api/ml";

import type {
  ModelMetadata,
  ScoreModelMetadata,
} from "./api/ml";

import ModelPerformanceCard from "./components/ModelPerformanceCard";

// =========================================================
// APP
// =========================================================

export default function App() {

  return (
    <BrowserRouter>

      <div className="min-h-screen bg-slate-950 text-white">

        <Sidebar />

        <main className="min-h-screen md:ml-64">

          <Suspense
            fallback={
              <RouteLoading />
            }
          >
            <Routes>

              <Route
                path="/"
                element={
                  <HomePage />
                }
              />

              <Route
                path="/games"
                element={
                  <Games />
                }
              />

              <Route
                path="/games/:id"
                element={
                  <GameDetails />
                }
              />

              <Route
                path="/teams"
                element={
                  <Teams />
                }
              />

              <Route
                path="/teams/:id"
                element={
                  <TeamDetails />
                }
              />

              <Route
                path="/rankings"
                element={
                  <Rankings />
                }
              />

              <Route
                path="/analytics"
                element={
                  <Analytics />
                }
              />

              <Route
                path="/analytics/compare"
                element={
                  <CompareTeams />
                }
              />

              <Route
                path="/predict"
                element={
                  <PredictMatchup />
                }
              />

            </Routes>
          </Suspense>

        </main>

      </div>

    </BrowserRouter>
  );
}

// =========================================================
// ROUTE LOADING
// =========================================================

function RouteLoading() {
  return (
    <div className="min-h-screen bg-slate-950 p-8 text-white">
      Loading...
    </div>
  );
}

// =========================================================
// SIDEBAR
// =========================================================

function Sidebar() {

  const navItems = [

    {
      to: "/",
      label: "Dashboard",
      icon: Home,
      end: true,
    },

    {
      to: "/games",
      label: "Games",
      icon: CalendarDays,
    },

    {
      to: "/teams",
      label: "Teams",
      icon: Users,
    },

    {
      to: "/rankings",
      label: "Rankings",
      icon: Trophy,
    },

    {
      to: "/analytics",
      label: "Analytics",
      icon: BarChart3,
      end: true,
    },

    {
      to: "/analytics/compare",
      label: "Compare Teams",
      icon: Activity,
    },

    {
      to: "/predict",
      label: "Predict Matchup",
      icon: Brain,
    },

  ];

  return (

    <aside className="fixed left-0 top-0 z-30 hidden h-screen w-64 border-r border-slate-800 bg-slate-900 md:block">

      <div className="flex h-full flex-col">

        {/* =================================================
            BRAND
           ================================================= */}

        <div className="border-b border-slate-800 p-6">

          <Link
            to="/"
            className="flex items-center gap-3"
          >

            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-600">

              <Shield className="h-6 w-6 text-white" />

            </div>

            <div>

              <h1 className="font-bold text-white">
                FourthDown AI
              </h1>

              <p className="text-xs text-slate-500">
                College Football Analytics
              </p>

            </div>

          </Link>

        </div>

        {/* =================================================
            NAVIGATION
           ================================================= */}

        <nav className="flex-1 space-y-2 p-4">

          {navItems.map(
            ({
              to,
              label,
              icon: Icon,
              end,
            }) => (

              <NavLink
                key={
                  to
                }
                to={
                  to
                }
                end={
                  end
                }
                className={({
                  isActive,
                }) =>
                  [
                    "flex items-center gap-3 rounded-lg px-4 py-3 text-sm font-medium transition",

                    isActive
                      ? "bg-blue-600 text-white"
                      : "text-slate-400 hover:bg-slate-800 hover:text-white",

                  ].join(" ")
                }
              >

                <Icon className="h-5 w-5" />

                {label}

              </NavLink>

            )
          )}

        </nav>

        {/* =================================================
            SIDEBAR FOOTER
           ================================================= */}

        <div className="border-t border-slate-800 p-4">

          <div className="rounded-lg bg-slate-950 p-4">

            <div className="mb-2 flex items-center gap-2">

              <Brain className="h-4 w-4 text-blue-400" />

              <span className="text-sm font-semibold">
                Prediction Engine
              </span>

            </div>

            <p className="text-xs leading-5 text-slate-500">
              ML-powered winner and score
              predictions using historical
              college football data.
            </p>

          </div>

        </div>

      </div>

    </aside>
  );
}

// =========================================================
// HOME PAGE
// =========================================================

function HomePage() {

  // =======================================================
  // DASHBOARD STATE
  // =======================================================

  const [
    dashboard,
    setDashboard,
  ] =
    useState<DashboardData | null>(
      null
    );

  const [
    loadingDashboard,
    setLoadingDashboard,
  ] =
    useState(true);

  const [
    dashboardError,
    setDashboardError,
  ] =
    useState("");

  // =======================================================
  // WINNER MODEL STATE
  // =======================================================

  const [
    modelMetadata,
    setModelMetadata,
  ] =
    useState<ModelMetadata | null>(
      null
    );

  // =======================================================
  // SCORE MODEL STATE
  // =======================================================

  const [
    scoreModelMetadata,
    setScoreModelMetadata,
  ] =
    useState<ScoreModelMetadata | null>(
      null
    );

  const [
    loadingModel,
    setLoadingModel,
  ] =
    useState(true);

  // =======================================================
  // LOAD DASHBOARD
  // =======================================================

  useEffect(() => {

    async function loadDashboard() {

      try {

        const data =
          await getDashboardData();

        setDashboard(
          data
        );

      } catch (error) {

        console.error(
          "Failed to load dashboard:",
          error
        );

        setDashboardError(
          "Unable to load dashboard data."
        );

      } finally {

        setLoadingDashboard(
          false
        );
      }
    }

    loadDashboard();

  }, []);

  // =======================================================
  // LOAD BOTH ML MODELS
  // =======================================================

  useEffect(() => {

    async function loadModelMetadata() {

      try {

        const [
          winnerData,
          scoreData,
        ] =
          await Promise.all([
            getModelMetadata(),
            getScoreModelMetadata(),
          ]);

        setModelMetadata(
          winnerData
        );

        setScoreModelMetadata(
          scoreData
        );

      } catch (error) {

        console.error(
          "Failed to load model metadata:",
          error
        );

      } finally {

        setLoadingModel(
          false
        );
      }
    }

    loadModelMetadata();

  }, []);

  // =======================================================
  // DASHBOARD LOADING
  // =======================================================

  if (
    loadingDashboard
  ) {

    return (

      <div className="min-h-screen bg-slate-950 p-8 text-white">
        Loading dashboard...
      </div>
    );
  }

  // =======================================================
  // DASHBOARD ERROR
  // =======================================================

  if (
    dashboardError ||
    !dashboard
  ) {

    return (

      <div className="min-h-screen bg-slate-950 p-8 text-white">

        <p className="text-red-400">

          {
            dashboardError ||
            "Dashboard data is unavailable."
          }

        </p>

      </div>
    );
  }

  // =======================================================
  // PAGE
  // =======================================================

  return (

    <div className="min-h-screen bg-slate-950 p-8 text-white">

      <div className="mx-auto max-w-7xl">

        {/* =================================================
            HEADER
           ================================================= */}

        <div className="mb-8">

          <div className="mb-3 flex items-center gap-3">

            <Brain className="h-8 w-8 text-blue-400" />

            <h1 className="text-3xl font-bold">
              FourthDown AI
            </h1>

          </div>

          <p className="max-w-3xl text-slate-400">
            College football analytics,
            historical performance data,
            machine-learning win probabilities,
            and ML-generated score projections.
          </p>

        </div>

        {/* =================================================
            SUMMARY CARDS
           ================================================= */}

        <div className="mb-8 grid gap-5 sm:grid-cols-2 xl:grid-cols-4">

          <DashboardMetricCard
            title="Teams"
            value={
              dashboard.totalTeams
                .toLocaleString()
            }
            description="Tracked programs"
            icon={
              <Users className="h-6 w-6 text-blue-400" />
            }
          />

          <DashboardMetricCard
            title="Games"
            value={
              dashboard.totalGames
                .toLocaleString()
            }
            description="Games in database"
            icon={
              <Gamepad2 className="h-6 w-6 text-purple-400" />
            }
          />

          <DashboardMetricCard
            title="Upcoming Games"
            value={
              dashboard.upcomingGames
                .toLocaleString()
            }
            description="Scheduled matchups"
            icon={
              <CalendarDays className="h-6 w-6 text-emerald-400" />
            }
          />

          <DashboardMetricCard
            title="Prediction Engine"
            value="Live"
            description="Winner + score models"
            icon={
              <Brain className="h-6 w-6 text-cyan-400" />
            }
          />

        </div>

        {/* =================================================
            MODEL PERFORMANCE
           ================================================= */}

        <div className="mb-8">

          <div className="mb-5">

            <h2 className="text-2xl font-bold">
              Model Performance
            </h2>

            <p className="mt-1 text-sm text-slate-400">
              Evaluation results for the
              production prediction models
            </p>

          </div>

          <ModelPerformanceCard
            winnerMetadata={
              modelMetadata
            }
            scoreMetadata={
              scoreModelMetadata
            }
            loading={
              loadingModel
            }
          />

        </div>

        {/* =================================================
            QUICK ACTIONS
           ================================================= */}

        <div className="mb-8">

          <div className="mb-5">

            <h2 className="text-2xl font-bold">
              Quick Actions
            </h2>

            <p className="mt-1 text-sm text-slate-400">
              Explore FourthDown AI
            </p>

          </div>

          <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-5">

            <QuickLinkCard
              to="/predict"
              title="Predict Matchup"
              description="Generate ML-powered win probabilities and projected scores."
              icon={
                <Brain className="h-6 w-6 text-blue-400" />
              }
            />

            <QuickLinkCard
              to="/teams"
              title="Browse Teams"
              description="Explore team information and season analytics."
              icon={
                <Users className="h-6 w-6 text-emerald-400" />
              }
            />

            <QuickLinkCard
              to="/games"
              title="Browse Games"
              description="View scheduled and completed college football games."
              icon={
                <CalendarDays className="h-6 w-6 text-purple-400" />
              }
            />

            <QuickLinkCard
              to="/rankings"
              title="View Rankings"
              description="Explore FourthDown AI FBS and FCS season rankings and standings."
              icon={
                <Trophy className="h-6 w-6 text-amber-400" />
              }
            />

            <QuickLinkCard
              to="/analytics/compare"
              title="Compare Teams"
              description="Compare team performance and advanced metrics."
              icon={
                <BarChart3 className="h-6 w-6 text-cyan-400" />
              }
            />

          </div>

        </div>

        {/* =================================================
            PREDICTION ENGINE SUMMARY
           ================================================= */}

        <div className="rounded-2xl border border-slate-800 bg-slate-900 p-6">

          <div className="flex flex-col gap-5 md:flex-row md:items-center md:justify-between">

            <div>

              <div className="mb-2 flex items-center gap-2">

                <Trophy className="h-5 w-5 text-yellow-400" />

                <span className="text-sm font-semibold uppercase tracking-wider text-slate-400">
                  FourthDown AI
                </span>

              </div>

              <h2 className="text-2xl font-bold">
                Two-model prediction engine
              </h2>

              <p className="mt-2 max-w-3xl text-sm leading-6 text-slate-400">
                Logistic Regression predicts
                win probabilities while Ridge
                Regression predicts scoring
                margin and total points to
                generate projected final scores.
              </p>

            </div>

            <Link
              to="/predict"
              className="inline-flex shrink-0 items-center justify-center gap-2 rounded-lg bg-blue-600 px-6 py-3 font-semibold text-white transition hover:bg-blue-500"
            >

              <Brain className="h-5 w-5" />

              Open Predictor

            </Link>

          </div>

        </div>

      </div>

    </div>
  );
}

// =========================================================
// DASHBOARD METRIC CARD
// =========================================================

interface DashboardMetricCardProps {

  title: string;

  value: string;

  description: string;

  icon: React.ReactNode;
}

function DashboardMetricCard({
  title,
  value,
  description,
  icon,
}: DashboardMetricCardProps) {

  return (

    <div className="rounded-2xl border border-slate-800 bg-slate-900 p-6">

      <div className="mb-4 flex items-center justify-between">

        <p className="text-sm font-medium text-slate-400">
          {title}
        </p>

        <div className="rounded-lg bg-slate-950 p-2">
          {icon}
        </div>

      </div>

      <p className="text-3xl font-bold text-white">
        {value}
      </p>

      <p className="mt-2 text-sm text-slate-500">
        {description}
      </p>

    </div>
  );
}

// =========================================================
// QUICK LINK CARD
// =========================================================

interface QuickLinkCardProps {

  to: string;

  title: string;

  description: string;

  icon: React.ReactNode;
}

function QuickLinkCard({
  to,
  title,
  description,
  icon,
}: QuickLinkCardProps) {

  return (

    <Link
      to={
        to
      }
      className="group rounded-2xl border border-slate-800 bg-slate-900 p-6 transition hover:border-blue-500/40 hover:bg-slate-900/80"
    >

      <div className="mb-4 flex h-11 w-11 items-center justify-center rounded-lg bg-slate-950">

        {icon}

      </div>

      <h3 className="font-semibold text-white transition group-hover:text-blue-400">
        {title}
      </h3>

      <p className="mt-2 text-sm leading-6 text-slate-500">
        {description}
      </p>

    </Link>
  );
}