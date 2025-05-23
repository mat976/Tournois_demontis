package com.example.tournois_demontis.Controller;

import com.example.tournois_demontis.Entity.game.Game;
import com.example.tournois_demontis.Entity.player.Player;
import com.example.tournois_demontis.Entity.player.User;
import com.example.tournois_demontis.Entity.tournament.DoubleEliminationTournament;
import com.example.tournois_demontis.Entity.tournament.RoundRobinTournament;
import com.example.tournois_demontis.Entity.tournament.SingleEliminationTournament;
import com.example.tournois_demontis.Entity.tournament.Tournament;
import com.example.tournois_demontis.Service.GameService;
import com.example.tournois_demontis.Service.TournamentService;
import com.example.tournois_demontis.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/tournaments")
public class TournamentController {
    
    /**
     * Helper method to create breadcrumb items for tournament pages
     * @param currentPage The current page name
     * @param tournament The tournament object (can be null for list and new pages)
     * @return List of breadcrumb items with label and URL
     */
    private List<Map<String, String>> createBreadcrumbItems(String currentPage, Tournament tournament) {
        List<Map<String, String>> items = new ArrayList<>();
        
        // Home item
        Map<String, String> homeItem = new HashMap<>();
        homeItem.put("label", "Accueil");
        homeItem.put("url", "/");
        items.add(homeItem);
        
        // Tournaments list item
        Map<String, String> tournamentsItem = new HashMap<>();
        tournamentsItem.put("label", "Tournois");
        
        // Only add URL if we're not on the tournaments list page
        if (!"list".equals(currentPage)) {
            tournamentsItem.put("url", "/tournaments");
        } else {
            tournamentsItem.put("url", null);
        }
        items.add(tournamentsItem);
        
        // Add tournament-specific items if applicable
        if (tournament != null && tournament.getId() != null) {
            Map<String, String> tournamentItem = new HashMap<>();
            tournamentItem.put("label", tournament.getName());
            
            // If we're on the detail page, don't add URL
            if (!"detail".equals(currentPage)) {
                tournamentItem.put("url", "/tournaments/" + tournament.getId());
            } else {
                tournamentItem.put("url", null);
            }
            items.add(tournamentItem);
        }
        
        // Add action item if needed (create/edit)
        if ("form".equals(currentPage)) {
            Map<String, String> actionItem = new HashMap<>();
            if (tournament != null && tournament.getId() != null) {
                actionItem.put("label", "Modifier");
            } else {
                actionItem.put("label", "Créer");
            }
            actionItem.put("url", null); // Current page, no URL
            items.add(actionItem);
        }
        
        return items;
    }

    private final TournamentService tournamentService;
    private final UserService userService;
    private final GameService gameService;

    @Autowired
    public TournamentController(TournamentService tournamentService, UserService userService, GameService gameService) {
        this.tournamentService = tournamentService;
        this.userService = userService;
        this.gameService = gameService;
    }

    @GetMapping
    public String listTournaments(Model model) {
        List<Tournament> tournaments = tournamentService.findAll();
        
        // Logs pour vérifier les tournois récupérés
        System.out.println("=== LISTE DES TOURNOIS ===");
        System.out.println("Nombre de tournois récupérés: " + tournaments.size());
        for (Tournament t : tournaments) {
            System.out.println("Tournoi ID: " + t.getId() + ", Nom: " + t.getName() + ", Type: " + t.getClass().getSimpleName());
        }
        
        model.addAttribute("tournaments", tournaments);
        
        // Ajout de la liste des utilisateurs au modèle
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        
        // Ajout des éléments du fil d'Ariane
        model.addAttribute("breadcrumbItems", createBreadcrumbItems("list", null));
        
        return "tournament/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model, @RequestParam(value = "type", required = false, defaultValue = "single") String type) {
        // Stocker uniquement le type de tournoi dans le modèle
        model.addAttribute("tournamentType", type);
        
        // Créer le bon type de tournoi en fonction du paramètre
        Tournament tournament;
        switch (type.toLowerCase()) {
            case "double":
                tournament = new DoubleEliminationTournament();
                break;
            case "roundrobin":
                tournament = new RoundRobinTournament();
                break;
            case "single":
            default:
                tournament = new SingleEliminationTournament();
                break;
        }
        
        // Définir le statut initial
        tournament.setStatus(Tournament.TournamentStatus.PENDING);
        model.addAttribute("tournament", tournament);
        
        // Ajout de la liste des utilisateurs et des jeux au modèle
        List<User> users = userService.findAll();
        List<Game> games = gameService.findAll();
        model.addAttribute("users", users);
        model.addAttribute("games", games);
        
        // Ajout des éléments du fil d'Ariane
        model.addAttribute("breadcrumbItems", createBreadcrumbItems("form", tournament));
        
        return "tournament/form";
    }

    @PostMapping
    @Transactional
    public String createTournament(@RequestParam(value = "tournamentType", required = false) String tournamentType,
                                 @RequestParam(value = "name", required = false) String name,
                                 @RequestParam(value = "description", required = false) String description,
                                 @RequestParam(value = "game", required = false) Long gameId,
                                 @RequestParam(value = "startDate", required = false) String startDateStr,
                                 @RequestParam(value = "endDate", required = false) String endDateStr,
                                 @RequestParam(value = "maxParticipants", defaultValue = "8") Integer maxParticipants,
                                 @RequestParam(value = "status", defaultValue = "PENDING") String statusStr,
                                 @RequestParam(value = "participantIds", required = false) List<Long> participantIds,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        
        try {
            // Logs de débogage pour voir les valeurs reçues
            System.out.println("=== DEBUG CRÉATION TOURNOI ===");
            System.out.println("Type: " + tournamentType);
            System.out.println("Nom: " + name);
            System.out.println("Description: " + description);
            System.out.println("Game ID: " + gameId);
            System.out.println("Date début: " + startDateStr);
            System.out.println("Date fin: " + endDateStr);
            System.out.println("Max participants: " + maxParticipants);
            System.out.println("Status: " + statusStr);
            System.out.println("Participants IDs: " + participantIds);
            
            // Vérifier si des paramètres essentiels sont manquants
            if (tournamentType == null || name == null || gameId == null) {
                throw new IllegalArgumentException("Paramètres obligatoires manquants: type de tournoi, nom ou jeu");
            }
            
            // 1. Créer le bon type de tournoi
            Tournament tournament;
            switch (tournamentType.toLowerCase()) {
                case "double":
                    tournament = new DoubleEliminationTournament();
                    break;
                case "roundrobin":
                    tournament = new RoundRobinTournament();
                    break;
                case "single":
                default:
                    tournament = new SingleEliminationTournament();
                    break;
            }
            
            // 2. Définir les propriétés de base
            tournament.setName(name);
            tournament.setDescription(description);
            tournament.setMaxParticipants(maxParticipants);
            
            // 3. Définir le jeu
            Game game = gameService.findById(gameId)
                    .orElseThrow(() -> new IllegalArgumentException("Jeu non trouvé avec l'ID: " + gameId));
            tournament.setGame(game);
            
            // 4. Définir les dates
            System.out.println("Format de la date de début reçue: " + startDateStr);
            if (startDateStr != null && !startDateStr.isEmpty()) {
                try {
                    // Essayer différents formats de date
                    LocalDateTime startDate;
                    try {
                        startDate = LocalDateTime.parse(startDateStr);
                    } catch (Exception e1) {
                        // Si le format ISO standard ne fonctionne pas, essayer un format personnalisé
                        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                        startDate = LocalDateTime.parse(startDateStr, formatter);
                    }
                    tournament.setStartDate(startDate);
                    System.out.println("Date de début définie: " + startDate);
                } catch (Exception e) {
                    System.out.println("Erreur lors du parsing de la date de début: " + e.getMessage());
                    throw new IllegalArgumentException("Format de date de début invalide: " + e.getMessage());
                }
            } else {
                throw new IllegalArgumentException("La date de début est requise");
            }
            
            System.out.println("Format de la date de fin reçue: " + endDateStr);
            if (endDateStr != null && !endDateStr.isEmpty()) {
                try {
                    // Essayer différents formats de date
                    LocalDateTime endDate;
                    try {
                        endDate = LocalDateTime.parse(endDateStr);
                    } catch (Exception e1) {
                        // Si le format ISO standard ne fonctionne pas, essayer un format personnalisé
                        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                        endDate = LocalDateTime.parse(endDateStr, formatter);
                    }
                    tournament.setEndDate(endDate);
                    System.out.println("Date de fin définie: " + endDate);
                } catch (Exception e) {
                    System.out.println("Erreur lors du parsing de la date de fin: " + e.getMessage());
                    // La date de fin est optionnelle, donc on affiche l'erreur mais on ne l'arrête pas
                    System.out.println("La date de fin n'a pas été définie en raison d'une erreur de format");
                }
            }
            
            // 5. Définir le statut
            try {
                Tournament.TournamentStatus status = Tournament.TournamentStatus.valueOf(statusStr);
                tournament.setStatus(status);
            } catch (Exception e) {
                tournament.setStatus(Tournament.TournamentStatus.PENDING);
            }
            
            // 6. Ajouter les participants sélectionnés
            if (participantIds != null && !participantIds.isEmpty()) {
                for (Long userId : participantIds) {
                    userService.findById(userId).ifPresent(user -> {
                        if (user instanceof Player) {
                            tournament.addParticipant((Player) user);
                        }
                    });
                }
            }
            
            // 7. Sauvegarder le tournoi
            try {
                System.out.println("=== SAUVEGARDE DU TOURNOI ===");
                System.out.println("Tournoi avant sauvegarde: " + tournament.getName() + ", Type: " + tournament.getClass().getSimpleName());
                
                // Vérifier que toutes les propriétés requises sont définies
                if (tournament.getName() == null || tournament.getName().isEmpty()) {
                    throw new IllegalArgumentException("Le nom du tournoi est requis");
                }
                if (tournament.getGame() == null) {
                    throw new IllegalArgumentException("Le jeu est requis");
                }
                if (tournament.getStartDate() == null) {
                    throw new IllegalArgumentException("La date de début est requise");
                }
                
                // Sauvegarder le tournoi
                Tournament savedTournament = tournamentService.save(tournament);
                System.out.println("Tournoi sauvegardé avec ID: " + savedTournament.getId());
                
                redirectAttributes.addFlashAttribute("success", "Tournoi créé avec succès!");
                return "redirect:/tournaments";
            } catch (Exception e) {
                System.out.println("=== ERREUR LORS DE LA SAUVEGARDE DU TOURNOI ===");
                System.out.println("Message d'erreur: " + e.getMessage());
                e.printStackTrace();
                
                model.addAttribute("error", "Erreur lors de la création du tournoi: " + e.getMessage());
                
                // Recharger les listes pour le formulaire
                List<User> users = userService.findAll();
                List<Game> games = gameService.findAll();
                model.addAttribute("users", users);
                model.addAttribute("games", games);
                model.addAttribute("tournament", tournament);
                model.addAttribute("tournamentType", tournamentType);
                
                return "tournament/form";
            }
            
        } catch (Exception e) {
            // En cas d'erreur, recharger le formulaire avec les données et un message d'erreur
            model.addAttribute("error", "Erreur lors de la création du tournoi: " + e.getMessage());
            
            // Recréer un tournoi du bon type
            Tournament tournament;
            switch (tournamentType.toLowerCase()) {
                case "double":
                    tournament = new DoubleEliminationTournament();
                    break;
                case "roundrobin":
                    tournament = new RoundRobinTournament();
                    break;
                case "single":
                default:
                    tournament = new SingleEliminationTournament();
                    break;
            }
            
            // Recharger les données de base
            tournament.setName(name);
            tournament.setDescription(description);
            tournament.setMaxParticipants(maxParticipants);
            
            // Recharger les listes pour le formulaire
            List<User> users = userService.findAll();
            List<Game> games = gameService.findAll();
            model.addAttribute("users", users);
            model.addAttribute("games", games);
            model.addAttribute("tournament", tournament);
            model.addAttribute("tournamentType", tournamentType);
            
            return "tournament/form";
        }
    }

    @GetMapping("/{id}")
    public String showTournamentDetails(@PathVariable("id") Long id, Model model) {
        Tournament tournament = tournamentService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid tournament Id:" + id));
        model.addAttribute("tournament", tournament);
        
        // Ajout des éléments du fil d'Ariane
        model.addAttribute("breadcrumbItems", createBreadcrumbItems("detail", tournament));
        
        return "tournament/detail";
    }

    @GetMapping("/{id}/edit")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        Tournament tournament = tournamentService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid tournament Id:" + id));
        model.addAttribute("tournament", tournament);
        
        // Déterminer le type de tournoi
        String tournamentType = "single";
        if (tournament instanceof DoubleEliminationTournament) {
            tournamentType = "double";
        } else if (tournament instanceof RoundRobinTournament) {
            tournamentType = "roundrobin";
        }
        model.addAttribute("tournamentType", tournamentType);
        
        // Ajout de la liste des utilisateurs et des jeux au modèle
        List<User> users = userService.findAll();
        List<Game> games = gameService.findAll();
        model.addAttribute("users", users);
        model.addAttribute("games", games);
        
        // Ajout des éléments du fil d'Ariane
        model.addAttribute("breadcrumbItems", createBreadcrumbItems("form", tournament));
        
        return "tournament/form";
    }

    @PostMapping("/{id}")
    public String updateTournament(@PathVariable("id") Long id, 
                                   @Valid @ModelAttribute("tournament") Tournament tournament,
                                   BindingResult result, Model model,
                                   @RequestParam(value = "participantIds", required = false) List<Long> participantIds,
                                   @RequestParam(value = "tournamentType", required = false) String tournamentType) {
        if (result.hasErrors()) {
            tournament.setId(id);
            // Rechargement de la liste des utilisateurs et des jeux en cas d'erreur
            List<User> users = userService.findAll();
            List<Game> games = gameService.findAll();
            model.addAttribute("users", users);
            model.addAttribute("games", games);
            model.addAttribute("tournamentType", tournamentType);
            return "tournament/form";
        }
        
        // Récupérer le tournoi existant pour conserver les relations
        Tournament existingTournament = tournamentService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid tournament Id:" + id));
        
        // Conserver les matchs existants
        tournament.setMatches(existingTournament.getMatches());
        
        // Mettre à jour les participants
        if (participantIds != null) {
            Set<Player> participants = new HashSet<>();
            for (Long userId : participantIds) {
                userService.findById(userId).ifPresent(user -> {
                    if (user instanceof Player) {
                        participants.add((Player) user);
                    }
                });
            }
            tournament.setParticipants(participants);
        } else {
            // Si aucun participant n'est sélectionné, conserver les participants existants
            tournament.setParticipants(existingTournament.getParticipants());
        }
        
        tournamentService.save(tournament);
        return "redirect:/tournaments";
    }

    @GetMapping("/{id}/delete")
    public String deleteTournament(@PathVariable("id") Long id) {
        tournamentService.deleteById(id);
        return "redirect:/tournaments";
    }
    
    @GetMapping("/{id}/start")
    public String startTournament(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            tournamentService.startTournament(id);
            redirectAttributes.addFlashAttribute("success", "Le tournoi a été démarré avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Impossible de démarrer le tournoi: " + e.getMessage());
        }
        return "redirect:/tournaments/" + id;
    }
    
    @GetMapping("/{id}/close-registration")
    public String closeRegistration(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Tournament tournament = tournamentService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Tournoi non trouvé avec l'ID: " + id));
            
            if (tournament.getStatus() != Tournament.TournamentStatus.PENDING) {
                throw new IllegalStateException("Les inscriptions pour ce tournoi ne sont pas ouvertes");
            }
            
            tournament.setStatus(Tournament.TournamentStatus.IN_PROGRESS);
            tournamentService.save(tournament);
            
            redirectAttributes.addFlashAttribute("success", "Les inscriptions sont maintenant fermées.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Impossible de fermer les inscriptions: " + e.getMessage());
        }
        return "redirect:/tournaments/" + id;
    }
    
    @GetMapping("/{id}/cancel")
    public String cancelTournament(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            tournamentService.cancelTournament(id);
            redirectAttributes.addFlashAttribute("success", "Le tournoi a été annulé.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Impossible d'annuler le tournoi: " + e.getMessage());
        }
        return "redirect:/tournaments/" + id;
    }
    
    @PostMapping("/{tournamentId}/register-player/{playerId}")
    public String registerPlayer(@PathVariable("tournamentId") Long tournamentId, 
                                @PathVariable("playerId") Long playerId,
                                RedirectAttributes redirectAttributes) {
        try {
            tournamentService.addParticipant(tournamentId, playerId);
            redirectAttributes.addFlashAttribute("success", "Joueur inscrit avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Impossible d'inscrire le joueur: " + e.getMessage());
        }
        return "redirect:/tournaments/" + tournamentId;
    }
}
