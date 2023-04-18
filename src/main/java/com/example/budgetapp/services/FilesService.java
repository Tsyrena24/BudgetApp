package com.example.budgetapp.services;

import java.io.File;

public interface FilesService {

    boolean saveToFile(String json);

    String readToFile();

    //служебные методы, приватные, нужны чтобы сервис норм работал
    //1. для удаления файла; 2.
    //нужно писать чек ресепшен
    boolean cleanDataFile();

    //метод возвращающий файл (по сути возвр не сам файл, самих данных файлов не касаемся)
    File getDataFile();
}
