<template>
  <div class="dept-view">
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd(null)">新增部门</el-button>
      </div>

      <el-table :data="deptTree" border row-key="id" default-expand-all :tree-props="{ children: 'children' }">
        <el-table-column prop="name" label="部门名称" min-width="200" />
        <el-table-column prop="leader" label="负责人" width="120" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button size="small" @click="handleAdd(row)">新增</el-button>
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑部门' : '新增部门'" width="500px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="上级部门">
          <el-input :value="parentName" disabled />
        </el-form-item>
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="负责人" prop="leader">
          <el-input v-model="form.leader" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getDeptTree, addDept, updateDept, deleteDept } from '@/api/dept'
import { ElMessage, ElMessageBox } from 'element-plus'

const deptTree = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const parentDept = ref(null)

const form = reactive({ id: null, parentId: 0, name: '', leader: '', sort: 0, status: 1 })

const formRules = { name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }] }

const parentName = computed(() => parentDept.value ? parentDept.value.name : '顶级部门')

onMounted(() => { loadData() })

async function loadData() {
  const res = await getDeptTree()
  deptTree.value = res.data || []
}

function handleAdd(row) {
  isEdit.value = false
  parentDept.value = row
  Object.assign(form, { id: null, parentId: row ? row.id : 0, name: '', leader: '', sort: 0, status: 1 })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  parentDept.value = findParent(deptTree.value, row.parentId)
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

function findParent(tree, parentId) {
  for (const dept of tree) {
    if (dept.id === parentId) return dept
    if (dept.children) {
      const found = findParent(dept.children, parentId)
      if (found) return found
    }
  }
  return null
}

async function handleSubmit() {
  const formEl = formRef.value
  if (!formEl) return
  await formEl.validate()
  if (isEdit.value) {
    await updateDept(form)
    ElMessage.success('更新成功')
  } else {
    await addDept(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

async function handleDelete(row) {
  if (row.children && row.children.length) {
    ElMessage.warning('请先删除子部门')
    return
  }
  await ElMessageBox.confirm(`确定删除部门 ${row.name} 吗?`, '提示', { type: 'warning' })
  await deleteDept(row.id)
  ElMessage.success('删除成功')
  loadData()
}
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
</style>
