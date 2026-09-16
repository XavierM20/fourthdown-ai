package com.fourthdown.ai.dto.cfbd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CfbdRankingResponse {

    private Integer season;

    private String seasonType;

    private Integer week;

    private List<Poll> polls;

    public CfbdRankingResponse() {
    }

    public Integer getSeason() {
        return season;
    }

    public void setSeason(
            Integer season
    ) {
        this.season = season;
    }

    public String getSeasonType() {
        return seasonType;
    }

    public void setSeasonType(
            String seasonType
    ) {
        this.seasonType = seasonType;
    }

    public Integer getWeek() {
        return week;
    }

    public void setWeek(
            Integer week
    ) {
        this.week = week;
    }

    public List<Poll> getPolls() {
        return polls;
    }

    public void setPolls(
            List<Poll> polls
    ) {
        this.polls = polls;
    }

    // =========================================================
    // POLL
    // =========================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Poll {

        private String poll;

        private Boolean isFinal;

        private List<Rank> ranks;

        public Poll() {
        }

        public String getPoll() {
            return poll;
        }

        public void setPoll(
                String poll
        ) {
            this.poll = poll;
        }

        public Boolean getIsFinal() {
            return isFinal;
        }

        public void setIsFinal(
                Boolean isFinal
        ) {
            this.isFinal = isFinal;
        }

        public List<Rank> getRanks() {
            return ranks;
        }

        public void setRanks(
                List<Rank> ranks
        ) {
            this.ranks = ranks;
        }
    }

    // =========================================================
    // RANK
    // =========================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Rank {

        private Integer rank;

        private Long teamId;

        private String school;

        private String conference;

        private Integer firstPlaceVotes;

        private Integer points;

        public Rank() {
        }

        public Integer getRank() {
            return rank;
        }

        public void setRank(
                Integer rank
        ) {
            this.rank = rank;
        }

        public Long getTeamId() {
            return teamId;
        }

        public void setTeamId(
                Long teamId
        ) {
            this.teamId = teamId;
        }

        public String getSchool() {
            return school;
        }

        public void setSchool(
                String school
        ) {
            this.school = school;
        }

        public String getConference() {
            return conference;
        }

        public void setConference(
                String conference
        ) {
            this.conference = conference;
        }

        public Integer getFirstPlaceVotes() {
            return firstPlaceVotes;
        }

        public void setFirstPlaceVotes(
                Integer firstPlaceVotes
        ) {
            this.firstPlaceVotes =
                    firstPlaceVotes;
        }

        public Integer getPoints() {
            return points;
        }

        public void setPoints(
                Integer points
        ) {
            this.points = points;
        }
    }
}