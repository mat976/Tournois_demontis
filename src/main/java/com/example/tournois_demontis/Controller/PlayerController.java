package com.example.tournois_demontis.Controller;

import com.example.tournois_demontis.Entity.player.Player;
import com.example.tournois_demontis.Service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@Controller
@RequestMapping("/players")
public class PlayerController {

    private final PlayerService playerService;

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public String listPlayers(Model model) {
        List<Player> players = playerService.findAll();
        model.addAttribute("players", players);
        return "player/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("player", new Player());
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
        return "player/detail";
    }

    @GetMapping("/{id}/edit")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        Player player = playerService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid player Id:" + id));
        model.addAttribute("player", player);
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
