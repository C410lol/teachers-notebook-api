package com.api.notebook.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MissingTasksModel {

    private List<MissingTaskLessonModel> missingLessons;
    private List<MissingTaskWorkModel> missingWorks;

    public boolean isEmpty() {
        return missingLessons.isEmpty() && missingWorks.isEmpty();
    }

}
