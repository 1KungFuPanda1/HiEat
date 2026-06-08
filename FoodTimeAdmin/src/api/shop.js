import request from '@/utils/request'

/**
 * 获取店铺审核列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {number} params.auditStatus - 审核状态(可选)
 * @returns {Promise}
 */
export function getShopAuditList(params) {
  return request({
    url: '/admin/shop/audit/page',
    method: 'get',
    params
  })
}

/**
 * 审核店铺
 * @param {Object} data - 审核数据
 * @param {number} data.shopId - 店铺ID
 * @param {number} data.auditStatus - 审核状态(1:通过, 2:拒绝)
 * @param {string} data.auditOpinion - 审核意见(当auditStatus为2时必填)
 * @returns {Promise}
 */
export function auditShop(data) {
  return request({
    url: '/admin/shop/audit',
    method: 'post',
    data
  })
}

/**
 * 获取店铺详情
 * @param {number} id - 店铺ID
 * @returns {Promise}
 */
export function getShopDetail(id) {
  return request({
    url: `/shop/${id}`,
    method: 'get'
  })
}

/**
 * 导出店铺数据
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function exportShopData(params) {
  return request({
    url: '/shop/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
