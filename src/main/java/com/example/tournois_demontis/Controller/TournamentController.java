package com.example.tournois_demontis.Controller;

import com.example.tournois_demontis.Entity.game.Game;
import com.example.tournois_demontis.Entity.player.Player;
import com.example.tournois_demontis.Entity.player.User;
import com.example.tournois_demontis.Entity.tournament.SingleEliminationTournament;
import com.example.tournois_demontis.Entity.tournament.Tournament;
import com.example.tournois_demontis.Service.GameService;
import com.example.tournois_demontis.Service.TournamentService;
import com.example.tournois_demontis.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/tournaments")
public class TournamentController {

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
        model.addAttribute("tournaments", tournaments);
        
        // Ajout de la liste des utilisateurs au modèle
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        
        return "tournament/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("tournament", new SingleEliminationTournament());
        // Ajout de la liste des utilisateurs et des jeux au modèle
        List<User> users = userService.findAll();
        List<Game> games = gameService.findAll();
        model.addAttribute("users", users);
        model.addAttribute("games", games);
        return "tournament/form";
    }

    @PostMapping
    public String createTournament(@Valid @ModelAttribute("tournament") Tournament tournament, 
                                   BindingResult result, Model model,
                                   @RequestParam(value = "participantIds", required = false) List<Long> participantIds) {
        if (result.hasErrors()) {
            // Rechargement de la liste des utilisateurs et des jeux en cas d'erreur
            List<User> users = userService.findAll();
            List<Game> games = gameService.findAll();
            model.addAttribute("users", users);
            model.addAttribute("games", games);
            return "tournament/form";
        }
        
        // Ajouter les participants sélectionnés
        if (participantIds != null && !participantIds.isEmpty()) {
            Set<Player> participants = new HashSet<>();
            for (Long userId : participantIds) {
                userService.findById(userId).ifPresent(user -> {
                    // Vérifier si l'utilisateur est un joueur
                    if (user instanceof Player) {
                        participants.add((Player) user);
                    }
                });
            }
            tournament.setParticipants(participants);
        }
        
        tournamentService.save(tournament);
        return "redirect:/tournaments";
    }

    @GetMapping("/{id}")
    public String showTournamentDetails(@PathVariable("id") Long id, Model model) {
        Tournament tournament = tournamentService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid tournament Id:" + id));
        model.addAttribute("tournament", tournament);
        return "tournament/detail";
    }

    @GetMapping("/{id}/edit")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        Tournament tournament = tournamentService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid tournament Id:" + id));
        model.addAttribute("tournament", tournament);
        
        // Ajout de la liste des utilisateurs et des jeux au modèle
        List<User> users = userService.findAll();
        List<Game> games = gameService.findAll();
        model.addAttribute("users", users);
        model.addAttribute("games", games);
        return "tournament/form";
    }

    @PostMapping("/{id}")
    public String updateTournament(@PathVariable("id") Long id, 
                                   @Valid @ModelAttribute("tournament") Tournament tournament,
                                   BindingResult result, Model model,
                                   @RequestParam(value = "participantIds", required = false) List<Long> participantIds) {
        if (result.hasErrors()) {
            tournament.setId(id);
            // Rechargement de la liste des utilisateurs et des jeux en cas d'erreur
            List<User> users = userService.findAll();
            List<Game> games = gameService.findAll();
            model.addAttribute("users", users);
            model.addAttribute("games", games);
            return "tournament/form";
        }
        
        // Récupérer le tournoi existant pour conserver les relations
        Tournament existingTournament = tournamentService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid tournament Id:" + id));
        
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
}
