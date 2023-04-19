package com.example.budgetapp.services.impl;

import com.example.budgetapp.services.FilesService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FilesServiceImpl implements FilesService {

   //для того чтобы спринг вставил значение из .properties  внутрь переменых нужно написать анатацию
    @Value("${path.to.data.file}")
    private String dataFilePath;
    @Value("${name.to.data.file}")
    private String dataFileName;
    //зачем это делать: если мы не захотим хранить файл в одной папке/изменить его имя, то достатьяно изменить
    //изменить в ресурсах .properties -> не нужно перекомпилировать проект

    @Override
    public boolean saveToFile(String json) {
        try {
            Files.writeString(Path.of(dataFilePath,dataFileName), json);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }
    @Override
    public String readToFile () {
        try {
            return Files.readString(Path.of(dataFilePath,dataFileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }



    //служебные методы, приватные, нужны чтобы сервис норм работал
    //1. для удаления файла; 2.
    //нужно писать чек ресепшен
    @Override
    public boolean cleanDataFile () {
        try {
            Path path = Path.of(dataFilePath,dataFileName);
            Files.deleteIfExists(path);                      //метод- удалить если существует файл, Path.of передаем строку с нашей папкой
            Files.createFile(path);
            return true;
        } catch (IOException e) {
            e.printStackTrace();                        //отображение информации о ошибке
            return false;
        }
    }

    @Override
    public Path createTempFile(String suffix) {
        try {
            return Files.createTempFile(Path.of(dataFilePath), "tempFile", suffix);   //файл временый тк будет генерируется автомотичиски у него состовное название - сначала идет префикс + случайное число и потом суффикс (избегаем одинаковых имен)
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    //метод возвращающий файл (по сути возвр не сам файл, самих данных файлов не касаемся)
    @Override
    public File getDataFile() {
        return new File(dataFilePath + "/" + dataFileName);

    }





}
