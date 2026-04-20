package com.flowoa.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.flowoa.common.PageResult;
import com.flowoa.common.Result;
import com.flowoa.entity.ExpenseApply;
import com.flowoa.entity.GenericApply;
import com.flowoa.entity.LeaveApply;
import com.flowoa.service.ExpenseApplyService;
import com.flowoa.service.GenericApplyService;
import com.flowoa.service.LeaveApplyService;
import com.flowoa.vo.MyApplyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/approval/my")
@RequiredArgsConstructor
public class MyApplyController {

    private final LeaveApplyService leaveApplyService;
    private final ExpenseApplyService expenseApplyService;
    private final GenericApplyService genericApplyService;

    @GetMapping("/page")
    public Result<PageResult<MyApplyVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        Long userId = StpUtil.getLoginIdAsLong();
        if ("expense".equals(type)) {
            PageResult<ExpenseApply> result = expenseApplyService.page(pageNum, pageSize, status, userId);
            List<MyApplyVO> records = result.getRecords().stream().map(this::convertExpense).toList();
            return Result.ok(new PageResult<>(records, result.getTotal(), result.getPageNum(), result.getPageSize()));
        } else if ("generic".equals(type)) {
            PageResult<GenericApply> result = genericApplyService.page(pageNum, pageSize, status, userId, null);
            List<MyApplyVO> records = result.getRecords().stream().map(this::convertGeneric).toList();
            return Result.ok(new PageResult<>(records, result.getTotal(), result.getPageNum(), result.getPageSize()));
        } else {
            PageResult<LeaveApply> result = leaveApplyService.page(pageNum, pageSize, status, userId);
            List<MyApplyVO> records = result.getRecords().stream().map(this::convertLeave).toList();
            return Result.ok(new PageResult<>(records, result.getTotal(), result.getPageNum(), result.getPageSize()));
        }
    }

    private MyApplyVO convertLeave(LeaveApply a) {
        MyApplyVO vo = new MyApplyVO();
        vo.setId(a.getId());
        vo.setTitle(a.getTitle());
        vo.setType("leave");
        vo.setTypeName("请假申请");
        vo.setStatus(a.getStatus());
        vo.setUserName(a.getUserName());
        vo.setCreateTime(a.getCreateTime());
        return vo;
    }

    private MyApplyVO convertExpense(ExpenseApply a) {
        MyApplyVO vo = new MyApplyVO();
        vo.setId(a.getId());
        vo.setTitle(a.getTitle());
        vo.setType("expense");
        vo.setTypeName("报销申请");
        vo.setStatus(a.getStatus());
        vo.setUserName(a.getUserName());
        vo.setCreateTime(a.getCreateTime());
        return vo;
    }

    private MyApplyVO convertGeneric(GenericApply a) {
        MyApplyVO vo = new MyApplyVO();
        vo.setId(a.getId());
        vo.setTitle(a.getTitle());
        vo.setType("generic");
        vo.setTypeName("通用申请");
        vo.setStatus(a.getStatus());
        vo.setUserName(a.getUserName());
        vo.setCreateTime(a.getCreateTime());
        return vo;
    }
}
