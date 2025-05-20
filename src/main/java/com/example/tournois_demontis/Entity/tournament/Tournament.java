package com.example.tournois_demontis.Entity.tournament;

import com.example.tournois_demontis.Entity.game.Game;
import com.example.tournois_demontis.Entity.match.Match;
import com.example.tournois_demontis.Entity.player.Player;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "tournaments")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tournament_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Tournament {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @NotNull
    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;
    
    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Min(2)
    @Column(name = "max_participants")
    private Integer maxParticipants;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TournamentStatus status = TournamentStatus.PENDING;
    
    @ManyToMany
    @JoinTable(
            name = "tournament_participants",
            joinColumns = @JoinColumn(name = "tournament_id"),
            inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    private Set<Player> participants = new HashSet<>();
    
    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Match> matches = new HashSet<>();
    
    @ManyToOne
    @JoinColumn(name = "winner_id")
    private Player winner;
    
    // Constructeur vide requis par JPA
    public Tournament() {
    }
    
    // Constructeur avec paramètres de base
    public Tournament(String name, Game game, LocalDateTime startDate) {
        this.name = name;
        this.game = game;
        this.startDate = startDate;
    }
    
    // Constructeur complet
    public Tournament(String name, String description, Game game, 
                      LocalDateTime startDate, LocalDateTime endDate, Integer maxParticipants) {
        this.name = name;
        this.description = description;
        this.game = game;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxParticipants = maxParticipants;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Game getGame() {
        return game;
    }
    
    public void setGame(Game game) {
        this.game = game;
    }
    
    public LocalDateTime getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    
    public LocalDateTime getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    
    public Integer getMaxParticipants() {
        return maxParticipants;
    }
    
    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }
    
    public TournamentStatus getStatus() {
        return status;
    }
    
    public void setStatus(TournamentStatus status) {
        this.status = status;
    }
    
    public Set<Player> getParticipants() {
        return participants;
    }
    
    public void setParticipants(Set<Player> participants) {
        this.participants = participants;
    }
    
    public Set<Match> getMatches() {
        return matches;
    }
    
    public void setMatches(Set<Match> matches) {
        this.matches = matches;
    }
    
    public Player getWinner() {
        return winner;
    }
    
    public void setWinner(Player winner) {
        this.winner = winner;
    }
    
    // Méthodes utilitaires
    public void addParticipant(Player player) {
        if (maxParticipants == null || participants.size() < maxParticipants) {
            participants.add(player);
            player.getTournaments().add(this);
            
            // Mettre à jour les statistiques du joueur
            if (player.getStats() != null) {
                player.getStats().recordTournamentJoined();
            }
        }
    }
    
    public void removeParticipant(Player player) {
        participants.remove(player);
        player.getTournaments().remove(this);
    }
    
    public void addMatch(Match match) {
        matches.add(match);
        match.setTournament(this);
    }
    
    public void removeMatch(Match match) {
        matches.remove(match);
        match.setTournament(null);
    }
    
    public boolean isRegistrationOpen() {
        return status == TournamentStatus.PENDING && 
               (maxParticipants == null || participants.size() < maxParticipants);
    }
    
    public boolean canStart() {
        return status == TournamentStatus.PENDING && participants.size() >= 2;
    }
    
    public void startTournament() {
        if (canStart()) {
            this.status = TournamentStatus.IN_PROGRESS;
            generateMatches();
        }
    }
    
    public void endTournament(Player winner) {
        this.status = TournamentStatus.COMPLETED;
        this.winner = winner;
        this.endDate = LocalDateTime.now();
        
        // Mettre à jour les statistiques du gagnant
        if (winner != null && winner.getStats() != null) {
            winner.getStats().recordTournamentWon();
        }
    }
    
    // Méthode à implémenter par les sous-classes
    protected abstract void generateMatches();
    
    // equals et hashCode basés sur l'identité métier
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tournament that = (Tournament) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Tournament{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", game=" + (game != null ? game.getName() : "null") +
                ", status=" + status +
                ", participants=" + participants.size() +
                '}';
    }
    
    // Énumération pour le statut du tournoi
    public enum TournamentStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }
}
