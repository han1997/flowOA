import request from '@/utils/request'

export function getUserPage(params) {
  return request.get('/system/user/page', { params })
}

export function getUserList() {
  return request.get('/system/user/list')
}

export function getUserById(id) {
  return request.get(`/system/user/${id}`)
}

export function addUser(data) {
  return request.post('/system/user', data)
}

export function updateUser(data) {
  return request.put('/system/user', data)
}

export function deleteUser(id) {
  return request.delete(`/system/user/${id}`)
}

export function resetPassword(id, newPassword) {
  return request.put(`/system/user/${id}/reset-password`, newPassword)
}
