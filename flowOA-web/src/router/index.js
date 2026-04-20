import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue')
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '首页' }
      },
      // System management
      {
        path: 'system/user',
        name: 'User',
        component: () => import('@/views/system/UserView.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'system/dept',
        name: 'Dept',
        component: () => import('@/views/system/DeptView.vue'),
        meta: { title: '部门管理' }
      },
      {
        path: 'system/role',
        name: 'Role',
        component: () => import('@/views/system/RoleView.vue'),
        meta: { title: '角色管理' }
      },
      // Flow management
      {
        path: 'flow/definition',
        name: 'FlowDefinition',
        component: () => import('@/views/flow/DefinitionView.vue'),
        meta: { title: '流程定义' }
      },
      {
        path: 'flow/diagram',
        name: 'FlowDiagram',
        component: () => import('@/views/flow/DiagramView.vue'),
        meta: { title: '流程设计' }
      },
      // Approval
      {
        path: 'approval/leave',
        name: 'LeaveApply',
        component: () => import('@/views/approval/LeaveApplyView.vue'),
        meta: { title: '请假申请' }
      },
      {
        path: 'approval/expense',
        name: 'ExpenseApply',
        component: () => import('@/views/approval/ExpenseApplyView.vue'),
        meta: { title: '报销申请' }
      },
      {
        path: 'approval/generic',
        name: 'GenericApply',
        component: () => import('@/views/approval/GenericApplyView.vue'),
        meta: { title: '通用申请' }
      },
      {
        path: 'approval/my-apply',
        name: 'MyApply',
        component: () => import('@/views/approval/MyApplyView.vue'),
        meta: { title: '我的申请' }
      },
      {
        path: 'approval/my-task',
        name: 'MyTask',
        component: () => import('@/views/approval/MyTaskView.vue'),
        meta: { title: '我的待办' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard
router.beforeEach((to, from, next) => {
  const token = getToken()
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
