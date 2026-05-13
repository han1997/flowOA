package com.flowoa.service.warmflow;

import org.dromara.warm.flow.core.dto.Tree;
import org.dromara.warm.flow.ui.service.FormPathService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 表单路径服务实现
 */
@Service
public class FormPathServiceImpl implements FormPathService {

    @Override
    public List<Tree> queryFormPath() {
        // Return available form paths for flow design
        List<Tree> formPaths = new ArrayList<>();

        Tree leaveForm = new Tree();
        leaveForm.setId("leave_apply");
        leaveForm.setName("请假申请");
        leaveForm.setParentId("0");
        formPaths.add(leaveForm);

        Tree expenseForm = new Tree();
        expenseForm.setId("expense_apply");
        expenseForm.setName("报销申请");
        expenseForm.setParentId("0");
        formPaths.add(expenseForm);

        Tree genericForm = new Tree();
        genericForm.setId("generic_apply");
        genericForm.setName("通用申请");
        genericForm.setParentId("0");
        formPaths.add(genericForm);

        return formPaths;
    }
}
