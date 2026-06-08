import request from '@/utils/request'
import { getShopId } from '@/utils/cookies'
// 修改密码
export const editPassword = (data: any) =>
  request({
    'url': '/employee/editPassword',
    'method': 'put',
    data
  })
  // 获取营业状态
  export const getStatus = () =>
  request({
    'url': `/shop/status/` + getShopId(),
    'method': 'get'
  })
    // 设置营业状态
    export const setStatus = (data:any) =>
    request({
      'url': `/shop/set`,
      'method': 'put',
      'data':data
    })