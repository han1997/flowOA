package com.flowoa.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.flowoa.common.Result;
import com.flowoa.entity.SysRole;
import com.flowoa.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @GetMapping("/list")
    public Result<List<SysRole>> list() {
        return Result.ok(roleService.listAll());
    }

    @GetMapping("/{id}")
    public Result<SysRole> getById(@PathVariable Long id) {
        return Result.ok(roleService.getById(id));
    }

    @SaCheckRole("admin")
    @PostMapping
    public Result<?> add(@RequestBody SysRole role) {
        roleService.save(role);
        return Result.ok();
    }

    @SaCheckRole("admin")
    @PutMapping
    public Result<?> update(@RequestBody SysRole role) {
        roleService.updateById(role);
        return Result.ok();
    }

    @SaCheckRole("admin")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        roleService.removeById(id);
        return Result.ok();
    }
}
