package com.flowoa.controller;

import com.flowoa.common.Result;
import com.flowoa.dto.ApproveDTO;
import com.flowoa.dto.TransferDTO;
import com.flowoa.service.FlowService;
import org.dromara.warm.flow.core.entity.Instance;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flow")
@RequiredArgsConstructor
public class FlowController {

    private final FlowService flowService;

    @GetMapping("/instance/{id}")
    public Result<Instance> getInstance(@PathVariable Long id) {
        return Result.ok(flowService.getInstance(id));
    }

    @PostMapping("/approve")
    public Result<?> approve(@Valid @RequestBody ApproveDTO dto) {
        flowService.approve(dto.getTaskId(), dto.getComment());
        return Result.ok();
    }

    @PostMapping("/reject")
    public Result<?> reject(@Valid @RequestBody ApproveDTO dto) {
        flowService.reject(dto.getTaskId(), dto.getComment());
        return Result.ok();
    }

    @PostMapping("/transfer")
    public Result<?> transfer(@Valid @RequestBody TransferDTO dto) {
        flowService.transfer(dto.getTaskId(), dto.getTargetUserId(), dto.getComment());
        return Result.ok();
    }

    @PostMapping("/terminate/{instanceId}")
    public Result<?> terminate(@PathVariable Long instanceId) {
        flowService.terminate(instanceId);
        return Result.ok();
    }
}
