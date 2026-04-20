package com.flowoa.controller;

import com.flowoa.common.PageResult;
import com.flowoa.common.Result;
import com.flowoa.dto.ApproveDTO;
import com.flowoa.entity.LeaveApply;
import com.flowoa.service.LeaveApplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/approval/leave")
@RequiredArgsConstructor
public class LeaveApplyController {

    private final LeaveApplyService leaveApplyService;

    @GetMapping("/page")
    public Result<PageResult<LeaveApply>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId) {
        return Result.ok(leaveApplyService.page(pageNum, pageSize, status, userId));
    }

    @GetMapping("/{id}")
    public Result<LeaveApply> getById(@PathVariable Long id) {
        return Result.ok(leaveApplyService.getById(id));
    }

    @PostMapping("/submit")
    public Result<?> submit(@RequestBody LeaveApply apply) {
        leaveApplyService.submitApply(apply);
        return Result.ok();
    }

    @PostMapping("/approve")
    public Result<?> approve(@Valid @RequestBody ApproveDTO dto) {
        // 从业务路径中获取applyId，这里通过taskId反查
        leaveApplyService.approve(dto.getApplyId(), dto.getTaskId(), dto.getComment());
        return Result.ok();
    }

    @PostMapping("/reject")
    public Result<?> reject(@Valid @RequestBody ApproveDTO dto) {
        leaveApplyService.reject(dto.getApplyId(), dto.getTaskId(), dto.getComment());
        return Result.ok();
    }

    @PostMapping("/cancel/{id}")
    public Result<?> cancel(@PathVariable Long id) {
        leaveApplyService.cancel(id);
        return Result.ok();
    }
}
