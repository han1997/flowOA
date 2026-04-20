package com.flowoa.service;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flowoa.common.BusinessException;
import com.flowoa.common.PageResult;
import com.flowoa.entity.SysDept;
import com.flowoa.entity.SysRole;
import com.flowoa.entity.SysUser;
import com.flowoa.mapper.SysDeptMapper;
import com.flowoa.mapper.SysRoleMapper;
import com.flowoa.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {

    private final SysDeptMapper deptMapper;
    private final SysRoleMapper roleMapper;

    public SysUser login(String username, String password) {
        SysUser user = lambdaQuery().eq(SysUser::getUsername, username).one();
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("用户已被停用");
        }
        return user;
    }

    public PageResult<SysUser> page(Integer pageNum, Integer pageSize, String name, Long deptId, Integer status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), SysUser::getName, name)
               .eq(deptId != null, SysUser::getDeptId, deptId)
               .eq(status != null, SysUser::getStatus, status)
               .orderByDesc(SysUser::getCreateTime);

        Page<SysUser> page = page(new Page<>(pageNum, pageSize), wrapper);
        page.getRecords().forEach(u -> {
            u.setPassword(null);
            fillUserInfo(u);
        });
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    public List<SysUser> listAll() {
        List<SysUser> users = lambdaQuery().eq(SysUser::getStatus, 1).list();
        users.forEach(u -> {
            u.setPassword(null);
            fillUserInfo(u);
        });
        return users;
    }

    public void createUser(SysUser user) {
        // 检查用户名唯一
        long count = lambdaQuery().eq(SysUser::getUsername, user.getUsername()).count();
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        user.setPassword(BCrypt.hashpw(user.getPassword()));
        save(user);
    }

    public void updateUser(SysUser user) {
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(BCrypt.hashpw(user.getPassword()));
        } else {
            // 不更新密码字段
            user.setPassword(null);
        }
        updateById(user);
    }

    public void resetPassword(Long userId, String newPassword) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setPassword(BCrypt.hashpw(newPassword));
        updateById(user);
    }

    public void fillUserInfo(SysUser user) {
        if (user.getDeptId() != null) {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) {
                user.setDeptName(dept.getName());
            }
        }
        if (user.getRoleId() != null) {
            SysRole role = roleMapper.selectById(user.getRoleId());
            if (role != null) {
                user.setRoleName(role.getName());
                user.setRoleCode(role.getCode());
            }
        }
    }
}
