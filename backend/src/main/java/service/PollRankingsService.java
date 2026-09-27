package com.fourthdown.ai.service;

import com.fourthdown.ai.client.CollegeFootballDataClient;
import com.fourthdown.ai.dto.PollRankingsResponse;
import com.fourthdown.ai.dto.PollTeamRankingResponse;
import com.fourthdown.ai.dto.RankingHistoryPointResponse;
import com.fourthdown.ai.dto.TeamRankingHistoryResponse;
import com.fourthdown.ai.dto.TeamStandingResponse;
import com.fourthdown.ai.dto.cfbd.CfbdRankingResponse;
import com.fourthdown.ai.model.Team;
import com.fourthdown.ai.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PollRankingsService {

    // =========================================================
    // CACHE SETTINGS
    // =========================================================

    /*
     * Rankings normally change once per week.
     *
     * A six-hour cache prevents unnecessary CFBD calls while
     * still allowing new rankings to appear in the app on the
     * same day they are published.
     */
    private static final Duration CACHE_DURATION =
            Duration.ofHours(6);

    private final CollegeFootballDataClient cfbdClient;

    private final TeamRepository teamRepository;

    private final StandingsService standingsService;

    /*
     * Cache key:
     *
     * season:classification
     *
     * Examples:
     *
     * 2026:fbs
     * 2026:fcs
     */
    private final Map<String, CachedRanking> cache =
            new ConcurrentHashMap<>();

    private final Map<String, CachedRankingHistory> historyCache =
            new ConcurrentHashMap<>();

    public PollRankingsService(
            CollegeFootballDataClient cfbdClient,
            TeamRepository teamRepository,
            StandingsService standingsService
    ) {

        this.cfbdClient =
                cfbdClient;

        this.teamRepository =
                teamRepository;

        this.standingsService =
                standingsService;
    }

    // =========================================================
    // GET TOP 25
    // =========================================================

    public PollRankingsResponse getRankings(
            int season,
            String classification,
            Integer week
    ) {

        String normalizedClassification =
                classification
                        .trim()
                        .toLowerCase();

        validateClassification(
                normalizedClassification
        );

        String cacheKey =
                buildCacheKey(
                        season,
                        normalizedClassification,
                        week
                );

        CachedRanking cached =
                cache.get(
                        cacheKey
                );

        /*
         * Return cached rankings if they are still fresh.
         */
        if (
                cached != null &&
                        !isExpired(
                                cached
                        )
        ) {

            return cached.response();
        }

        /*
         * Request either the selected week or, when week is null,
         * the newest available poll for the season.
         */
        PollRankingsResponse response =
                loadRankings(
                        season,
                        normalizedClassification,
                        week
                );

        cache.put(
                cacheKey,
                new CachedRanking(
                        response,
                        Instant.now()
                )
        );

        return response;
    }

    // =========================================================
    // GET AVAILABLE POLL WEEKS
    // =========================================================

    public List<Integer> getAvailableWeeks(
            int season,
            String classification
    ) {

        String normalizedClassification =
                classification
                        .trim()
                        .toLowerCase();

        validateClassification(
                normalizedClassification
        );

        List<CfbdRankingResponse> weeks =
                cfbdClient.getRankings(
                        season
                );

        if (
                weeks == null ||
                        weeks.isEmpty()
        ) {

            return List.of();
        }

        return weeks.stream()
                .filter(week ->
                        week != null &&
                                week.getWeek() != null &&
                                week.getPolls() != null &&
                                selectPollForWeek(
                                        week,
                                        normalizedClassification
                                ) != null
                )
                .map(
                        CfbdRankingResponse::getWeek
                )
                .distinct()
                .sorted()
                .toList();
    }

    // =========================================================
    // GET TEAM RANKING HISTORY
    // =========================================================

    public TeamRankingHistoryResponse getRankingHistory(
            int season,
            String classification,
            Long teamId
    ) {

        String normalizedClassification =
                classification
                        .trim()
                        .toLowerCase();

        validateClassification(
                normalizedClassification
        );

        if (teamId == null) {
            throw new IllegalArgumentException(
                    "teamId is required"
            );
        }

        Team team =
                teamRepository
                        .findById(
                                teamId
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Team not found"
                                        )
                        );

        String cacheKey =
                buildHistoryCacheKey(
                        season,
                        normalizedClassification,
                        teamId
                );

        CachedRankingHistory cached =
                historyCache.get(
                        cacheKey
                );

        if (
                cached != null &&
                        !isHistoryExpired(
                                cached
                        )
        ) {

            return cached.response();
        }

        TeamRankingHistoryResponse response =
                loadRankingHistory(
                        season,
                        normalizedClassification,
                        team
                );

        historyCache.put(
                cacheKey,
                new CachedRankingHistory(
                        response,
                        Instant.now()
                )
        );

        return response;
    }


    private TeamRankingHistoryResponse loadRankingHistory(
            int season,
            String classification,
            Team team
    ) {

        List<CfbdRankingResponse> weeks =
                cfbdClient.getRankings(
                        season
                );

        if (
                weeks == null ||
                        weeks.isEmpty()
        ) {

            return emptyHistoryResponse(
                    season,
                    classification,
                    team
            );
        }

        List<RankingHistoryPointResponse> history =
                new ArrayList<>();

        weeks.stream()
                .filter(week ->
                        week != null &&
                                week.getWeek() != null &&
                                week.getPolls() != null
                )
                .sorted(
                        Comparator.comparingInt(
                                CfbdRankingResponse::getWeek
                        )
                )
                .forEach(week -> {

                    CfbdRankingResponse.Poll selectedPoll =
                            selectPollForWeek(
                                    week,
                                    classification
                            );

                    if (selectedPoll == null) {
                        return;
                    }

                    CfbdRankingResponse.Rank teamRank =
                            findTeamRank(
                                    selectedPoll,
                                    team
                            );

                    history.add(
                            new RankingHistoryPointResponse(
                                    week.getWeek(),
                                    selectedPoll.getPoll(),
                                    teamRank == null
                                            ? null
                                            : teamRank.getRank(),
                                    teamRank == null
                                            ? null
                                            : teamRank.getPoints(),
                                    teamRank == null
                                            ? null
                                            : teamRank.getFirstPlaceVotes()
                            )
                    );
                });

        Integer currentRank =
                history.isEmpty()
                        ? null
                        : history.get(
                        history.size() - 1
                ).rank();

        Integer previousRank =
                history.size() < 2
                        ? null
                        : history.get(
                        history.size() - 2
                ).rank();

        Integer movement =
                calculateMovement(
                        currentRank,
                        previousRank
                );

        return new TeamRankingHistoryResponse(
                season,
                classification,
                team.getId(),
                team.getName(),
                currentRank,
                previousRank,
                movement,
                history
        );
    }


    private CfbdRankingResponse.Poll selectPollForWeek(
            CfbdRankingResponse week,
            String classification
    ) {

        if (
                week == null ||
                        week.getPolls() == null
        ) {

            return null;
        }

        if (
                "fbs".equals(
                        classification
                )
        ) {

            CfbdRankingResponse.Poll cfp =
                    findPollInWeek(
                            week,
                            PollType.CFP
                    );

            if (cfp != null) {
                return cfp;
            }

            return findPollInWeek(
                    week,
                    PollType.AP
            );
        }

        return findPollInWeek(
                week,
                PollType.FCS_COACHES
        );
    }


    private CfbdRankingResponse.Poll findPollInWeek(
            CfbdRankingResponse week,
            PollType pollType
    ) {

        for (
                CfbdRankingResponse.Poll poll :
                week.getPolls()
        ) {

            if (
                    poll == null ||
                            poll.getPoll() == null ||
                            poll.getRanks() == null
            ) {

                continue;
            }

            if (
                    matchesPoll(
                            poll.getPoll(),
                            pollType
                    )
            ) {

                return poll;
            }
        }

        return null;
    }


    private CfbdRankingResponse.Rank findTeamRank(
            CfbdRankingResponse.Poll poll,
            Team team
    ) {

        if (
                poll == null ||
                        poll.getRanks() == null ||
                        team == null
        ) {

            return null;
        }

        for (
                CfbdRankingResponse.Rank rank :
                poll.getRanks()
        ) {

            if (rank == null) {
                continue;
            }

            if (
                    team.getCfbdId() != null &&
                            rank.getTeamId() != null &&
                            team.getCfbdId()
                                    .equals(
                                            rank.getTeamId()
                                    )
            ) {

                return rank;
            }

            if (
                    team.getName() != null &&
                            rank.getSchool() != null &&
                            team.getName()
                                    .equalsIgnoreCase(
                                            rank.getSchool()
                                    )
            ) {

                return rank;
            }
        }

        return null;
    }


    private Integer calculateMovement(
            Integer currentRank,
            Integer previousRank
    ) {

        if (
                currentRank == null ||
                        previousRank == null
        ) {

            return null;
        }

        return previousRank -
                currentRank;
    }


    // =========================================================
    // LOAD CURRENT RANKINGS FROM CFBD
    // =========================================================

    private PollRankingsResponse loadRankings(
            int season,
            String classification,
            Integer requestedWeek
    ) {

        List<CfbdRankingResponse> weeks =
                cfbdClient.getRankings(
                        season
                );

        if (
                weeks == null ||
                        weeks.isEmpty()
        ) {

            return emptyResponse(
                    season,
                    classification
            );
        }

        PollSnapshot snapshot =
                null;

        // =====================================================
        // SPECIFIC WEEK
        // =====================================================

        if (requestedWeek != null) {

            for (CfbdRankingResponse week : weeks) {

                if (
                        week == null ||
                                week.getWeek() == null ||
                                week.getWeek() != requestedWeek
                ) {
                    continue;
                }

                CfbdRankingResponse.Poll selectedPoll =
                        selectPollForWeek(
                                week,
                                classification
                        );

                if (
                        selectedPoll != null &&
                                selectedPoll.getRanks() != null &&
                                !selectedPoll.getRanks().isEmpty()
                ) {

                    snapshot =
                            new PollSnapshot(
                                    requestedWeek,
                                    selectedPoll
                            );

                    break;
                }
            }
        }

        // =====================================================
        // LATEST AVAILABLE WEEK
        // =====================================================

        else if (
                "fbs".equals(
                        classification
                )
        ) {

            /*
             * CFP becomes the primary official ranking
             * once the committee begins releasing rankings.
             */
            snapshot =
                    findLatestPoll(
                            weeks,
                            PollType.CFP
                    );

            /*
             * Before CFP rankings exist, use AP Top 25.
             */
            if (
                    snapshot == null
            ) {

                snapshot =
                        findLatestPoll(
                                weeks,
                                PollType.AP
                        );
            }

        } else {

            snapshot =
                    findLatestPoll(
                            weeks,
                            PollType.FCS_COACHES
                    );
        }

        if (
                snapshot == null
        ) {

            return emptyResponse(
                    season,
                    classification
            );
        }

        return buildResponse(
                season,
                classification,
                snapshot
        );
    }

    // =========================================================
    // BUILD RESPONSE
    // =========================================================

    private PollRankingsResponse buildResponse(
            int season,
            String classification,
            PollSnapshot snapshot
    ) {

        /*
         * The poll determines rank.
         *
         * StandingsService is used only to attach
         * the team's W-L record.
         */
        List<TeamStandingResponse> standings =
                standingsService
                        .getStandings(
                                season,
                                classification,
                                snapshot.week()
                        );

        Map<Long, TeamStandingResponse> standingByTeamId =
                new HashMap<>();

        for (
                TeamStandingResponse standing :
                standings
        ) {

            standingByTeamId.put(
                    standing.teamId(),
                    standing
            );
        }

        List<PollTeamRankingResponse> rankings =
                new ArrayList<>();

        List<CfbdRankingResponse.Rank> pollRanks =
                snapshot.poll()
                        .getRanks();

        if (
                pollRanks == null
        ) {

            pollRanks =
                    List.of();
        }

        /*
         * IMPORTANT:
         *
         * The API rank controls ordering.
         *
         * We do not re-sort teams based on our own
         * win percentage or scoring metrics.
         */
        pollRanks.stream()

                .filter(rank ->
                        rank != null &&
                                rank.getRank() != null &&
                                rank.getRank() >= 1 &&
                                rank.getRank() <= 25
                )

                .sorted(
                        Comparator.comparingInt(
                                CfbdRankingResponse.Rank::getRank
                        )
                )

                .forEach(rank -> {

                    Team team =
                            findTeam(
                                    rank
                            );

                    if (
                            team == null
                    ) {
                        return;
                    }

                    /*
                     * Prevent an FBS team from appearing
                     * in the FCS poll page or vice versa.
                     */
                    if (
                            team.getClassification() == null ||
                                    !team.getClassification()
                                            .equalsIgnoreCase(
                                                    classification
                                            )
                    ) {

                        return;
                    }

                    TeamStandingResponse standing =
                            standingByTeamId.get(
                                    team.getId()
                            );

                    int wins =
                            standing == null
                                    ? 0
                                    : standing.wins();

                    int losses =
                            standing == null
                                    ? 0
                                    : standing.losses();

                    int ties =
                            standing == null
                                    ? 0
                                    : standing.ties();

                    rankings.add(
                            new PollTeamRankingResponse(

                                    rank.getRank(),

                                    team.getId(),

                                    team.getName(),

                                    team.getAbbreviation(),

                                    team.getConference(),

                                    team.getClassification(),

                                    team.getLogoUrl(),

                                    wins,

                                    losses,

                                    ties,

                                    rank.getPoints(),

                                    rank.getFirstPlaceVotes()
                            )
                    );
                });

        return new PollRankingsResponse(

                season,

                snapshot.week(),

                snapshot.poll()
                        .getPoll(),

                classification,

                rankings
        );
    }

    // =========================================================
    // FIND TEAM
    // =========================================================

    private Team findTeam(
            CfbdRankingResponse.Rank rank
    ) {

        Team team =
                null;

        /*
         * CFBD ID is preferred because it is more
         * reliable than comparing school names.
         */
        if (
                rank.getTeamId() != null
        ) {

            team =
                    teamRepository
                            .findByCfbdId(
                                    rank.getTeamId()
                            )
                            .orElse(null);
        }

        /*
         * Fallback for historical data or naming differences.
         */
        if (
                team == null &&
                        rank.getSchool() != null
        ) {

            team =
                    teamRepository
                            .findByNameIgnoreCase(
                                    rank.getSchool()
                            )
                            .orElse(null);
        }

        return team;
    }

    // =========================================================
    // FIND NEWEST POLL
    // =========================================================

    private PollSnapshot findLatestPoll(
            List<CfbdRankingResponse> weeks,
            PollType pollType
    ) {

        PollSnapshot newest =
                null;

        for (
                CfbdRankingResponse week :
                weeks
        ) {

            if (
                    week == null ||
                            week.getWeek() == null ||
                            week.getPolls() == null
            ) {
                continue;
            }

            for (
                    CfbdRankingResponse.Poll poll :
                    week.getPolls()
            ) {

                if (
                        poll == null ||
                                poll.getPoll() == null ||
                                poll.getRanks() == null ||
                                poll.getRanks().isEmpty()
                ) {

                    continue;
                }

                if (
                        !matchesPoll(
                                poll.getPoll(),
                                pollType
                        )
                ) {

                    continue;
                }

                PollSnapshot candidate =
                        new PollSnapshot(
                                week.getWeek(),
                                poll
                        );

                /*
                 * Always keep the newest available
                 * weekly ranking.
                 */
                if (
                        newest == null ||
                                candidate.week() >
                                        newest.week()
                ) {

                    newest =
                            candidate;
                }
            }
        }

        return newest;
    }

    // =========================================================
    // POLL NAME MATCHING
    // =========================================================

    private boolean matchesPoll(
            String pollName,
            PollType pollType
    ) {

        String normalized =
                pollName
                        .trim()
                        .toLowerCase();

        return switch (
                pollType
                ) {

            case AP ->
                    normalized.equals(
                            "ap top 25"
                    ) ||
                            (
                                    normalized.contains(
                                            "ap"
                                    ) &&
                                            normalized.contains(
                                                    "top 25"
                                            )
                            );

            case CFP ->
                    normalized.contains(
                            "playoff committee"
                    ) ||
                            normalized.contains(
                                    "college football playoff"
                            ) ||
                            normalized.equals(
                                    "cfp"
                            );

            case FCS_COACHES ->
                    normalized.contains(
                            "fcs coaches"
                    );
        };
    }

    // =========================================================
    // CACHE
    // =========================================================

    private String buildCacheKey(
            int season,
            String classification,
            Integer week
    ) {

        return season +
                ":" +
                classification +
                ":" +
                (
                        week == null
                                ? "latest"
                                : week
                );
    }

    private boolean isExpired(
            CachedRanking cached
    ) {

        Instant expiresAt =
                cached.loadedAt()
                        .plus(
                                CACHE_DURATION
                        );

        return Instant.now()
                .isAfter(
                        expiresAt
                );
    }


    private String buildHistoryCacheKey(
            int season,
            String classification,
            Long teamId
    ) {

        return season +
                ":" +
                classification +
                ":" +
                teamId;
    }

    private boolean isHistoryExpired(
            CachedRankingHistory cached
    ) {

        Instant expiresAt =
                cached.loadedAt()
                        .plus(
                                CACHE_DURATION
                        );

        return Instant.now()
                .isAfter(
                        expiresAt
                );
    }

    // =========================================================
    // MANUAL CACHE CLEAR
    // =========================================================

    public void clearCache() {

        cache.clear();
        historyCache.clear();
    }

    public void clearCache(
            int season,
            String classification
    ) {

        String rankingPrefix =
                season +
                        ":" +
                        classification
                                .toLowerCase() +
                        ":";

        cache.keySet()
                .removeIf(
                        key ->
                                key.startsWith(
                                        rankingPrefix
                                )
                );

        String historyPrefix =
                season +
                        ":" +
                        classification
                                .toLowerCase() +
                        ":";

        historyCache.keySet()
                .removeIf(
                        key ->
                                key.startsWith(
                                        historyPrefix
                                )
                );
    }

    // =========================================================
    // EMPTY RESPONSE
    // =========================================================

    private PollRankingsResponse emptyResponse(
            int season,
            String classification
    ) {

        String poll =
                "fbs".equals(
                        classification
                )
                        ? "AP Top 25"
                        : "FCS Coaches Poll";

        return new PollRankingsResponse(

                season,

                0,

                poll,

                classification,

                List.of()
        );
    }

    // =========================================================
    // EMPTY HISTORY RESPONSE
    // =========================================================

    private TeamRankingHistoryResponse emptyHistoryResponse(
            int season,
            String classification,
            Team team
    ) {

        return new TeamRankingHistoryResponse(
                season,
                classification,
                team.getId(),
                team.getName(),
                null,
                null,
                null,
                List.of()
        );
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateClassification(
            String classification
    ) {

        if (
                !"fbs".equals(
                        classification
                ) &&
                        !"fcs".equals(
                                classification
                        )
        ) {

            throw new IllegalArgumentException(
                    "Classification must be fbs or fcs"
            );
        }
    }

    // =========================================================
    // TYPES
    // =========================================================

    private enum PollType {

        AP,

        CFP,

        FCS_COACHES
    }

    private record PollSnapshot(

            int week,

            CfbdRankingResponse.Poll poll

    ) {
    }

    private record CachedRanking(

            PollRankingsResponse response,

            Instant loadedAt

    ) {
    }

    private record CachedRankingHistory(

            TeamRankingHistoryResponse response,

            Instant loadedAt

    ) {
    }

}