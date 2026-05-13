package com.flowoa.vo;

import lombok.Data;

import java.util.List;

@Data
public class FlowProgressVO {
    private Long flowInstanceId;
    private String flowStatus;
    private List<String> currentNodeNames;
    private List<String> currentApproverNames;

    public static String join(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "-";
        }
        return String.join("、", values);
    }
}

