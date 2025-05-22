package by.grgu.recommendationservice.controller;

import by.grgu.recommendationservice.database.entity.RecommendationReport;
import by.grgu.recommendationservice.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Autowired
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public String showRecommendationsPage(Model model) {
        model.addAttribute("username", "—");
        model.addAttribute("totalIncome", "—");
        model.addAttribute("totalExpense", "—");
        model.addAttribute("remainingBudget", "—");
        model.addAttribute("desiredExpenses", "—");
        model.addAttribute("recommendations", List.of());
        return "recommendation";
    }

    @GetMapping("/personal")
    public String getRecommendations(@RequestHeader("username") String username,
                                     @RequestParam int month,
                                     @RequestParam int year,
                                     @RequestParam BigDecimal desiredExpenses,
                                     Model model) {

        RecommendationReport report = recommendationService.generateRecommendations(
                username, month, year, desiredExpenses);

        model.addAttribute("totalIncome", report.getTotalIncome());
        model.addAttribute("totalExpense", report.getTotalExpense());
        model.addAttribute("remainingBudget", report.getRemainingBudget());
        model.addAttribute("desiredExpenses", report.getDesiredExpenses());
        model.addAttribute("recommendations", report.getRecommendations());

        return "recommendation";
    }
}