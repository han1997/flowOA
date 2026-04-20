<template>
  <div class="generic-apply-view">
    <el-card>
      <div class="toolbar">
        <el-form :inline="true" :model="queryParams">
          <el-form-item label="申请类型">
            <el-input v-model="queryParams.applyType" placeholder="申请类型" clearable />
          </el-form-item>
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
        <el-button type="primary" @click="showApplyDialog">通用申请</el-button>
      </div>

      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="userName" label="申请人" width="100" />
        <el-table-column prop="deptName" label="部门" width="100" />
        <el-table-column prop="applyType" label="申请类型" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTypeMap[row.status]">{{ statusMap[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button size="small" type="danger" v-if="row.status === 'pending'"
              @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pagination" v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize" :total="total" :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper" @size-change="loadData" @current-change="loadData" />
    </el-card>

    <!-- Apply Dialog -->
    <el-dialog v-model="dialogVisible" title="通用申请" width="600px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入申请标题" />
        </el-form-item>
        <el-form-item label="申请类型" prop="applyType">
          <el-input v-model="form.applyType" placeholder="如: 用章申请、车辆申请等" />
        </el-form-item>
        <el-form-item label="申请内容" prop="contentJson">
          <el-input v-model="form.contentJson" type="textarea" :rows="8" placeholder="请输入申请内容(支持JSON格式)" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>

    <!-- View Dialog -->
    <el-dialog v-model="viewDialogVisible" title="查看申请" width="600px">
      <el-descriptions :column="1" border v-if="currentRow">
        <el-descriptions-item label="标题">{{ currentRow.title }}</el-descriptions-item>
        <el-descriptions-item label="申请类型">{{ currentRow.applyType }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTypeMap[currentRow.status]">{{ statusMap[currentRow.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请内容">
          <pre class="content-json">{{ formatJson(currentRow.contentJson) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getGenericPage, submitGeneric, cancelGeneric } from '@/api/generic'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const currentRow = ref(null)

const statusMap = { draft: '草稿', pending: '待审批', approved: '已通过', rejected: '已驳回', cancelled: '已取消' }
const statusTypeMap = { draft: 'info', pending: 'warning', approved: 'success', rejected: 'danger', cancelled: 'info' }

const queryParams = reactive({ pageNum: 1, pageSize: 10, status: '', applyType: '' })

const form = reactive({ title: '', applyType: '', contentJson: '' })

const formRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  applyType: [{ required: true, message: '请输入申请类型', trigger: 'blur' }]
}

onMounted(() => { loadData() })

async function loadData() {
  const res = await getGenericPage(queryParams)
  tableData.value = res.data.records
  total.value = res.data.total
}

function handleSearch() {
  queryParams.pageNum = 1
  loadData()
}

function resetQuery() {
  queryParams.status = ''
  queryParams.applyType = ''
  queryParams.pageNum = 1
  loadData()
}

function showApplyDialog() {
  Object.assign(form, { title: '', applyType: '', contentJson: '' })
  dialogVisible.value = true
}

async function handleSubmit() {
  const formEl = formRef.value
  if (!formEl) return
  await formEl.validate()
  submitting.value = true
  try {
    await submitGeneric(form)
    ElMessage.success('提交成功')
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

function handleView(row) {
  currentRow.value = row
  viewDialogVisible.value = true
}

async function handleCancel(row) {
  await ElMessageBox.confirm('确定取消该申请吗?', '提示', { type: 'warning' })
  await cancelGeneric(row.id)
  ElMessage.success('已取消')
  loadData()
}

function formatJson(str) {
  if (!str) return ''
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
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
.content-json {
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
  font-size: 13px;
}
</style>
