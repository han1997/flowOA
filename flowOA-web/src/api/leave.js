import request from '@/utils/request'

export function getLeavePage(params) {
  return request.get('/approval/leave/page', { params })
}

export function getLeaveById(id) {
  return request.get(`/approval/leave/${id}`)
}

export function submitLeave(data) {
  return request.post('/approval/leave/submit', data)
}

export function approveLeave(data) {
  return request.post('/approval/leave/approve', data)
}

export function rejectLeave(data) {
  return request.post('/approval/leave/reject', data)
}

export function cancelLeave(id) {
  return request.post(`/approval/leave/cancel/${id}`)
}
