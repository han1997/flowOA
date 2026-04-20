package com.flowoa.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.flowoa.common.Result;
import com.flowoa.service.FlowService;
import org.dromara.warm.flow.core.entity.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/approval/task")
@RequiredArgsConstructor
public class MyTaskController {

    private final FlowService flowService;

    @GetMapping("/todo")
    public Result<List<Task>> myTodoTasks() {
        return Result.ok(flowService.myTodoTasks());
    }
}
