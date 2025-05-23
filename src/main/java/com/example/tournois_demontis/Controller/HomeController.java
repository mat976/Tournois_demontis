package com.example.tournois_demontis.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Model model) {
        // Créer un fil d'Ariane simple pour la page d'accueil
        List<Map<String, String>> breadcrumbItems = new ArrayList<>();
        
        // Ajouter uniquement l'accueil comme élément actif
        Map<String, String> homeItem = new HashMap<>();
        homeItem.put("label", "Accueil");
        homeItem.put("url", null); // Pas d'URL car c'est la page courante
        breadcrumbItems.add(homeItem);
        
        model.addAttribute("breadcrumbItems", breadcrumbItems);
        
        return "index";
    }
}
