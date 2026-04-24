package com.splitpay.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("activeTab", "dashboard");
        return "dashboard";
    }

    @GetMapping("/conciliacao")
    public String conciliacao(Model model) {
        model.addAttribute("activeTab", "conciliacao");
        return "conciliacao";
    }

    @GetMapping("/simulador")
    public String simulador(Model model) {
        model.addAttribute("activeTab", "simulador");
        return "simulador";
    }

    @GetMapping("/api")
    public String apiDocs(Model model) {
        model.addAttribute("activeTab", "api");
        return "api";
    }

    @GetMapping("/roadmap")
    public String roadmap(Model model) {
        model.addAttribute("activeTab", "roadmap");
        return "roadmap";
    }

    @GetMapping("/declaracao")
    public String declaracao(Model model) {
        model.addAttribute("activeTab", "declaracao");
        return "declaracao";
    }
}
