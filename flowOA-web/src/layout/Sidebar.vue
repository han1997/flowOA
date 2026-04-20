<template>
  <div class="sidebar-container">
    <div class="logo">
      <h2 v-if="!appStore.sidebarCollapsed">FlowOA</h2>
      <h2 v-else>OA</h2>
    </div>
    <el-menu
      :default-active="activeMenu"
      :collapse="appStore.sidebarCollapsed"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409eff"
      router
    >
      <el-menu-item index="/dashboard">
        <el-icon><HomeFilled /></el-icon>
        <template #title>首页</template>
      </el-menu-item>

      <el-sub-menu index="system">
        <template #title>
          <el-icon><Setting /></el-icon>
          <span>系统管理</span>
        </template>
        <el-menu-item index="/system/user">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        <el-menu-item index="/system/dept">
          <el-icon><OfficeBuilding /></el-icon>
          <template #title>部门管理</template>
        </el-menu-item>
        <el-menu-item index="/system/role">
          <el-icon><UserFilled /></el-icon>
          <template #title>角色管理</template>
        </el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="flow">
        <template #title>
          <el-icon><Connection /></el-icon>
          <span>流程管理</span>
        </template>
        <el-menu-item index="/flow/definition">
          <el-icon><Document /></el-icon>
          <template #title>流程定义</template>
        </el-menu-item>
        <el-menu-item index="/flow/diagram">
          <el-icon><EditPen /></el-icon>
          <template #title>流程设计</template>
        </el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="approval">
        <template #title>
          <el-icon><Stamp /></el-icon>
          <span>审批管理</span>
        </template>
        <el-menu-item index="/approval/leave">
          <el-icon><Calendar /></el-icon>
          <template #title>请假申请</template>
        </el-menu-item>
        <el-menu-item index="/approval/expense">
          <el-icon><Money /></el-icon>
          <template #title>报销申请</template>
        </el-menu-item>
        <el-menu-item index="/approval/generic">
          <el-icon><Tickets /></el-icon>
          <template #title>通用申请</template>
        </el-menu-item>
        <el-menu-item index="/approval/my-apply">
          <el-icon><Document /></el-icon>
          <template #title>我的申请</template>
        </el-menu-item>
        <el-menu-item index="/approval/my-task">
          <el-icon><Bell /></el-icon>
          <template #title>我的待办</template>
        </el-menu-item>
      </el-sub-menu>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const appStore = useAppStore()

const activeMenu = computed(() => route.path)
</script>

<style scoped>
.sidebar-container {
  height: 100%;
  overflow-y: auto;
}
.logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.logo h2 {
  margin: 0;
  font-size: 18px;
  white-space: nowrap;
}
.el-menu {
  border-right: none;
}
</style>
