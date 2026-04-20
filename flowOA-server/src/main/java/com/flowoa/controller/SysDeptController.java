package com.flowoa.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.flowoa.common.Result;
import com.flowoa.entity.SysDept;
import com.flowoa.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService deptService;

    @GetMapping("/tree")
    public Result<List<SysDept>> tree() {
        return Result.ok(deptService.treeList());
    }

    @GetMapping("/{id}")
    public Result<SysDept> getById(@PathVariable Long id) {
        return Result.ok(deptService.getById(id));
    }

    @SaCheckRole("admin")
    @PostMapping
    public Result<?> add(@RequestBody SysDept dept) {
        deptService.save(dept);
        return Result.ok();
    }

    @SaCheckRole("admin")
    @PutMapping
    public Result<?> update(@RequestBody SysDept dept) {
        deptService.updateById(dept);
        return Result.ok();
    }

    @SaCheckRole("admin")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        deptService.removeById(id);
        return Result.ok();
    }
}
