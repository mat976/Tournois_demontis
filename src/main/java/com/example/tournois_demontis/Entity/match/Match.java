package com.example.tournois_demontis.Entity.match;

import com.example.tournois_demontis.Entity.game.Game;
import com.example.tournois_demontis.Entity.player.Player;
import com.example.tournois_demontis.Entity.tournament.Tournament;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "matches")
public class Match {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;
    
    @ManyToOne
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;
    
    @Min(1)
    @Column(name = "round")
    private Integer round;
    
    @Column(name = "match_number")
    private Integer matchNumber;
    
    @Column(name = "bracket")
    private String bracket;
    
    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "completion_date")
    private LocalDateTime completionDate;
    
    @Column(name = "score1")
    private Integer score1;
    
    @Column(name = "score2")
    private Integer score2;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MatchStatus status = MatchStatus.PENDING;
    
    @Column(name = "result_details", length = 1000)
    private String resultDetails;
    
    @ManyToMany
    @JoinTable(
            name = "match_players",
            joinColumns = @JoinColumn(name = "match_id"),
            inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    private Set<Player> players = new HashSet<>();
    
    @ManyToOne
    @JoinColumn(name = "winner_id")
    private Player winner;
    
    // Constructeur vide requis par JPA
    public Match() {
    }
    
    // Constructeur avec paramètres de base
    public Match(Game game) {
        this.game = game;
    }
    
    // Constructeur pour match de tournoi
    public Match(Game game, Tournament tournament, Integer round, Integer matchNumber) {
        this.game = game;
        this.tournament = tournament;
        this.round = round;
        this.matchNumber = matchNumber;
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Game getGame() {
        return game;
    }
    
    public void setGame(Game game) {
        this.game = game;
    }
    
    public Tournament getTournament() {
        return tournament;
    }
    
    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }
    
    public Integer getRound() {
        return round;
    }
    
    public void setRound(Integer round) {
        this.round = round;
    }
    
    public Integer getMatchNumber() {
        return matchNumber;
    }
    
    public void setMatchNumber(Integer matchNumber) {
        this.matchNumber = matchNumber;
    }
    
    public String getBracket() {
        return bracket;
    }
    
    public void setBracket(String bracket) {
        this.bracket = bracket;
    }
    
    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }
    
    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public MatchStatus getStatus() {
        return status;
    }
    
    public void setStatus(MatchStatus status) {
        this.status = status;
    }
    
    public String getResultDetails() {
        return resultDetails;
    }
    
    public void setResultDetails(String resultDetails) {
        this.resultDetails = resultDetails;
    }
    
    public Set<Player> getPlayers() {
        return players;
    }
    
    public void setPlayers(Set<Player> players) {
        this.players = players;
    }
    
    public Player getWinner() {
        return winner;
    }
    
    public void setWinner(Player winner) {
        this.winner = winner;
    }
    
    public Integer getScore1() {
        return score1;
    }
    
    public void setScore1(Integer score1) {
        this.score1 = score1;
    }
    
    public Integer getScore2() {
        return score2;
    }
    
    public void setScore2(Integer score2) {
        this.score2 = score2;
    }
    
    public LocalDateTime getCompletionDate() {
        return completionDate;
    }
    
    public void setCompletionDate(LocalDateTime completionDate) {
        this.completionDate = completionDate;
    }
    
    // Helper methods
    public void addPlayer(Player player) {
        players.add(player);
        player.getMatches().add(this);
    }
    
    public void removePlayer(Player player) {
        players.remove(player);
        player.getMatches().remove(this);
    }
    
    // Les méthodes liées aux scores ont été supprimées pour simplifier l'entité
    
    /**
     * Démarre le match, en mettant à jour le statut et l'heure de début
     */
    public void startMatch() {
        this.status = MatchStatus.IN_PROGRESS;
        this.startTime = LocalDateTime.now();
    }
    
    /**
     * Termine le match, en enregistrant le vainqueur et mettant à jour les statistiques des joueurs
     */
    public void endMatch(Player winner, String resultDetails) {
        this.status = MatchStatus.COMPLETED;
        this.winner = winner;
        this.resultDetails = resultDetails;
        this.endTime = LocalDateTime.now();
        
        // Mettre à jour les statistiques des joueurs
        for (Player player : players) {
            if (player.getStats() != null) {
                boolean isWinner = player.equals(winner);
                player.getStats().recordMatchPlayed(isWinner);
            }
        }
    }
    
    /**
     * Annule le match
     */
    public void cancelMatch(String reason) {
        this.status = MatchStatus.CANCELLED;
        this.resultDetails = reason;
    }
    
    /**
     * Vérifie si le match peut commencer
     */
    public boolean canStart() {
        return status == MatchStatus.PENDING && players.size() >= 2;
    }
    
    /**
     * Obtient la durée du match en minutes
     */
    public Long getDurationInMinutes() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMinutes();
        }
        return null;
    }
    
    // equals et hashCode basés sur l'identité métier
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Match match = (Match) o;
        return Objects.equals(id, match.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Match{" +
                "id=" + id +
                ", game=" + (game != null ? game.getName() : "null") +
                ", tournament=" + (tournament != null ? tournament.getName() : "null") +
                ", round=" + round +
                ", matchNumber=" + matchNumber +
                ", status=" + status +
                ", players=" + players.size() +
                '}';
    }
    
    // Énumération pour le statut du match
    public enum MatchStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }
}
