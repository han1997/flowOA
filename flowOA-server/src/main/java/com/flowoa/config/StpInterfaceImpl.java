package com.flowoa.config;

import cn.dev33.satoken.stp.StpInterface;
import com.flowoa.entity.SysRole;
import com.flowoa.entity.SysUser;
import com.flowoa.mapper.SysRoleMapper;
import com.flowoa.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        SysUser user = userMapper.selectById(Long.valueOf(loginId.toString()));
        if (user == null || user.getRoleId() == null) {
            return new ArrayList<>();
        }
        SysRole role = roleMapper.selectById(user.getRoleId());
        if (role == null) {
            return new ArrayList<>();
        }
        List<String> roles = new ArrayList<>();
        roles.add(role.getCode());
        return roles;
    }
}
