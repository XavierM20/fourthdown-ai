package com.fourthdown.ai.dto.cfbd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CfbdTeamGameStatsResponse {

    private Long id;
    private List<TeamStats> teams;

    public CfbdTeamGameStatsResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<TeamStats> getTeams() {
        return teams;
    }

    public void setTeams(List<TeamStats> teams) {
        this.teams = teams;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TeamStats {

        private Long teamId;
        private String team;
        private String conference;
        private String homeAway;
        private Integer points;
        private List<Stat> stats;

        public TeamStats() {
        }

        public Long getTeamId() {
            return teamId;
        }

        public void setTeamId(Long teamId) {
            this.teamId = teamId;
        }

        public String getTeam() {
            return team;
        }

        public void setTeam(String team) {
            this.team = team;
        }

        public String getConference() {
            return conference;
        }

        public void setConference(String conference) {
            this.conference = conference;
        }

        public String getHomeAway() {
            return homeAway;
        }

        public void setHomeAway(String homeAway) {
            this.homeAway = homeAway;
        }

        public Integer getPoints() {
            return points;
        }

        public void setPoints(Integer points) {
            this.points = points;
        }

        public List<Stat> getStats() {
            return stats;
        }

        public void setStats(List<Stat> stats) {
            this.stats = stats;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Stat {

        private String category;
        private String stat;

        public Stat() {
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getStat() {
            return stat;
        }

        public void setStat(String stat) {
            this.stat = stat;
        }
    }
}