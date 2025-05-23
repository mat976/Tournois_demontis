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

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public String listGames(Model model) {
        List<Game> games = gameService.findAll();
        model.addAttribute("games", games);
        return "game/list";
    }

    @GetMapping("/new")
    public String showGameForm(Model model) {
        model.addAttribute("game", new Game());
        model.addAttribute("gameModes", Arrays.asList(GameMode.values()));
        return "game/form";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Game game = gameService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid game Id:" + id));
        
        model.addAttribute("game", game);
        model.addAttribute("gameModes", Arrays.asList(GameMode.values()));
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
        return "game/view";
    }
}
