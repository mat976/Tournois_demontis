package com.example.tournois_demontis.Entity.score;

import com.example.tournois_demontis.Entity.game.Game;
import com.example.tournois_demontis.Entity.match.Match;
import com.example.tournois_demontis.Entity.player.Player;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "scores")
public class Score {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(nullable = false)
    private Integer value;
    
    @Column(nullable = false)
    private LocalDateTime recordedAt;
    
    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;
    
    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;
    
    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;
    
    @Column(length = 500)
    private String details;
    
    // Constructeur vide requis par JPA
    public Score() {
    }
    
    // Constructeur avec paramètres
    public Score(Integer value, Player player, Game game) {
        this.value = value;
        this.player = player;
        this.game = game;
        this.recordedAt = LocalDateTime.now();
    }
    
    // Constructeur pour un score dans un match
    public Score(Integer value, Player player, Match match) {
        this.value = value;
        this.player = player;
        this.match = match;
        this.game = match.getGame();
        this.recordedAt = LocalDateTime.now();
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getValue() {
        return value;
    }
    
    public void setValue(Integer value) {
        this.value = value;
    }
    
    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }
    
    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public void setPlayer(Player player) {
        this.player = player;
    }
    
    public Game getGame() {
        return game;
    }
    
    public void setGame(Game game) {
        this.game = game;
    }
    
    public Match getMatch() {
        return match;
    }
    
    public void setMatch(Match match) {
        this.match = match;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
    
    // equals et hashCode basés sur l'identité métier
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Score score = (Score) o;
        return Objects.equals(id, score.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Score{" +
                "id=" + id +
                ", value=" + value +
                ", player=" + (player != null ? player.getId() : "null") +
                ", game=" + (game != null ? game.getId() : "null") +
                ", recordedAt=" + recordedAt +
                '}';
    }
}
