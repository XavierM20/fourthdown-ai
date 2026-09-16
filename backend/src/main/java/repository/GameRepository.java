package com.fourthdown.ai.repository;

import com.fourthdown.ai.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {

    long countByGameDateAfter(
            LocalDateTime dateTime
    );

    Optional<Game> findFirstByGameDateAfterOrderByGameDateAsc(
            LocalDateTime dateTime
    );

    List<Game> findByGameDateAfterOrderByGameDateAsc(
            LocalDateTime dateTime
    );

    boolean existsByHomeTeamIdAndAwayTeamIdAndGameDate(
            Long homeTeamId,
            Long awayTeamId,
            LocalDateTime gameDate
    );

    Optional<Game> findByCfbdId(
            Long cfbdId
    );

    Optional<Game> findByHomeTeamIdAndAwayTeamIdAndGameDate(
            Long homeTeamId,
            Long awayTeamId,
            LocalDateTime gameDate
    );

    List<Game> findBySeasonBetweenAndStatusOrderByGameDateAsc(
            Integer startSeason,
            Integer endSeason,
            String status
    );

    // =========================================================
    // COMPLETED GAMES FOR ONE SEASON
    // =========================================================

    List<Game> findBySeasonAndStatusOrderByGameDateAsc(
            Integer season,
            String status
    );
}