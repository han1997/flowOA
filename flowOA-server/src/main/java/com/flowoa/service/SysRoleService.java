package com.flowoa.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flowoa.entity.SysRole;
import com.flowoa.mapper.SysRoleMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> {

    public List<SysRole> listAll() {
        return lambdaQuery().eq(SysRole::getStatus, 1).orderByAsc(SysRole::getId).list();
    }
}
