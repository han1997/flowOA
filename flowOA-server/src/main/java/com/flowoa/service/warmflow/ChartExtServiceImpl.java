package com.flowoa.service.warmflow;

import org.dromara.warm.flow.core.dto.DefJson;
import org.dromara.warm.flow.ui.service.ChartExtService;
import org.springframework.stereotype.Service;

/**
 * 流程图扩展服务实现
 */
@Service
public class ChartExtServiceImpl implements ChartExtService {

    @Override
    public void initPromptContent(DefJson defJson) {
        // Initialize prompt content for flow chart
        // Can add custom prompt messages for different node types
    }

    @Override
    public void execute(DefJson defJson) {
        // Execute custom logic for flow chart
        // Can add custom data or processing for the flow chart
    }
}
