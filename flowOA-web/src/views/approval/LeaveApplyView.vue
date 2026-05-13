<template>
  <div class="leave-apply-view">
    <el-card>
      <div class="toolbar">
        <el-form :inline="true" :model="queryParams">
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="全部" clearable>
              <el-option label="待审批" value="pending" />
              <el-option label="已通过" value="approved" />
              <el-option label="已驳回" value="rejected" />
              <el-option label="已取消" value="cancelled" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <el-button type="primary" @click="showApplyDialog">请假申请</el-button>
      </div>

      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="userName" label="申请人" width="100" />
        <el-table-column prop="deptName" label="部门" width="100" />
        <el-table-column prop="leaveType" label="请假类型" width="100">
          <template #default="{ row }">{{ leaveTypeMap[row.leaveType] || row.leaveType }}</template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column prop="days" label="天数" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTypeMap[row.status]">{{ statusMap[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="danger" v-if="row.status === 'pending'" @click="handleCancel(row)">
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" title="请假申请" width="550px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入请假标题" />
        </el-form-item>
        <el-form-item label="请假类型" prop="leaveType">
          <el-select v-model="form.leaveType" placeholder="请选择请假类型" style="width: 100%">
            <el-option label="年假" value="annual" />
            <el-option label="病假" value="sick" />
            <el-option label="事假" value="personal" />
            <el-option label="产假" value="maternity" />
            <el-option label="婚假" value="marriage" />
            <el-option label="丧假" value="bereavement" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="请假天数" prop="days">
          <el-input-number v-model="form.days" :min="0.5" :step="0.5" :precision="1" />
        </el-form-item>
        <el-form-item label="请假原因" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请输入请假原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getLeavePage, submitLeave, cancelLeave } from '@/api/leave'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const leaveTypeMap = {
  annual: '年假',
  sick: '病假',
  personal: '事假',
  maternity: '产假',
  marriage: '婚假',
  bereavement: '丧假'
}
const statusMap = {
  draft: '草稿',
  pending: '待审批',
  approved: '已通过',
  rejected: '已驳回',
  cancelled: '已取消'
}
const statusTypeMap = {
  draft: 'info',
  pending: 'warning',
  approved: 'success',
  rejected: 'danger',
  cancelled: 'info'
}

const queryParams = reactive({ pageNum: 1, pageSize: 10, status: '' })
const form = reactive({
  title: '',
  leaveType: '',
  startDate: '',
  endDate: '',
  days: 1,
  reason: ''
})

const formRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  days: [{ required: true, message: '请输入请假天数', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入请假原因', trigger: 'blur' }]
}

onMounted(() => {
  loadData()
})

async function loadData() {
  const res = await getLeavePage(queryParams)
  tableData.value = res.data.records
  total.value = res.data.total
}

function handleSearch() {
  queryParams.pageNum = 1
  loadData()
}

function resetQuery() {
  queryParams.status = ''
  queryParams.pageNum = 1
  loadData()
}

function showApplyDialog() {
  Object.assign(form, { title: '', leaveType: '', startDate: '', endDate: '', days: 1, reason: '' })
  dialogVisible.value = true
}

async function handleSubmit() {
  const formEl = formRef.value
  if (!formEl) return
  await formEl.validate()
  submitting.value = true
  try {
    await submitLeave(form)
    ElMessage.success('提交成功')
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleCancel(row) {
  await ElMessageBox.confirm('确定取消该请假申请吗？', '提示', { type: 'warning' })
  await cancelLeave(row.id)
  ElMessage.success('已取消')
  loadData()
}
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>

