package com.flowoa.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class FlowDesignDTO {

    private Long id;

    @NotBlank(message = "流程编码不能为空")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{0,63}$", message = "流程编码须以字母开头，仅含字母、数字、下划线，长度<=64")
    private String flowCode;

    @NotBlank(message = "流程名称不能为空")
    private String flowName;

    private String flowCategory;

    @Valid
    private List<NodeDesign> nodes;

    @Valid
    private List<SkipDesign> skips;

    @Data
    public static class NodeDesign {
        @NotBlank(message = "节点编码不能为空")
        private String code;

        @NotBlank(message = "节点名称不能为空")
        private String name;

        @NotNull(message = "节点类型不能为空")
        private Integer type;  // 0=start, 1=task, 2=end

        private Double x;
        private Double y;
        private String permissionFlag;
    }

    @Data
    public static class SkipDesign {
        @NotBlank(message = "起始节点不能为空")
        private String fromNodeCode;

        @NotBlank(message = "目标节点不能为空")
        private String toNodeCode;

        private String skipType;
        private String skipName;
    }
}
