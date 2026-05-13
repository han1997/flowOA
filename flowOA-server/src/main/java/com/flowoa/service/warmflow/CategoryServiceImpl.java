package com.flowoa.service.warmflow;

import org.dromara.warm.flow.core.dto.Tree;
import org.dromara.warm.flow.ui.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 流程分类服务实现
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Override
    public List<Tree> queryCategory() {
        // Return default categories
        List<Tree> categories = new ArrayList<>();

        Tree approvalCategory = new Tree();
        approvalCategory.setId("1");
        approvalCategory.setName("审批流程");
        approvalCategory.setParentId("0");
        categories.add(approvalCategory);

        Tree businessCategory = new Tree();
        businessCategory.setId("2");
        businessCategory.setName("业务流程");
        businessCategory.setParentId("0");
        categories.add(businessCategory);

        return categories;
    }
}
