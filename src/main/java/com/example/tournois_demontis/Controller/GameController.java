package com.example.tournois_demontis.Controller;

import com.example.tournois_demontis.Entity.game.Game;
import com.example.tournois_demontis.Entity.game.GameMode;
import com.example.tournois_demontis.Service.GameService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/games")
public class GameController {
    
    /**
     * Helper method to create breadcrumb items for game pages
     * @param currentPage The current page name
     * @param game The game object (can be null for list and new pages)
     * @return List of breadcrumb items with label and URL
     */
    private List<Map<String, String>> createBreadcrumbItems(String currentPage, Game game) {
        List<Map<String, String>> items = new ArrayList<>();
        
        // Home item
        Map<String, String> homeItem = new HashMap<>();
        homeItem.put("label", "Accueil");
        homeItem.put("url", "/");
        items.add(homeItem);
        
        // Games list item
        Map<String, String> gamesItem = new HashMap<>();
        gamesItem.put("label", "Jeux");
        
        // Only add URL if we're not on the games list page
        if (!"list".equals(currentPage)) {
            gamesItem.put("url", "/games");
        } else {
            gamesItem.put("url", null);
        }
        items.add(gamesItem);
        
        // Add game-specific items if applicable
        if (game != null && game.getId() != null) {
            Map<String, String> gameItem = new HashMap<>();
            gameItem.put("label", game.getName());
            
            // If we're on the detail page, don't add URL
            if (!"view".equals(currentPage)) {
                gameItem.put("url", "/games/" + game.getId());
            } else {
                gameItem.put("url", null);
            }
            items.add(gameItem);
        }
        
        // Add action item if needed (create/edit)
        if ("form".equals(currentPage)) {
            Map<String, String> actionItem = new HashMap<>();
            if (game != null && game.getId() != null) {
                actionItem.put("label", "Modifier");
            } else {
                actionItem.put("label", "Créer");
            }
            actionItem.put("url", null); // Current page, no URL
            items.add(actionItem);
        }
        
        return items;
    }

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public String listGames(Model model) {
        List<Game> games = gameService.findAll();
        model.addAttribute("games", games);
        
        // Ajout des éléments du fil d'Ariane
        model.addAttribute("breadcrumbItems", createBreadcrumbItems("list", null));
        
        return "game/list";
    }

    @GetMapping("/new")
    public String showGameForm(Model model) {
        Game game = new Game();
        model.addAttribute("game", game);
        model.addAttribute("gameModes", Arrays.asList(GameMode.values()));
        
        // Ajout des éléments du fil d'Ariane
        model.addAttribute("breadcrumbItems", createBreadcrumbItems("form", game));
        
        return "game/form";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Game game = gameService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid game Id:" + id));
        
        model.addAttribute("game", game);
        model.addAttribute("gameModes", Arrays.asList(GameMode.values()));
        
        // Ajout des éléments du fil d'Ariane
        model.addAttribute("breadcrumbItems", createBreadcrumbItems("form", game));
        
        return "game/form";
    }

    @PostMapping
    public String createGame(@Valid @ModelAttribute("game") Game game, 
                           BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("gameModes", Arrays.asList(GameMode.values()));
            return "game/form";
        }
        
        gameService.save(game);
        return "redirect:/games";
    }

    @PostMapping("/{id}")
    public String updateGame(@PathVariable("id") Long id, 
                           @Valid @ModelAttribute("game") Game game,
                           BindingResult result, Model model) {
        if (result.hasErrors()) {
            game.setId(id);
            model.addAttribute("gameModes", Arrays.asList(GameMode.values()));
            return "game/form";
        }
        
        // Récupérer le jeu existant pour préserver les relations
        Game existingGame = gameService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid game Id:" + id));
        
        // Mettre à jour les propriétés du jeu
        game.setMatches(existingGame.getMatches());
        game.setScores(existingGame.getScores());
        game.setPlayers(existingGame.getPlayers()); // Conserver les joueurs existants
        
        gameService.save(game);
        return "redirect:/games";
    }

    @GetMapping("/{id}/delete")
    public String deleteGame(@PathVariable("id") Long id) {
        gameService.deleteById(id);
        return "redirect:/games";
    }

    @GetMapping("/{id}")
    public String viewGame(@PathVariable("id") Long id, Model model) {
        Game game = gameService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid game Id:" + id));
        
        model.addAttribute("game", game);
        
        // Ajout des éléments du fil d'Ariane
        model.addAttribute("breadcrumbItems", createBreadcrumbItems("view", game));
        
        return "game/view";
    }
}
