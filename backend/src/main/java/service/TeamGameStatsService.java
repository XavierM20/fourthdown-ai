package com.fourthdown.ai.service;

import com.fourthdown.ai.model.Game;
import com.fourthdown.ai.model.Team;
import com.fourthdown.ai.model.TeamGameStats;
import com.fourthdown.ai.repository.GameRepository;
import com.fourthdown.ai.repository.TeamGameStatsRepository;
import com.fourthdown.ai.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamGameStatsService {

    private final TeamGameStatsRepository statsRepository;
    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;

    public TeamGameStatsService(
            TeamGameStatsRepository statsRepository,
            GameRepository gameRepository,
            TeamRepository teamRepository
    ) {
        this.statsRepository =
                statsRepository;

        this.gameRepository =
                gameRepository;

        this.teamRepository =
                teamRepository;
    }

    // =========================================================
    // GET ALL STATS
    // =========================================================

    public List<TeamGameStats> getAllStats() {

        return statsRepository.findAll();
    }

    
    public List<TeamGameStats> getAll() {

        return getAllStats();
    }

    // =========================================================
    // GET ONE STAT ENTRY
    // =========================================================

    public TeamGameStats getStatsById(
            Long id
    ) {

        return statsRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Team game stats not found"
                        )
                );
    }

    // =========================================================
// GET STATS FOR GAME
// =========================================================

    public List<TeamGameStats> getStatsByGame(
            Long gameId
    ) {
        return statsRepository
                .findByGameId(gameId);
    }

    public List<TeamGameStats> getStatsByGameId(
            Long gameId
    ) {
        return statsRepository
                .findByGameId(gameId);
    }

    public List<TeamGameStats> getByGameId(
            Long gameId
    ) {
        return statsRepository
                .findByGameId(gameId);
    }

// =========================================================
// GET STATS FOR TEAM
// =========================================================

    public List<TeamGameStats> getStatsByTeam(
            Long teamId
    ) {
        return statsRepository
                .findByTeamId(teamId);
    }

    public List<TeamGameStats> getStatsByTeamId(
            Long teamId
    ) {
        return statsRepository
                .findByTeamId(teamId);
    }

    public List<TeamGameStats> getByTeamId(
            Long teamId
    ) {
        return statsRepository
                .findByTeamId(teamId);
    }

    // =========================================================
    // CREATE STATS
    // =========================================================

    public TeamGameStats createStats(
            TeamGameStats stats
    ) {

        if (stats == null) {

            throw new IllegalArgumentException(
                    "Stats are required"
            );
        }

        if (stats.getGame() == null ||
                stats.getGame().getId() == null) {

            throw new IllegalArgumentException(
                    "Game is required"
            );
        }

        if (stats.getTeam() == null ||
                stats.getTeam().getId() == null) {

            throw new IllegalArgumentException(
                    "Team is required"
            );
        }

        Long gameId =
                stats.getGame().getId();

        Long teamId =
                stats.getTeam().getId();

        Game game =
                gameRepository
                        .findById(gameId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Game not found"
                                )
                        );

        Team team =
                teamRepository
                        .findById(teamId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Team not found"
                                )
                        );

        statsRepository
                .findByGameIdAndTeamId(
                        gameId,
                        teamId
                )
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Stats already exist for this team and game"
                    );
                });

        stats.setGame(game);
        stats.setTeam(team);

        return statsRepository.save(
                stats
        );
    }

    // Alias in case your controller currently calls saveStats().
    public TeamGameStats saveStats(
            TeamGameStats stats
    ) {

        return createStats(stats);
    }

    // =========================================================
    // UPDATE STATS
    // =========================================================

    public TeamGameStats updateStats(
            Long id,
            TeamGameStats updated
    ) {

        TeamGameStats existing =
                getStatsById(id);

        if (updated.getPoints() != null) {
            existing.setPoints(
                    updated.getPoints()
            );
        }

        if (updated.getTotalYards() != null) {
            existing.setTotalYards(
                    updated.getTotalYards()
            );
        }

        if (updated.getPassingYards() != null) {
            existing.setPassingYards(
                    updated.getPassingYards()
            );
        }

        if (updated.getRushingYards() != null) {
            existing.setRushingYards(
                    updated.getRushingYards()
            );
        }

        if (updated.getTurnovers() != null) {
            existing.setTurnovers(
                    updated.getTurnovers()
            );
        }

        if (updated.getFirstDowns() != null) {
            existing.setFirstDowns(
                    updated.getFirstDowns()
            );
        }

        if (updated.getThirdDownAttempts() != null) {
            existing.setThirdDownAttempts(
                    updated.getThirdDownAttempts()
            );
        }

        if (updated.getThirdDownConversions() != null) {
            existing.setThirdDownConversions(
                    updated.getThirdDownConversions()
            );
        }

        if (updated.getRedZoneAttempts() != null) {
            existing.setRedZoneAttempts(
                    updated.getRedZoneAttempts()
            );
        }

        if (updated.getRedZoneScores() != null) {
            existing.setRedZoneScores(
                    updated.getRedZoneScores()
            );
        }

        if (updated.getPenalties() != null) {
            existing.setPenalties(
                    updated.getPenalties()
            );
        }

        if (updated.getPenaltyYards() != null) {
            existing.setPenaltyYards(
                    updated.getPenaltyYards()
            );
        }

        return statsRepository.save(
                existing
        );
    }

    // =========================================================
    // DELETE STATS
    // =========================================================

    public void deleteStats(
            Long id
    ) {

        if (!statsRepository.existsById(id)) {

            throw new IllegalArgumentException(
                    "Team game stats not found"
            );
        }

        statsRepository.deleteById(id);
    }
}