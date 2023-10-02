package com.api.notebook.models.dtos;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AttendanceDto {

    private List<UUID> presentStudentsIds;

    private List<UUID> absentStudentsIds;

}
