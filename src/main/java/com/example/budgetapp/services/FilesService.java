package com.example.budgetapp.services;

public interface FilesService {

    boolean saveToFile(String json);

    String readToFile();
}
