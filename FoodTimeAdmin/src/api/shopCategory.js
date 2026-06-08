import request from '@/utils/request'

/**
 * 获取所有店铺分类列表
 * @returns {Promise}
 */
export function getShopCategoryList() {
  return request({
    url: '/admin/shopCategory/list',
    method: 'get'
  })
}

/**
 * 根据ID获取店铺分类详情
 * @param {number} id - 店铺分类ID
 * @returns {Promise}
 */
export function getShopCategoryById(id) {
  return request({
    url: `/admin/shopCategory/${id}`,
    method: 'get'
  })
}

/**
 * 新增店铺分类
 * @param {Object} data - 店铺分类数据
 * @param {string} data.name - 分类名称
 * @param {string} data.image - 分类图片
 * @param {number} data.status - 分类状态
 * @returns {Promise}
 */
export function addShopCategory(data) {
  return request({
    url: '/admin/shopCategory',
    method: 'post',
    data
  })
}

/**
 * 修改店铺分类
 * @param {Object} data - 店铺分类数据
 * @param {number} data.id - 分类ID
 * @param {string} data.name - 分类名称
 * @param {string} data.image - 分类图片
 * @param {number} data.status - 分类状态
 * @returns {Promise}
 */
export function updateShopCategory(data) {
  return request({
    url: '/admin/shopCategory',
    method: 'put',
    data
  })
}

/**
 * 删除店铺分类
 * @param {number} id - 店铺分类ID
 * @returns {Promise}
 */
export function deleteShopCategory(id) {
  return request({
    url: `/admin/shopCategory/${id}`,
    method: 'delete'
  })
}

/**
 * 启用或禁用店铺分类
 * @param {number} id - 店铺分类ID
 * @param {number} status - 状态(0禁用，1启用)
 * @returns {Promise}
 */
export function changeShopCategoryStatus(id, status) {
  return request({
    url: `/admin/shopCategory/status/${status}`,
    method: 'post',
    data: { id }
  })
} 