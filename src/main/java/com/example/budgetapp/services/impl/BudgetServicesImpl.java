package com.example.budgetapp.services.impl;

import com.example.budgetapp.model.Category;
import com.example.budgetapp.model.Transaction;
import com.example.budgetapp.services.BudgetServices;
import com.example.budgetapp.services.FilesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

@Service
public class BudgetServicesImpl implements BudgetServices {


    // для того чтобы использовать сервис, мы должны его заинжектить, используем конструктор
    final private FilesService filesService;

    public static final int SALARY = 30_000 - 9_750;
    public static final int SAVING = 3_000;
    public static final int DAILY_BUDGET = (SALARY - SAVING) / LocalDate.now().lengthOfMonth();
    public static int balance = 0;

    //  public static final int AVG_SALARY = (10000 + 10000 + 10000 + 10000 + 10000 + 15000 + 15000 + 15000 + 15000 + 15000 + 15000 + 20000) / 12;
    public static final int AVG_SALARY = SALARY;
    public static final double AVG_DAY = 29.3;

    private static TreeMap<Month, LinkedHashMap<Long, Transaction>> transactions = new TreeMap<>();  // здесь сохраняются данные
    public static long lastId = 0;

    public BudgetServicesImpl(FilesService filesService) {
        this.filesService = filesService;
    }

    @PostConstruct   //спинг автоматически вызывет этот метот в тот момент когда этот пин будет создан ()
    private void init() {
        try {
            readFromFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getDailyBudget() {
        return DAILY_BUDGET;
    }

    @Override
    public int getBalance() {
        return SALARY - SAVING - getAllSpend();
    }

    @Override
    public long addTransaction(Transaction transaction) {
        LinkedHashMap<Long, Transaction> monthTransaction = transactions.getOrDefault(LocalDate.now().getMonth(), new LinkedHashMap<>());
        monthTransaction.put(lastId, transaction);
        transactions.put(LocalDate.now().getMonth(), monthTransaction);
        saveToFile();
        return lastId++;
    }

    @Override
    public Transaction getTransaction(long id) {
        for (Map<Long, Transaction> transactionsByMonth : transactions.values()) {
            Transaction transaction = transactionsByMonth.get(id);
            if (transaction != null) {
                return transaction;
            }
        }
        return null;
    }
    @Override
    public Transaction editTransaction(long id, Transaction transaction) {
        for (Map<Long, Transaction> transactionsByMonth : transactions.values()) {
            if (transactionsByMonth.containsKey(id)) {
                transactionsByMonth.put(id, transaction);
                return transaction;
            }
        }
        saveToFile();
        return null;
    }

    @Override
    public boolean deleteTransaction(long id) {
        for (Map<Long, Transaction> transactionsByMonth : transactions.values()) {
            Transaction transaction = transactionsByMonth.get(id);
            if (transaction != null) {
                transactionsByMonth.remove(id);
                return true;
            }
        }
        return false;
    }
    @Override
    public void deleteAllTransaction() {
        transactions = new TreeMap<>();
    }

        @Override
        public int getDailyBalance() {
            return DAILY_BUDGET * LocalDate.now().getDayOfMonth() - getAllSpend();
        }

        @Override
        public int getAllSpend() {
            Map<Long, Transaction> monthTransaction = transactions.getOrDefault(LocalDate.now().getMonth(), new LinkedHashMap<>());
            int sum = 0;
            for (Transaction transaction : monthTransaction.values()) {
                sum += transaction.getSum();
            }
            return sum;
        }

    @Override
    public Path createMonthlyReport(Month month) throws IOException {

        LinkedHashMap<Long, Transaction> monthlyTransaction = transactions.getOrDefault(month, new LinkedHashMap<>());
        Path path = filesService.createTempFile("monthlyReport");
        for (Transaction transaction : monthlyTransaction.values()) {
            try (Writer writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND)) {
                writer.append(transaction.getCategory().getText() + ": " + transaction.getSum() + " руб. - " + transaction.getComment());
                writer.append("\n");
            }
        }
        return path;
    }


    @Override
    //добавление рецептов без удаления старого
    public void addTransactionFromInputStream(InputStream inputStream) throws IOException {
        //преобразовать байты в строку InputStreamReader
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))){
            String line;
            while ((line = reader.readLine())!= null) {
                String[] array = StringUtils.split(line, '|');
                Transaction transaction = new Transaction(Category.valueOf(array[0]), Integer.valueOf(array[1]), array[2]);
                addTransaction(transaction);
            }
        }
    }


        @Override

        public int getVacationBonus(int daysCount) {

            double avgDailySalary = AVG_SALARY / AVG_DAY;
            return (int) (daysCount * avgDailySalary);
        }

        @Override
        public int getSalaryWithVacation(int vacationDaysCount, int vacationWorkingCount, int workingDaysMonth) {
            int salary = SALARY / workingDaysMonth * (workingDaysMonth - vacationWorkingCount);
            return salary + getVacationBonus(vacationDaysCount);
        }

    //запись файла, приватные, пользователь не может управлять
    //для того чтобы чтото в файл записать, нужно строчку подготовить карта transactions -> в строку
    //работаем с библеотекой джексон(работает в json обьектами) writeValueAsString -> обьект переобразует в json
    private void saveToFile() {
        try {
             DataFile dataFile = new DataFile(lastId + 1, transactions);
//            Map<String, Object> map = new HashMap<>();
//            map.put("lastId", lastId);
//            map.put("transactions", transactions);
            String json = new ObjectMapper().writeValueAsString(dataFile);
            filesService.saveToFile(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    //чтение файла, приватные, пользователь не может управлять
    private void readFromFile() {
        //нужно в обратную сторону json замапить
        //json -> преобрауем в обьект, испольуем класс TypeReference, внутри описывает какой конечный обьект
        try {
            String json = filesService.readToFile();

             DataFile dataFile = new ObjectMapper().readValue(json, new TypeReference<DataFile>(){
             });
            lastId = dataFile.getListId();
            transactions = dataFile.getTransaction();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class DataFile {
        private long listId;
        private TreeMap<Month, LinkedHashMap<Long, Transaction>> transaction;


    }



    }
