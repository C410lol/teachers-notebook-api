package com.api.notebook.services;

import com.api.notebook.enums.BimesterEnum;
import com.api.notebook.enums.ClassEnum;
import com.api.notebook.models.entities.FinishedNotebookEntity;
import com.api.notebook.repositories.FinishedNotebookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinishedNotebookService {

    private final FinishedNotebookRepository finishedNotebookRepository;




    public void save(
            FinishedNotebookEntity finishedNotebook
    ) {
        finishedNotebookRepository.save(finishedNotebook);
    }




    public Optional<FinishedNotebookEntity> findById(
            UUID id
    ) {
        return finishedNotebookRepository.findById(id);
    }

    public Optional<FinishedNotebookEntity> findByNotebookId(
            UUID notebookId
    ) {
        return finishedNotebookRepository.findByNotebookId(notebookId);
    }

    public List<FinishedNotebookEntity> findAllByInstitutionIdAndNotebookClasseAndNotebookBimester(
            UUID institutionId,
            ClassEnum notebookClasse,
            BimesterEnum notebookBimester
    ) {
        return finishedNotebookRepository.findAllByInstitutionIdAndNotebookClasseAndNotebookBimester(
                institutionId,
                notebookClasse.name(),
                notebookBimester.name()
        );
    }

}
