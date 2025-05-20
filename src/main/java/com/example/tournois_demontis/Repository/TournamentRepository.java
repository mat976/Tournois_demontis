package com.example.tournois_demontis.Repository;

import com.example.tournois_demontis.Entity.tournament.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    // Méthodes personnalisées peuvent être ajoutées ici si nécessaire
}
