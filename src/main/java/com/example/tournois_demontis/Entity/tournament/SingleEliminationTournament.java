package com.example.tournois_demontis.Entity.tournament;

import com.example.tournois_demontis.Entity.game.Game;
import com.example.tournois_demontis.Entity.match.Match;
import com.example.tournois_demontis.Entity.player.Player;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@DiscriminatorValue("SINGLE_ELIMINATION")
public class SingleEliminationTournament extends Tournament {
    
    // Constructeur vide requis par JPA
    public SingleEliminationTournament() {
        super();
    }
    
    // Constructeur avec paramètres de base
    public SingleEliminationTournament(String name, Game game, LocalDateTime startDate) {
        super(name, game, startDate);
    }
    
    // Constructeur complet
    public SingleEliminationTournament(String name, String description, Game game, 
                                      LocalDateTime startDate, LocalDateTime endDate, Integer maxParticipants) {
        super(name, description, game, startDate, endDate, maxParticipants);
    }
    
    /**
     * Génère les matchs pour un tournoi à élimination simple.
     * Dans ce format, les perdants sont éliminés et les gagnants avancent au prochain tour.
     */
    @Override
    protected void generateMatches() {
        // Convertir l'ensemble de participants en liste pour pouvoir les mélanger
        List<Player> playerList = new ArrayList<>(getParticipants());
        Collections.shuffle(playerList);
        
        // Si le nombre de joueurs n'est pas une puissance de 2, certains joueurs peuvent recevoir un "bye"
        int numPlayers = playerList.size();
        int numRounds = (int) Math.ceil(Math.log(numPlayers) / Math.log(2));
        int totalMatches = (int) Math.pow(2, numRounds) - 1;
        int round1Matches = numPlayers / 2;
        
        // Génération des matchs du premier tour
        LocalDateTime matchTime = getStartDate();
        for (int i = 0; i < round1Matches; i++) {
            Match match = new Match();
            match.setTournament(this);
            match.setGame(getGame());
            match.setRound(1);
            match.setMatchNumber(i + 1);
            match.setScheduledTime(matchTime);
            match.addPlayer(playerList.get(i * 2));
            match.addPlayer(playerList.get(i * 2 + 1));
            
            // Ajouter 30 minutes pour le match suivant
            matchTime = matchTime.plusMinutes(30);
            
            addMatch(match);
        }
        
        // Si le nombre de joueurs n'est pas une puissance de 2, traiter les joueurs qui ont un "bye"
        if (numPlayers % 2 != 0) {
            // Le dernier joueur obtient un bye
            Player playerWithBye = playerList.get(playerList.size() - 1);
            
            // Créer un match "vide" pour indiquer le bye
            Match byeMatch = new Match();
            byeMatch.setTournament(this);
            byeMatch.setGame(getGame());
            byeMatch.setRound(1);
            byeMatch.setMatchNumber(round1Matches + 1);
            byeMatch.setScheduledTime(matchTime);
            byeMatch.addPlayer(playerWithBye);
            byeMatch.setStatus(Match.MatchStatus.COMPLETED); // Le match est automatiquement terminé
            byeMatch.setWinner(playerWithBye); // Le joueur gagne automatiquement
            
            addMatch(byeMatch);
        }
    }
    
    /**
     * Avance un joueur au prochain tour après une victoire
     */
    public void advancePlayer(Player winner, int currentRound, int currentMatchNumber) {
        int numMatchesInCurrentRound = (int) Math.pow(2, getMaxRounds() - currentRound);
        int nextRound = currentRound + 1;
        int nextMatchNumber = (currentMatchNumber + 1) / 2;
        
        boolean matchExists = getMatches().stream()
                .anyMatch(m -> m.getRound() == nextRound && m.getMatchNumber() == nextMatchNumber);
        
        if (!matchExists) {
            Match nextMatch = new Match();
            nextMatch.setTournament(this);
            nextMatch.setGame(getGame());
            nextMatch.setRound(nextRound);
            nextMatch.setMatchNumber(nextMatchNumber);
            nextMatch.setScheduledTime(LocalDateTime.now().plusHours(1)); // Programmer le match 1 heure plus tard
            nextMatch.addPlayer(winner);
            
            addMatch(nextMatch);
        } else {
            getMatches().stream()
                    .filter(m -> m.getRound() == nextRound && m.getMatchNumber() == nextMatchNumber)
                    .findFirst()
                    .ifPresent(match -> match.addPlayer(winner));
        }
    }
    
    /**
     * Obtient le nombre maximum de tours pour ce tournoi
     */
    public int getMaxRounds() {
        int numPlayers = getParticipants().size();
        return (int) Math.ceil(Math.log(numPlayers) / Math.log(2));
    }
    
    /**
     * Détermine si le tournoi est terminé
     */
    public boolean isComplete() {
        // Le tournoi est terminé quand il y a un match de la dernière ronde qui est complété
        int finalRound = getMaxRounds();
        return getMatches().stream()
                .anyMatch(m -> m.getRound() == finalRound && m.getStatus() == Match.MatchStatus.COMPLETED);
    }
    
    @Override
    public String toString() {
        return "SingleEliminationTournament{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", participants=" + getParticipants().size() +
                ", maxRounds=" + getMaxRounds() +
                '}';
    }
}
