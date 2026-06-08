import request from '@/utils/request'

/**
 * 获取通知分页列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码，从1开始
 * @param {number} params.pageSize - 每页记录数
 * @returns {Promise}
 */
export function getNoticePage(params) {
  return request({
    url: '/admin/notice/page',
    method: 'get',
    params
  })
}

/**
 * 根据ID获取通知详情
 * @param {number} id - 通知ID
 * @returns {Promise}
 */
export function getNoticeById(id) {
  return request({
    url: `/admin/notice/${id}`,
    method: 'get'
  })
}

/**
 * 新增通知
 * @param {Object} data - 通知数据
 * @param {string} data.content - 通知内容
 * @param {string} data.effectiveTime - 生效时间
 * @param {string} data.expireTime - 失效时间(可选)
 * @param {number} data.duration - 持续时间(分钟)(可选)
 * @returns {Promise}
 */
export function addNotice(data) {
  return request({
    url: '/admin/notice',
    method: 'post',
    data
  })
}

/**
 * 修改通知
 * @param {Object} data - 通知数据
 * @param {number} data.id - 通知ID
 * @param {string} data.content - 通知内容
 * @param {string} data.effectiveTime - 生效时间
 * @param {string} data.expireTime - 失效时间(可选)
 * @param {number} data.duration - 持续时间(分钟)(可选)
 * @returns {Promise}
 */
export function updateNotice(data) {
  return request({
    url: '/admin/notice',
    method: 'put',
    data
  })
}

/**
 * 删除通知
 * @param {number} id - 通知ID
 * @returns {Promise}
 */
export function deleteNotice(id) {
  return request({
    url: `/admin/notice/${id}`,
    method: 'delete'
  })
} 