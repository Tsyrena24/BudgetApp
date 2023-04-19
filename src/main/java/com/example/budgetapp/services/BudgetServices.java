package com.example.budgetapp.services;

import com.example.budgetapp.model.Transaction;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Month;

public interface BudgetServices {
    int getDailyBudget();

    int getBalance();

    long addTransaction(Transaction transaction);

    Transaction getTransaction(long id);

    Transaction editTransaction(long id, Transaction transaction);

    boolean deleteTransaction(long id);

    void deleteAllTransaction();

    int getDailyBalance();

    int getAllSpend();

    //добавление рецептов без удаления старого
    void addTransactionFromInputStream(InputStream inputStream) throws IOException;

    int getVacationBonus(int daysCount);

    int getSalaryWithVacation(int vacationDaysCount, int vacationWorkingCount, int workingDaysMonth);

    Path createMonthlyReport(Month month) throws IOException;
}
