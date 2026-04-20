package com.flowoa.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flowoa.entity.SysDept;
import com.flowoa.mapper.SysDeptMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysDeptService extends ServiceImpl<SysDeptMapper, SysDept> {

    public List<SysDept> treeList() {
        List<SysDept> allDepts = lambdaQuery().orderByAsc(SysDept::getSort).list();
        return buildTree(allDepts);
    }

    private List<SysDept> buildTree(List<SysDept> allDepts) {
        Map<Long, List<SysDept>> groupByParent = allDepts.stream()
                .collect(Collectors.groupingBy(SysDept::getParentId));

        allDepts.forEach(dept -> dept.setChildren(groupByParent.getOrDefault(dept.getId(), new ArrayList<>())));
        return allDepts.stream()
                .filter(dept -> dept.getParentId() == 0)
                .collect(Collectors.toList());
    }
}
