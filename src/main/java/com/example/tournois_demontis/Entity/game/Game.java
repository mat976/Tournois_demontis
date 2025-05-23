package com.example.tournois_demontis.Entity.game;

import com.example.tournois_demontis.Entity.match.Match;
import com.example.tournois_demontis.Entity.player.Player;
import com.example.tournois_demontis.Entity.score.Score;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "games")
public class Game {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameMode gameMode;
    
    @Column(length = 1000)
    private String description;
    
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;
    
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Match> matches = new HashSet<>();
    
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Score> scores = new HashSet<>();
    
    @ManyToMany
    @JoinTable(
            name = "game_players",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    private Set<Player> players = new HashSet<>();
    
    // Constructeur vide requis par JPA
    public Game() {
    }
    
    // Constructor avec paramètres
    public Game(String name, GameMode gameMode, String description) {
        this.name = name;
        this.gameMode = gameMode;
        this.description = description;
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public GameMode getGameMode() {
        return gameMode;
    }
    
    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public Set<Match> getMatches() {
        return matches;
    }
    
    public void setMatches(Set<Match> matches) {
        this.matches = matches;
    }
    
    public Set<Score> getScores() {
        return scores;
    }
    
    public void setScores(Set<Score> scores) {
        this.scores = scores;
    }
    
    public Set<Player> getPlayers() {
        return players;
    }
    
    public void setPlayers(Set<Player> players) {
        this.players = players;
    }
    
    // Helper methods
    public void addMatch(Match match) {
        matches.add(match);
        match.setGame(this);
    }
    
    public void removeMatch(Match match) {
        matches.remove(match);
        match.setGame(null);
    }
    
    public void addScore(Score score) {
        scores.add(score);
        score.setGame(this);
    }
    
    public void removeScore(Score score) {
        scores.remove(score);
        score.setGame(null);
    }
    
    public void addPlayer(Player player) {
        players.add(player);
        player.getGames().add(this);
    }
    
    public void removePlayer(Player player) {
        players.remove(player);
        player.getGames().remove(this);
    }
    
    // equals et hashCode basés sur l'identité métier
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return Objects.equals(id, game.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Game{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", gameMode=" + gameMode +
                '}';
    }
}
