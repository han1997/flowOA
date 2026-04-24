package com.flowoa.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskActionDTO {

    @NotNull(message = "Task ID cannot be null")
    private Long taskId;

    private String comment;
}
