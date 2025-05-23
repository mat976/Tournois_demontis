package com.example.tournois_demontis.Controller;

import com.example.tournois_demontis.Entity.match.Match;
import com.example.tournois_demontis.Entity.player.Player;
import com.example.tournois_demontis.Service.MatchService;
import com.example.tournois_demontis.Service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/matches")
public class MatchController {

    private final MatchService matchService;
    private final TournamentService tournamentService;

    @Autowired
    public MatchController(MatchService matchService, TournamentService tournamentService) {
        this.matchService = matchService;
        this.tournamentService = tournamentService;
    }

    @GetMapping("/{id}")
    public String showMatchDetails(@PathVariable("id") Long id, Model model) {
        Match match = matchService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Match non trouvé avec l'ID: " + id));
        model.addAttribute("match", match);
        return "match/detail";
    }

    @GetMapping("/{id}/edit")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        Match match = matchService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Match non trouvé avec l'ID: " + id));
        model.addAttribute("match", match);
        
        // Récupérer la liste des joueurs pour le formulaire
        List<Player> availablePlayers = match.getTournament().getParticipants().stream().toList();
        model.addAttribute("players", availablePlayers);
        
        return "match/form";
    }

    @PostMapping("/{id}")
    public String updateMatch(@PathVariable("id") Long id, 
                              @Valid @ModelAttribute("match") Match match,
                              BindingResult result, Model model) {
        if (result.hasErrors()) {
            // Récupérer la liste des joueurs pour le formulaire en cas d'erreur
            Match existingMatch = matchService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Match non trouvé avec l'ID: " + id));
            List<Player> availablePlayers = existingMatch.getTournament().getParticipants().stream().toList();
            model.addAttribute("players", availablePlayers);
            return "match/form";
        }
        
        matchService.save(match);
        return "redirect:/tournaments/" + match.getTournament().getId();
    }

    @PostMapping("/{id}/result")
    public String recordMatchResult(@PathVariable("id") Long id,
                                   @RequestParam("winnerId") Long winnerId,
                                   @RequestParam("score1") Integer score1,
                                   @RequestParam("score2") Integer score2,
                                   RedirectAttributes redirectAttributes) {
        try {
            Match updatedMatch = tournamentService.recordMatchResult(id, winnerId, score1, score2);
            redirectAttributes.addFlashAttribute("success", "Résultat du match enregistré avec succès.");
            return "redirect:/tournaments/" + updatedMatch.getTournament().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'enregistrement du résultat: " + e.getMessage());
            return "redirect:/matches/" + id;
        }
    }
}
