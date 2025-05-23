package com.example.tournois_demontis.Service;

import com.example.tournois_demontis.Entity.match.Match;
import com.example.tournois_demontis.Entity.player.Player;
import com.example.tournois_demontis.Entity.tournament.DoubleEliminationTournament;
import com.example.tournois_demontis.Entity.tournament.RoundRobinTournament;
import com.example.tournois_demontis.Entity.tournament.SingleEliminationTournament;
import com.example.tournois_demontis.Entity.tournament.Tournament;
import com.example.tournois_demontis.Repository.MatchRepository;
import com.example.tournois_demontis.Repository.PlayerRepository;
import com.example.tournois_demontis.Repository.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;

    @Autowired
    public TournamentService(TournamentRepository tournamentRepository, 
                            MatchRepository matchRepository,
                            PlayerRepository playerRepository) {
        this.tournamentRepository = tournamentRepository;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
    }

    public List<Tournament> findAll() {
        return tournamentRepository.findAll();
    }

    public Optional<Tournament> findById(Long id) {
        return tournamentRepository.findById(id);
    }

    @Transactional
    public Tournament save(Tournament tournament) {
        try {
            System.out.println("=== SERVICE: SAUVEGARDE DU TOURNOI ===");
            System.out.println("Tournoi à sauvegarder: " + tournament.getName() + ", Type: " + tournament.getClass().getSimpleName());
            System.out.println("Game: " + (tournament.getGame() != null ? tournament.getGame().getName() : "null"));
            System.out.println("StartDate: " + tournament.getStartDate());
            System.out.println("Status: " + tournament.getStatus());
            
            // Vérifier que toutes les propriétés requises sont définies
            if (tournament.getName() == null || tournament.getName().isEmpty()) {
                throw new IllegalArgumentException("Le nom du tournoi est requis");
            }
            if (tournament.getGame() == null) {
                throw new IllegalArgumentException("Le jeu est requis");
            }
            if (tournament.getStartDate() == null) {
                throw new IllegalArgumentException("La date de début est requise");
            }
            
            // Sauvegarder le tournoi
            Tournament savedTournament = tournamentRepository.save(tournament);
            System.out.println("Tournoi sauvegardé avec ID: " + savedTournament.getId());
            return savedTournament;
        } catch (Exception e) {
            System.out.println("=== ERREUR LORS DE LA SAUVEGARDE DU TOURNOI DANS LE SERVICE ===");
            System.out.println("Message d'erreur: " + e.getMessage());
            e.printStackTrace();
            throw e; // Relancer l'exception pour qu'elle soit gérée par le contrôleur
        }
    }

    public void deleteById(Long id) {
        tournamentRepository.deleteById(id);
    }
    
    /**
     * Démarre un tournoi en générant les matchs initiaux
     */
    @Transactional
    public Tournament startTournament(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournoi non trouvé avec l'ID: " + tournamentId));
        
        if (!tournament.canStart()) {
            throw new IllegalStateException("Le tournoi ne peut pas être démarré. Vérifiez qu'il y a au moins 2 participants.");
        }
        
        // Appeler la méthode startTournament de la classe Tournament qui génère les matchs
        tournament.startTournament();
        
        return tournamentRepository.save(tournament);
    }
    
    /**
     * Enregistre un résultat de match et fait progresser le tournoi
     */
    @Transactional
    public Match recordMatchResult(Long matchId, Long winnerId, Integer score1, Integer score2) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match non trouvé avec l'ID: " + matchId));
        
        if (match.getStatus() == Match.MatchStatus.COMPLETED) {
            throw new IllegalStateException("Ce match est déjà terminé");
        }
        
        Player winner = playerRepository.findById(winnerId)
                .orElseThrow(() -> new IllegalArgumentException("Joueur non trouvé avec l'ID: " + winnerId));
        
        if (!match.getPlayers().contains(winner)) {
            throw new IllegalArgumentException("Le gagnant spécifié ne participe pas à ce match");
        }
        
        // Mettre à jour le match
        match.setWinner(winner);
        match.setScore1(score1);
        match.setScore2(score2);
        match.setStatus(Match.MatchStatus.COMPLETED);
        match.setCompletionDate(LocalDateTime.now());
        match.setEndTime(LocalDateTime.now());
        
        // Sauvegarder le match
        Match updatedMatch = matchRepository.save(match);
        
        // Faire avancer le tournoi si nécessaire
        Tournament tournament = match.getTournament();
        if (tournament != null) {
            if (tournament instanceof SingleEliminationTournament) {
                ((SingleEliminationTournament) tournament).advancePlayer(winner, match.getRound(), match.getMatchNumber());
            } else if (tournament instanceof DoubleEliminationTournament) {
                DoubleEliminationTournament det = (DoubleEliminationTournament) tournament;
                det.advanceWinner(winner, match.getRound(), match.getMatchNumber());
                
                // Trouver le perdant
                Player loser = match.getPlayers().stream()
                    .filter(p -> !p.equals(winner))
                    .findFirst().orElse(null);
                
                if (loser != null && "WINNERS".equals(match.getBracket())) {
                    det.sendToLosersBracket(loser, match.getRound(), match.getMatchNumber());
                }
            }
            
            // Vérifier si le tournoi est terminé
            checkTournamentCompletion(tournament);
            
            tournamentRepository.save(tournament);
        }
        
        return updatedMatch;
    }
    
    /**
     * Vérifie si un tournoi est terminé et met à jour son statut si nécessaire
     */
    private void checkTournamentCompletion(Tournament tournament) {
        boolean isComplete = false;
        Player winner = null;
        
        if (tournament instanceof SingleEliminationTournament) {
            SingleEliminationTournament set = (SingleEliminationTournament) tournament;
            isComplete = set.isComplete();
            
            if (isComplete) {
                // Trouver le match final
                int finalRound = set.getMaxRounds();
                Optional<Match> finalMatch = tournament.getMatches().stream()
                        .filter(m -> m.getRound() == finalRound && m.getStatus() == Match.MatchStatus.COMPLETED)
                        .findFirst();
                
                if (finalMatch.isPresent()) {
                    winner = finalMatch.get().getWinner();
                }
            }
        } else if (tournament instanceof DoubleEliminationTournament) {
            DoubleEliminationTournament det = (DoubleEliminationTournament) tournament;
            isComplete = det.isComplete();
            
            if (isComplete) {
                // Trouver le match de la grande finale
                Optional<Match> grandFinal = tournament.getMatches().stream()
                        .filter(m -> "GRAND_FINAL".equals(m.getBracket()) && m.getStatus() == Match.MatchStatus.COMPLETED)
                        .findFirst();
                
                if (grandFinal.isPresent()) {
                    winner = grandFinal.get().getWinner();
                }
            }
        } else if (tournament instanceof RoundRobinTournament) {
            RoundRobinTournament rrt = (RoundRobinTournament) tournament;
            isComplete = rrt.isAllMatchesCompleted();
            
            if (isComplete) {
                winner = rrt.determineWinner();
            }
        }
        
        if (isComplete && winner != null) {
            tournament.endTournament(winner);
        }
    }
    
    /**
     * Ajoute un participant à un tournoi
     */
    @Transactional
    public Tournament addParticipant(Long tournamentId, Long playerId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournoi non trouvé avec l'ID: " + tournamentId));
        
        if (tournament.getStatus() != Tournament.TournamentStatus.PENDING) {
            throw new IllegalStateException("Les inscriptions pour ce tournoi ne sont pas ouvertes");
        }
        
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Joueur non trouvé avec l'ID: " + playerId));
        
        tournament.addParticipant(player);
        return tournamentRepository.save(tournament);
    }
    
    /**
     * Annule un tournoi
     */
    @Transactional
    public Tournament cancelTournament(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournoi non trouvé avec l'ID: " + tournamentId));
        
        if (tournament.getStatus() == Tournament.TournamentStatus.COMPLETED) {
            throw new IllegalStateException("Impossible d'annuler un tournoi déjà terminé");
        }
        
        tournament.setStatus(Tournament.TournamentStatus.CANCELLED);
        return tournamentRepository.save(tournament);
    }
}
