import request from '@/utils/request'

export function getExpensePage(params) {
  return request.get('/approval/expense/page', { params })
}

export function getExpenseById(id) {
  return request.get(`/approval/expense/${id}`)
}

export function getExpenseProgress(id) {
  return request.get(`/approval/expense/progress/${id}`)
}

export function submitExpense(data) {
  return request.post('/approval/expense/submit', data)
}

export function approveExpense(data) {
  return request.post('/approval/expense/approve', data)
}

export function rejectExpense(data) {
  return request.post('/approval/expense/reject', data)
}

export function cancelExpense(id) {
  return request.post(`/approval/expense/cancel/${id}`)
}
