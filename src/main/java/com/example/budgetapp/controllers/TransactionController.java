package com.example.budgetapp.controllers;

import com.example.budgetapp.model.Category;
import com.example.budgetapp.model.Transaction;
import com.example.budgetapp.services.BudgetServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Month;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/transaction")
@Tag(name = "Транзакция", description = "CRUD-операции и др. эндпоинты для работ с транзакциями")
public class TransactionController {

    private final BudgetServices budgetServices;

    public TransactionController(BudgetServices budgetServices) {
        this.budgetServices = budgetServices;
    }

    @PostMapping
    public ResponseEntity<Long> addTransaction(@RequestBody Transaction transaction) {
        long id = budgetServices.addTransaction(transaction);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionId(@PathVariable long id) {
        Transaction transaction = budgetServices.getTransaction(id);
        if (transaction == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(transaction);
    }


    //Метод всех транзакций, можно найти по месяцам, по категориям (не обяз)

    @GetMapping
    @Operation(
            summary = "Поиск транзакций по месяцу и/или категории",
            description = "Можно искать по одному параметру или вообще без параметров"
    )
    @Parameters(value = {
            @Parameter(name = "month",
                    example = "Декабрь" )
    })
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Транзакции были найдены",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = Transaction.class))
                            )
                    }

            )
    })
    public ResponseEntity<Transaction> getsAllTransaction(@RequestParam(required = false) Month month,
                                                          @RequestParam(required = false) Category category) {

        return null;
    }

    //Метод только по месяцам, и обязательно у казвать в юрл
    @GetMapping("/byMonth/{month}")
    public ResponseEntity<Object> getTransactionsMonth(@PathVariable Month month) {
        try {
            Path path = budgetServices.createMonthlyReport(month);
            if (Files.size(path) == 0) {
                return ResponseEntity.noContent().build();  //в ок, но содержимого нет
            }
            InputStreamResource resource = new InputStreamResource(new FileInputStream(path.toFile()));   //создание потока. InputStreamResource некая обертка в которую мы кладем в FileInputStream (входной поток стрим)
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)  //ЗАГОЛОВОК ЗАПРОСА URL: TEXT_PLAIN - текстовый файл
                    .contentLength(Files.size(path))    //ЗАГОЛОВОК ЗАПРОСА URL: передает длину файла
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + month + "-report.txt\"") //ЗАГОЛОВОК ЗАПРОСА URL: CONTENT_DISPOSITION есть инфа что это за контент (attachment - показывает что нужно скачивать, filename - скачать с опред названием)
                    .body(resource);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.toString());
        }
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> addTransactionsFromFile (@RequestParam MultipartFile file) {
        try (InputStream stream = file.getInputStream()) {
            budgetServices.addTransactionFromInputStream(file.getInputStream());
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.toString());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> editTransaction(@PathVariable long id, @RequestBody Transaction transaction) {
        Transaction transaction1 = budgetServices.editTransaction(id, transaction);
        if (transaction == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(transaction1);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable long id) {
        if (budgetServices.deleteTransaction(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllTransaction() {
        budgetServices.deleteAllTransaction();
        return ResponseEntity.ok().build();
    }

}
