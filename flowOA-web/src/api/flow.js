import request from '@/utils/request'

export function getFlowDefinitionList() {
  return request.get('/flow/definition/list')
}

export function getFlowDefinitionById(id) {
  return request.get(`/flow/definition/${id}`)
}

export function deployFlowDefinition(xml) {
  return request.post('/flow/definition/deploy', xml, {
    headers: { 'Content-Type': 'text/plain' }
  })
}

export function deleteFlowDefinition(id) {
  return request.delete(`/flow/definition/${id}`)
}

export function getTodoTasks() {
  return request.get('/approval/task/todo')
}

export function getFlowInstance(id) {
  return request.get(`/flow/instance/${id}`)
}

export function approveFlow(data) {
  return request.post('/flow/approve', data)
}

export function rejectFlow(data) {
  return request.post('/flow/reject', data)
}

export function transferFlow(data) {
  return request.post('/flow/transfer', data)
}

export function terminateFlow(instanceId) {
  return request.post(`/flow/terminate/${instanceId}`)
}

export function saveFlowDesign(data) {
  return request.post('/flow/definition/save', data)
}

export function publishFlowDefinition(id) {
  return request.post(`/flow/definition/publish/${id}`)
}

// Re-export getUserList from user.js for convenience
export { getUserList } from './user'
