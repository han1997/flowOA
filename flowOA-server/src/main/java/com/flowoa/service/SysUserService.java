package com.flowoa.service;

import cn.dev33.satoken.secure.BCrypt;
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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
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
            throw new BusinessException("用户已被禁用");
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
        validateUsername(user.getUsername());
        long count = lambdaQuery().eq(SysUser::getUsername, user.getUsername()).count();
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        // Clear historical logically deleted rows with the same username so username can be reused.
        baseMapper.forceDeleteDeletedByUsername(user.getUsername());
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setPassword(BCrypt.hashpw(user.getPassword()));
        try {
            save(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("用户名已存在");
        }
    }

    public void updateUser(SysUser user) {
        if (user.getId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (StringUtils.hasText(user.getUsername())) {
            validateUsername(user.getUsername());
            long duplicateCount = lambdaQuery()
                    .eq(SysUser::getUsername, user.getUsername())
                    .ne(SysUser::getId, user.getId())
                    .count();
            if (duplicateCount > 0) {
                throw new BusinessException("用户名已存在");
            }
            baseMapper.forceDeleteDeletedByUsername(user.getUsername());
        }
        user.setUpdateTime(LocalDateTime.now());
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(BCrypt.hashpw(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        try {
            updateById(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("用户名已存在");
        }
    }

    public void resetPassword(Long userId, String newPassword) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setPassword(BCrypt.hashpw(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        updateById(user);
    }

    public void deleteUser(Long userId) {
        removeById(userId);
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

    private void validateUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new BusinessException("用户名不能为空");
        }
    }
}
