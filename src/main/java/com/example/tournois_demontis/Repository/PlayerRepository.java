package com.example.tournois_demontis.Repository;

import com.example.tournois_demontis.Entity.player.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    // Méthodes personnalisées peuvent être ajoutées ici si nécessaire
}
