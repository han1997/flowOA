<template>
  <div class="definition-view">
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="showDeployDialog">部署流程</el-button>
        <el-button @click="loadData">刷新</el-button>
      </div>

      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="flowCode" label="流程编码" width="200" />
        <el-table-column prop="flowName" label="流程名称" min-width="200" />
        <el-table-column prop="flowCategory" label="流程分类" width="150" />
        <el-table-column prop="flowVersion" label="版本" width="80" />
        <el-table-column prop="flowStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.flowStatus === 1 ? 'success' : 'info'">
              {{ row.flowStatus === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="goDesign(row)">设计</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Deploy Dialog -->
    <el-dialog v-model="deployDialogVisible" title="部署流程定义" width="700px">
      <el-form label-width="100px">
        <el-form-item label="流程编码">
          <el-select v-model="deployForm.flowCode" placeholder="选择流程模板">
            <el-option label="请假审批 (leave_apply)" value="leave_apply" />
            <el-option label="报销审批 (expense_apply)" value="expense_apply" />
            <el-option label="通用审批 (generic_apply)" value="generic_apply" />
          </el-select>
        </el-form-item>
        <el-form-item label="XML内容">
          <el-input v-model="deployForm.xml" type="textarea" :rows="15" placeholder="请输入流程定义XML" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deployDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDeploy" :loading="deploying">部署</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getFlowDefinitionList, deployFlowDefinition, deleteFlowDefinition } from '@/api/flow'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const tableData = ref([])
const deployDialogVisible = ref(false)
const deploying = ref(false)

const deployForm = reactive({
  flowCode: '',
  xml: ''
})

// Default XML templates for each flow type
const xmlTemplates = {
  leave_apply: `<?xml version="1.0" encoding="UTF-8"?>
<flow flowCode="leave_apply" flowName="请假审批流程">
  <nodes>
    <node nodeCode="start" nodeName="开始" nodeType="0"/>
    <node nodeCode="apply" nodeName="提交申请" nodeType="1"/>
    <node nodeCode="manager" nodeName="部门经理审批" nodeType="1"/>
    <node nodeCode="end" nodeName="结束" nodeType="2"/>
  </nodes>
  <skips>
    <skip skipCode="start_apply" fromNodeCode="start" toNodeCode="apply" skipType="1"/>
    <skip skipCode="apply_manager" fromNodeCode="apply" toNodeCode="manager" skipType="1"/>
    <skip skipCode="manager_end_pass" fromNodeCode="manager" toNodeCode="end" skipType="1"/>
    <skip skipCode="manager_apply_reject" fromNodeCode="manager" toNodeCode="apply" skipType="2"/>
  </skips>
</flow>`,
  expense_apply: `<?xml version="1.0" encoding="UTF-8"?>
<flow flowCode="expense_apply" flowName="报销审批流程">
  <nodes>
    <node nodeCode="start" nodeName="开始" nodeType="0"/>
    <node nodeCode="apply" nodeName="提交申请" nodeType="1"/>
    <node nodeCode="manager" nodeName="部门经理审批" nodeType="1"/>
    <node nodeCode="finance" nodeName="财务审批" nodeType="1"/>
    <node nodeCode="end" nodeName="结束" nodeType="2"/>
  </nodes>
  <skips>
    <skip skipCode="start_apply" fromNodeCode="start" toNodeCode="apply" skipType="1"/>
    <skip skipCode="apply_manager" fromNodeCode="apply" toNodeCode="manager" skipType="1"/>
    <skip skipCode="manager_finance_pass" fromNodeCode="manager" toNodeCode="finance" skipType="1"/>
    <skip skipCode="manager_apply_reject" fromNodeCode="manager" toNodeCode="apply" skipType="2"/>
    <skip skipCode="finance_end_pass" fromNodeCode="finance" toNodeCode="end" skipType="1"/>
    <skip skipCode="finance_apply_reject" fromNodeCode="finance" toNodeCode="apply" skipType="2"/>
  </skips>
</flow>`,
  generic_apply: `<?xml version="1.0" encoding="UTF-8"?>
<flow flowCode="generic_apply" flowName="通用审批流程">
  <nodes>
    <node nodeCode="start" nodeName="开始" nodeType="0"/>
    <node nodeCode="apply" nodeName="提交申请" nodeType="1"/>
    <node nodeCode="manager" nodeName="审批人审批" nodeType="1"/>
    <node nodeCode="end" nodeName="结束" nodeType="2"/>
  </nodes>
  <skips>
    <skip skipCode="start_apply" fromNodeCode="start" toNodeCode="apply" skipType="1"/>
    <skip skipCode="apply_manager" fromNodeCode="apply" toNodeCode="manager" skipType="1"/>
    <skip skipCode="manager_end_pass" fromNodeCode="manager" toNodeCode="end" skipType="1"/>
    <skip skipCode="manager_apply_reject" fromNodeCode="manager" toNodeCode="apply" skipType="2"/>
  </skips>
</flow>`
}

onMounted(() => { loadData() })

async function loadData() {
  const res = await getFlowDefinitionList()
  tableData.value = res.data || []
}

function showDeployDialog() {
  deployForm.flowCode = ''
  deployForm.xml = ''
  deployDialogVisible.value = true
}

function handleFlowCodeChange() {
  if (xmlTemplates[deployForm.flowCode]) {
    deployForm.xml = xmlTemplates[deployForm.flowCode]
  }
}

// Watch flowCode change
import { watch } from 'vue'
watch(() => deployForm.flowCode, () => {
  handleFlowCodeChange()
})

async function handleDeploy() {
  if (!deployForm.xml) {
    ElMessage.warning('请输入流程定义XML')
    return
  }
  deploying.value = true
  try {
    await deployFlowDefinition(deployForm.xml)
    ElMessage.success('部署成功')
    deployDialogVisible.value = false
    loadData()
  } finally {
    deploying.value = false
  }
}

function goDesign(row) {
  router.push({ path: '/flow/diagram', query: { id: row.id } })
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除流程定义 ${row.flowName} 吗?`, '提示', { type: 'warning' })
  await deleteFlowDefinition(row.id)
  ElMessage.success('删除成功')
  loadData()
}
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
</style>
