package com.flowoa.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flowoa.common.BusinessException;
import com.flowoa.entity.SysRole;
import com.flowoa.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> {

    public List<SysRole> listAll() {
        return lambdaQuery().eq(SysRole::getStatus, 1).orderByAsc(SysRole::getId).list();
    }

    public void createRole(SysRole role) {
        validateRoleCode(role.getCode());
        if (existsActiveCode(role.getCode(), null)) {
            throw new BusinessException("角色编码已存在");
        }
        // Clear historical logically deleted rows with the same code so code can be reused.
        baseMapper.forceDeleteDeletedByCode(role.getCode());
        try {
            save(role);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("角色编码已存在");
        }
    }

    public void updateRole(SysRole role) {
        if (role.getId() == null) {
            throw new BusinessException("角色ID不能为空");
        }
        validateRoleCode(role.getCode());
        if (existsActiveCode(role.getCode(), role.getId())) {
            throw new BusinessException("角色编码已存在");
        }
        baseMapper.forceDeleteDeletedByCode(role.getCode());
        try {
            updateById(role);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("角色编码已存在");
        }
    }

    public void deleteRole(Long id) {
        removeById(id);
    }

    private boolean existsActiveCode(String code, Long excludeId) {
        return lambdaQuery()
                .eq(SysRole::getCode, code)
                .ne(excludeId != null, SysRole::getId, excludeId)
                .count() > 0;
    }

    private void validateRoleCode(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BusinessException("角色编码不能为空");
        }
    }
}
