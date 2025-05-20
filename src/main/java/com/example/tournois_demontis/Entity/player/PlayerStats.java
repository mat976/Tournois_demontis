package com.example.tournois_demontis.Entity.player;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

@Entity
@Table(name = "player_stats")
public class PlayerStats {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "player_id", nullable = false, unique = true)
    private Player player;
    
    @Min(0)
    @Column(name = "total_matches", nullable = false)
    private Integer totalMatches = 0;
    
    @Min(0)
    @Column(name = "matches_won", nullable = false)
    private Integer matchesWon = 0;
    
    @Min(0)
    @Column(name = "matches_lost", nullable = false)
    private Integer matchesLost = 0;
    
    @Min(0)
    @Column(name = "tournaments_joined", nullable = false)
    private Integer tournamentsJoined = 0;
    
    @Min(0)
    @Column(name = "tournaments_won", nullable = false)
    private Integer tournamentsWon = 0;
    
    @Min(0)
    @Column(name = "total_points", nullable = false)
    private Integer totalPoints = 0;
    
    @Column(name = "highest_score")
    private Integer highestScore = 0;
    
    @Column(name = "last_active")
    private LocalDateTime lastActive;
    
    @Column(name = "rank")
    private Integer rank;
    
    @Column(name = "current_win_streak")
    private Integer currentWinStreak = 0;
    
    @Column(name = "max_win_streak")
    private Integer maxWinStreak = 0;
    
    // Constructeur vide requis par JPA
    public PlayerStats() {
        this.lastActive = LocalDateTime.now();
    }
    
    // Constructeur avec joueur
    public PlayerStats(Player player) {
        this();
        this.player = player;
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public void setPlayer(Player player) {
        this.player = player;
    }
    
    public Integer getTotalMatches() {
        return totalMatches;
    }
    
    public void setTotalMatches(Integer totalMatches) {
        this.totalMatches = totalMatches;
    }
    
    public Integer getMatchesWon() {
        return matchesWon;
    }
    
    public void setMatchesWon(Integer matchesWon) {
        this.matchesWon = matchesWon;
    }
    
    public Integer getMatchesLost() {
        return matchesLost;
    }
    
    public void setMatchesLost(Integer matchesLost) {
        this.matchesLost = matchesLost;
    }
    
    public Integer getTournamentsJoined() {
        return tournamentsJoined;
    }
    
    public void setTournamentsJoined(Integer tournamentsJoined) {
        this.tournamentsJoined = tournamentsJoined;
    }
    
    public Integer getTournamentsWon() {
        return tournamentsWon;
    }
    
    public void setTournamentsWon(Integer tournamentsWon) {
        this.tournamentsWon = tournamentsWon;
    }
    
    public Integer getTotalPoints() {
        return totalPoints;
    }
    
    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }
    
    public Integer getHighestScore() {
        return highestScore;
    }
    
    public void setHighestScore(Integer highestScore) {
        this.highestScore = highestScore;
    }
    
    public LocalDateTime getLastActive() {
        return lastActive;
    }
    
    public void setLastActive(LocalDateTime lastActive) {
        this.lastActive = lastActive;
    }
    
    public Integer getRank() {
        return rank;
    }
    
    public void setRank(Integer rank) {
        this.rank = rank;
    }
    
    public Integer getCurrentWinStreak() {
        return currentWinStreak;
    }
    
    public void setCurrentWinStreak(Integer currentWinStreak) {
        this.currentWinStreak = currentWinStreak;
    }
    
    public Integer getMaxWinStreak() {
        return maxWinStreak;
    }
    
    public void setMaxWinStreak(Integer maxWinStreak) {
        this.maxWinStreak = maxWinStreak;
    }
    
    // Méthodes utilitaires pour mettre à jour les statistiques
    public void recordMatchPlayed(boolean won) {
        this.totalMatches++;
        if (won) {
            this.matchesWon++;
            this.currentWinStreak++;
            if (this.currentWinStreak > this.maxWinStreak) {
                this.maxWinStreak = this.currentWinStreak;
            }
        } else {
            this.matchesLost++;
            this.currentWinStreak = 0;
        }
        this.lastActive = LocalDateTime.now();
    }
    
    public void recordTournamentJoined() {
        this.tournamentsJoined++;
        this.lastActive = LocalDateTime.now();
    }
    
    public void recordTournamentWon() {
        this.tournamentsWon++;
        this.lastActive = LocalDateTime.now();
    }
    
    public void addPoints(int points) {
        this.totalPoints += points;
        this.lastActive = LocalDateTime.now();
    }
    
    public void updateHighestScore(int score) {
        if (score > this.highestScore) {
            this.highestScore = score;
        }
        this.lastActive = LocalDateTime.now();
    }
    
    // Calcul du ratio de victoire
    public double getWinRatio() {
        if (totalMatches == 0) {
            return 0.0;
        }
        return (double) matchesWon / totalMatches;
    }
    
    @Override
    public String toString() {
        return "PlayerStats{" +
                "player=" + (player != null ? player.getUsername() : "null") +
                ", totalMatches=" + totalMatches +
                ", matchesWon=" + matchesWon +
                ", tournamentsWon=" + tournamentsWon +
                ", winRatio=" + getWinRatio() +
                '}';
    }
}
