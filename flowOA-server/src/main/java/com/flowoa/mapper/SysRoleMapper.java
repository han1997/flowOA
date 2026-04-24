package com.flowoa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flowoa.entity.SysRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    @Delete("DELETE FROM sys_role WHERE code = #{code} AND deleted = 1")
    int forceDeleteDeletedByCode(@Param("code") String code);
}
