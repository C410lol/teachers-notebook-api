package com.api.notebook.utils;

import com.api.notebook.models.entities.StudentEntity;
import org.jetbrains.annotations.NotNull;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class StudentComparator implements Comparator<StudentEntity> {

    private final Collator collator;

    public StudentComparator() {
        collator = Collator.getInstance(new Locale("pt", "BR"));
        collator.setStrength(Collator.NO_DECOMPOSITION);
    }

    @Override
    public int compare(@NotNull StudentEntity o1, @NotNull StudentEntity o2) {
        return collator.compare(o1.getName(), o2.getName());
    }
}
