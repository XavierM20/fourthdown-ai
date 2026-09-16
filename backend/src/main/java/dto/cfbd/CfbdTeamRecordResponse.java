package com.fourthdown.ai.dto.cfbd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CfbdTeamRecordResponse {

    private Integer year;
    private Long teamId;
    private String team;
    private String classification;
    private String conference;

    private TeamRecord total;
    private TeamRecord conferenceGames;

    public CfbdTeamRecordResponse() {
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
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

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getConference() {
        return conference;
    }

    public void setConference(String conference) {
        this.conference = conference;
    }

    public TeamRecord getTotal() {
        return total;
    }

    public void setTotal(TeamRecord total) {
        this.total = total;
    }

    public TeamRecord getConferenceGames() {
        return conferenceGames;
    }

    public void setConferenceGames(TeamRecord conferenceGames) {
        this.conferenceGames = conferenceGames;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TeamRecord {

        private Integer games;
        private Integer wins;
        private Integer losses;
        private Integer ties;

        public TeamRecord() {
        }

        public Integer getGames() {
            return games;
        }

        public void setGames(Integer games) {
            this.games = games;
        }

        public Integer getWins() {
            return wins;
        }

        public void setWins(Integer wins) {
            this.wins = wins;
        }

        public Integer getLosses() {
            return losses;
        }

        public void setLosses(Integer losses) {
            this.losses = losses;
        }

        public Integer getTies() {
            return ties;
        }

        public void setTies(Integer ties) {
            this.ties = ties;
        }
    }
}