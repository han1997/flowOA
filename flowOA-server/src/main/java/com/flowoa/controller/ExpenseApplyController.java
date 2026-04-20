package com.flowoa.controller;

import com.flowoa.common.PageResult;
import com.flowoa.common.Result;
import com.flowoa.dto.ApproveDTO;
import com.flowoa.entity.ExpenseApply;
import com.flowoa.service.ExpenseApplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/approval/expense")
@RequiredArgsConstructor
public class ExpenseApplyController {

    private final ExpenseApplyService expenseApplyService;

    @GetMapping("/page")
    public Result<PageResult<ExpenseApply>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId) {
        return Result.ok(expenseApplyService.page(pageNum, pageSize, status, userId));
    }

    @GetMapping("/{id}")
    public Result<ExpenseApply> getById(@PathVariable Long id) {
        return Result.ok(expenseApplyService.getById(id));
    }

    @PostMapping("/submit")
    public Result<?> submit(@RequestBody ExpenseApply apply) {
        expenseApplyService.submitApply(apply);
        return Result.ok();
    }

    @PostMapping("/approve")
    public Result<?> approve(@Valid @RequestBody ApproveDTO dto) {
        expenseApplyService.approve(dto.getApplyId(), dto.getTaskId(), dto.getComment());
        return Result.ok();
    }

    @PostMapping("/reject")
    public Result<?> reject(@Valid @RequestBody ApproveDTO dto) {
        expenseApplyService.reject(dto.getApplyId(), dto.getTaskId(), dto.getComment());
        return Result.ok();
    }

    @PostMapping("/cancel/{id}")
    public Result<?> cancel(@PathVariable Long id) {
        expenseApplyService.cancel(id);
        return Result.ok();
    }
}
