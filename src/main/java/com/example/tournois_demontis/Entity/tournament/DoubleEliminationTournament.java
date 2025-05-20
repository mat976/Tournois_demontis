package com.example.tournois_demontis.Entity.tournament;

import com.example.tournois_demontis.Entity.game.Game;
import com.example.tournois_demontis.Entity.match.Match;
import com.example.tournois_demontis.Entity.player.Player;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@DiscriminatorValue("DOUBLE_ELIMINATION")
public class DoubleEliminationTournament extends Tournament {
    
    @Column(name = "winners_bracket")
    private boolean hasWinnersBracket = true;
    
    @Column(name = "losers_bracket")
    private boolean hasLosersBracket = true;
    
    @Column(name = "grand_final")
    private boolean hasGrandFinal = true;
    
    // Constructeur vide requis par JPA
    public DoubleEliminationTournament() {
        super();
    }
    
    // Constructeur avec paramètres de base
    public DoubleEliminationTournament(String name, Game game, LocalDateTime startDate) {
        super(name, game, startDate);
    }
    
    // Constructeur complet
    public DoubleEliminationTournament(String name, String description, Game game, 
                                      LocalDateTime startDate, LocalDateTime endDate, Integer maxParticipants) {
        super(name, description, game, startDate, endDate, maxParticipants);
    }
    
    // Getters et Setters spécifiques
    public boolean isHasWinnersBracket() {
        return hasWinnersBracket;
    }
    
    public void setHasWinnersBracket(boolean hasWinnersBracket) {
        this.hasWinnersBracket = hasWinnersBracket;
    }
    
    public boolean isHasLosersBracket() {
        return hasLosersBracket;
    }
    
    public void setHasLosersBracket(boolean hasLosersBracket) {
        this.hasLosersBracket = hasLosersBracket;
    }
    
    public boolean isHasGrandFinal() {
        return hasGrandFinal;
    }
    
    public void setHasGrandFinal(boolean hasGrandFinal) {
        this.hasGrandFinal = hasGrandFinal;
    }
    
    /**
     * Génère les matchs pour un tournoi à double élimination.
     * Dans ce format, un joueur doit perdre deux fois pour être éliminé.
     */
    @Override
    protected void generateMatches() {
        // Convertir l'ensemble de participants en liste pour pouvoir les mélanger
        List<Player> playerList = new ArrayList<>(getParticipants());
        Collections.shuffle(playerList);
        
        int numPlayers = playerList.size();
        int numRounds = (int) Math.ceil(Math.log(numPlayers) / Math.log(2));
        int round1Matches = numPlayers / 2;
        
        // Génération des matchs du premier tour (Winners Bracket)
        LocalDateTime matchTime = getStartDate();
        for (int i = 0; i < round1Matches; i++) {
            Match match = new Match();
            match.setTournament(this);
            match.setGame(getGame());
            match.setRound(1);
            match.setMatchNumber(i + 1);
            match.setBracket("WINNERS");
            match.setScheduledTime(matchTime);
            match.addPlayer(playerList.get(i * 2));
            match.addPlayer(playerList.get(i * 2 + 1));
            
            // Ajouter 30 minutes pour le match suivant
            matchTime = matchTime.plusMinutes(30);
            
            addMatch(match);
        }
        
        // Traitement des joueurs qui obtiennent un "bye" si nécessaire
        if (numPlayers % 2 != 0) {
            Player playerWithBye = playerList.get(playerList.size() - 1);
            
            Match byeMatch = new Match();
            byeMatch.setTournament(this);
            byeMatch.setGame(getGame());
            byeMatch.setRound(1);
            byeMatch.setMatchNumber(round1Matches + 1);
            byeMatch.setBracket("WINNERS");
            byeMatch.setScheduledTime(matchTime);
            byeMatch.addPlayer(playerWithBye);
            byeMatch.setStatus(Match.MatchStatus.COMPLETED);
            byeMatch.setWinner(playerWithBye);
            
            addMatch(byeMatch);
        }
    }
    
    /**
     * Avance un joueur au prochain tour dans le bracket gagnant
     */
    public void advanceWinner(Player winner, int currentRound, int currentMatchNumber) {
        int nextRound = currentRound + 1;
        int nextMatchNumber = (currentMatchNumber + 1) / 2;
        
        boolean matchExists = getMatches().stream()
                .anyMatch(m -> m.getRound() == nextRound && 
                           m.getMatchNumber() == nextMatchNumber && 
                           "WINNERS".equals(m.getBracket()));
        
        if (!matchExists) {
            Match nextMatch = new Match();
            nextMatch.setTournament(this);
            nextMatch.setGame(getGame());
            nextMatch.setRound(nextRound);
            nextMatch.setMatchNumber(nextMatchNumber);
            nextMatch.setBracket("WINNERS");
            nextMatch.setScheduledTime(LocalDateTime.now().plusHours(1));
            nextMatch.addPlayer(winner);
            
            addMatch(nextMatch);
        } else {
            getMatches().stream()
                    .filter(m -> m.getRound() == nextRound && 
                               m.getMatchNumber() == nextMatchNumber && 
                               "WINNERS".equals(m.getBracket()))
                    .findFirst()
                    .ifPresent(match -> match.addPlayer(winner));
        }
    }
    
    /**
     * Envoie un joueur perdant dans le bracket des perdants
     */
    public void sendToLosersBracket(Player loser, int fromWinnersRound, int fromMatchNumber) {
        int losersRound = fromWinnersRound * 2 - 1;
        int losersMatchNumber = calculateLosersMatchNumber(fromWinnersRound, fromMatchNumber);
        
        boolean matchExists = getMatches().stream()
                .anyMatch(m -> m.getRound() == losersRound && 
                           m.getMatchNumber() == losersMatchNumber && 
                           "LOSERS".equals(m.getBracket()));
        
        if (!matchExists) {
            Match losersMatch = new Match();
            losersMatch.setTournament(this);
            losersMatch.setGame(getGame());
            losersMatch.setRound(losersRound);
            losersMatch.setMatchNumber(losersMatchNumber);
            losersMatch.setBracket("LOSERS");
            losersMatch.setScheduledTime(LocalDateTime.now().plusHours(1));
            losersMatch.addPlayer(loser);
            
            addMatch(losersMatch);
        } else {
            getMatches().stream()
                    .filter(m -> m.getRound() == losersRound && 
                               m.getMatchNumber() == losersMatchNumber && 
                               "LOSERS".equals(m.getBracket()))
                    .findFirst()
                    .ifPresent(match -> match.addPlayer(loser));
        }
    }
    
    /**
     * Calcule le numéro de match dans le bracket des perdants
     */
    private int calculateLosersMatchNumber(int winnersRound, int winnersMatchNumber) {
        // Cette méthode dépend de la structure exacte du tournoi à double élimination
        // Ceci est une implémentation simplifiée
        return winnersMatchNumber;
    }
    
    /**
     * Génère le match de la grande finale entre le gagnant du bracket gagnant et le gagnant du bracket perdant
     */
    public void createGrandFinal(Player winnersChampion, Player losersChampion) {
        Match grandFinal = new Match();
        grandFinal.setTournament(this);
        grandFinal.setGame(getGame());
        grandFinal.setRound(100); // Utilisation d'un nombre élevé pour la grande finale
        grandFinal.setMatchNumber(1);
        grandFinal.setBracket("GRAND_FINAL");
        grandFinal.setScheduledTime(LocalDateTime.now().plusHours(2));
        grandFinal.addPlayer(winnersChampion);
        grandFinal.addPlayer(losersChampion);
        
        addMatch(grandFinal);
    }
    
    /**
     * Obtient le champion du bracket gagnant
     */
    public Player getWinnersBracketChampion() {
        // Trouver le dernier match du bracket gagnant qui est terminé
        return getMatches().stream()
                .filter(m -> "WINNERS".equals(m.getBracket()) && m.getStatus() == Match.MatchStatus.COMPLETED)
                .max((m1, m2) -> Integer.compare(m1.getRound(), m2.getRound()))
                .map(Match::getWinner)
                .orElse(null);
    }
    
    /**
     * Obtient le champion du bracket perdant
     */
    public Player getLosersBracketChampion() {
        // Trouver le dernier match du bracket perdant qui est terminé
        return getMatches().stream()
                .filter(m -> "LOSERS".equals(m.getBracket()) && m.getStatus() == Match.MatchStatus.COMPLETED)
                .max((m1, m2) -> Integer.compare(m1.getRound(), m2.getRound()))
                .map(Match::getWinner)
                .orElse(null);
    }
    
    /**
     * Vérifie si le tournoi est complet
     */
    public boolean isComplete() {
        // Le tournoi est complet si la grande finale est terminée
        return getMatches().stream()
                .anyMatch(m -> "GRAND_FINAL".equals(m.getBracket()) && 
                           m.getStatus() == Match.MatchStatus.COMPLETED);
    }
    
    /**
     * Obtient tous les matchs du bracket gagnant
     */
    public List<Match> getWinnersBracketMatches() {
        return getMatches().stream()
                .filter(m -> "WINNERS".equals(m.getBracket()))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtient tous les matchs du bracket perdant
     */
    public List<Match> getLosersBracketMatches() {
        return getMatches().stream()
                .filter(m -> "LOSERS".equals(m.getBracket()))
                .collect(Collectors.toList());
    }
    
    @Override
    public String toString() {
        return "DoubleEliminationTournament{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", participants=" + getParticipants().size() +
                ", winnersMatches=" + getWinnersBracketMatches().size() +
                ", losersMatches=" + getLosersBracketMatches().size() +
                '}';
    }
}
