package com.flowoa.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.flowoa.common.PageResult;
import com.flowoa.common.Result;
import com.flowoa.entity.SysUser;
import com.flowoa.service.SysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @GetMapping("/page")
    public Result<PageResult<SysUser>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Integer status) {
        return Result.ok(userService.page(pageNum, pageSize, name, deptId, status));
    }

    @GetMapping("/list")
    public Result<List<SysUser>> list() {
        return Result.ok(userService.listAll());
    }

    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        SysUser user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
            userService.fillUserInfo(user);
        }
        return Result.ok(user);
    }

    @SaCheckRole("admin")
    @PostMapping
    public Result<?> add(@RequestBody SysUser user) {
        userService.createUser(user);
        return Result.ok();
    }

    @SaCheckRole("admin")
    @PutMapping
    public Result<?> update(@RequestBody SysUser user) {
        userService.updateUser(user);
        return Result.ok();
    }

    @SaCheckRole("admin")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.ok();
    }

    @SaCheckRole("admin")
    @PutMapping("/{id}/reset-password")
    public Result<?> resetPassword(@PathVariable Long id, @RequestBody String newPassword) {
        userService.resetPassword(id, newPassword);
        return Result.ok();
    }
}
