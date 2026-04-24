package com.flowoa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flowoa.entity.SysUser;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Delete("DELETE FROM sys_user WHERE username = #{username} AND deleted = 1")
    int forceDeleteDeletedByUsername(@Param("username") String username);
}
