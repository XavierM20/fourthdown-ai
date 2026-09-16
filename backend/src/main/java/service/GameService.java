package com.fourthdown.ai.service;

import com.fourthdown.ai.model.Game;
import com.fourthdown.ai.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameService {

    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public Game getGameById(Long id) {
        return gameRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Game not found")
                );
    }

    public Game createGame(Game game) {

        if (game.getHomeTeam() == null ||
                game.getHomeTeam().getId() == null) {
            throw new IllegalArgumentException(
                    "Home team is required"
            );
        }

        if (game.getAwayTeam() == null ||
                game.getAwayTeam().getId() == null) {
            throw new IllegalArgumentException(
                    "Away team is required"
            );
        }

        if (game.getGameDate() == null) {
            throw new IllegalArgumentException(
                    "Game date is required"
            );
        }

        if (game.getHomeTeam().getId()
                .equals(game.getAwayTeam().getId())) {
            throw new IllegalArgumentException(
                    "Home team and away team cannot be the same"
            );
        }

        boolean duplicate =
                gameRepository
                        .existsByHomeTeamIdAndAwayTeamIdAndGameDate(
                                game.getHomeTeam().getId(),
                                game.getAwayTeam().getId(),
                                game.getGameDate()
                        );

        if (duplicate) {
            throw new IllegalArgumentException(
                    "This game already exists"
            );
        }

        return gameRepository.save(game);
    }

    public Game updateGame(
            Long id,
            Game updatedGame
    ) {
        Game existingGame = getGameById(id);

        existingGame.setHomeTeam(
                updatedGame.getHomeTeam()
        );

        existingGame.setAwayTeam(
                updatedGame.getAwayTeam()
        );

        existingGame.setGameDate(
                updatedGame.getGameDate()
        );

        existingGame.setStatus(
                updatedGame.getStatus()
        );

        existingGame.setHomeScore(
                updatedGame.getHomeScore()
        );

        existingGame.setAwayScore(
                updatedGame.getAwayScore()
        );

        existingGame.setVenue(
                updatedGame.getVenue()
        );

        return gameRepository.save(existingGame);
    }

    public void deleteGame(Long id) {
        gameRepository.deleteById(id);
    }
}