import request from '@/utils/request'

export function getGenericPage(params) {
  return request.get('/approval/generic/page', { params })
}

export function getGenericById(id) {
  return request.get(`/approval/generic/${id}`)
}

export function submitGeneric(data) {
  return request.post('/approval/generic/submit', data)
}

export function approveGeneric(data) {
  return request.post('/approval/generic/approve', data)
}

export function rejectGeneric(data) {
  return request.post('/approval/generic/reject', data)
}

export function cancelGeneric(id) {
  return request.post(`/approval/generic/cancel/${id}`)
}
