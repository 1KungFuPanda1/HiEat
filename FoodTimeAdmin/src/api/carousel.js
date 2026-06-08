import request from '@/utils/request'

/**
 * 获取轮播图分页列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码，从1开始
 * @param {number} params.pageSize - 每页记录数
 * @returns {Promise}
 */
export function getCarouselPage(params) {
  return request({
    url: '/admin/carousel/page',
    method: 'get',
    params
  })
}

/**
 * 根据ID获取轮播图详情
 * @param {number} id - 轮播图ID
 * @returns {Promise}
 */
export function getCarouselById(id) {
  return request({
    url: `/admin/carousel/${id}`,
    method: 'get'
  })
}

/**
 * 新增轮播图
 * @param {Object} data - 轮播图数据
 * @param {string} data.imageUrl - 图片URL
 * @param {number} [data.shopId] - 关联的店铺ID(可选)
 * @returns {Promise}
 */
export function addCarousel(data) {
  return request({
    url: '/admin/carousel',
    method: 'post',
    data
  })
}

/**
 * 修改轮播图
 * @param {Object} data - 轮播图数据
 * @param {number} data.id - 轮播图ID
 * @param {string} data.imageUrl - 图片URL
 * @param {number} [data.shopId] - 关联的店铺ID(可选)
 * @returns {Promise}
 */
export function updateCarousel(data) {
  return request({
    url: '/admin/carousel',
    method: 'put',
    data
  })
}

/**
 * 删除轮播图
 * @param {number} id - 轮播图ID
 * @returns {Promise}
 */
export function deleteCarousel(id) {
  return request({
    url: `/admin/carousel/${id}`,
    method: 'delete'
  })
}