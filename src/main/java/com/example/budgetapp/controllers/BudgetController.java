package com.example.budgetapp.controllers;

import com.example.budgetapp.services.BudgetServices;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/budget")
public class BudgetController {

    private BudgetServices budgetServices;

    public BudgetController(BudgetServices budgetServices) {
        this.budgetServices = budgetServices;
    }

    @GetMapping("/daily")
    public int dailyBudget() {
        return budgetServices.getDailyBudget();
    }

    @GetMapping("/balance")
    public int balance() {
        return budgetServices.getBalance();
    }


}
