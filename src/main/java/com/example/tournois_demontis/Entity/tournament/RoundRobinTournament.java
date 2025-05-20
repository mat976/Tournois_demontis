package com.example.tournois_demontis.Entity.tournament;

import com.example.tournois_demontis.Entity.game.Game;
import com.example.tournois_demontis.Entity.match.Match;
import com.example.tournois_demontis.Entity.player.Player;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@DiscriminatorValue("ROUND_ROBIN")
public class RoundRobinTournament extends Tournament {
    
    @Column(name = "num_rounds")
    private Integer numberOfRounds;
    
    @Column(name = "points_for_win")
    private Integer pointsForWin = 3;
    
    @Column(name = "points_for_draw")
    private Integer pointsForDraw = 1;
    
    @Column(name = "points_for_loss")
    private Integer pointsForLoss = 0;
    
    // Constructeur vide requis par JPA
    public RoundRobinTournament() {
        super();
    }
    
    // Constructeur avec paramètres de base
    public RoundRobinTournament(String name, Game game, LocalDateTime startDate) {
        super(name, game, startDate);
    }
    
    // Constructeur complet
    public RoundRobinTournament(String name, String description, Game game, 
                               LocalDateTime startDate, LocalDateTime endDate, Integer maxParticipants,
                               Integer numberOfRounds) {
        super(name, description, game, startDate, endDate, maxParticipants);
        this.numberOfRounds = numberOfRounds;
    }
    
    // Getters et Setters spécifiques
    public Integer getNumberOfRounds() {
        return numberOfRounds;
    }
    
    public void setNumberOfRounds(Integer numberOfRounds) {
        this.numberOfRounds = numberOfRounds;
    }
    
    public Integer getPointsForWin() {
        return pointsForWin;
    }
    
    public void setPointsForWin(Integer pointsForWin) {
        this.pointsForWin = pointsForWin;
    }
    
    public Integer getPointsForDraw() {
        return pointsForDraw;
    }
    
    public void setPointsForDraw(Integer pointsForDraw) {
        this.pointsForDraw = pointsForDraw;
    }
    
    public Integer getPointsForLoss() {
        return pointsForLoss;
    }
    
    public void setPointsForLoss(Integer pointsForLoss) {
        this.pointsForLoss = pointsForLoss;
    }
    
    /**
     * Génère les matchs pour un tournoi Round Robin.
     * Dans ce format, chaque joueur affronte tous les autres joueurs.
     */
    @Override
    protected void generateMatches() {
        List<Player> playerList = new ArrayList<>(getParticipants());
        int numPlayers = playerList.size();
        
        // Décider du nombre de tours si non spécifié
        if (numberOfRounds == null) {
            // Par défaut, chaque joueur rencontre chaque autre joueur une fois
            numberOfRounds = 1;
        }
        
        LocalDateTime matchTime = getStartDate();
        int matchNumber = 1;
        
        // Algorithme de Round Robin classique
        for (int round = 1; round <= numberOfRounds; round++) {
            for (int i = 0; i < numPlayers; i++) {
                for (int j = i + 1; j < numPlayers; j++) {
                    Match match = new Match();
                    match.setTournament(this);
                    match.setGame(getGame());
                    match.setRound(round);
                    match.setMatchNumber(matchNumber++);
                    match.setScheduledTime(matchTime);
                    match.addPlayer(playerList.get(i));
                    match.addPlayer(playerList.get(j));
                    
                    // Ajouter 30 minutes pour le match suivant
                    matchTime = matchTime.plusMinutes(30);
                    
                    addMatch(match);
                }
            }
        }
    }
    
    /**
     * Calcule le classement actuel du tournoi
     * @return Map avec les joueurs et leurs points
     */
    public Map<Player, Integer> getCurrentStandings() {
        Map<Player, Integer> standings = new HashMap<>();
        
        // Initialiser les points à 0 pour tous les participants
        for (Player player : getParticipants()) {
            standings.put(player, 0);
        }
        
        // Parcourir tous les matches terminés et attribuer les points
        for (Match match : getMatches()) {
            if (match.getStatus() == Match.MatchStatus.COMPLETED) {
                Player winner = match.getWinner();
                
                if (winner != null) {
                    // Victoire
                    standings.put(winner, standings.get(winner) + pointsForWin);
                    
                    // Attribution des points de défaite pour les perdants
                    for (Player player : match.getPlayers()) {
                        if (!player.equals(winner)) {
                            standings.put(player, standings.get(player) + pointsForLoss);
                        }
                    }
                } else if (match.getStatus() == Match.MatchStatus.COMPLETED) {
                    // Match nul
                    for (Player player : match.getPlayers()) {
                        standings.put(player, standings.get(player) + pointsForDraw);
                    }
                }
            }
        }
        
        return standings;
    }
    
    /**
     * Détermine le vainqueur du tournoi en fonction du plus grand nombre de points
     */
    public Player determineWinner() {
        Map<Player, Integer> standings = getCurrentStandings();
        
        Player currentLeader = null;
        int highestPoints = -1;
        
        for (Map.Entry<Player, Integer> entry : standings.entrySet()) {
            if (entry.getValue() > highestPoints) {
                highestPoints = entry.getValue();
                currentLeader = entry.getKey();
            }
        }
        
        return currentLeader;
    }
    
    /**
     * Vérifie si tous les matchs sont terminés
     */
    public boolean isAllMatchesCompleted() {
        return getMatches().stream()
                .allMatch(m -> m.getStatus() == Match.MatchStatus.COMPLETED);
    }
    
    /**
     * Vérifie si le tournoi est terminé
     */
    @Override
    public boolean canStart() {
        // Un tournoi round robin nécessite au moins 3 joueurs pour être intéressant
        return super.canStart() && getParticipants().size() >= 3;
    }
    
    /**
     * Obtient le nombre de matchs par joueur
     */
    public int getMatchesPerPlayer() {
        int numPlayers = getParticipants().size();
        return (numPlayers - 1) * numberOfRounds;
    }
    
    /**
     * Obtient le nombre total de matchs dans le tournoi
     */
    public int getTotalMatches() {
        int numPlayers = getParticipants().size();
        return (numPlayers * (numPlayers - 1) * numberOfRounds) / 2;
    }
    
    @Override
    public String toString() {
        return "RoundRobinTournament{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", participants=" + getParticipants().size() +
                ", numberOfRounds=" + numberOfRounds +
                ", totalMatches=" + getTotalMatches() +
                '}';
    }
}
