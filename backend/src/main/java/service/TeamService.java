package com.fourthdown.ai.service;

import com.fourthdown.ai.model.Team;
import com.fourthdown.ai.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Team getTeamById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));
    }

    public Team createTeam(Team team) {
        return teamRepository.save(team);
    }

    public Team updateTeam(Long id, Team updatedTeam) {

        Team existingTeam = getTeamById(id);

        existingTeam.setName(updatedTeam.getName());
        existingTeam.setAbbreviation(updatedTeam.getAbbreviation());
        existingTeam.setConference(updatedTeam.getConference());
        existingTeam.setCity(updatedTeam.getCity());
        existingTeam.setState(updatedTeam.getState());
        existingTeam.setLogoUrl(updatedTeam.getLogoUrl());

        return teamRepository.save(existingTeam);
    }

    public void deleteTeam(Long id) {
        teamRepository.deleteById(id);
    }
}
