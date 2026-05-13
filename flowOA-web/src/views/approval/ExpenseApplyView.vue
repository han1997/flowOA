<template>
  <div class="expense-apply-view">
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
        <el-button type="primary" @click="showApplyDialog">报销申请</el-button>
      </div>

      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="userName" label="申请人" width="100" />
        <el-table-column prop="deptName" label="部门" width="100" />
        <el-table-column prop="expenseType" label="报销类型" width="100">
          <template #default="{ row }">{{ expenseTypeMap[row.expenseType] || row.expenseType }}</template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">￥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTypeMap[row.status]">{{ statusMap[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentNodeName" label="当前节点" min-width="120" />
        <el-table-column prop="currentApprovers" label="当前审批人" min-width="140" />
        <el-table-column prop="createTime" label="申请时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleViewFlow(row)">流程</el-button>
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

    <el-dialog v-model="dialogVisible" title="报销申请" width="550px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入报销标题" />
        </el-form-item>
        <el-form-item label="报销类型" prop="expenseType">
          <el-select v-model="form.expenseType" placeholder="请选择报销类型" style="width: 100%">
            <el-option label="差旅费" value="travel" />
            <el-option label="餐饮费" value="meal" />
            <el-option label="办公费" value="office" />
            <el-option label="通讯费" value="communication" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="报销金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0.01" :precision="2" :step="100" />
        </el-form-item>
        <el-form-item label="报销说明" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入报销说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="progressDialogVisible" title="流程进度" width="520px">
      <el-descriptions v-loading="progressLoading" :column="1" border>
        <el-descriptions-item label="流程实例ID">{{ currentProgress.flowInstanceId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="流程状态">
          {{ flowStatusMap[currentProgress.flowStatus] || currentProgress.flowStatus || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="当前节点">
          {{ formatJoin(currentProgress.currentNodeNames) }}
        </el-descriptions-item>
        <el-descriptions-item label="当前审批人">
          {{ formatJoin(currentProgress.currentApproverNames) }}
        </el-descriptions-item>
      </el-descriptions>
      <div class="progress-tip" v-if="!progressLoading && !currentProgress.currentApproverNames?.length">
        暂未识别到待办审批人，请检查流程节点是否配置了办理人权限。
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getExpensePage, submitExpense, cancelExpense, getExpenseProgress } from '@/api/expense'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const progressDialogVisible = ref(false)
const progressLoading = ref(false)
const currentProgress = ref({})
const submitting = ref(false)
const formRef = ref(null)

const expenseTypeMap = {
  travel: '差旅费',
  meal: '餐饮费',
  office: '办公费',
  communication: '通讯费',
  other: '其他'
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
const flowStatusMap = {
  '0': '待提交',
  '1': '审批中',
  '2': '审批通过',
  '3': '自动完成',
  '4': '终止',
  '5': '作废',
  '6': '撤销',
  '7': '取回',
  '8': '已完成',
  '9': '已退回',
  '10': '失效',
  '11': '拿回',
  '12': '重启',
  '13': '暂存'
}

const queryParams = reactive({ pageNum: 1, pageSize: 10, status: '' })
const form = reactive({
  title: '',
  expenseType: '',
  amount: 100,
  description: ''
})

const formRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  expenseType: [{ required: true, message: '请选择报销类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入报销金额', trigger: 'blur' }]
}

onMounted(() => {
  loadData()
})

async function loadData() {
  const res = await getExpensePage(queryParams)
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
  Object.assign(form, { title: '', expenseType: '', amount: 100, description: '' })
  dialogVisible.value = true
}

async function handleSubmit() {
  const formEl = formRef.value
  if (!formEl) return
  await formEl.validate()
  submitting.value = true
  try {
    await submitExpense(form)
    ElMessage.success('提交成功')
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleCancel(row) {
  await ElMessageBox.confirm('确定取消该报销申请吗？', '提示', { type: 'warning' })
  await cancelExpense(row.id)
  ElMessage.success('已取消')
  loadData()
}

async function handleViewFlow(row) {
  progressDialogVisible.value = true
  progressLoading.value = true
  try {
    const res = await getExpenseProgress(row.id)
    currentProgress.value = res.data || {}
  } finally {
    progressLoading.value = false
  }
}

function formatJoin(values) {
  return values && values.length ? values.join('、') : '-'
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
.progress-tip {
  margin-top: 12px;
  color: #e6a23c;
}
</style>
