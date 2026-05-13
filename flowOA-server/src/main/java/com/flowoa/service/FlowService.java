package com.flowoa.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flowoa.common.BusinessException;
import com.flowoa.common.Constants;
import com.flowoa.dto.FlowDesignDTO;
import com.flowoa.entity.ExpenseApply;
import com.flowoa.entity.GenericApply;
import com.flowoa.entity.LeaveApply;
import com.flowoa.entity.SysUser;
import com.flowoa.mapper.ExpenseApplyMapper;
import com.flowoa.mapper.GenericApplyMapper;
import com.flowoa.mapper.LeaveApplyMapper;
import com.flowoa.mapper.SysUserMapper;
import com.flowoa.vo.FlowProgressVO;
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
import org.dromara.warm.flow.core.service.NodeService;
import org.dromara.warm.flow.core.service.SkipService;
import org.dromara.warm.flow.core.service.TaskService;
import org.dromara.warm.flow.core.service.UserService;
import org.dromara.warm.flow.orm.entity.FlowTask;
import org.dromara.warm.flow.orm.mapper.FlowTaskMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private final NodeService nodeService;
    private final SkipService skipService;
    private final LeaveApplyMapper leaveApplyMapper;
    private final ExpenseApplyMapper expenseApplyMapper;
    private final GenericApplyMapper genericApplyMapper;
    private final SysUserMapper sysUserMapper;
    private final ObjectMapper objectMapper;
    private final FlowTaskMapper flowTaskMapper;

    public Definition deploy(String content) {
        if (!StringUtils.hasText(content)) {
            throw new BusinessException("流程定义内容不能为空");
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

    public Definition getDefinitionWithNodes(Long id) {
        Definition definition = defService.getById(id);
        if (definition == null) {
            return null;
        }
        // Get nodes and skips for this definition using service methods
        Node nodeQuery = FlowEngine.newNode();
        nodeQuery.setDefinitionId(id);
        List<Node> nodeList = nodeService.list(nodeQuery);

        Skip skipQuery = FlowEngine.newSkip();
        skipQuery.setDefinitionId(id);
        List<Skip> skipList = skipService.list(skipQuery);

        // Attach to definition via ext field (as JSON string)
        Map<String, Object> ext = new HashMap<>();
        ext.put("nodeList", nodeList);
        ext.put("skipList", skipList);
        try {
            definition.setExt(objectMapper.writeValueAsString(ext));
        } catch (Exception e) {
            log.error("序列化节点和连线数据失败", e);
        }
        return definition;
    }

    @Transactional
    public void saveDesign(FlowDesignDTO design) {
        if (design.getId() == null) {
            // Create new definition
            createDefinitionFromDesign(design);
        } else {
            // Update existing definition
            updateDefinitionFromDesign(design);
        }
    }

    public void publishDefinition(Long id) {
        defService.publish(id);
    }

    private void createDefinitionFromDesign(FlowDesignDTO design) {
        List<Definition> existing = defService.getByFlowCode(design.getFlowCode());
        if (existing != null && !existing.isEmpty()) {
            throw new BusinessException("流程编码已存在: " + design.getFlowCode());
        }

        Definition definition = FlowEngine.newDef()
                .setFlowCode(design.getFlowCode())
                .setFlowName(design.getFlowName())
                .setCategory(design.getFlowCategory())
                .setFormCustom("N")
                .setIsPublish(0)  // Draft
                .setActivityStatus(1);
        FlowEngine.dataFillHandler().idFill(definition);
        Long definitionId = definition.getId();

        List<Node> nodes = createNodesFromDesign(design.getNodes(), definitionId);
        List<Skip> skips = createSkipsFromDesign(design.getSkips(), design.getNodes(), definitionId);

        defService.insertFlow(definition, nodes, skips);
    }

    private void updateDefinitionFromDesign(FlowDesignDTO design) {
        Definition existing = defService.getById(design.getId());
        if (existing == null) {
            throw new BusinessException("流程定义不存在");
        }
        if (Integer.valueOf(1).equals(existing.getIsPublish())) {
            throw new BusinessException("已发布的流程定义不可直接编辑，请新建版本或取消发布");
        }

        // Update basic info
        existing.setFlowName(design.getFlowName());
        existing.setCategory(design.getFlowCategory());

        // Delete existing nodes and skips using service
        Node nodeQuery = FlowEngine.newNode();
        nodeQuery.setDefinitionId(design.getId());
        nodeService.remove(nodeQuery);

        Skip skipQuery = FlowEngine.newSkip();
        skipQuery.setDefinitionId(design.getId());
        skipService.remove(skipQuery);

        // Create new nodes and skips
        List<Node> nodes = createNodesFromDesign(design.getNodes(), design.getId());
        List<Skip> skips = createSkipsFromDesign(design.getSkips(), design.getNodes(), design.getId());

        // Update existing definition row (do not call insertFlow — it would INSERT a duplicate
        // and bump the version inappropriately for an in-place edit of a draft).
        defService.updateById(existing);
        if (!nodes.isEmpty()) {
            nodeService.saveBatch(nodes);
        }
        if (!skips.isEmpty()) {
            skipService.saveBatch(skips);
        }
    }

    private List<Node> createNodesFromDesign(List<FlowDesignDTO.NodeDesign> nodeDesigns, Long definitionId) {
        if (nodeDesigns == null) return new ArrayList<>();
        return nodeDesigns.stream()
                .map(nd -> {
                    Node node = FlowEngine.newNode()
                            .setDefinitionId(definitionId)
                            .setNodeCode(nd.getCode())
                            .setNodeName(nd.getName())
                            .setNodeType(nd.getType())
                            .setNodeRatio("0")
                            .setPermissionFlag(nd.getPermissionFlag());
                    // Store coordinates in coordinate field as JSON
                    if (nd.getX() != null && nd.getY() != null) {
                        node.setCoordinate(String.format(java.util.Locale.ROOT, "{\"x\":%.0f,\"y\":%.0f}", nd.getX(), nd.getY()));
                    }
                    return node;
                })
                .collect(Collectors.toList());
    }

    private List<Skip> createSkipsFromDesign(List<FlowDesignDTO.SkipDesign> skipDesigns,
                                              List<FlowDesignDTO.NodeDesign> nodeDesigns,
                                              Long definitionId) {
        if (skipDesigns == null) return new ArrayList<>();

        Map<String, Integer> nodeTypeMap = nodeDesigns == null ? new HashMap<>() :
                nodeDesigns.stream().collect(Collectors.toMap(FlowDesignDTO.NodeDesign::getCode, FlowDesignDTO.NodeDesign::getType));

        return skipDesigns.stream()
                .map(sd -> FlowEngine.newSkip()
                        .setDefinitionId(definitionId)
                        .setNowNodeCode(sd.getFromNodeCode())
                        .setNowNodeType(nodeTypeMap.getOrDefault(sd.getFromNodeCode(), 1))
                        .setNextNodeCode(sd.getToNodeCode())
                        .setNextNodeType(nodeTypeMap.getOrDefault(sd.getToNodeCode(), 1))
                        .setSkipType(sd.getSkipType())
                        .setSkipName(sd.getSkipName()))
                .collect(Collectors.toList());
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

    public Map<Long, FlowProgressVO> getProgressBatch(List<Long> instanceIds) {
        if (instanceIds == null || instanceIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> validIds = instanceIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (validIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // Seed result with default VO for every requested instance so callers can rely on
        // a non-null entry per id.
        Map<Long, FlowProgressVO> result = new HashMap<>();
        for (Long id : validIds) {
            result.put(id, defaultProgressVO(id));
        }

        // Batch 1: instance flow statuses.
        List<Instance> instances = insService.getByIds(validIds);
        if (instances != null) {
            for (Instance inst : instances) {
                FlowProgressVO vo = result.get(inst.getId());
                if (vo != null) {
                    vo.setFlowStatus(inst.getFlowStatus());
                }
            }
        }

        // Batch 2: all current tasks across the given instances in a single query.
        List<FlowTask> tasks = flowTaskMapper.selectList(
                new LambdaQueryWrapper<FlowTask>().in(FlowTask::getInstanceId, validIds));
        if (tasks == null || tasks.isEmpty()) {
            return result;
        }
        Map<Long, List<FlowTask>> tasksByInstance =
                tasks.stream().collect(Collectors.groupingBy(FlowTask::getInstanceId));

        tasksByInstance.forEach((insId, taskList) -> {
            Set<String> nodeNames = taskList.stream()
                    .map(FlowTask::getNodeName)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            result.get(insId).setCurrentNodeNames(new ArrayList<>(nodeNames));
        });

        // Batch 3: approver/transfer/cc associations for all tasks at once.
        List<Long> taskIds = tasks.stream()
                .map(FlowTask::getId)
                .distinct()
                .collect(Collectors.toList());
        List<User> users = userService.getByAssociateds(taskIds, "1", "2", "3");
        if (users == null || users.isEmpty()) {
            return result;
        }

        Map<Long, LinkedHashSet<String>> approverIdsByTask = new HashMap<>();
        Set<String> allApproverIds = new LinkedHashSet<>();
        for (User u : users) {
            if (!StringUtils.hasText(u.getProcessedBy())) {
                continue;
            }
            approverIdsByTask.computeIfAbsent(u.getAssociated(), k -> new LinkedHashSet<>())
                    .add(u.getProcessedBy());
            allApproverIds.add(u.getProcessedBy());
        }

        // Batch 4: resolve numeric handler ids to display names in a single SysUser query.
        Map<Long, String> userNameMap = loadUserNameMap(allApproverIds);

        tasksByInstance.forEach((insId, taskList) -> {
            Set<String> approverIds = new LinkedHashSet<>();
            for (FlowTask t : taskList) {
                Set<String> tIds = approverIdsByTask.get(t.getId());
                if (tIds != null) {
                    approverIds.addAll(tIds);
                }
            }
            if (approverIds.isEmpty()) {
                return;
            }
            List<String> names = approverIds.stream()
                    .map(approverId -> resolveApproverName(approverId, userNameMap))
                    .collect(Collectors.toList());
            result.get(insId).setCurrentApproverNames(names);
        });

        return result;
    }

    public Instance getInstance(Long instanceId) {
        return insService.getById(instanceId);
    }

    public FlowProgressVO getProgress(Long instanceId) {
        if (instanceId == null) {
            return defaultProgressVO(null);
        }
        return getProgressBatch(Collections.singletonList(instanceId))
                .getOrDefault(instanceId, defaultProgressVO(instanceId));
    }

    private FlowProgressVO defaultProgressVO(Long instanceId) {
        FlowProgressVO vo = new FlowProgressVO();
        vo.setFlowInstanceId(instanceId);
        vo.setCurrentNodeNames(Collections.emptyList());
        vo.setCurrentApproverNames(Collections.emptyList());
        return vo;
    }

    private Map<Long, String> loadUserNameMap(Set<String> approverIds) {
        List<Long> numericUserIds = approverIds.stream()
                .filter(this::isNumericId)
                .map(Long::valueOf)
                .collect(Collectors.toList());
        if (numericUserIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return sysUserMapper.selectBatchIds(numericUserIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        SysUser::getId,
                        user -> StringUtils.hasText(user.getName()) ? user.getName() : user.getUsername(),
                        (left, right) -> left
                ));
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
            log.warn("同步流程状态时缺少业务关联信息，instanceId={}", instance.getId());
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
            default -> log.warn("同步流程状态时遇到未知业务类型: {}", businessType);
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
        throw new BusinessException("部署内容缺少 flowCode");
    }

    private Definition deployTemplateByFlowCode(String flowCode) {
        return switch (flowCode) {
            case FLOW_CODE_LEAVE -> deployLeaveTemplate();
            case FLOW_CODE_EXPENSE -> deployExpenseTemplate();
            case FLOW_CODE_GENERIC -> deployGenericTemplate();
            default -> throw new BusinessException("不支持的流程编码: " + flowCode);
        };
    }

    private Definition deployLeaveTemplate() {
        String managerId = resolveUserId("manager1", "admin");
        List<NodeSpec> nodeSpecs = List.of(
                new NodeSpec("start", "开始", NodeType.START.getKey(), null),
                new NodeSpec("manager", "经理审批", NodeType.BETWEEN.getKey(), managerId),
                new NodeSpec("end", "结束", NodeType.END.getKey(), null)
        );
        List<SkipSpec> skipSpecs = List.of(
                new SkipSpec("start", "manager", SkipType.PASS.getKey(), "提交"),
                new SkipSpec("manager", "end", SkipType.PASS.getKey(), "同意"),
                new SkipSpec("manager", "start", SkipType.REJECT.getKey(), "驳回")
        );
        return saveTemplate(FLOW_CODE_LEAVE, "请假审批", nodeSpecs, skipSpecs);
    }

    private Definition deployExpenseTemplate() {
        String managerId = resolveUserId("manager1", "admin");
        String financeId = resolveUserId("finance1", "manager1", "admin");
        List<NodeSpec> nodeSpecs = List.of(
                new NodeSpec("start", "开始", NodeType.START.getKey(), null),
                new NodeSpec("manager", "经理审批", NodeType.BETWEEN.getKey(), managerId),
                new NodeSpec("finance", "财务审批", NodeType.BETWEEN.getKey(), financeId),
                new NodeSpec("end", "结束", NodeType.END.getKey(), null)
        );
        List<SkipSpec> skipSpecs = List.of(
                new SkipSpec("start", "manager", SkipType.PASS.getKey(), "提交"),
                new SkipSpec("manager", "finance", SkipType.PASS.getKey(), "经理同意"),
                new SkipSpec("finance", "end", SkipType.PASS.getKey(), "财务同意"),
                new SkipSpec("manager", "start", SkipType.REJECT.getKey(), "经理驳回"),
                new SkipSpec("finance", "start", SkipType.REJECT.getKey(), "财务驳回")
        );
        return saveTemplate(FLOW_CODE_EXPENSE, "报销审批", nodeSpecs, skipSpecs);
    }

    private Definition deployGenericTemplate() {
        String managerId = resolveUserId("manager1", "admin");
        List<NodeSpec> nodeSpecs = List.of(
                new NodeSpec("start", "开始", NodeType.START.getKey(), null),
                new NodeSpec("manager", "审批人", NodeType.BETWEEN.getKey(), managerId),
                new NodeSpec("end", "结束", NodeType.END.getKey(), null)
        );
        List<SkipSpec> skipSpecs = List.of(
                new SkipSpec("start", "manager", SkipType.PASS.getKey(), "提交"),
                new SkipSpec("manager", "end", SkipType.PASS.getKey(), "同意"),
                new SkipSpec("manager", "start", SkipType.REJECT.getKey(), "驳回")
        );
        return saveTemplate(FLOW_CODE_GENERIC, "通用审批", nodeSpecs, skipSpecs);
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
        throw new BusinessException("未找到可用审批人");
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

    private boolean isNumericId(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private String resolveApproverName(String approverId, Map<Long, String> userNameMap) {
        if (!isNumericId(approverId)) {
            return approverId;
        }
        Long userId = Long.valueOf(approverId);
        return userNameMap.getOrDefault(userId, approverId);
    }

    private record NodeSpec(String code, String nodeName, Integer nodeType, String permissionFlag) {
    }

    private record SkipSpec(String fromNodeCode, String toNodeCode, String skipType, String skipName) {
    }
}
