package com.example.tournois_demontis.Repository;

import com.example.tournois_demontis.Entity.match.Match;
import com.example.tournois_demontis.Entity.tournament.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByTournamentOrderByRoundAscMatchNumberAsc(Tournament tournament);
    List<Match> findByTournamentAndRoundOrderByMatchNumberAsc(Tournament tournament, int round);
    List<Match> findByTournamentAndBracketOrderByRoundAscMatchNumberAsc(Tournament tournament, String bracket);
}
