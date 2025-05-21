package com.example.tournois_demontis.Repository;

import com.example.tournois_demontis.Entity.player.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    // Compte le nombre de joueurs dont les IDs sont dans la liste fournie
    long countByIdIn(Iterable<Long> ids);
}
