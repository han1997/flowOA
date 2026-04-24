package com.flowoa.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flowoa.common.BusinessException;
import com.flowoa.common.Constants;
import com.flowoa.entity.ExpenseApply;
import com.flowoa.entity.GenericApply;
import com.flowoa.entity.LeaveApply;
import com.flowoa.entity.SysUser;
import com.flowoa.mapper.ExpenseApplyMapper;
import com.flowoa.mapper.GenericApplyMapper;
import com.flowoa.mapper.LeaveApplyMapper;
import com.flowoa.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.warm.flow.core.FlowEngine;
import org.dromara.warm.flow.core.dto.FlowParams;
import org.dromara.warm.flow.core.entity.Definition;
import org.dromara.warm.flow.core.entity.Instance;
import org.dromara.warm.flow.core.entity.Node;
import org.dromara.warm.flow.core.entity.Skip;
import org.dromara.warm.flow.core.entity.Task;
import org.dromara.warm.flow.core.entity.User;
import org.dromara.warm.flow.core.enums.NodeType;
import org.dromara.warm.flow.core.enums.SkipType;
import org.dromara.warm.flow.core.service.DefService;
import org.dromara.warm.flow.core.service.InsService;
import org.dromara.warm.flow.core.service.TaskService;
import org.dromara.warm.flow.core.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlowService {

    private static final Pattern FLOW_CODE_PATTERN = Pattern.compile("flowCode\\s*=\\s*\"([^\"]+)\"");
    private static final String FLOW_CODE_LEAVE = "leave_apply";
    private static final String FLOW_CODE_EXPENSE = "expense_apply";
    private static final String FLOW_CODE_GENERIC = "generic_apply";

    private final DefService defService;
    private final InsService insService;
    private final TaskService taskService;
    private final UserService userService;
    private final LeaveApplyMapper leaveApplyMapper;
    private final ExpenseApplyMapper expenseApplyMapper;
    private final GenericApplyMapper genericApplyMapper;
    private final SysUserMapper sysUserMapper;

    public Definition deploy(String content) {
        if (!StringUtils.hasText(content)) {
            throw new BusinessException("Flow definition content cannot be empty");
        }
        String trimmed = content.trim();
        if (trimmed.startsWith("{")) {
            return defService.importJson(trimmed);
        }
        return deployTemplateByFlowCode(resolveFlowCode(trimmed));
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

    public Long getPublishedDefinitionId(String flowCode, String notFoundMessage) {
        Definition definition = defService.getPublishByFlowCode(flowCode);
        if (definition == null) {
            throw new BusinessException(notFoundMessage);
        }
        return definition.getId();
    }

    public Instance startProcess(Long definitionId, String businessId, Map<String, Object> variables) {
        Long userId = StpUtil.getLoginIdAsLong();
        Definition def = defService.getById(definitionId);
        if (def == null) {
            throw new BusinessException("Flow definition does not exist");
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
        Instance instance = taskService.skip(taskId, flowParams);
        syncBusinessStatus(instance, false);
    }

    public void reject(Long taskId, String comment) {
        Long userId = StpUtil.getLoginIdAsLong();
        FlowParams flowParams = FlowParams.build()
                .skipType("REJECT")
                .handler(String.valueOf(userId))
                .message(comment)
                .ignore(true);
        Instance instance = taskService.skip(taskId, flowParams);
        syncBusinessStatus(instance, true);
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

    private void syncBusinessStatus(Instance instance, boolean rejected) {
        if (instance == null) {
            return;
        }
        Map<String, Object> variableMap = instance.getVariableMap();
        String businessType = variableMap == null ? null : asString(variableMap.get("businessType"));
        Long businessId = variableMap == null ? null : asLong(variableMap.get("businessId"));
        if (businessId == null) {
            businessId = asLong(instance.getBusinessId());
        }
        if (!StringUtils.hasText(businessType) || businessId == null) {
            log.warn("Missing business linkage when syncing process status. instanceId={}", instance.getId());
            return;
        }
        String status = rejected
                ? Constants.STATUS_REJECTED
                : (Constants.FLOW_STATUS_FINISHED.equals(instance.getFlowStatus())
                ? Constants.STATUS_APPROVED
                : Constants.STATUS_PENDING);

        switch (businessType) {
            case "leave" -> updateLeaveStatus(businessId, status);
            case "expense" -> updateExpenseStatus(businessId, status);
            case "generic" -> updateGenericStatus(businessId, status);
            default -> log.warn("Unknown businessType when syncing process status: {}", businessType);
        }
    }

    private void updateLeaveStatus(Long businessId, String status) {
        LeaveApply apply = new LeaveApply();
        apply.setId(businessId);
        apply.setStatus(status);
        apply.setUpdateTime(LocalDateTime.now());
        leaveApplyMapper.updateById(apply);
    }

    private void updateExpenseStatus(Long businessId, String status) {
        ExpenseApply apply = new ExpenseApply();
        apply.setId(businessId);
        apply.setStatus(status);
        apply.setUpdateTime(LocalDateTime.now());
        expenseApplyMapper.updateById(apply);
    }

    private void updateGenericStatus(Long businessId, String status) {
        GenericApply apply = new GenericApply();
        apply.setId(businessId);
        apply.setStatus(status);
        apply.setUpdateTime(LocalDateTime.now());
        genericApplyMapper.updateById(apply);
    }

    private String resolveFlowCode(String content) {
        if (!content.startsWith("<")) {
            return content;
        }
        Matcher matcher = FLOW_CODE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new BusinessException("flowCode is missing in deploy content");
    }

    private Definition deployTemplateByFlowCode(String flowCode) {
        return switch (flowCode) {
            case FLOW_CODE_LEAVE -> deployLeaveTemplate();
            case FLOW_CODE_EXPENSE -> deployExpenseTemplate();
            case FLOW_CODE_GENERIC -> deployGenericTemplate();
            default -> throw new BusinessException("Unsupported flow code: " + flowCode);
        };
    }

    private Definition deployLeaveTemplate() {
        String managerId = resolveUserId("manager1", "admin");
        List<NodeSpec> nodeSpecs = List.of(
                new NodeSpec("start", "Start", NodeType.START.getKey(), null),
                new NodeSpec("manager", "Manager Approval", NodeType.BETWEEN.getKey(), managerId),
                new NodeSpec("end", "End", NodeType.END.getKey(), null)
        );
        List<SkipSpec> skipSpecs = List.of(
                new SkipSpec("start", "manager", SkipType.PASS.getKey(), "Submit"),
                new SkipSpec("manager", "end", SkipType.PASS.getKey(), "Approve"),
                new SkipSpec("manager", "start", SkipType.REJECT.getKey(), "Reject")
        );
        return saveTemplate(FLOW_CODE_LEAVE, "Leave Approval", nodeSpecs, skipSpecs);
    }

    private Definition deployExpenseTemplate() {
        String managerId = resolveUserId("manager1", "admin");
        String financeId = resolveUserId("finance1", "manager1", "admin");
        List<NodeSpec> nodeSpecs = List.of(
                new NodeSpec("start", "Start", NodeType.START.getKey(), null),
                new NodeSpec("manager", "Manager Approval", NodeType.BETWEEN.getKey(), managerId),
                new NodeSpec("finance", "Finance Approval", NodeType.BETWEEN.getKey(), financeId),
                new NodeSpec("end", "End", NodeType.END.getKey(), null)
        );
        List<SkipSpec> skipSpecs = List.of(
                new SkipSpec("start", "manager", SkipType.PASS.getKey(), "Submit"),
                new SkipSpec("manager", "finance", SkipType.PASS.getKey(), "Manager Approve"),
                new SkipSpec("finance", "end", SkipType.PASS.getKey(), "Finance Approve"),
                new SkipSpec("manager", "start", SkipType.REJECT.getKey(), "Manager Reject"),
                new SkipSpec("finance", "start", SkipType.REJECT.getKey(), "Finance Reject")
        );
        return saveTemplate(FLOW_CODE_EXPENSE, "Expense Approval", nodeSpecs, skipSpecs);
    }

    private Definition deployGenericTemplate() {
        String managerId = resolveUserId("manager1", "admin");
        List<NodeSpec> nodeSpecs = List.of(
                new NodeSpec("start", "Start", NodeType.START.getKey(), null),
                new NodeSpec("manager", "Approver", NodeType.BETWEEN.getKey(), managerId),
                new NodeSpec("end", "End", NodeType.END.getKey(), null)
        );
        List<SkipSpec> skipSpecs = List.of(
                new SkipSpec("start", "manager", SkipType.PASS.getKey(), "Submit"),
                new SkipSpec("manager", "end", SkipType.PASS.getKey(), "Approve"),
                new SkipSpec("manager", "start", SkipType.REJECT.getKey(), "Reject")
        );
        return saveTemplate(FLOW_CODE_GENERIC, "Generic Approval", nodeSpecs, skipSpecs);
    }

    private Definition saveTemplate(String flowCode, String flowName, List<NodeSpec> nodeSpecs, List<SkipSpec> skipSpecs) {
        Definition definition = FlowEngine.newDef()
                .setFlowCode(flowCode)
                .setFlowName(flowName)
                .setCategory("approval")
                .setFormCustom("N")
                .setIsPublish(1)
                .setActivityStatus(1);
        FlowEngine.dataFillHandler().idFill(definition);
        Long definitionId = definition.getId();

        Map<String, Integer> nodeTypeMap = nodeSpecs.stream()
                .collect(Collectors.toMap(NodeSpec::code, NodeSpec::nodeType));

        List<Node> nodes = nodeSpecs.stream()
                .map(spec -> toNode(spec, definitionId))
                .collect(Collectors.toList());

        List<Skip> skips = skipSpecs.stream()
                .map(spec -> toSkip(spec, nodeTypeMap, definitionId))
                .collect(Collectors.toList());

        Definition created = defService.insertFlow(definition, nodes, skips);
        defService.publish(created.getId());
        return defService.getById(created.getId());
    }

    private Node toNode(NodeSpec spec, Long definitionId) {
        return FlowEngine.newNode()
                .setDefinitionId(definitionId)
                .setNodeCode(spec.code())
                .setNodeName(spec.nodeName())
                .setNodeType(spec.nodeType())
                .setNodeRatio("0")
                .setPermissionFlag(spec.permissionFlag());
    }

    private Skip toSkip(SkipSpec spec, Map<String, Integer> nodeTypeMap, Long definitionId) {
        return FlowEngine.newSkip()
                .setDefinitionId(definitionId)
                .setNowNodeCode(spec.fromNodeCode())
                .setNowNodeType(nodeTypeMap.get(spec.fromNodeCode()))
                .setNextNodeCode(spec.toNodeCode())
                .setNextNodeType(nodeTypeMap.get(spec.toNodeCode()))
                .setSkipType(spec.skipType())
                .setSkipName(spec.skipName());
    }

    private String resolveUserId(String... usernames) {
        for (String username : usernames) {
            SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUsername, username)
                    .eq(SysUser::getStatus, 1));
            if (user != null) {
                return String.valueOf(user.getId());
            }
        }
        throw new BusinessException("No available approver found");
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private record NodeSpec(String code, String nodeName, Integer nodeType, String permissionFlag) {
    }

    private record SkipSpec(String fromNodeCode, String toNodeCode, String skipType, String skipName) {
    }
}
