package com.flowoa.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.flowoa.common.Result;
import com.flowoa.entity.SysUser;
import com.flowoa.service.SysUserService;
import com.flowoa.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService userService;

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginVO loginVO) {
        SysUser user = userService.login(loginVO.getUsername(), loginVO.getPassword());
        StpUtil.login(user.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("token", StpUtil.getTokenValue());
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("name", user.getName());
        return Result.ok(result);
    }

    @PostMapping("/logout")
    public Result<?> logout() {
        StpUtil.logout();
        return Result.ok();
    }

    @GetMapping("/info")
    public Result<?> info() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userService.getById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        user.setPassword(null);
        userService.fillUserInfo(user);
        return Result.ok(user);
    }
}
