package com.fourthdown.ai.repository;

import com.fourthdown.ai.model.TeamGameStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TeamGameStatsRepository
        extends JpaRepository<TeamGameStats, Long> {

    List<TeamGameStats> findByGameId(
            Long gameId
    );

    List<TeamGameStats> findByTeamId(
            Long teamId
    );

    Optional<TeamGameStats> findByGameIdAndTeamId(
            Long gameId,
            Long teamId
    );

    // =========================================================
    // HISTORICAL QUERY
    // =========================================================

    List<TeamGameStats>
    findByTeamIdAndGameGameDateBeforeOrderByGameGameDateAsc(
            Long teamId,
            LocalDateTime gameDate
    );

    // =========================================================
    // CURRENT-SEASON STATS BEFORE CUTOFF
    // =========================================================

    List<TeamGameStats>
    findByTeamIdAndGameSeasonAndGameGameDateBeforeOrderByGameGameDateAsc(
            Long teamId,
            Integer season,
            LocalDateTime gameDate
    );

    // =========================================================
    // COMPLETE PRIOR-SEASON HISTORY
    // =========================================================

    List<TeamGameStats>
    findByTeamIdAndGameSeasonOrderByGameGameDateAsc(
            Long teamId,
            Integer season
    );

    List<TeamGameStats>
    findByGameSeasonOrderByGameGameDateAsc(
            Integer season
    );
}