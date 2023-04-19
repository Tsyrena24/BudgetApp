package com.example.budgetapp.services;

import java.io.File;
import java.nio.file.Path;

public interface FilesService {

    boolean saveToFile(String json);

    String readToFile();

    //служебные методы, приватные, нужны чтобы сервис норм работал
    //1. для удаления файла; 2.
    //нужно писать чек ресепшен
    boolean cleanDataFile();

    Path createTempFile(String suffix);

    //метод возвращающий файл (по сути возвр не сам файл, самих данных файлов не касаемся)
    File getDataFile();
}
