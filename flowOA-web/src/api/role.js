import request from '@/utils/request'

export function getRoleList() {
  return request.get('/system/role/list')
}

export function getRoleById(id) {
  return request.get(`/system/role/${id}`)
}

export function addRole(data) {
  return request.post('/system/role', data)
}

export function updateRole(data) {
  return request.put('/system/role', data)
}

export function deleteRole(id) {
  return request.delete(`/system/role/${id}`)
}
