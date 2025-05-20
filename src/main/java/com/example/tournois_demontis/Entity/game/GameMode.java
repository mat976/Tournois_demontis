package com.example.tournois_demontis.Entity.game;

/**
 * Énumération des différents modes de jeu possibles.
 */
public enum GameMode {
    SOLO,
    MULTIPLAYER,
    TEAM_BASED,
    COOPERATIVE,
    COMPETITIVE;
    
    // Vous pouvez ajouter des méthodes utilitaires si nécessaire
    
    public boolean isTeamBased() {
        return this == TEAM_BASED;
    }
    
    public boolean isCompetitive() {
        return this == COMPETITIVE || this == TEAM_BASED;
    }
    
    public boolean isCooperative() {
        return this == COOPERATIVE;
    }
}
