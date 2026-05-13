package com.flowoa.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowoa.common.BusinessException;
import com.flowoa.dto.FlowDesignDTO;
import com.flowoa.entity.SysUser;
import com.flowoa.mapper.ExpenseApplyMapper;
import com.flowoa.mapper.GenericApplyMapper;
import com.flowoa.mapper.LeaveApplyMapper;
import com.flowoa.mapper.SysUserMapper;
import com.flowoa.vo.FlowProgressVO;
import org.dromara.warm.flow.core.entity.Definition;
import org.dromara.warm.flow.core.entity.Instance;
import org.dromara.warm.flow.core.service.DefService;
import org.dromara.warm.flow.core.service.InsService;
import org.dromara.warm.flow.core.service.NodeService;
import org.dromara.warm.flow.core.service.SkipService;
import org.dromara.warm.flow.core.service.TaskService;
import org.dromara.warm.flow.core.service.UserService;
import org.dromara.warm.flow.orm.entity.FlowInstance;
import org.dromara.warm.flow.orm.entity.FlowTask;
import org.dromara.warm.flow.orm.entity.FlowUser;
import org.dromara.warm.flow.orm.mapper.FlowTaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlowServiceTest {

    @Mock private DefService defService;
    @Mock private InsService insService;
    @Mock private TaskService taskService;
    @Mock private UserService userService;
    @Mock private NodeService nodeService;
    @Mock private SkipService skipService;
    @Mock private LeaveApplyMapper leaveApplyMapper;
    @Mock private ExpenseApplyMapper expenseApplyMapper;
    @Mock private GenericApplyMapper genericApplyMapper;
    @Mock private SysUserMapper sysUserMapper;
    @Mock private FlowTaskMapper flowTaskMapper;

    private FlowService flowService;

    @BeforeEach
    void setUp() {
        flowService = new FlowService(
                defService, insService, taskService, userService, nodeService, skipService,
                leaveApplyMapper, expenseApplyMapper, genericApplyMapper, sysUserMapper,
                new ObjectMapper(), flowTaskMapper);
    }

    @Test
    void publishDefinition_delegatesToDefService() {
        flowService.publishDefinition(42L);
        verify(defService).publish(42L);
    }

    @Test
    void updateDefinitionFromDesign_rejectsPublishedFlow() {
        Definition existing = mock(Definition.class);
        when(existing.getIsPublish()).thenReturn(1);
        when(defService.getById(7L)).thenReturn(existing);

        FlowDesignDTO design = new FlowDesignDTO();
        design.setId(7L);
        design.setFlowCode("foo");
        design.setFlowName("Foo");

        assertThatThrownBy(() -> flowService.saveDesign(design))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已发布");

        verify(nodeService, never()).remove(any());
        verify(skipService, never()).remove(any());
        verify(defService, never()).updateById(any());
    }

    @Test
    void updateDefinitionFromDesign_missingDefinitionThrows() {
        when(defService.getById(99L)).thenReturn(null);

        FlowDesignDTO design = new FlowDesignDTO();
        design.setId(99L);
        design.setFlowCode("foo");
        design.setFlowName("Foo");

        assertThatThrownBy(() -> flowService.saveDesign(design))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不存在");
    }

    @Test
    void createDefinitionFromDesign_rejectsDuplicateFlowCode() {
        when(defService.getByFlowCode("dup_code")).thenReturn(List.of(mock(Definition.class)));

        FlowDesignDTO design = new FlowDesignDTO();
        design.setFlowCode("dup_code");
        design.setFlowName("Dup");

        assertThatThrownBy(() -> flowService.saveDesign(design))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("流程编码已存在");

        verify(defService, never()).insertFlow(any(), anyList(), anyList());
    }

    @Test
    void getProgressBatch_emptyInput_returnsEmpty() {
        assertThat(flowService.getProgressBatch(null)).isEmpty();
        assertThat(flowService.getProgressBatch(Collections.emptyList())).isEmpty();
        assertThat(flowService.getProgressBatch(Arrays.asList(null, null))).isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Test
    void getProgressBatch_issuesSingleQueryForTasksAcrossInstances() {
        List<Long> instanceIds = List.of(101L, 102L);

        Instance inst1 = new FlowInstance().setId(101L).setFlowStatus("running");
        Instance inst2 = new FlowInstance().setId(102L).setFlowStatus("running");
        when(insService.getByIds(instanceIds)).thenReturn(List.of(inst1, inst2));

        FlowTask t1 = new FlowTask().setId(1001L).setInstanceId(101L).setNodeName("经理审批");
        FlowTask t2 = new FlowTask().setId(1002L).setInstanceId(102L).setNodeName("财务审批");
        when(flowTaskMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(t1, t2));

        FlowUser u1 = new FlowUser();
        u1.setAssociated(1001L);
        u1.setProcessedBy("10");
        FlowUser u2 = new FlowUser();
        u2.setAssociated(1002L);
        u2.setProcessedBy("20");
        when(userService.getByAssociateds(anyList(), any(String[].class))).thenReturn(List.of(u1, u2));

        SysUser sys1 = new SysUser();
        sys1.setId(10L);
        sys1.setName("Alice");
        SysUser sys2 = new SysUser();
        sys2.setId(20L);
        sys2.setName("Bob");
        when(sysUserMapper.selectBatchIds(anyList())).thenReturn(List.of(sys1, sys2));

        Map<Long, FlowProgressVO> result = flowService.getProgressBatch(instanceIds);

        assertThat(result).hasSize(2);
        assertThat(result.get(101L).getCurrentNodeNames()).containsExactly("经理审批");
        assertThat(result.get(101L).getCurrentApproverNames()).containsExactly("Alice");
        assertThat(result.get(101L).getFlowStatus()).isEqualTo("running");
        assertThat(result.get(102L).getCurrentNodeNames()).containsExactly("财务审批");
        assertThat(result.get(102L).getCurrentApproverNames()).containsExactly("Bob");

        // The batch path must use a single task query and a single user-association query —
        // not one per instance. That is the perf point of this method.
        verify(flowTaskMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
        verify(userService, times(1)).getByAssociateds(anyList(), any(String[].class));
        verify(sysUserMapper, times(1)).selectBatchIds(anyList());
        // Per-instance fallbacks must NOT be invoked.
        verify(taskService, never()).getByInsId(any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getProgressBatch_noTasks_keepsDefaultEmptyLists() {
        when(insService.getByIds(List.of(7L)))
                .thenReturn(List.of(new FlowInstance().setId(7L).setFlowStatus("finished")));
        when(flowTaskMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        Map<Long, FlowProgressVO> result = flowService.getProgressBatch(List.of(7L));

        assertThat(result).containsOnlyKeys(7L);
        FlowProgressVO vo = result.get(7L);
        assertThat(vo.getFlowStatus()).isEqualTo("finished");
        assertThat(vo.getCurrentNodeNames()).isEmpty();
        assertThat(vo.getCurrentApproverNames()).isEmpty();
        verify(userService, never()).getByAssociateds(anyList(), any(String[].class));
    }

    @Test
    void getProgress_nullInstance_returnsDefaultVO() {
        FlowProgressVO vo = flowService.getProgress(null);
        assertThat(vo.getFlowInstanceId()).isNull();
        assertThat(vo.getCurrentNodeNames()).isEmpty();
        assertThat(vo.getCurrentApproverNames()).isEmpty();
    }
}
