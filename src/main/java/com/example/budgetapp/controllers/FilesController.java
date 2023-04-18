package com.example.budgetapp.controllers;

import com.example.budgetapp.services.FilesService;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@RestController
@RequestMapping("/files")
public class FilesController {
    private final FilesService filesService;

    public FilesController(FilesService filesService) {
        this.filesService = filesService;
    }

    //Загрузка
    @GetMapping("/export") // @GetMapping(value = "/export", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity<InputStreamResource> downloadDataFile() throws FileNotFoundException {
        File file = filesService.getDataFile();
        if (file.exists()) {
            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));   //создание потока. InputStreamResource некая обертка в которую мы кладем в FileInputStream (входной поток стрим)
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)  //ЗАГОЛОВОК ЗАПРОСА URL: APPLICATION_OCTET_STREAM мы можем указать что конкретно передаем (поток байт или тп)
                    .contentLength(file.length())    //ЗАГОЛОВОК ЗАПРОСА URL: передает длину файла, для того чтобы браузер понял сколько байт
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"TransactionData.json\"") //ЗАГОЛОВОК ЗАПРОСА URL: CONTENT_DISPOSITION есть инфа что это за контент (attachment - показывает что нужно скачивать, filename - скачать с опред названием)
                    .body(resource);
        } else {
            return ResponseEntity.noContent().build();  //в ок, но содержимого нет
        }
    }


    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadDataFile( @RequestParam MultipartFile file) { // MultipartFile содержит данные которые загужаются
        filesService.cleanDataFile();
        File fileData = filesService.getDataFile();

        try (FileOutputStream fos = new FileOutputStream(fileData)) {
            IOUtils.copy(file.getInputStream(), fos);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}

/*  Ручной ввод инпорта
        try (BufferedInputStream bis = new BufferedInputStream(file.getInputStream());
                FileOutputStream fos = new FileOutputStream(fileData);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            byte[] buffer = new byte[1024];
            while (bis.read() > 0) {
                bos.write(buffer);
            }

    } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
*/














