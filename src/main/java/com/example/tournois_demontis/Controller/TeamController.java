package com.example.tournois_demontis.Controller;

import com.example.tournois_demontis.Entity.team.Team;
import com.example.tournois_demontis.Entity.player.Player;
import com.example.tournois_demontis.Repository.PlayerRepository;
import com.example.tournois_demontis.Service.TeamService;
import com.example.tournois_demontis.Service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/teams")
public class TeamController {
    
    /**
     * Helper method to create breadcrumb items for team pages
     * @param currentPage The current page name
     * @param team The team object (can be null for list and new pages)
     * @return List of breadcrumb items with label and URL
     */
    private List<Map<String, String>> createBreadcrumbItems(String currentPage, Team team) {
        List<Map<String, String>> items = new ArrayList<>();
        
        // Home item
        Map<String, String> homeItem = new HashMap<>();
        homeItem.put("label", "Accueil");
        homeItem.put("url", "/");
        items.add(homeItem);
        
        // Teams list item
        Map<String, String> teamsItem = new HashMap<>();
        teamsItem.put("label", "Équipes");
        
        // Only add URL if we're not on the teams list page
        if (!"list".equals(currentPage)) {
            teamsItem.put("url", "/teams");
        } else {
            teamsItem.put("url", null);
        }
        items.add(teamsItem);
        
        // Add team-specific items if applicable
        if (team != null && team.getId() != null) {
            Map<String, String> teamItem = new HashMap<>();
            teamItem.put("label", team.getName());
            
            // If we're on the detail page, don't add URL
            if (!"detail".equals(currentPage)) {
                teamItem.put("url", "/teams/" + team.getId());
            } else {
                teamItem.put("url", null);
            }
            items.add(teamItem);
        }
        
        // Add action item if needed (create/edit)
        if ("form".equals(currentPage)) {
            Map<String, String> actionItem = new HashMap<>();
            if (team != null && team.getId() != null) {
                actionItem.put("label", "Modifier");
            } else {
                actionItem.put("label", "Créer");
            }
            actionItem.put("url", null); // Current page, no URL
            items.add(actionItem);
        }
        
        return items;
    }
    @Autowired
    private TeamService teamService;
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private PlayerRepository playerRepository;

    @GetMapping
    public String listTeams(Model model) {
        model.addAttribute("teams", teamService.findAll());
        
        // Ajout des éléments du fil d'Ariane
        model.addAttribute("breadcrumbItems", createBreadcrumbItems("list", null));
        
        return "team/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        Team team = new Team();
        team.setPlayers(new HashSet<>()); // Initialiser la liste des joueurs
        model.addAttribute("team", team);
        
        // Récupérer tous les joueurs et les ajouter au modèle
        Set<Player> allPlayers = new HashSet<>(playerService.findAll());
        model.addAttribute("allPlayers", allPlayers);
        
        // Ajouter une méthode utilitaire pour vérifier si un joueur est dans l'équipe
        model.addAttribute("isPlayerInTeam", (java.util.function.BiFunction<Team, Long, Boolean>) (t, playerId) -> {
            if (t == null || t.getPlayers() == null || playerId == null) {
                return false;
            }
            return t.getPlayers().stream().anyMatch(p -> p.getId().equals(playerId));
        });
        
        // Ajout des éléments du fil d'Ariane
        model.addAttribute("breadcrumbItems", createBreadcrumbItems("form", team));
        
        return "team/form";
    }

    @PostMapping("/new")
    public String createTeam(
            @ModelAttribute("team") Team team,
            BindingResult result,
            @RequestParam(value = "players", required = false) Set<Long> playerIds,
            @RequestParam(value = "captainId", required = false) Long captainId,
            @RequestParam(value = "logo", required = false) MultipartFile logo,
            Model model) {
            
        // Initialiser la liste des joueurs si elle est nulle
        if (playerIds == null) {
            playerIds = new HashSet<>();
        }
        
        // Validation manuelle
        boolean hasErrors = false;
        
        // Valider le nom de l'équipe
        if (team.getName() == null || team.getName().trim().isEmpty()) {
            result.rejectValue("name", "required.team.name", "Le nom de l'équipe est obligatoire");
            hasErrors = true;
        } else if (teamService.existsByName(team.getName())) {
            result.rejectValue("name", "duplicate.team.name", "Une équipe avec ce nom existe déjà");
            hasErrors = true;
        }
        
        // Valider et définir le capitaine
        if (captainId == null || captainId <= 0) {
            model.addAttribute("captainError", "Veuillez sélectionner un capitaine");
            hasErrors = true;
        } else {
            try {
                Player captain = playerRepository.findById(captainId).orElse(null);
                if (captain == null) {
                    model.addAttribute("captainError", "Le capitaine sélectionné n'existe pas");
                    hasErrors = true;
                } else {
                    team.setCaptain(captain);
                    playerIds.add(captainId); // Ajouter le capitaine aux joueurs
                }
            } catch (Exception e) {
                model.addAttribute("captainError", "Erreur lors de la récupération du capitaine");
                hasErrors = true;
            }
        }
        
        // Valider les joueurs sélectionnés
        if (!playerIds.isEmpty()) {
            long existingPlayersCount = playerRepository.countByIdIn(playerIds);
            if (existingPlayersCount != playerIds.size()) {
                result.rejectValue("players", "invalid.players", "Un ou plusieurs joueurs sélectionnés n'existent pas");
                hasErrors = true;
            }
        }
        
        if (hasErrors || result.hasErrors()) {
            model.addAttribute("allPlayers", playerRepository.findAll());
            return "team/form";
        }
        
        try {
            
            // Gérer le logo
            if (logo != null && !logo.isEmpty()) {
                // Ici, vous pourriez ajouter la logique pour sauvegarder le fichier
                team.setLogoUrl(logo.getOriginalFilename());
            }
            
            // Sauvegarder l'équipe
            Team savedTeam = teamService.save(team, playerIds);
            return "redirect:/teams/" + savedTeam.getId();
            
        } catch (Exception e) {
            model.addAttribute("error", "Une erreur est survenue lors de la création de l'équipe: " + e.getMessage());
            model.addAttribute("allPlayers", playerRepository.findAll());
            return "team/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Team> teamOpt = teamService.findById(id);
        if (teamOpt.isEmpty()) {
            return "redirect:/teams";
        }
        Team team = teamOpt.get();
        
        // Charger explicitement les joueurs de l'équipe
        Set<Player> players = team.getPlayers();
        if (players != null) {
            // Initialiser la collection si nécessaire
            players.size(); // Force le chargement des joueurs
        }
        
        // Récupérer tous les joueurs et les ajouter au modèle
        Set<Player> allPlayers = new HashSet<>(playerService.findAll());
        
        model.addAttribute("team", team);
        model.addAttribute("allPlayers", allPlayers);
        
        // Ajouter la même méthode utilitaire que dans showCreateForm
        model.addAttribute("isPlayerInTeam", (java.util.function.BiFunction<Team, Long, Boolean>) (t, playerId) -> {
            if (t == null || t.getPlayers() == null || playerId == null) {
                return false;
            }
            return t.getPlayers().stream().anyMatch(p -> p.getId().equals(playerId));
        });
        
        return "team/form";
    }

    @PostMapping("/{id}/edit")
    public String updateTeam(
            @PathVariable Long id,
            @Valid @ModelAttribute("team") Team team,
            BindingResult result,
            @RequestParam(value = "players", required = false) Set<Long> playerIds,
            @RequestParam(value = "captainId", required = false) Long captainId,
            @RequestParam(value = "logo", required = false) MultipartFile logo,
            Model model) {
            
        // Vérifications personnalisées
        Optional<Team> existingTeamOpt = teamService.findById(id);
        if (existingTeamOpt.isPresent()) {
            Team existingTeam = existingTeamOpt.get();
            // Vérifier si le nom est déjà utilisé par une autre équipe
            if (!existingTeam.getName().equals(team.getName()) && 
                teamService.existsByName(team.getName())) {
                result.rejectValue("name", "duplicate.team.name", "Une équipe avec ce nom existe déjà");
            }
            
            if (result.hasErrors()) {
                model.addAttribute("allPlayers", playerService.findAll());
                return "team/form";
            }
            
            // Gérer le capitaine
            if (captainId != null) {
                Optional<Player> captain = playerService.findById(captainId);
                captain.ifPresent(team::setCaptain);
            } else {
                // Conserver le capitaine existant si non modifié
                team.setCaptain(existingTeam.getCaptain());
            }
            
            // Gérer le logo
            if (logo != null && !logo.isEmpty()) {
                team.setLogoUrl(logo.getOriginalFilename());
            } else {
                // Conserver le logo existant si non modifié
                team.setLogoUrl(existingTeam.getLogoUrl());
            }
            
            team.setId(id);
            teamService.save(team, playerIds != null ? playerIds : new HashSet<>());
            return "redirect:/teams";
        }
        
        return "redirect:/teams";
    }

    @GetMapping("/{id}")
    public String viewTeam(@PathVariable Long id, Model model) {
        Optional<Team> teamOpt = teamService.findById(id);
        if (teamOpt.isEmpty()) {
            return "redirect:/teams";
        }
        model.addAttribute("team", teamOpt.get());
        return "team/detail";
    }

    @GetMapping("/{id}/delete")
    public String deleteTeam(@PathVariable Long id) {
        teamService.deleteById(id);
        return "redirect:/teams";
    }
    
    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("isPlayerInTeam", (java.util.function.BiFunction<Team, Long, Boolean>) (team, playerId) -> {
            if (team == null || team.getPlayers() == null || playerId == null) {
                return false;
            }
            return team.getPlayers().stream().anyMatch(p -> p.getId().equals(playerId));
        });
    }
}
