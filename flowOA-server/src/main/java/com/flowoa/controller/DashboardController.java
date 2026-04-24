package com.flowoa.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.flowoa.common.Result;
import com.flowoa.entity.ExpenseApply;
import com.flowoa.entity.GenericApply;
import com.flowoa.entity.LeaveApply;
import com.flowoa.service.ExpenseApplyService;
import com.flowoa.service.FlowService;
import com.flowoa.service.GenericApplyService;
import com.flowoa.service.LeaveApplyService;
import lombok.RequiredArgsConstructor;
import org.dromara.warm.flow.core.entity.Task;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final LeaveApplyService leaveApplyService;
    private final ExpenseApplyService expenseApplyService;
    private final GenericApplyService genericApplyService;
    private final FlowService flowService;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Long userId = StpUtil.getLoginIdAsLong();
        Map<String, Object> stats = new HashMap<>();

        long leaveCount = leaveApplyService.lambdaQuery()
                .eq(LeaveApply::getUserId, userId).count();
        long expenseCount = expenseApplyService.lambdaQuery()
                .eq(ExpenseApply::getUserId, userId).count();
        long genericCount = genericApplyService.lambdaQuery()
                .eq(GenericApply::getUserId, userId).count();
        List<Task> todoTasks = flowService.myTodoTasks();

        stats.put("leaveCount", leaveCount);
        stats.put("expenseCount", expenseCount);
        stats.put("genericCount", genericCount);
        stats.put("todoCount", todoTasks.size());
        stats.put("totalApply", leaveCount + expenseCount + genericCount);

        return Result.ok(stats);
    }
}