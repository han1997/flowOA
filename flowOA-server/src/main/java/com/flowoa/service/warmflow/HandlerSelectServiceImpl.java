package com.flowoa.service.warmflow;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flowoa.entity.SysUser;
import com.flowoa.mapper.SysUserMapper;
import org.dromara.warm.flow.core.dto.FlowPage;
import org.dromara.warm.flow.ui.dto.HandlerQuery;
import org.dromara.warm.flow.ui.service.HandlerSelectService;
import org.dromara.warm.flow.ui.vo.HandlerSelectVo;
import org.dromara.warm.flow.ui.vo.HandlerAuth;
import org.dromara.warm.flow.ui.vo.HandlerFeedBackVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 办理人选择服务实现
 */
@Service
public class HandlerSelectServiceImpl implements HandlerSelectService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public List<String> getHandlerType() {
        // Return handler types for tabs
        List<String> types = new ArrayList<>();
        types.add("用户");
        // Can add more types like: 部门, 角色 etc.
        return types;
    }

    @Override
    public HandlerSelectVo getHandlerSelect(HandlerQuery query) {
        // Query users based on search text
        List<SysUser> users = sysUserMapper.selectList(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getStatus, 1)
                .like(query.getHandlerName() != null && !query.getHandlerName().isEmpty(),
                      SysUser::getName, query.getHandlerName())
        );

        // Convert to HandlerAuth list
        List<HandlerAuth> handlerAuths = users.stream().map(user -> {
            HandlerAuth auth = new HandlerAuth();
            auth.setStorageId(String.valueOf(user.getId()));
            auth.setHandlerCode(user.getUsername());
            auth.setHandlerName(user.getName());
            auth.setGroupName("用户");
            return auth;
        }).collect(Collectors.toList());

        return getResult(handlerAuths, handlerAuths.size());
    }

    @Override
    public List<HandlerFeedBackVo> handlerFeedback(List<String> storageIds) {
        if (storageIds == null || storageIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> normalizedIds = storageIds.stream()
            .filter(Objects::nonNull)
            .flatMap(storageId -> Arrays.stream(storageId.split(",")))
            .map(String::trim)
            .filter(storageId -> !storageId.isEmpty())
            .collect(Collectors.toList());

        if (normalizedIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Only query numeric ids from DB and keep non-numeric expressions as-is.
        List<Long> numericIds = normalizedIds.stream()
            .filter(this::isNumericId)
            .map(Long::valueOf)
            .distinct()
            .collect(Collectors.toList());

        Map<Long, String> userNameMap = numericIds.isEmpty()
            ? Collections.emptyMap()
            : sysUserMapper.selectBatchIds(numericIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                    SysUser::getId,
                    user -> user.getName() == null || user.getName().isEmpty()
                        ? String.valueOf(user.getId())
                        : user.getName(),
                    (left, right) -> left
                ));

        return normalizedIds.stream().map(storageId -> {
            HandlerFeedBackVo vo = new HandlerFeedBackVo();
            vo.setStorageId(storageId);
            if (isNumericId(storageId)) {
                vo.setHandlerName(userNameMap.getOrDefault(Long.valueOf(storageId), storageId));
            } else {
                vo.setHandlerName(storageId);
            }
            return vo;
        }).collect(Collectors.toList());
    }

    private boolean isNumericId(String storageId) {
        if (storageId == null || storageId.isEmpty()) {
            return false;
        }
        for (int i = 0; i < storageId.length(); i++) {
            if (!Character.isDigit(storageId.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
