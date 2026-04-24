package com.flowoa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransferDTO {
    @NotNull(message = "taskId cannot be null")
    private Long taskId;

    @NotBlank(message = "targetUserId cannot be blank")
    private String targetUserId;

    private String comment;
}