package com.api.notebook.controllers;

import com.api.notebook.enums.BimesterEnum;
import com.api.notebook.enums.ClassEnum;
import com.api.notebook.models.entities.StudentEntity;
import com.api.notebook.services.FinishedNotebookService;
import com.api.notebook.services.StudentService;
import com.api.notebook.utils.FinishedNotebookUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Comparator;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/finished-notebooks")
public class FinishedNotebookController {

    private final FinishedNotebookService finishedNotebookService;
    private final StudentService studentService;




    @GetMapping("/{institutionId}/get-finished")
    public ResponseEntity<?> getAllByInstitutionIdAndClasseAndBimester(
            @PathVariable(value = "institutionId") UUID institutionId,
            @RequestParam(value = "classe") ClassEnum classe,
            @RequestParam(value = "bimester") BimesterEnum bimesterEnum
    ) {
        return ResponseEntity.ok(finishedNotebookService.findAllByInstitutionIdAndNotebookClasseAndNotebookBimester(
                institutionId,
                classe,
                bimesterEnum
        ));
    }

    @GetMapping("download-all-finished")
    public ResponseEntity<?> downloadAllFinished(
            @RequestParam(value = "institutionId") UUID institutionId,
            @RequestParam(value = "classe") ClassEnum classe,
            @RequestParam(value = "bimester") BimesterEnum bimesterEnum
    ) throws IOException {
        var students = studentService.findAllStudentsByClasse(classe);
        students.sort(Comparator.comparing(StudentEntity::getNumber));

        var finishedNotebooks = finishedNotebookService.findAllByInstitutionIdAndNotebookClasseAndNotebookBimester(
                institutionId,
                classe,
                bimesterEnum
        );

        var file = FinishedNotebookUtils.buildFinishedNotebooksWorkbook(
                classe.name(),
                bimesterEnum.name(),
                finishedNotebooks,
                students
        );

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "médias.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(file);
    }

}
