package com.flowoa.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flowoa.common.BusinessException;
import com.flowoa.common.Constants;
import com.flowoa.common.PageResult;
import com.flowoa.entity.ExpenseApply;
import com.flowoa.entity.SysDept;
import com.flowoa.entity.SysUser;
import com.flowoa.mapper.ExpenseApplyMapper;
import com.flowoa.mapper.SysDeptMapper;
import com.flowoa.mapper.SysUserMapper;
import org.dromara.warm.flow.core.entity.Instance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExpenseApplyService extends ServiceImpl<ExpenseApplyMapper, ExpenseApply> {

    private final FlowService flowService;
    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;

    private static final String FLOW_CODE = "expense_apply";

    public PageResult<ExpenseApply> page(Integer pageNum, Integer pageSize, String status, Long userId) {
        LambdaQueryWrapper<ExpenseApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(status), ExpenseApply::getStatus, status)
               .eq(userId != null, ExpenseApply::getUserId, userId)
               .orderByDesc(ExpenseApply::getCreateTime);
        Page<ExpenseApply> page = page(new Page<>(pageNum, pageSize), wrapper);
        page.getRecords().forEach(this::fillApplyInfo);
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Transactional
    public void submitApply(ExpenseApply apply) {
        Long userId = StpUtil.getLoginIdAsLong();
        apply.setUserId(userId);
        apply.setStatus(Constants.STATUS_PENDING);
        save(apply);

        Map<String, Object> variables = new HashMap<>();
        variables.put("businessType", "expense");
        variables.put("businessId", apply.getId());
        variables.put("title", apply.getTitle());

        Instance instance = flowService.startProcess(getDefinitionId(), String.valueOf(apply.getId()), variables);
        apply.setFlowInstanceId(instance.getId());
        updateById(apply);
    }

    @Transactional
    public void approve(Long applyId, Long taskId, String comment) {
        ExpenseApply apply = getById(applyId);
        if (apply == null) {
            throw new BusinessException("申请不存在");
        }
        flowService.approve(taskId, comment);

        Instance instance = flowService.getInstance(apply.getFlowInstanceId());
        if (instance != null && Constants.FLOW_STATUS_FINISHED.equals(instance.getFlowStatus())) {
            apply.setStatus(Constants.STATUS_APPROVED);
        } else {
            apply.setStatus(Constants.STATUS_PENDING);
        }
        updateById(apply);
    }

    @Transactional
    public void reject(Long applyId, Long taskId, String comment) {
        ExpenseApply apply = getById(applyId);
        if (apply == null) {
            throw new BusinessException("申请不存在");
        }
        flowService.reject(taskId, comment);
        apply.setStatus(Constants.STATUS_REJECTED);
        updateById(apply);
    }

    @Transactional
    public void cancel(Long applyId) {
        ExpenseApply apply = getById(applyId);
        if (apply == null) {
            throw new BusinessException("申请不存在");
        }
        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (!apply.getUserId().equals(currentUserId)) {
            throw new BusinessException("只能取消自己的申请");
        }
        if (apply.getFlowInstanceId() != null) {
            flowService.terminate(apply.getFlowInstanceId());
        }
        apply.setStatus(Constants.STATUS_CANCELLED);
        updateById(apply);
    }

    private void fillApplyInfo(ExpenseApply apply) {
        SysUser user = userMapper.selectById(apply.getUserId());
        if (user != null) {
            apply.setUserName(user.getName());
            if (user.getDeptId() != null) {
                SysDept dept = deptMapper.selectById(user.getDeptId());
                if (dept != null) {
                    apply.setDeptName(dept.getName());
                }
            }
        }
    }

    private Long getDefinitionId() {
        var definitions = flowService.definitionList();
        return definitions.stream()
                .filter(d -> FLOW_CODE.equals(d.getFlowCode()))
                .findFirst()
                .map(d -> d.getId())
                .orElseThrow(() -> new BusinessException("报销流程定义不存在，请先部署流程"));
    }
}
