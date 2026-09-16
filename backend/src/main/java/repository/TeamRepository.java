package com.fourthdown.ai.repository;

import com.fourthdown.ai.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByCfbdId(Long cfbdId);

    Optional<Team> findByNameIgnoreCase(String name);
}