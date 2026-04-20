<template>
  <div class="my-task-view">
    <el-card>
      <div class="toolbar">
        <h3>我的待办任务</h3>
        <el-button @click="loadData" :icon="Refresh">刷新</el-button>
      </div>

      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="任务ID" width="100" />
        <el-table-column prop="flowName" label="流程名称" min-width="180" />
        <el-table-column prop="nodeName" label="当前节点" width="150" />
        <el-table-column prop="nodeCode" label="节点编码" width="150" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="success" @click="handleApprove(row)">通过</el-button>
            <el-button size="small" type="danger" @click="handleReject(row)">驳回</el-button>
            <el-button size="small" @click="handleTransfer(row)">转办</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!tableData.length" description="暂无待办任务" />
    </el-card>

    <!-- Approve/Reject Dialog -->
    <el-dialog v-model="actionDialogVisible" :title="actionTitle" width="450px">
      <el-form label-width="80px">
        <el-form-item label="审批意见">
          <el-input v-model="actionComment" type="textarea" :rows="3" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="actionDialogVisible = false">取消</el-button>
        <el-button :type="actionType === 'approve' ? 'success' : 'danger'" @click="submitAction" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- Transfer Dialog -->
    <el-dialog v-model="transferDialogVisible" title="转办任务" width="450px">
      <el-form label-width="80px">
        <el-form-item label="转办给">
          <el-select v-model="transferUserId" placeholder="请选择转办人" style="width: 100%">
            <el-option v-for="user in userList" :key="user.id" :label="user.name" :value="String(user.id)" />
          </el-select>
        </el-form-item>
        <el-form-item label="转办意见">
          <el-input v-model="transferComment" type="textarea" :rows="3" placeholder="请输入转办意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTransfer" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTodoTasks, approveFlow, rejectFlow, transferFlow } from '@/api/flow'
import { getUserList } from '@/api/user'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'

const tableData = ref([])
const userList = ref([])

// Action dialog
const actionDialogVisible = ref(false)
const actionType = ref('approve')
const actionTitle = ref('')
const actionComment = ref('')
const currentTask = ref(null)
const submitting = ref(false)

// Transfer dialog
const transferDialogVisible = ref(false)
const transferUserId = ref('')
const transferComment = ref('')
const transferTask = ref(null)

onMounted(() => {
  loadData()
  loadUsers()
})

async function loadData() {
  const res = await getTodoTasks()
  tableData.value = res.data || []
}

async function loadUsers() {
  const res = await getUserList()
  userList.value = res.data || []
}

function handleApprove(row) {
  currentTask.value = row
  actionType.value = 'approve'
  actionTitle.value = '审批通过'
  actionComment.value = ''
  actionDialogVisible.value = true
}

function handleReject(row) {
  currentTask.value = row
  actionType.value = 'reject'
  actionTitle.value = '审批驳回'
  actionComment.value = ''
  actionDialogVisible.value = true
}

async function submitAction() {
  submitting.value = true
  try {
    const data = { taskId: currentTask.value.id, comment: actionComment.value }
    if (actionType.value === 'approve') {
      await approveFlow(data)
      ElMessage.success('审批通过')
    } else {
      await rejectFlow(data)
      ElMessage.success('已驳回')
    }
    actionDialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

function handleTransfer(row) {
  transferTask.value = row
  transferUserId.value = ''
  transferComment.value = ''
  transferDialogVisible.value = true
}

async function submitTransfer() {
  if (!transferUserId.value) {
    ElMessage.warning('请选择转办人')
    return
  }
  submitting.value = true
  try {
    await transferFlow({
      taskId: transferTask.value.id,
      targetUserId: transferUserId.value,
      comment: transferComment.value
    })
    ElMessage.success('转办成功')
    transferDialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.toolbar h3 { margin: 0; }
</style>
