package com.flowoa.service.warmflow;

import org.dromara.warm.flow.ui.service.NodeExtService;
import org.dromara.warm.flow.ui.vo.NodeExt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 节点扩展属性服务实现
 */
@Service
public class NodeExtServiceImpl implements NodeExtService {

    @Override
    public List<NodeExt> getNodeExt() {
        // Return node extension properties
        List<NodeExt> nodeExts = new ArrayList<>();

        // Add default node extensions if needed
        // For example: priority, due date, etc.

        return nodeExts;
    }
}
