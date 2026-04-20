import request from '@/utils/request'

export function getDeptTree() {
  return request.get('/system/dept/tree')
}

export function getDeptById(id) {
  return request.get(`/system/dept/${id}`)
}

export function addDept(data) {
  return request.post('/system/dept', data)
}

export function updateDept(data) {
  return request.put('/system/dept', data)
}

export function deleteDept(id) {
  return request.delete(`/system/dept/${id}`)
}
