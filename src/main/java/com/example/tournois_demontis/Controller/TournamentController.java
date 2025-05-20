package com.example.tournois_demontis.Controller;


import com.example.tournois_demontis.Entity.player.User;
import com.example.tournois_demontis.Entity.tournament.SingleEliminationTournament;
import com.example.tournois_demontis.Entity.tournament.Tournament;
import com.example.tournois_demontis.Service.TournamentService;
import com.example.tournois_demontis.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;
    private final UserService userService;

    @Autowired
    public TournamentController(TournamentService tournamentService, UserService userService) {
        this.tournamentService = tournamentService;
        this.userService = userService;
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
        // Ajout de la liste des utilisateurs au modèle
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "tournament/form";
    }

    @PostMapping
    public String createTournament(@Valid @ModelAttribute("tournament") Tournament tournament, 
                                   BindingResult result, Model model) {
        if (result.hasErrors()) {
            // Rechargement de la liste des utilisateurs en cas d'erreur
            List<User> users = userService.findAll();
            model.addAttribute("users", users);
            return "tournament/form";
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
        
        // Ajout de la liste des utilisateurs au modèle
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "tournament/form";
    }

    @PostMapping("/{id}")
    public String updateTournament(@PathVariable("id") Long id, 
                                   @Valid @ModelAttribute("tournament") Tournament tournament,
                                   BindingResult result, Model model) {
        if (result.hasErrors()) {
            tournament.setId(id);
            // Rechargement de la liste des utilisateurs en cas d'erreur
            List<User> users = userService.findAll();
            model.addAttribute("users", users);
            return "tournament/form";
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
