package com.example.tournois_demontis.Controller;

import com.example.tournois_demontis.Entity.team.Team;
import com.example.tournois_demontis.Entity.player.Player;
import com.example.tournois_demontis.Service.TeamService;
import com.example.tournois_demontis.Service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/teams")
public class TeamController {
    @Autowired
    private TeamService teamService;
    @Autowired
    private PlayerService playerService;

    @GetMapping
    public String listTeams(Model model) {
        model.addAttribute("teams", teamService.findAll());
        return "team/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("team", new Team());
        model.addAttribute("allPlayers", playerService.findAll());
        return "team/form";
    }

    @PostMapping("/new")
    public String createTeam(@ModelAttribute("team") Team team, BindingResult result, @RequestParam(value = "players", required = false) Set<Long> playerIds, @RequestParam(value = "logo", required = false) MultipartFile logo, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("allPlayers", playerService.findAll());
            return "team/form";
        }
        if (playerIds == null) playerIds = new HashSet<>();
        if (logo != null && !logo.isEmpty()) {
            team.setLogoUrl(logo.getOriginalFilename());
        }
        teamService.save(team, playerIds);
        return "redirect:/teams";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Team> teamOpt = teamService.findById(id);
        if (teamOpt.isEmpty()) {
            return "redirect:/teams";
        }
        model.addAttribute("team", teamOpt.get());
        model.addAttribute("allPlayers", playerService.findAll());
        return "team/form";
    }

    @PostMapping("/{id}/edit")
    public String updateTeam(@PathVariable Long id, @ModelAttribute("team") Team team, BindingResult result, @RequestParam(value = "players", required = false) Set<Long> playerIds, @RequestParam(value = "logo", required = false) MultipartFile logo, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("allPlayers", playerService.findAll());
            return "team/form";
        }
        team.setId(id);
        if (playerIds == null) playerIds = new HashSet<>();
        teamService.save(team, playerIds);
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
}
