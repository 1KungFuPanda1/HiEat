/**
 * 评论相关接口
 */
import http from '@/utils/request';

export const reviewApi = {
  /**
   * 分页查询评论
   * @param {Object} params - 查询参数
   * @param {Long} params.shopId - 店铺ID
   * @param {Integer} params.rating - 评分筛选（可选）
   * @param {String} params.status - 状态（可选）
   * @param {Number} params.page - 页码
   * @param {Number} params.pageSize - 每页数量
   * @returns {Promise}
   */
  getReviewList(params) {
    return http.get('/user/review/page', params);
  },

  /**
   * 提交评价
   * @param {Object} reviewData - 评价数据
   * @param {Long} reviewData.shopId - 店铺ID
   * @param {Integer} reviewData.rating - 评分(1-5)
   * @param {String} reviewData.title - 评价标题
   * @param {String} reviewData.content - 评价内容
   * @param {Array<String>} reviewData.photoUrls - 图片URL列表
   * @returns {Promise}
   */
  submitReview(reviewData) {
    return http.post('/user/review', reviewData);
  },

  /**
   * 获取订单评价详情
   * @param {Long} orderId - 订单ID
   * @returns {Promise}
   */
  getOrderReview(orderId) {
    return http.get(`/user/review/order/${orderId}`);
  }
}; 