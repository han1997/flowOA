# 更新日志

本文件记录 FlowOA 项目的重要变更。格式参考 [Keep a Changelog](https://keepachangelog.com/zh-CN/)。

## [Unreleased] - 2026-05-13

### 新增
- `FlowServiceTest`：覆盖发布委托、已发布流程禁止编辑、流程不存在、`flowCode` 重复拦截、空入参、批量进度仅发 1 次任务查询、无任务时的默认值、`getProgress(null)` 默认值，共 8 个用例。
- `CHANGELOG.md`：新增本更新日志。

### 变更
- `FlowService.getProgressBatch`：由"循环调用 `getProgress`"重写为真正的批处理，每次调用固定 4 次查询（实例、任务、用户关联、SysUser 名称），与列表大小无关；`getProgress(Long)` 改为委托到批处理路径。新增依赖 `FlowTaskMapper` 直接以 `LambdaQueryWrapper.in(FlowTask::getInstanceId, ...)` 检索一批实例的当前任务。
- `FlowService.createDefinitionFromDesign`：保存前调用 `defService.getByFlowCode` 校验 `flowCode` 唯一性，重复时抛 `BusinessException`。
- `FlowService.updateDefinitionFromDesign`：
  - 已发布（`isPublish == 1`）的流程定义禁止直接编辑，提示新建版本或先取消发布；
  - 修复语义错误：原实现调用 `defService.insertFlow(existing, ...)` 会执行 INSERT 并提升版本号，现改为 `defService.updateById(existing)` + `nodeService.saveBatch` + `skipService.saveBatch`。
- `FlowService` 节点坐标序列化使用 `String.format(Locale.ROOT, ...)`，避免在不同区域设置下生成非法 JSON（如逗号小数分隔符）。
- `FlowService` 移除自建 `new ObjectMapper()`，改为通过 Lombok 构造函数注入 Spring 容器中的 `ObjectMapper` bean。
- `FlowDesignDTO`：`flowCode` 增加 `@NotBlank` 与 `@Pattern(^[a-zA-Z][a-zA-Z0-9_]{0,63}$)`，`flowName` 增加 `@NotBlank`，节点 `code/name/type`、连线 `fromNodeCode/toNodeCode` 增加非空校验，集合标注 `@Valid` 触发嵌套校验。
- `FlowDefinitionController.save`：增加 `@Valid` 触发上述参数校验。
- `ExpenseApplyController.getProgress`：增加归属校验，仅申请人本人或 `admin` 角色可查看其他人申请的流程进度，否则返回 403。
- `FlowProgressVO`：新增静态工具方法 `join(List<String>)`，统一进度信息的拼接（空列表降级为 "-"）。
- `LeaveApplyService` / `ExpenseApplyService` / `GenericApplyService`：移除各自重复实现的 `joinList`，复用 `FlowProgressVO.join`。

### 修复
- 节点坐标在非英语区域设置下可能序列化为 `{"x":1,5,"y":2,0}` 这类非法 JSON 的潜在缺陷。
- 草稿态流程定义"保存"时实际走 INSERT 路径导致主键冲突 / 版本号被错误提升的缺陷。
- 任意登录用户可查看他人报销申请审批进度的越权问题。
