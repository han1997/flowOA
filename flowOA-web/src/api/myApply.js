import request from '@/utils/request'

export function getMyApplyPage(params) {
  return request.get('/approval/my/page', { params })
}
