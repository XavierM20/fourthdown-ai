package com.fourthdown.ai.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "team_game_stats",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"game_id", "team_id"})
        }
)
public class TeamGameStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    private Integer points;

    private Integer totalYards;

    private Integer passingYards;

    private Integer rushingYards;

    private Integer turnovers;

    private Integer firstDowns;

    private Integer thirdDownAttempts;

    private Integer thirdDownConversions;

    private Integer redZoneAttempts;

    private Integer redZoneScores;

    private Integer penalties;

    private Integer penaltyYards;

    public TeamGameStats() {
    }

    public TeamGameStats(
            Game game,
            Team team,
            Integer points,
            Integer totalYards,
            Integer passingYards,
            Integer rushingYards,
            Integer turnovers,
            Integer firstDowns,
            Integer thirdDownAttempts,
            Integer thirdDownConversions,
            Integer redZoneAttempts,
            Integer redZoneScores,
            Integer penalties,
            Integer penaltyYards
    ) {
        this.game = game;
        this.team = team;
        this.points = points;
        this.totalYards = totalYards;
        this.passingYards = passingYards;
        this.rushingYards = rushingYards;
        this.turnovers = turnovers;
        this.firstDowns = firstDowns;
        this.thirdDownAttempts = thirdDownAttempts;
        this.thirdDownConversions = thirdDownConversions;
        this.redZoneAttempts = redZoneAttempts;
        this.redZoneScores = redZoneScores;
        this.penalties = penalties;
        this.penaltyYards = penaltyYards;
    }

    public Long getId() {
        return id;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Integer getTotalYards() {
        return totalYards;
    }

    public void setTotalYards(Integer totalYards) {
        this.totalYards = totalYards;
    }

    public Integer getPassingYards() {
        return passingYards;
    }

    public void setPassingYards(Integer passingYards) {
        this.passingYards = passingYards;
    }

    public Integer getRushingYards() {
        return rushingYards;
    }

    public void setRushingYards(Integer rushingYards) {
        this.rushingYards = rushingYards;
    }

    public Integer getTurnovers() {
        return turnovers;
    }

    public void setTurnovers(Integer turnovers) {
        this.turnovers = turnovers;
    }

    public Integer getFirstDowns() {
        return firstDowns;
    }

    public void setFirstDowns(Integer firstDowns) {
        this.firstDowns = firstDowns;
    }

    public Integer getThirdDownAttempts() {
        return thirdDownAttempts;
    }

    public void setThirdDownAttempts(Integer thirdDownAttempts) {
        this.thirdDownAttempts = thirdDownAttempts;
    }

    public Integer getThirdDownConversions() {
        return thirdDownConversions;
    }

    public void setThirdDownConversions(Integer thirdDownConversions) {
        this.thirdDownConversions = thirdDownConversions;
    }

    public Integer getRedZoneAttempts() {
        return redZoneAttempts;
    }

    public void setRedZoneAttempts(Integer redZoneAttempts) {
        this.redZoneAttempts = redZoneAttempts;
    }

    public Integer getRedZoneScores() {
        return redZoneScores;
    }

    public void setRedZoneScores(Integer redZoneScores) {
        this.redZoneScores = redZoneScores;
    }

    public Integer getPenalties() {
        return penalties;
    }

    public void setPenalties(Integer penalties) {
        this.penalties = penalties;
    }

    public Integer getPenaltyYards() {
        return penaltyYards;
    }

    public void setPenaltyYards(Integer penaltyYards) {
        this.penaltyYards = penaltyYards;
    }
}
