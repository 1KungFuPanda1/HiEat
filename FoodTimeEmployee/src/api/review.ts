import request from '@/utils/request'
import { getShopId } from '@/utils/cookies'

/**
 * 分页查询评论
 * @param params 查询参数
 * @returns Promise
 */
export const getReviewPage = (params: any) => {
  return request({
    url: '/review/page',
    method: 'get',
    params
  })
}

/**
 * 回复评论
 * @param data 回复数据
 * @returns Promise
 */
export const replyReview = (data: any) => {
  return request({
    url: '/review/reply',
    method: 'post',
    data
  })
}

/**
 * 更新评论状态
 * @param data 状态更新数据
 * @returns Promise
 */
export const updateReviewStatus = (data: any) => {
  return request({
    url: '/review/status',
    method: 'put',
    data
  })
} 