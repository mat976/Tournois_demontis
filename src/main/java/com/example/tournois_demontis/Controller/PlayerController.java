package com.example.tournois_demontis.Controller;

import com.example.tournois_demontis.Entity.player.Player;
import com.example.tournois_demontis.Service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/players")
public class PlayerController {
    
    /**
     * Helper method to create breadcrumb items for player pages
     * @param currentPage The current page name
     * @param player The player object (can be null for list and new pages)
     * @return List of breadcrumb items with label and URL
     */
    private List<Map<String, String>> createBreadcrumbItems(String currentPage, Player player) {
        List<Map<String, String>> items = new ArrayList<>();
        
        // Home item
        Map<String, String> homeItem = new HashMap<>();
        homeItem.put("label", "Accueil");
        homeItem.put("url", "/");
        items.add(homeItem);
        
        // Players list item
        Map<String, String> playersItem = new HashMap<>();
        playersItem.put("label", "Joueurs");
        
        // Only add URL if we're not on the players list page
        if (!"list".equals(currentPage)) {
            playersItem.put("url", "/players");
        } else {
            playersItem.put("url", null);
        }
        items.add(playersItem);
        
        // Add player-specific items if applicable
        if (player != null && player.getId() != null) {
            Map<String, String> playerItem = new HashMap<>();
            playerItem.put("label", player.getUsername());
            
            // If we're on the detail page, don't add URL
            if (!"detail".equals(currentPage)) {
                playerItem.put("url", "/players/" + player.getId());
            } else {
                playerItem.put("url", null);
            }
            items.add(playerItem);
        }
        
        // Add action item if needed (create/edit)
        if ("form".equals(currentPage)) {
            Map<String, String> actionItem = new HashMap<>();
            if (player != null && player.getId() != null) {
                actionItem.put("label", "Modifier");
            } else {
                actionItem.put("label", "Créer");
            }
            actionItem.put("url", null); // Current page, no URL
            items.add(actionItem);
        }
        
        return items;
    }

    private final PlayerService playerService;

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public String listPlayers(Model model) {
        List<Player> players = playerService.findAll();
        model.addAttribute("players", players);
        
        // Ajout des éléments du fil d'Ariane
        model.addAttribute("breadcrumbItems", createBreadcrumbItems("list", null));
        
        return "player/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        Player player = new Player();
        model.addAttribute("player", player);
        
        // Ajout des éléments du fil d'Ariane
        model.addAttribute("breadcrumbItems", createBreadcrumbItems("form", player));
        
        return "player/form";
    }

    @PostMapping
    public String createPlayer(@Valid @ModelAttribute("player") Player player, 
                              BindingResult result) {
        if (result.hasErrors()) {
            return "player/form";
        }
        
        // Les valeurs par défaut comme createdAt sont gérées dans le constructeur
        // Nous n'avons pas besoin de définir manuellement createdAt
        if (!player.isActive()) {
            player.setActive(true);
        }
        
        playerService.save(player);
        return "redirect:/players";
    }

    @GetMapping("/{id}")
    public String showPlayerDetails(@PathVariable("id") Long id, Model model) {
        Player player = playerService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid player Id:" + id));
        model.addAttribute("player", player);
        
        // Récupération des équipes associées au joueur (déjà disponible via player.getTeams())
        // Mais on pourrait ajouter d'autres données ici si nécessaire
        
        // Ajout des éléments du fil d'Ariane
        model.addAttribute("breadcrumbItems", createBreadcrumbItems("detail", player));
        
        return "player/view";
    }

    @GetMapping("/{id}/edit")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        Player player = playerService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid player Id:" + id));
        model.addAttribute("player", player);
        
        // Ajout des éléments du fil d'Ariane
        model.addAttribute("breadcrumbItems", createBreadcrumbItems("form", player));
        
        return "player/form";
    }

    @PostMapping("/{id}")
    public String updatePlayer(@PathVariable("id") Long id, 
                              @Valid @ModelAttribute("player") Player player,
                              BindingResult result) {
        if (result.hasErrors()) {
            player.setId(id);
            return "player/form";
        }
        
        playerService.save(player);
        return "redirect:/players";
    }

    @GetMapping("/{id}/delete")
    public String deletePlayer(@PathVariable("id") Long id) {
        playerService.deleteById(id);
        return "redirect:/players";
    }
}
