package com.flowoa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApproveDTO {
    @NotNull(message = "申请ID不能为空")
    private Long applyId;

    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    private String comment;
}
