package com.flowoa.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.flowoa.common.Result;
import com.flowoa.dto.FlowDesignDTO;
import com.flowoa.service.FlowService;
import org.dromara.warm.flow.core.entity.Definition;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flow/definition")
@RequiredArgsConstructor
public class FlowDefinitionController {

    private final FlowService flowService;

    @GetMapping("/list")
    public Result<List<Definition>> list() {
        return Result.ok(flowService.definitionList());
    }

    @GetMapping("/{id}")
    public Result<Definition> getById(@PathVariable Long id) {
        return Result.ok(flowService.getDefinitionWithNodes(id));
    }

    @SaCheckRole("admin")
    @PostMapping("/deploy")
    public Result<?> deploy(@RequestBody String xml) {
        flowService.deploy(xml);
        return Result.ok();
    }

    @SaCheckRole("admin")
    @PostMapping("/save")
    public Result<?> save(@Valid @RequestBody FlowDesignDTO design) {
        flowService.saveDesign(design);
        return Result.ok();
    }

    @SaCheckRole("admin")
    @PostMapping("/publish/{id}")
    public Result<?> publish(@PathVariable Long id) {
        flowService.publishDefinition(id);
        return Result.ok();
    }

    @SaCheckRole("admin")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        flowService.deleteDefinition(id);
        return Result.ok();
    }
}
