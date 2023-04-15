package com.example.budgetapp.controllers;

import com.example.budgetapp.services.BudgetServices;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vacation")
public class VacationController {

    private final BudgetServices budgetServices;

    public VacationController(BudgetServices budgetServices) {
        this.budgetServices = budgetServices;
    }

    @GetMapping
    public int vacationBonus(@RequestParam int vacationDays) {
        return budgetServices.getVacationBonus(vacationDays);
    }
    @GetMapping("/salary")
    public int salaryWithVacation(@RequestParam int vacationDays, @RequestParam int workingDays, @RequestParam int vacWorkDays){
        return budgetServices.getSalaryWithVacation(vacationDays,vacWorkDays, workingDays );
    }
}
