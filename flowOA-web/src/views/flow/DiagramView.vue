<template>
  <div class="diagram-view">
    <el-card>
      <div class="toolbar">
        <el-button @click="goBack">返回列表</el-button>
        <h3 v-if="definition">流程: {{ definition.flowName }}</h3>
      </div>

      <div v-if="definition" class="diagram-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="流程编码">{{ definition.flowCode }}</el-descriptions-item>
          <el-descriptions-item label="流程名称">{{ definition.flowName }}</el-descriptions-item>
          <el-descriptions-item label="流程分类">{{ definition.flowCategory || '-' }}</el-descriptions-item>
          <el-descriptions-item label="版本">{{ definition.flowVersion }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="definition.flowStatus === 1 ? 'success' : 'info'">
              {{ definition.flowStatus === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div class="flow-visual">
          <h4>流程节点预览</h4>
          <div class="flow-nodes">
            <div v-for="node in nodes" :key="node.nodeCode" class="flow-node" :class="nodeTypeClass(node.nodeType)">
              <div class="node-icon">
                <el-icon v-if="node.nodeType === 0"><VideoPlay /></el-icon>
                <el-icon v-else-if="node.nodeType === 2"><CircleCheck /></el-icon>
                <el-icon v-else><User /></el-icon>
              </div>
              <div class="node-name">{{ node.nodeName }}</div>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-else description="请从流程定义列表选择一个流程进行设计" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getFlowDefinitionById } from '@/api/flow'

const route = useRoute()
const router = useRouter()
const definition = ref(null)
const nodes = ref([])

onMounted(async () => {
  const id = route.query.id
  if (id) {
    const res = await getFlowDefinitionById(id)
    definition.value = res.data
    // Parse nodes from definition if available
    if (definition.value?.nodeList) {
      nodes.value = definition.value.nodeList
    }
  }
})

function goBack() {
  router.push('/flow/definition')
}

function nodeTypeClass(nodeType) {
  switch (nodeType) {
    case 0: return 'node-start'
    case 2: return 'node-end'
    default: return 'node-task'
  }
}
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}
.toolbar h3 { margin: 0; }
.diagram-content { margin-top: 20px; }
.flow-visual { margin-top: 24px; }
.flow-visual h4 { margin-bottom: 16px; }
.flow-nodes {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}
.flow-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 24px;
  border-radius: 8px;
  min-width: 100px;
}
.node-start { background: #e6f7ff; border: 2px solid #1890ff; }
.node-end { background: #f6ffed; border: 2px solid #52c41a; }
.node-task { background: #fff7e6; border: 2px solid #faad14; }
.node-icon { font-size: 24px; margin-bottom: 8px; }
.node-name { font-size: 14px; font-weight: 500; }
</style>
