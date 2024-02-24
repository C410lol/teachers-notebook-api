package com.api.notebook.services;

import com.api.notebook.enums.ClassEnum;
import com.api.notebook.enums.SubjectEnum;
import com.api.notebook.models.entities.BNCCCodeEntity;
import com.api.notebook.models.entities.LessonEntity;
import com.api.notebook.repositories.BNCCCodeRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BNCCCodeService {

    private final BNCCCodeRepository bnccCodeRepository;

    public void saveBnccCodeEntity(BNCCCodeEntity bnccCodeEntity) {
        bnccCodeRepository.save(bnccCodeEntity);
    }

    public List<BNCCCodeEntity> findAllByMatchingPatternAndSubjectAndClasse(
            String pattern,
            @NotNull SubjectEnum subjectEnum,
            @NotNull ClassEnum classEnum
    ) {
        return bnccCodeRepository.findByMatchingPatternAndSubjectAndClasse(
                pattern,
                subjectEnum.name(),
                classEnum.name());
    }

    public boolean setBnccCodesToLesson(@NotNull List<String> bnccCodes, LessonEntity lessonEntity) {
        List<BNCCCodeEntity> bnccCodeEntities = new ArrayList<>();
        for (String s:
                bnccCodes) {
            var bnccCodeOptional = bnccCodeRepository.findByCode(s);
            if (bnccCodeOptional.isEmpty()) return false;
            bnccCodeEntities.add(bnccCodeOptional.get());
        }
        lessonEntity.setBnccCodes(bnccCodeEntities);
        return true;
    }

}
