package com.fourthdown.ai.service;

import com.fourthdown.ai.model.Game;
import com.fourthdown.ai.repository.GameRepository;
import com.fourthdown.ai.repository.TeamGameStatsRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class DataRefreshScheduler {

    /*
     * The coordinator itself is lightweight. It wakes up every five
     * minutes and looks at FourthDown AI's stored schedule.
     *
     * CFBD is only called aggressively when games are close to kickoff
     * or are in the expected live/finalization window.
     */
    private static final int BEFORE_KICKOFF_HOURS = 2;
    private static final int AFTER_KICKOFF_HOURS = 6;

    /*
     * Even when no game is close to kickoff, refresh the season schedule
     * periodically so postponed games, kickoff changes, venues, etc.
     * are picked up automatically.
     */
    private static final long IDLE_SCHEDULE_REFRESH_MINUTES = 120;

    /*
     * Once games begin going final, refresh that week's team statistics
     * at most every 30 minutes. This keeps analytics current without
     * re-importing the full season repeatedly.
     */
    private static final long WEEK_STATS_REFRESH_MINUTES = 30;

    private final DataImportService dataImportService;
    private final GameRepository gameRepository;
    private final TeamGameStatsRepository teamGameStatsRepository;

    private final AtomicBoolean syncRunning =
            new AtomicBoolean(false);

    private LocalDateTime lastScheduleRefresh;

    private final Map<WeekKey, LocalDateTime>
            lastWeekStatsRefresh =
            new HashMap<>();

    public DataRefreshScheduler(
            DataImportService dataImportService,
            GameRepository gameRepository,
            TeamGameStatsRepository teamGameStatsRepository
    ) {

        this.dataImportService =
                dataImportService;

        this.gameRepository =
                gameRepository;

        this.teamGameStatsRepository =
                teamGameStatsRepository;
    }

    // =========================================================
    // SCHEDULE-AWARE COORDINATOR
    //
    // Every 5 minutes:
    // 1. Inspect the stored schedule.
    // 2. Refresh CFBD aggressively only when games are nearby.
    // 3. Detect recently completed/current-week games.
    // 4. Refresh only the affected week's team statistics.
    //
    // When no games are nearby, the full schedule is refreshed only
    // every two hours.
    // =========================================================

    @Scheduled(
            fixedDelayString =
                    "${fourthdown.sync.coordinator-ms:300000}",
            initialDelayString =
                    "${fourthdown.sync.initial-delay-ms:30000}"
    )
    public void coordinateRefresh() {

        if (
                !syncRunning.compareAndSet(
                        false,
                        true
                )
        ) {
            return;
        }

        try {

            int season =
                    getActiveSeason();

            LocalDateTime now =
                    LocalDateTime.now();

            List<Game> seasonGames =
                    gameRepository
                            .findAll()
                            .stream()
                            .filter(game ->
                                    Objects.equals(
                                            game.getSeason(),
                                            season
                                    )
                            )
                            .toList();

            boolean gameWindowActive =
                    seasonGames
                            .stream()
                            .anyMatch(game ->
                                    isGameInRefreshWindow(
                                            game,
                                            now
                                    )
                            );

            boolean idleRefreshDue =
                    lastScheduleRefresh == null ||
                            lastScheduleRefresh
                                    .plusMinutes(
                                            IDLE_SCHEDULE_REFRESH_MINUTES
                                    )
                                    .isBefore(now);

            if (
                    !gameWindowActive &&
                            !idleRefreshDue
            ) {

                return;
            }

            System.out.println(
                    "[AUTO-SYNC] Refreshing " +
                            season +
                            " schedule. gameWindowActive=" +
                            gameWindowActive
            );

            refreshSeasonSchedule(
                    season
            );

            lastScheduleRefresh =
                    now;

            /*
             * Reload games after CFBD updates statuses/scores.
             */
            List<Game> refreshedGames =
                    gameRepository
                            .findAll()
                            .stream()
                            .filter(game ->
                                    Objects.equals(
                                            game.getSeason(),
                                            season
                                    )
                            )
                            .toList();

            refreshRelevantWeekStats(
                    season,
                    refreshedGames,
                    now
            );

            System.out.println(
                    "[AUTO-SYNC] Schedule refresh complete."
            );

        } catch (Exception exception) {

            System.err.println(
                    "[AUTO-SYNC] Coordinator failed: " +
                            exception.getMessage()
            );

            exception.printStackTrace();

        } finally {

            syncRunning.set(false);
        }
    }

    // =========================================================
    // DAILY TEAM METADATA
    //
    // Logos, conference, mascot, colors, location, etc.
    // This is intentionally separate from game-day polling.
    // =========================================================

    @Scheduled(
            fixedDelayString =
                    "${fourthdown.sync.teams-ms:86400000}",
            initialDelayString =
                    "${fourthdown.sync.teams-initial-delay-ms:180000}"
    )
    public void refreshTeamMetadata() {

        if (
                !syncRunning.compareAndSet(
                        false,
                        true
                )
        ) {
            return;
        }

        try {

            int season =
                    getActiveSeason();

            System.out.println(
                    "[AUTO-SYNC] Refreshing team metadata for " +
                            season
            );

            dataImportService.importTeamMetadata(
                    season,
                    "fbs"
            );

            dataImportService.importTeamMetadata(
                    season,
                    "fcs"
            );

            System.out.println(
                    "[AUTO-SYNC] Team metadata refresh complete."
            );

        } catch (Exception exception) {

            System.err.println(
                    "[AUTO-SYNC] Team metadata refresh failed: " +
                            exception.getMessage()
            );

            exception.printStackTrace();

        } finally {

            syncRunning.set(false);
        }
    }

    // =========================================================
    // REFRESH CURRENT SEASON SCHEDULE
    // =========================================================

    private void refreshSeasonSchedule(
            int season
    ) {

        /*
         * importSeason is idempotent in FourthDown AI:
         * existing CFBD games are updated instead of duplicated.
         *
         * This refreshes:
         * - kickoff/date
         * - week
         * - venue
         * - schedule additions
         * - SCHEDULED -> COMPLETED
         * - home/away score
         * - postseason names/rounds
         * - opponent classification repairs
         */
        dataImportService.importSeason(
                season,
                "fbs"
        );

        dataImportService.importSeason(
                season,
                "fcs"
        );
    }

    // =========================================================
    // REFRESH RELEVANT WEEK STATS
    //
    // Once at least one game in an active game window is completed,
    // refresh that exact week instead of importing all 16 weeks.
    // =========================================================

    private void refreshRelevantWeekStats(
            int season,
            List<Game> games,
            LocalDateTime now
    ) {

        /*
         * Look for ANY completed game in the active season that is
         * missing one or both TeamGameStats rows.
         *
         * This is intentionally not limited to the current kickoff
         * window. It lets FourthDown AI repair older games that have
         * a final score/status but whose detailed stats were not
         * imported yet.
         */
        Map<WeekKey, Boolean>
                weeksMissingStats =
                new HashMap<>();

        for (Game game : games) {

            if (
                    game.getWeek() == null ||
                            game.getId() == null ||
                            !"COMPLETED".equalsIgnoreCase(
                                    game.getStatus()
                            ) ||
                            game.getHomeTeam() == null ||
                            game.getAwayTeam() == null
            ) {
                continue;
            }

            boolean homeStatsPresent =
                    teamGameStatsRepository
                            .findByGameIdAndTeamId(
                                    game.getId(),
                                    game.getHomeTeam().getId()
                            )
                            .isPresent();

            boolean awayStatsPresent =
                    teamGameStatsRepository
                            .findByGameIdAndTeamId(
                                    game.getId(),
                                    game.getAwayTeam().getId()
                            )
                            .isPresent();

            if (
                    homeStatsPresent &&
                            awayStatsPresent
            ) {
                continue;
            }

            String seasonType =
                    normalizeSeasonType(
                            game.getSeasonType()
                    );

            weeksMissingStats.put(
                    new WeekKey(
                            game.getWeek(),
                            seasonType
                    ),
                    true
            );
        }

        for (
                WeekKey weekKey :
                weeksMissingStats.keySet()
        ) {

            LocalDateTime lastRefresh =
                    lastWeekStatsRefresh.get(
                            weekKey
                    );

            boolean refreshDue =
                    lastRefresh == null ||
                            lastRefresh
                                    .plusMinutes(
                                            WEEK_STATS_REFRESH_MINUTES
                                    )
                                    .isBefore(now);

            if (!refreshDue) {
                continue;
            }

            System.out.println(
                    "[AUTO-SYNC] Missing stats detected. Refreshing " +
                            season +
                            " " +
                            weekKey.seasonType() +
                            " week " +
                            weekKey.week()
            );

            dataImportService.importWeekStats(
                    season,
                    weekKey.week(),
                    "fbs",
                    weekKey.seasonType()
            );

            dataImportService.importWeekStats(
                    season,
                    weekKey.week(),
                    "fcs",
                    weekKey.seasonType()
            );

            lastWeekStatsRefresh.put(
                    weekKey,
                    now
            );
        }
    }

    // =========================================================
    // GAME WINDOW
    //
    // A game drives aggressive syncing from two hours before
    // kickoff through six hours after kickoff.
    //
    // Completed games are still considered during the window so
    // team stats can be pulled shortly after the final whistle.
    // =========================================================

    private boolean isGameInRefreshWindow(
            Game game,
            LocalDateTime now
    ) {

        if (
                game == null ||
                        game.getGameDate() == null
        ) {
            return false;
        }

        LocalDateTime windowStart =
                game.getGameDate()
                        .minusHours(
                                BEFORE_KICKOFF_HOURS
                        );

        LocalDateTime windowEnd =
                game.getGameDate()
                        .plusHours(
                                AFTER_KICKOFF_HOURS
                        );

        return !now.isBefore(
                windowStart
        ) &&
                !now.isAfter(
                        windowEnd
                );
    }

    // =========================================================
    // ACTIVE COLLEGE FOOTBALL SEASON
    //
    // January-June can still contain postseason data belonging
    // to the prior season.
    // =========================================================

    private int getActiveSeason() {

        LocalDate today =
                LocalDate.now();

        int year =
                today.getYear();

        int month =
                today.getMonthValue();

        return month <= 6
                ? year - 1
                : year;
    }

    private String normalizeSeasonType(
            String seasonType
    ) {

        if (
                seasonType != null &&
                        seasonType
                                .trim()
                                .equalsIgnoreCase(
                                        "postseason"
                                )
        ) {

            return "postseason";
        }

        return "regular";
    }

    private record WeekKey(
            int week,
            String seasonType
    ) {
    }
}
