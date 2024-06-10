package com.api.notebook.controllers;

import com.api.notebook.enums.BimesterEnum;
import com.api.notebook.enums.ClassEnum;
import com.api.notebook.models.entities.StudentEntity;
import com.api.notebook.services.FinishedNotebookService;
import com.api.notebook.services.FinishedStudentService;
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
    private final FinishedStudentService finishedStudentService;
    private final StudentService studentService;




    @GetMapping("/get-by-notebook")
    public ResponseEntity<?> getByNotebookId(
            @RequestParam(value = "notebookId") UUID notebookId
    ) {
        var finishedNotebookOptional = finishedNotebookService.findByNotebookId(notebookId);
        if (finishedNotebookOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(finishedNotebookOptional.get());
    }

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
        headers.setContentDispositionFormData("attachment", classe.name() + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(file);
    }








    //EDIT

    @PutMapping("/finished-students/{finishedStudentId}/edit-grade")
    public ResponseEntity<?> editFinishedStudentGrade(
            @PathVariable(value = "finishedStudentId") UUID finishedStudentId,
            @RequestBody Double grade
    ) {
        var finishedStudentOptional = finishedStudentService.findById(finishedStudentId);
        if (finishedStudentOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nota não encontrada!");
        }

        if (grade == null || grade < 0 || grade > 10 || grade.isNaN()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insira uma nota válida!");
        }

        finishedStudentOptional.get().setFinalGrade(grade);
        finishedStudentService.save(finishedStudentOptional.get());
        return ResponseEntity.ok("Nota editada com sucesso!");
    }

}
