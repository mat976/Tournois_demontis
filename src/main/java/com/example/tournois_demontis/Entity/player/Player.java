package com.example.tournois_demontis.Entity.player;

import com.example.tournois_demontis.Entity.game.Game;
import com.example.tournois_demontis.Entity.match.Match;
import com.example.tournois_demontis.Entity.score.Score;
import com.example.tournois_demontis.Entity.tournament.Tournament;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "players")
public class Player extends User {
    
    @Column(name = "nickname")
    private String nickname;
    
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    
    @Column(name = "profile_picture", columnDefinition = "TEXT")
    private String profilePictureUrl;
    
    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private PlayerStats stats;
    
    @ManyToMany(mappedBy = "players")
    private Set<Game> games = new HashSet<>();
    
    @ManyToMany(mappedBy = "participants")
    private Set<Tournament> tournaments = new HashSet<>();
    
    @ManyToMany(mappedBy = "players")
    private Set<Match> matches = new HashSet<>();
    
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Score> scores = new HashSet<>();
    
    // Constructeur vide requis par JPA
    public Player() {
        super();
    }
    
    // Constructeur avec paramètres de base
    public Player(String username, String password, String email) {
        super(username, password, email);
    }
    
    // Constructeur complet
    public Player(String username, String password, String email, String firstName, String lastName,
                  String nickname, LocalDate dateOfBirth) {
        super(username, password, email, firstName, lastName);
        this.nickname = nickname;
        this.dateOfBirth = dateOfBirth;
    }
    
    // Getters et Setters
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }
    
    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
    
    public PlayerStats getStats() {
        return stats;
    }
    
    public void setStats(PlayerStats stats) {
        this.stats = stats;
        if (stats != null) {
            stats.setPlayer(this);
        }
    }
    
    public Set<Game> getGames() {
        return games;
    }
    
    public void setGames(Set<Game> games) {
        this.games = games;
    }
    
    public Set<Tournament> getTournaments() {
        return tournaments;
    }
    
    public void setTournaments(Set<Tournament> tournaments) {
        this.tournaments = tournaments;
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
    
    // Helper methods
    public void addGame(Game game) {
        games.add(game);
        game.getPlayers().add(this);
    }
    
    public void removeGame(Game game) {
        games.remove(game);
        game.getPlayers().remove(this);
    }
    
    public void addTournament(Tournament tournament) {
        tournaments.add(tournament);
        tournament.getParticipants().add(this);
    }
    
    public void removeTournament(Tournament tournament) {
        tournaments.remove(tournament);
        tournament.getParticipants().remove(this);
    }
    
    public void addMatch(Match match) {
        matches.add(match);
        match.getPlayers().add(this);
    }
    
    public void removeMatch(Match match) {
        matches.remove(match);
        match.getPlayers().remove(this);
    }
    
    public void addScore(Score score) {
        scores.add(score);
        score.setPlayer(this);
    }
    
    public void removeScore(Score score) {
        scores.remove(score);
        score.setPlayer(null);
    }
    
    // Initialize player stats if not already present
    public void initializeStats() {
        if (this.stats == null) {
            this.stats = new PlayerStats(this);
        }
    }
    
    @Override
    public String toString() {
        return "Player{" +
                "id=" + getId() +
                ", username='" + getUsername() + '\'' +
                ", nickname='" + nickname + '\'' +
                ", games=" + games.size() +
                ", tournaments=" + tournaments.size() +
                ", matches=" + matches.size() +
                '}';
    }
}
