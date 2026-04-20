<template>
  <div class="dashboard-view">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">我的申请</div>
              <div class="stat-value">{{ stats.totalApply || 0 }}</div>
            </div>
            <el-icon class="stat-icon" :size="48" color="#409eff"><Document /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">待办任务</div>
              <div class="stat-value">{{ stats.todoCount || 0 }}</div>
            </div>
            <el-icon class="stat-icon" :size="48" color="#e6a23c"><Bell /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">请假申请</div>
              <div class="stat-value">{{ stats.leaveCount || 0 }}</div>
            </div>
            <el-icon class="stat-icon" :size="48" color="#67c23a"><Calendar /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">报销申请</div>
              <div class="stat-value">{{ stats.expenseCount || 0 }}</div>
            </div>
            <el-icon class="stat-icon" :size="48" color="#f56c6c"><Money /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>快速入口</span>
          </template>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-button type="primary" size="large" style="width: 100%" @click="$router.push('/approval/leave')">
                请假申请
              </el-button>
            </el-col>
            <el-col :span="6">
              <el-button type="success" size="large" style="width: 100%" @click="$router.push('/approval/expense')">
                报销申请
              </el-button>
            </el-col>
            <el-col :span="6">
              <el-button type="warning" size="large" style="width: 100%" @click="$router.push('/approval/generic')">
                通用申请
              </el-button>
            </el-col>
            <el-col :span="6">
              <el-button type="danger" size="large" style="width: 100%" @click="$router.push('/approval/my-task')">
                待办任务
              </el-button>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>系统信息</span>
          </template>
          <el-descriptions :column="1">
            <el-descriptions-item label="系统名称">FlowOA 办公审批系统</el-descriptions-item>
            <el-descriptions-item label="版本号">1.0.0</el-descriptions-item>
            <el-descriptions-item label="工作流引擎">warm-flow</el-descriptions-item>
            <el-descriptions-item label="当前用户">{{ userStore.userInfo?.name || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDashboardStats } from '@/api/dashboard'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const stats = ref({})

onMounted(async () => {
  try {
    const res = await getDashboardStats()
    stats.value = res.data || {}
  } catch (e) {
    // Stats loading failed, show defaults
  }
})
</script>

<style scoped>
.stat-card {
  cursor: pointer;
}
.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.stat-info {
  display: flex;
  flex-direction: column;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}
.stat-icon {
  opacity: 0.8;
}
</style>
