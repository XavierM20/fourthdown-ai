package com.fourthdown.ai.dto.cfbd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CfbdGameResponse {

    private Long id;

    private Integer season;

    private Integer week;

    private String seasonType;

    private OffsetDateTime startDate;

    private Boolean completed;

    private String venue;

    /*
     * CFBD can provide a general descriptive
     * label for a game.
     */
    private String notes;

    /*
     * CFP-specific metadata.
     */
    private Playoff playoff;

    private Long homeId;

    private String homeTeam;

    private String homeConference;

    private Integer homePoints;

    private Long awayId;

    private String awayTeam;

    private String awayConference;

    private Integer awayPoints;

    public CfbdGameResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(
            Long id
    ) {
        this.id = id;
    }

    public Integer getSeason() {
        return season;
    }

    public void setSeason(
            Integer season
    ) {
        this.season = season;
    }

    public Integer getWeek() {
        return week;
    }

    public void setWeek(
            Integer week
    ) {
        this.week = week;
    }

    public String getSeasonType() {
        return seasonType;
    }

    public void setSeasonType(
            String seasonType
    ) {
        this.seasonType =
                seasonType;
    }

    public OffsetDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(
            OffsetDateTime startDate
    ) {
        this.startDate =
                startDate;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(
            Boolean completed
    ) {
        this.completed =
                completed;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(
            String venue
    ) {
        this.venue =
                venue;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(
            String notes
    ) {
        this.notes =
                notes;
    }

    public Playoff getPlayoff() {
        return playoff;
    }

    public void setPlayoff(
            Playoff playoff
    ) {
        this.playoff =
                playoff;
    }

    public Long getHomeId() {
        return homeId;
    }

    public void setHomeId(
            Long homeId
    ) {
        this.homeId =
                homeId;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(
            String homeTeam
    ) {
        this.homeTeam =
                homeTeam;
    }

    public String getHomeConference() {
        return homeConference;
    }

    public void setHomeConference(
            String homeConference
    ) {
        this.homeConference =
                homeConference;
    }

    public Integer getHomePoints() {
        return homePoints;
    }

    public void setHomePoints(
            Integer homePoints
    ) {
        this.homePoints =
                homePoints;
    }

    public Long getAwayId() {
        return awayId;
    }

    public void setAwayId(
            Long awayId
    ) {
        this.awayId =
                awayId;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(
            String awayTeam
    ) {
        this.awayTeam =
                awayTeam;
    }

    public String getAwayConference() {
        return awayConference;
    }

    public void setAwayConference(
            String awayConference
    ) {
        this.awayConference =
                awayConference;
    }

    public Integer getAwayPoints() {
        return awayPoints;
    }

    public void setAwayPoints(
            Integer awayPoints
    ) {
        this.awayPoints =
                awayPoints;
    }

    // =========================================================
    // PLAYOFF METADATA
    // =========================================================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Playoff {

        private String competition;

        private String format;

        private String round;

        private String roundName;

        private String bracketSlot;

        private Integer homeSeed;

        private Integer awaySeed;

        private String bowlName;

        public Playoff() {
        }

        public String getCompetition() {
            return competition;
        }

        public void setCompetition(
                String competition
        ) {
            this.competition =
                    competition;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(
                String format
        ) {
            this.format =
                    format;
        }

        public String getRound() {
            return round;
        }

        public void setRound(
                String round
        ) {
            this.round =
                    round;
        }

        public String getRoundName() {
            return roundName;
        }

        public void setRoundName(
                String roundName
        ) {
            this.roundName =
                    roundName;
        }

        public String getBracketSlot() {
            return bracketSlot;
        }

        public void setBracketSlot(
                String bracketSlot
        ) {
            this.bracketSlot =
                    bracketSlot;
        }

        public Integer getHomeSeed() {
            return homeSeed;
        }

        public void setHomeSeed(
                Integer homeSeed
        ) {
            this.homeSeed =
                    homeSeed;
        }

        public Integer getAwaySeed() {
            return awaySeed;
        }

        public void setAwaySeed(
                Integer awaySeed
        ) {
            this.awaySeed =
                    awaySeed;
        }

        public String getBowlName() {
            return bowlName;
        }

        public void setBowlName(
                String bowlName
        ) {
            this.bowlName =
                    bowlName;
        }
    }
}