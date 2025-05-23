package com.example.tournois_demontis.Repository;

import com.example.tournois_demontis.Entity.game.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
}
