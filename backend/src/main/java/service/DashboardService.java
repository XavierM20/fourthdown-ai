package com.fourthdown.ai.service;

import com.fourthdown.ai.dto.DashboardResponse;
import com.fourthdown.ai.model.Game;
import com.fourthdown.ai.repository.GameRepository;
import com.fourthdown.ai.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class DashboardService {

    private final TeamRepository teamRepository;
    private final GameRepository gameRepository;

    public DashboardService(
            TeamRepository teamRepository,
            GameRepository gameRepository
    ) {
        this.teamRepository = teamRepository;
        this.gameRepository = gameRepository;
    }

    public DashboardResponse getDashboard() {
        LocalDateTime now = LocalDateTime.now();

        long totalTeams = teamRepository.count();
        long totalGames = gameRepository.count();
        long upcomingGames =
                gameRepository.countByGameDateAfter(now);

        Optional<Game> nextGameOptional =
                gameRepository
                        .findFirstByGameDateAfterOrderByGameDateAsc(now);

        DashboardResponse.NextGame nextGame = null;

        if (nextGameOptional.isPresent()) {
            Game game = nextGameOptional.get();

            nextGame = new DashboardResponse.NextGame(
                    game.getId(),
                    game.getHomeTeam().getName(),
                    game.getAwayTeam().getName(),
                    game.getGameDate(),
                    game.getVenue()
            );
        }

        return new DashboardResponse(
                totalTeams,
                totalGames,
                upcomingGames,
                nextGame
        );
    }
}