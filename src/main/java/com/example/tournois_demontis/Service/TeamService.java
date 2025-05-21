package com.example.tournois_demontis.Service;

import com.example.tournois_demontis.Entity.team.Team;
import java.time.LocalDateTime;
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
        // Récupérer les joueurs depuis la base de données
        Set<Player> players = new HashSet<>();
        if (playerIds != null && !playerIds.isEmpty()) {
            players = new HashSet<>(playerRepository.findAllById(playerIds));
            
            // Si un capitaine est défini mais n'est pas dans la liste des joueurs, l'ajouter
            if (team.getCaptain() != null && players.stream().noneMatch(p -> p.getId().equals(team.getCaptain().getId()))) {
                Optional<Player> captain = playerRepository.findById(team.getCaptain().getId());
                captain.ifPresent(players::add);
            }
        } else if (team.getCaptain() != null) {
            // Si pas de joueurs mais un capitaine, l'ajouter
            Optional<Player> captain = playerRepository.findById(team.getCaptain().getId());
            captain.ifPresent(players::add);
        }
        
        // Mettre à jour les joueurs de l'équipe
        team.setPlayers(players);
        
        // Si pas de date de création, en définir une
        if (team.getCreationDate() == null) {
            team.setCreationDate(LocalDateTime.now());
        }
        
        // Sauvegarder l'équipe et ses relations
        Team savedTeam = teamRepository.save(team);
        
        // Mettre à jour la relation côté joueurs en utilisant la méthode utilitaire
        for (Player player : players) {
            player.addTeam(savedTeam);
            playerRepository.save(player);
        }
        
        return savedTeam;
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
