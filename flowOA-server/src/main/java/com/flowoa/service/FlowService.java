package com.flowoa.service;

import cn.dev33.satoken.stp.StpUtil;
import com.flowoa.common.BusinessException;
import org.dromara.warm.flow.core.FlowEngine;
import org.dromara.warm.flow.core.dto.FlowParams;
import org.dromara.warm.flow.core.entity.Definition;
import org.dromara.warm.flow.core.entity.Instance;
import org.dromara.warm.flow.core.entity.Task;
import org.dromara.warm.flow.core.entity.User;
import org.dromara.warm.flow.core.service.DefService;
import org.dromara.warm.flow.core.service.InsService;
import org.dromara.warm.flow.core.service.TaskService;
import org.dromara.warm.flow.core.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlowService {

    private final DefService defService;
    private final InsService insService;
    private final TaskService taskService;
    private final UserService userService;

    public Definition deploy(String json) {
        return defService.importJson(json);
    }

    public List<Definition> definitionList() {
        return defService.getByFlowCode(null);
    }

    public Definition getDefinition(Long id) {
        return defService.getById(id);
    }

    public void deleteDefinition(Long id) {
        defService.removeDef(Collections.singletonList(id));
    }

    public Instance startProcess(Long definitionId, String businessId, Map<String, Object> variables) {
        Long userId = StpUtil.getLoginIdAsLong();
        Definition def = defService.getById(definitionId);
        if (def == null) {
            throw new BusinessException("流程定义不存在");
        }

        FlowParams flowParams = FlowParams.build()
                .flowCode(def.getFlowCode())
                .handler(String.valueOf(userId))
                .variable(variables);

        return insService.start(businessId, flowParams);
    }

    public void approve(Long taskId, String comment) {
        Long userId = StpUtil.getLoginIdAsLong();
        FlowParams flowParams = FlowParams.build()
                .skipType("PASS")
                .handler(String.valueOf(userId))
                .message(comment)
                .ignore(true);
        taskService.skip(taskId, flowParams);
    }

    public void reject(Long taskId, String comment) {
        Long userId = StpUtil.getLoginIdAsLong();
        FlowParams flowParams = FlowParams.build()
                .skipType("REJECT")
                .handler(String.valueOf(userId))
                .message(comment)
                .ignore(true);
        taskService.skip(taskId, flowParams);
    }

    public void transfer(Long taskId, String targetUserId, String comment) {
        Long userId = StpUtil.getLoginIdAsLong();
        FlowParams flowParams = FlowParams.build()
                .handler(String.valueOf(userId))
                .addHandlers(Collections.singletonList(targetUserId))
                .message(comment)
                .ignore(true);
        taskService.transfer(taskId, flowParams);
    }

    public List<Task> myTodoTasks() {
        Long userId = StpUtil.getLoginIdAsLong();
        String handler = String.valueOf(userId);
        User userQuery = FlowEngine.newUser();
        userQuery.setProcessedBy(handler);
        userQuery.setType("1");
        List<User> users = userService.list(userQuery);
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> taskIds = users.stream()
                .map(User::getAssociated)
                .distinct()
                .collect(Collectors.toList());
        return taskService.getByIds(taskIds);
    }

    public Instance getInstance(Long instanceId) {
        return insService.getById(instanceId);
    }

    public void terminate(Long instanceId) {
        Long userId = StpUtil.getLoginIdAsLong();
        FlowParams flowParams = FlowParams.build()
                .handler(String.valueOf(userId))
                .ignore(true);
        taskService.terminationByInsId(instanceId, flowParams);
    }
}
