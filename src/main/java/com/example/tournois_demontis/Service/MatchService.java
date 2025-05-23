package com.example.tournois_demontis.Service;

import com.example.tournois_demontis.Entity.match.Match;
import com.example.tournois_demontis.Entity.player.Player;
import com.example.tournois_demontis.Entity.tournament.Tournament;
import com.example.tournois_demontis.Repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MatchService {

    private final MatchRepository matchRepository;

    @Autowired
    public MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    public List<Match> findAll() {
        return matchRepository.findAll();
    }

    public Optional<Match> findById(Long id) {
        return matchRepository.findById(id);
    }

    public Match save(Match match) {
        return matchRepository.save(match);
    }

    public void deleteById(Long id) {
        matchRepository.deleteById(id);
    }
    
    public List<Match> findByTournament(Tournament tournament) {
        return matchRepository.findByTournamentOrderByRoundAscMatchNumberAsc(tournament);
    }
    
    public List<Match> findByTournamentAndRound(Tournament tournament, int round) {
        return matchRepository.findByTournamentAndRoundOrderByMatchNumberAsc(tournament, round);
    }
    
    public List<Match> findByTournamentAndBracket(Tournament tournament, String bracket) {
        return matchRepository.findByTournamentAndBracketOrderByRoundAscMatchNumberAsc(tournament, bracket);
    }
    
    /**
     * Met à jour un match avec le résultat
     */
    @Transactional
    public Match updateMatchResult(Long matchId, Long winnerId, Integer score1, Integer score2) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match non trouvé avec l'ID: " + matchId));
        
        if (match.getStatus() == Match.MatchStatus.COMPLETED) {
            throw new IllegalStateException("Ce match est déjà terminé");
        }
        
        // Trouver le joueur gagnant
        Player winner = null;
        for (Player player : match.getPlayers()) {
            if (player.getId().equals(winnerId)) {
                winner = player;
                break;
            }
        }
        
        if (winner == null) {
            throw new IllegalArgumentException("Le gagnant spécifié ne participe pas à ce match");
        }
        
        // Mettre à jour le match
        match.setWinner(winner);
        match.setScore1(score1);
        match.setScore2(score2);
        match.setStatus(Match.MatchStatus.COMPLETED);
        match.setCompletionDate(LocalDateTime.now());
        
        return matchRepository.save(match);
    }
}
