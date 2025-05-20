package com.example.tournois_demontis.Entity.tournament;

/**
 * Énumération représentant les différents états possibles d'un tournoi
 */
public enum TournamentStatus {
    PENDING,               // En attente
    REGISTRATION_OPEN,     // Inscriptions ouvertes
    REGISTRATION_CLOSED,   // Inscriptions fermées
    IN_PROGRESS,           // Tournoi en cours
    COMPLETED,             // Tournoi terminé
    CANCELLED              // Tournoi annulé
}
