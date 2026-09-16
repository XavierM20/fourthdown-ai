package com.fourthdown.ai.service;

import com.fourthdown.ai.client.CollegeFootballDataClient;
import com.fourthdown.ai.dto.cfbd.CfbdGameResponse;
import com.fourthdown.ai.dto.cfbd.CfbdTeamGameStatsResponse;
import com.fourthdown.ai.dto.cfbd.CfbdTeamResponse;
import com.fourthdown.ai.model.Game;
import com.fourthdown.ai.model.Team;
import com.fourthdown.ai.model.TeamGameStats;
import com.fourthdown.ai.repository.GameRepository;
import com.fourthdown.ai.repository.TeamGameStatsRepository;
import com.fourthdown.ai.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DataImportService {

    private final CollegeFootballDataClient cfbdClient;
    private final TeamRepository teamRepository;
    private final GameRepository gameRepository;
    private final TeamGameStatsRepository teamGameStatsRepository;

    public DataImportService(
            CollegeFootballDataClient cfbdClient,
            TeamRepository teamRepository,
            GameRepository gameRepository,
            TeamGameStatsRepository teamGameStatsRepository
    ) {
        this.cfbdClient = cfbdClient;
        this.teamRepository = teamRepository;
        this.gameRepository = gameRepository;
        this.teamGameStatsRepository = teamGameStatsRepository;
    }

    // =========================================================
    // TEAM METADATA IMPORT
    // Supports FBS and FCS
    // =========================================================

    public TeamMetadataImportResult importTeamMetadata(
            int year,
            String classification
    ) {

        String normalizedClassification =
                classification.toLowerCase();

        List<CfbdTeamResponse> cfbdTeams =
                cfbdClient.getTeams(year)
                        .stream()
                        .filter(team ->
                                team.getClassification() != null &&
                                        team.getClassification()
                                                .equalsIgnoreCase(
                                                        normalizedClassification
                                                )
                        )
                        .toList();

        int teamsCreated = 0;
        int teamsUpdated = 0;

        for (CfbdTeamResponse cfbdTeam : cfbdTeams) {

            Team team = teamRepository
                    .findByCfbdId(
                            cfbdTeam.getId()
                    )
                    .orElse(null);

            if (team == null) {
                team = teamRepository
                        .findByNameIgnoreCase(
                                cfbdTeam.getSchool()
                        )
                        .orElse(null);
            }

            boolean isNew = team == null;

            if (isNew) {
                team = new Team();
            }

            team.setCfbdId(
                    cfbdTeam.getId()
            );

            team.setName(
                    cfbdTeam.getSchool()
            );

            team.setAbbreviation(
                    cfbdTeam.getAbbreviation()
            );

            team.setConference(
                    cfbdTeam.getConference() != null
                            ? cfbdTeam.getConference()
                            : "Independent"
            );

            team.setClassification(
                    cfbdTeam.getClassification()
            );

            team.setMascot(
                    cfbdTeam.getMascot()
            );

            team.setPrimaryColor(
                    cfbdTeam.getColor()
            );

            team.setAlternateColor(
                    cfbdTeam.getAlternateColor()
            );

            if (cfbdTeam.getLogos() != null &&
                    !cfbdTeam.getLogos().isEmpty()) {

                team.setLogoUrl(
                        cfbdTeam.getLogos().get(0)
                );
            }

            if (cfbdTeam.getLocation() != null) {

                team.setCity(
                        cfbdTeam.getLocation().getCity()
                );

                team.setState(
                        cfbdTeam.getLocation().getState()
                );
            }

            teamRepository.save(team);

            if (isNew) {
                teamsCreated++;
            } else {
                teamsUpdated++;
            }
        }

        return new TeamMetadataImportResult(
                year,
                normalizedClassification.toUpperCase(),
                cfbdTeams.size(),
                teamsCreated,
                teamsUpdated
        );
    }

    // =========================================================
    // SEASON GAME IMPORT
    // Supports FBS and FCS
    // =========================================================

    public ImportResult importSeason(
            int year,
            String classification
    ) {

        String normalizedClassification =
                classification.toLowerCase();

        List<CfbdGameResponse> cfbdGames =
                cfbdClient.getGamesByClassification(
                        year,
                        normalizedClassification
                );

        int gamesCreated = 0;
        int gamesUpdated = 0;
        int teamsCreated = 0;

        for (CfbdGameResponse cfbdGame : cfbdGames) {

            if (cfbdGame.getHomeId() == null ||
                    cfbdGame.getAwayId() == null ||
                    cfbdGame.getStartDate() == null) {

                continue;
            }

            TeamResult homeResult =
                    findOrCreateTeam(
                            cfbdGame.getHomeId(),
                            cfbdGame.getHomeTeam(),
                            cfbdGame.getHomeConference(),
                            normalizedClassification
                    );

            TeamResult awayResult =
                    findOrCreateTeam(
                            cfbdGame.getAwayId(),
                            cfbdGame.getAwayTeam(),
                            cfbdGame.getAwayConference(),
                            normalizedClassification
                    );

            Team homeTeam =
                    homeResult.team();

            Team awayTeam =
                    awayResult.team();

            if (homeResult.created()) {
                teamsCreated++;
            }

            if (awayResult.created()) {
                teamsCreated++;
            }

            LocalDateTime gameDate =
                    cfbdGame.getStartDate()
                            .toLocalDateTime();

            Game game = gameRepository
                    .findByCfbdId(
                            cfbdGame.getId()
                    )
                    .orElse(null);

            boolean existingGame =
                    game != null;

            /*
             * This catches manually inserted games
             * that existed before CFBD IDs were added.
             */
            if (game == null) {

                game = gameRepository
                        .findByHomeTeamIdAndAwayTeamIdAndGameDate(
                                homeTeam.getId(),
                                awayTeam.getId(),
                                gameDate
                        )
                        .orElse(null);

                existingGame =
                        game != null;
            }

            if (game == null) {
                game = new Game();
            }

            // -------------------------------------------------
            // CFBD IDENTIFIER
            // -------------------------------------------------

            game.setCfbdId(
                    cfbdGame.getId()
            );

            // -------------------------------------------------
            // TEAMS
            // -------------------------------------------------

            game.setHomeTeam(
                    homeTeam
            );

            game.setAwayTeam(
                    awayTeam
            );

            // -------------------------------------------------
            // DATE
            // -------------------------------------------------

            game.setGameDate(
                    gameDate
            );

            // -------------------------------------------------
            // HISTORICAL / ML DATA
            //
            // These fields allow us to organize games by
            // season and week when creating ML training data.
            // -------------------------------------------------

            game.setSeason(
                    cfbdGame.getSeason()
            );

            game.setWeek(
                    cfbdGame.getWeek()
            );

            game.setSeasonType(
                    cfbdGame.getSeasonType()
            );


            // -------------------------------------------------
// GAME / BOWL / PLAYOFF NAME
// -------------------------------------------------

            String gameName =
                    cfbdGame.getNotes();

            String playoffRound =
                    null;

            if (
                    cfbdGame.getPlayoff() !=
                            null
            ) {

                CfbdGameResponse.Playoff playoff =
                        cfbdGame.getPlayoff();

                /*
                 * Prefer CFBD's bowl name when available.
                 *
                 * Examples:
                 * Rose Bowl
                 * Sugar Bowl
                 * Cotton Bowl
                 */
                if (
                        playoff.getBowlName() !=
                                null &&
                                !playoff.getBowlName()
                                        .isBlank()
                ) {

                    gameName =
                            playoff.getBowlName();
                }

                /*
                 * Keep the playoff round separately so the
                 * frontend can show:
                 *
                 * Rose Bowl
                 * CFP Quarterfinal
                 */
                if (
                        playoff.getRoundName() !=
                                null &&
                                !playoff.getRoundName()
                                        .isBlank()
                ) {

                    playoffRound =
                            playoff.getRoundName();

                } else if (
                        playoff.getRound() !=
                                null &&
                                !playoff.getRound()
                                        .isBlank()
                ) {

                    playoffRound =
                            formatPlayoffRound(
                                    playoff.getRound()
                            );
                }
            }

            game.setGameName(
                    gameName
            );

            game.setPlayoffRound(
                    playoffRound
            );

            // -------------------------------------------------
            // STATUS
            // -------------------------------------------------

            game.setStatus(
                    Boolean.TRUE.equals(
                            cfbdGame.getCompleted()
                    )
                            ? "COMPLETED"
                            : "SCHEDULED"
            );

            // -------------------------------------------------
            // SCORE
            // -------------------------------------------------

            game.setHomeScore(
                    cfbdGame.getHomePoints()
            );

            game.setAwayScore(
                    cfbdGame.getAwayPoints()
            );

            // -------------------------------------------------
            // VENUE
            // -------------------------------------------------

            game.setVenue(
                    cfbdGame.getVenue()
            );

            gameRepository.save(game);

            if (existingGame) {
                gamesUpdated++;
            } else {
                gamesCreated++;
            }
        }

        return new ImportResult(
                year,
                normalizedClassification.toUpperCase(),
                cfbdGames.size(),
                teamsCreated,
                gamesCreated,
                gamesUpdated
        );
    }

    // =========================================================
    // SINGLE TEAM STATS IMPORT
    // =========================================================

    public StatsImportResult importTeamStats(
            int year,
            String teamName
    ) {

        List<CfbdTeamGameStatsResponse> games =
                cfbdClient.getTeamGameStats(
                        year,
                        teamName
                );

        int statsCreated = 0;
        int statsUpdated = 0;
        int gamesSkipped = 0;

        for (CfbdTeamGameStatsResponse cfbdGame
                : games) {

            Game game = gameRepository
                    .findByCfbdId(
                            cfbdGame.getId()
                    )
                    .orElse(null);

            if (game == null) {
                gamesSkipped++;
                continue;
            }

            if (cfbdGame.getTeams() == null) {
                continue;
            }

            for (CfbdTeamGameStatsResponse.TeamStats cfbdTeam
                    : cfbdGame.getTeams()) {

                Team team = teamRepository
                        .findByCfbdId(
                                cfbdTeam.getTeamId()
                        )
                        .orElse(null);

                if (team == null) {
                    continue;
                }

                TeamGameStats existing =
                        teamGameStatsRepository
                                .findByGameIdAndTeamId(
                                        game.getId(),
                                        team.getId()
                                )
                                .orElse(null);

                boolean updating =
                        existing != null;

                TeamGameStats stats =
                        updating
                                ? existing
                                : new TeamGameStats();

                stats.setGame(game);
                stats.setTeam(team);

                stats.setPoints(
                        cfbdTeam.getPoints()
                );

                if (cfbdTeam.getStats() != null) {

                    for (CfbdTeamGameStatsResponse.Stat stat
                            : cfbdTeam.getStats()) {

                        applyStat(
                                stats,
                                stat.getCategory(),
                                stat.getStat()
                        );
                    }
                }

                teamGameStatsRepository.save(
                        stats
                );

                if (updating) {
                    statsUpdated++;
                } else {
                    statsCreated++;
                }
            }
        }

        return new StatsImportResult(
                year,
                teamName,
                games.size(),
                statsCreated,
                statsUpdated,
                gamesSkipped
        );
    }

    // =========================================================
    // SEASON TEAM STATS IMPORT
    // Supports FBS and FCS
    //
    // Imports:
    // - regular-season Weeks 1 through 16
    // - every postseason game
    //
    // Postseason covers:
    // - FBS conference championships / bowls / CFP
    // - FCS playoff games
    //
    // Re-running is safe because TeamGameStats is matched by
    // game ID + team ID and updated instead of duplicated.
    // =========================================================

    public SeasonStatsImportResult importSeasonStats(
            int year,
            String classification
    ) {

        String normalizedClassification =
                classification.toLowerCase();

        int totalGamesReceived = 0;
        int totalStatsCreated = 0;
        int totalStatsUpdated = 0;
        int totalGamesSkipped = 0;

        // ---------------------------------------------------------
        // REGULAR SEASON
        //
        // Some seasons include Week 16, so do not stop at 15.
        // ---------------------------------------------------------

        for (int week = 1; week <= 16; week++) {

            List<CfbdTeamGameStatsResponse> games =
                    cfbdClient.getTeamGameStats(
                            year,
                            week,
                            normalizedClassification,
                            "regular"
                    );

            StatsBatchResult result =
                    importStatsGames(
                            games
                    );

            totalGamesReceived +=
                    result.gamesReceived();

            totalStatsCreated +=
                    result.statsCreated();

            totalStatsUpdated +=
                    result.statsUpdated();

            totalGamesSkipped +=
                    result.gamesSkipped();
        }

        // ---------------------------------------------------------
        // POSTSEASON
        //
        // CFBD requires week, team, or conference for /games/teams
        // when filtering by year. We first load the postseason
        // schedule, discover the actual postseason week numbers, and
        // then request team stats for each of those weeks.
        // ---------------------------------------------------------

        List<CfbdGameResponse> postseasonSchedule =
                cfbdClient.getGamesByClassification(
                        year,
                        normalizedClassification,
                        "postseason"
                );

        List<Integer> postseasonWeeks =
                postseasonSchedule
                        .stream()
                        .map(
                                CfbdGameResponse::getWeek
                        )
                        .filter(
                                java.util.Objects::nonNull
                        )
                        .distinct()
                        .sorted()
                        .toList();

        for (Integer postseasonWeek :
                postseasonWeeks) {

            List<CfbdTeamGameStatsResponse> postseasonGames =
                    cfbdClient.getTeamGameStats(
                            year,
                            postseasonWeek,
                            normalizedClassification,
                            "postseason"
                    );

            StatsBatchResult postseasonResult =
                    importStatsGames(
                            postseasonGames
                    );

            totalGamesReceived +=
                    postseasonResult.gamesReceived();

            totalStatsCreated +=
                    postseasonResult.statsCreated();

            totalStatsUpdated +=
                    postseasonResult.statsUpdated();

            totalGamesSkipped +=
                    postseasonResult.gamesSkipped();
        }

        return new SeasonStatsImportResult(
                year,
                normalizedClassification.toUpperCase(),
                totalGamesReceived,
                totalStatsCreated,
                totalStatsUpdated,
                totalGamesSkipped
        );
    }

    // =========================================================
    // IMPORT ONE BATCH OF TEAM GAME STATS
    // =========================================================

    private StatsBatchResult importStatsGames(
            List<CfbdTeamGameStatsResponse> games
    ) {

        if (games == null ||
                games.isEmpty()) {

            return new StatsBatchResult(
                    0,
                    0,
                    0,
                    0
            );
        }

        int statsCreated = 0;
        int statsUpdated = 0;
        int gamesSkipped = 0;

        for (CfbdTeamGameStatsResponse cfbdGame
                : games) {

            Game game = gameRepository
                    .findByCfbdId(
                            cfbdGame.getId()
                    )
                    .orElse(null);

            if (game == null) {
                gamesSkipped++;
                continue;
            }

            if (cfbdGame.getTeams() == null) {
                continue;
            }

            for (CfbdTeamGameStatsResponse.TeamStats cfbdTeam
                    : cfbdGame.getTeams()) {

                Team team = teamRepository
                        .findByCfbdId(
                                cfbdTeam.getTeamId()
                        )
                        .orElse(null);

                if (team == null) {
                    continue;
                }

                TeamGameStats existing =
                        teamGameStatsRepository
                                .findByGameIdAndTeamId(
                                        game.getId(),
                                        team.getId()
                                )
                                .orElse(null);

                boolean updating =
                        existing != null;

                TeamGameStats stats =
                        updating
                                ? existing
                                : new TeamGameStats();

                stats.setGame(
                        game
                );

                stats.setTeam(
                        team
                );

                stats.setPoints(
                        cfbdTeam.getPoints()
                );

                if (cfbdTeam.getStats() != null) {

                    for (CfbdTeamGameStatsResponse.Stat stat
                            : cfbdTeam.getStats()) {

                        applyStat(
                                stats,
                                stat.getCategory(),
                                stat.getStat()
                        );
                    }
                }

                teamGameStatsRepository.save(
                        stats
                );

                if (updating) {
                    statsUpdated++;
                } else {
                    statsCreated++;
                }
            }
        }

        return new StatsBatchResult(
                games.size(),
                statsCreated,
                statsUpdated,
                gamesSkipped
        );
    }

    // =========================================================
    // FIND OR CREATE TEAM
    // =========================================================

    private TeamResult findOrCreateTeam(
            Long cfbdId,
            String name,
            String conference,
            String classification
    ) {

        Team existing = teamRepository
                .findByCfbdId(
                        cfbdId
                )
                .orElse(null);

        if (existing != null) {

            if (name != null) {
                existing.setName(name);
            }

            if (conference != null) {
                existing.setConference(
                        conference
                );
            }

            /*
             * Do not blindly overwrite classification.
             *
             * Cross-classification games can exist,
             * such as an FBS team playing an FCS team.
             */
            if (existing.getClassification() == null) {
                existing.setClassification(
                        classification
                );
            }

            teamRepository.save(
                    existing
            );

            return new TeamResult(
                    existing,
                    false
            );
        }

        existing = teamRepository
                .findByNameIgnoreCase(
                        name
                )
                .orElse(null);

        if (existing != null) {

            existing.setCfbdId(
                    cfbdId
            );

            if (conference != null) {
                existing.setConference(
                        conference
                );
            }

            if (existing.getClassification() == null) {
                existing.setClassification(
                        classification
                );
            }

            teamRepository.save(
                    existing
            );

            return new TeamResult(
                    existing,
                    false
            );
        }

        Team team = new Team();

        team.setCfbdId(
                cfbdId
        );

        team.setName(
                name
        );

        team.setConference(
                conference != null
                        ? conference
                        : "Unknown"
        );

        team.setClassification(
                classification
        );

        Team savedTeam =
                teamRepository.save(
                        team
                );

        return new TeamResult(
                savedTeam,
                true
        );
    }

    // =========================================================
    // CFBD STAT MAPPING
    // =========================================================

    private void applyStat(
            TeamGameStats stats,
            String category,
            String value
    ) {

        if (category == null ||
                value == null) {
            return;
        }

        switch (category) {

            case "totalYards" ->
                    stats.setTotalYards(
                            parseInteger(value)
                    );

            case "netPassingYards" ->
                    stats.setPassingYards(
                            parseInteger(value)
                    );

            case "rushingYards" ->
                    stats.setRushingYards(
                            parseInteger(value)
                    );

            case "turnovers" ->
                    stats.setTurnovers(
                            parseInteger(value)
                    );

            case "firstDowns" ->
                    stats.setFirstDowns(
                            parseInteger(value)
                    );

            case "thirdDownEff" ->
                    applyThirdDownEfficiency(
                            stats,
                            value
                    );

            case "redZoneEff" ->
                    applyRedZoneEfficiency(
                            stats,
                            value
                    );

            case "totalPenaltiesYards" ->
                    applyPenalties(
                            stats,
                            value
                    );

            default -> {
                /*
                 * Ignore CFBD statistics that FourthDown AI
                 * does not currently store.
                 */
            }
        }
    }

    private Integer parseInteger(
            String value
    ) {

        if (value == null ||
                value.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(
                    value.trim()
            );
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    // =========================================================
    // THIRD DOWN
    // =========================================================

    private void applyThirdDownEfficiency(
            TeamGameStats stats,
            String value
    ) {

        String[] parts =
                value.split("-");

        if (parts.length != 2) {
            return;
        }

        stats.setThirdDownConversions(
                parseInteger(
                        parts[0]
                )
        );

        stats.setThirdDownAttempts(
                parseInteger(
                        parts[1]
                )
        );
    }

    // =========================================================
    // RED ZONE
    // =========================================================

    private void applyRedZoneEfficiency(
            TeamGameStats stats,
            String value
    ) {

        String[] parts =
                value.split("-");

        if (parts.length != 2) {
            return;
        }

        stats.setRedZoneScores(
                parseInteger(
                        parts[0]
                )
        );

        stats.setRedZoneAttempts(
                parseInteger(
                        parts[1]
                )
        );
    }

    // =========================================================
    // PENALTIES
    // =========================================================

    private void applyPenalties(
            TeamGameStats stats,
            String value
    ) {

        String[] parts =
                value.split("-");

        if (parts.length != 2) {
            return;
        }

        stats.setPenalties(
                parseInteger(
                        parts[0]
                )
        );

        stats.setPenaltyYards(
                parseInteger(
                        parts[1]
                )
        );
    }

    // =========================================================
    // INTERNAL TEAM RESULT
    // =========================================================

    private record TeamResult(
            Team team,
            boolean created
    ) {
    }

    private record StatsBatchResult(
            int gamesReceived,
            int statsCreated,
            int statsUpdated,
            int gamesSkipped
    ) {
    }

    // =========================================================
    // API RESPONSE RECORDS
    // =========================================================

    public record TeamMetadataImportResult(
            int year,
            String classification,
            int teamsReceived,
            int teamsCreated,
            int teamsUpdated
    ) {
    }

    public record ImportResult(
            int year,
            String classification,
            int gamesReceived,
            int teamsCreated,
            int gamesCreated,
            int gamesUpdated
    ) {
    }

    public record StatsImportResult(
            int year,
            String team,
            int gamesReceived,
            int statsCreated,
            int statsUpdated,
            int gamesSkipped
    ) {
    }

    public record SeasonStatsImportResult(
            int year,
            String classification,
            int gamesReceived,
            int statsCreated,
            int statsUpdated,
            int gamesSkipped
    ) {
    }

    private String formatPlayoffRound(
            String round
    ) {

        if (
                round == null ||
                        round.isBlank()
        ) {
            return null;
        }

        return switch (
                round.toLowerCase()
                ) {

            case "first_round" ->
                    "CFP First Round";

            case "quarterfinal" ->
                    "CFP Quarterfinal";

            case "semifinal" ->
                    "CFP Semifinal";

            case "championship" ->
                    "CFP National Championship";

            default ->
                    round;
        };
    }
}