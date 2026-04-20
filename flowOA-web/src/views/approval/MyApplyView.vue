<template>
  <div class="my-apply-view">
    <el-card>
      <div class="toolbar">
        <el-form :inline="true" :model="queryParams">
          <el-form-item label="申请类型">
            <el-select v-model="queryParams.type" placeholder="全部" clearable>
              <el-option label="请假申请" value="leave" />
              <el-option label="报销申请" value="expense" />
              <el-option label="通用申请" value="generic" />
            </el-select>
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
      </div>

      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="typeName" label="申请类型" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTypeMap[row.status]">{{ statusMap[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="170" />
      </el-table>

      <el-pagination class="pagination" v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize" :total="total" :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper" @size-change="loadData" @current-change="loadData" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMyApplyPage } from '@/api/myApply'

const tableData = ref([])
const total = ref(0)

const statusMap = { draft: '草稿', pending: '待审批', approved: '已通过', rejected: '已驳回', cancelled: '已取消' }
const statusTypeMap = { draft: 'info', pending: 'warning', approved: 'success', rejected: 'danger', cancelled: 'info' }

const queryParams = reactive({ pageNum: 1, pageSize: 10, status: '', type: '' })

onMounted(() => { loadData() })

async function loadData() {
  const res = await getMyApplyPage(queryParams)
  tableData.value = res.data.records
  total.value = res.data.total
}

function handleSearch() {
  queryParams.pageNum = 1
  loadData()
}

function resetQuery() {
  queryParams.status = ''
  queryParams.type = ''
  queryParams.pageNum = 1
  loadData()
}
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
