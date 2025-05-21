package com.example.tournois_demontis.Service;

import com.example.tournois_demontis.Entity.team.Team;
import com.example.tournois_demontis.Entity.player.Player;
import com.example.tournois_demontis.Repository.TeamRepository;
import com.example.tournois_demontis.Repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TeamService {
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private PlayerRepository playerRepository;

    public List<Team> findAll() {
        return teamRepository.findAll();
    }

    public Optional<Team> findById(Long id) {
        return teamRepository.findById(id);
    }

    public Team save(Team team, Set<Long> playerIds) {
        Set<Player> players = new HashSet<>(playerRepository.findAllById(playerIds));
        team.setPlayers(players);
        return teamRepository.save(team);
    }

    public Team save(Team team) {
        return teamRepository.save(team);
    }

    public void deleteById(Long id) {
        teamRepository.deleteById(id);
    }

    public boolean existsByName(String name) {
        return teamRepository.existsByName(name);
    }
}
