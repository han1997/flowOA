package com.flowoa.service.warmflow;

import org.dromara.warm.flow.ui.service.ListenerListService;
import org.dromara.warm.flow.ui.vo.ListenerVo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 监听器列表服务实现
 */
@Service
public class ListenerListServiceImpl implements ListenerListService {

    @Override
    public List<ListenerVo> listenerList() {
        // Return available listeners for flow nodes
        List<ListenerVo> listeners = new ArrayList<>();

        // Add default listeners if needed
        // For example: email notification, SMS notification, etc.

        return listeners;
    }
}
