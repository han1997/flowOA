package com.flowoa.controller;

import com.flowoa.common.PageResult;
import com.flowoa.common.Result;
import com.flowoa.dto.ApproveDTO;
import com.flowoa.entity.GenericApply;
import com.flowoa.service.GenericApplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/approval/generic")
@RequiredArgsConstructor
public class GenericApplyController {

    private final GenericApplyService genericApplyService;

    @GetMapping("/page")
    public Result<PageResult<GenericApply>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String applyType) {
        return Result.ok(genericApplyService.page(pageNum, pageSize, status, userId, applyType));
    }

    @GetMapping("/{id}")
    public Result<GenericApply> getById(@PathVariable Long id) {
        return Result.ok(genericApplyService.getById(id));
    }

    @PostMapping("/submit")
    public Result<?> submit(@RequestBody GenericApply apply) {
        genericApplyService.submitApply(apply);
        return Result.ok();
    }

    @PostMapping("/approve")
    public Result<?> approve(@Valid @RequestBody ApproveDTO dto) {
        genericApplyService.approve(dto.getApplyId(), dto.getTaskId(), dto.getComment());
        return Result.ok();
    }

    @PostMapping("/reject")
    public Result<?> reject(@Valid @RequestBody ApproveDTO dto) {
        genericApplyService.reject(dto.getApplyId(), dto.getTaskId(), dto.getComment());
        return Result.ok();
    }

    @PostMapping("/cancel/{id}")
    public Result<?> cancel(@PathVariable Long id) {
        genericApplyService.cancel(id);
        return Result.ok();
    }
}
