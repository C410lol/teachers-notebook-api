package com.api.notebook.controllers;

import com.api.notebook.services.BNCCCodeService;
import com.api.notebook.services.NotebookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/bncc_codes")
public class BNCCCodeController {

    private final BNCCCodeService bnccCodeService;
    private final NotebookService notebookService;

    @GetMapping("/all-by-filters")
    public ResponseEntity<Object> findAllBnccCodesByFilters(
            @RequestParam(value = "notebookId") UUID notebookId,
            @RequestParam(value = "pattern") String pattern
    ) {
        var notebookOptional = notebookService.findNotebookById(notebookId);
        if (notebookOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Caderneta não encontrada!");
        }

        return ResponseEntity.ok(bnccCodeService.findAllByMatchingPatternAndSubjectAndClasse(
                pattern,
                notebookOptional.get().getSubject(),
                notebookOptional.get().getClasse()));
    }

}
