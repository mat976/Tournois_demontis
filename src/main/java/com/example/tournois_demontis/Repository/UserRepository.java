package com.example.tournois_demontis.Repository;

import com.example.tournois_demontis.Entity.player.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Méthodes personnalisées peuvent être ajoutées ici si nécessaire
}
