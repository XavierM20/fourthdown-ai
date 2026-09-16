package com.fourthdown.ai.model;

import jakarta.persistence.*;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(unique = true, length = 10)
    private String abbreviation;

    @Column(nullable = false)
    private String conference;

    private String city;

    private String state;

    private String logoUrl;

    @Column(unique = true)
    private Long cfbdId;

    private String mascot;

    private String primaryColor;

    private String alternateColor;

    private String classification;

    public Team() {
    }

    public Team(
            String name,
            String abbreviation,
            String conference,
            String city,
            String state,
            String logoUrl
    ) {
        this.name = name;
        this.abbreviation = abbreviation;
        this.conference = conference;
        this.city = city;
        this.state = state;
        this.logoUrl = logoUrl;
    }

    public Long getId() {
        return id;
    }

    public Long getCfbdId() {
        return cfbdId;
    }

    public void setCfbdId(Long cfbdId) {
        this.cfbdId = cfbdId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getConference() {
        return conference;
    }

    public void setConference(String conference) {
        this.conference = conference;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getMascot() {
        return mascot;
    }

    public void setMascot(String mascot) {
        this.mascot = mascot;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    public String getAlternateColor() {
        return alternateColor;
    }

    public void setAlternateColor(String alternateColor) {
        this.alternateColor = alternateColor;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }
}