/**
 * 订单相关接口
 */
import http from '@/utils/request';

export const orderApi = {
  /**
   * 提交订单
   * @param {Object} data - 订单数据
   * @param {Long} data.shopId - 店铺ID
   * @param {Long} data.addressBookId - 地址簿ID
   * @param {number} data.payMethod - 付款方式
   * @param {string} data.remark - 备注
   * @param {string} data.estimatedDeliveryTime - 预计送达时间
   * @param {number} data.deliveryStatus - 配送状态 1立即送出 0选择具体时间
   * @param {number} data.tablewareNumber - 餐具数量
   * @param {number} data.tablewareStatus - 餐具数量状态 1按餐量提供 0选择具体数量
   * @param {number} data.packAmount - 打包费
   * @param {number} data.amount - 总金额
   * @returns {Promise}
   */
  submitOrder(data) {
    return http.post('/user/order/submit', data);
  },

  /**
   * 模拟支付
   * @param {Object} data - 支付数据
   * @param {string} data.orderNumber - 订单号
   * @param {number} data.payMethod - 支付方式 1-微信支付 2-模拟支付
   * @returns {Promise}
   */
  simulatePayment(data) {
    return http.put('/user/order/simulatePayment', data);
  },

  /**
   * 获取历史订单列表
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.pageSize - 每页条数
   * @param {number} [params.status] - 订单状态（可选）：1待付款 2待接单 3已接单 4派送中 5已完成 6已取消 7退款，不传查询所有
   * @returns {Promise}
   */
  getHistoryOrders(params) {
    return http.get('/user/order/historyOrders', params);
  },

  /**
   * 获取订单列表
   * @param {Object} params - 查询参数
   * @returns {Promise}
   */
  getOrderList(params) {
    return http.get('/order/list', params);
  },

  /**
   * 获取订单详情
   * @param {Long} id - 订单ID
   * @returns {Promise}
   */
  getOrderDetail(id) {
    return http.get(`/user/order/orderDetail/${id}`);
  },

  /**
   * 用户取消订单
   * @param {Long} id - 订单ID
   * @returns {Promise}
   */
  cancelOrder(id) {
    return http.put(`/user/order/cancel/${id}`);
  },

  /**
   * 再来一单
   * @param {Long} id - 订单ID
   * @returns {Promise}
   */
  repeatOrder(id) {
    return http.post(`/user/order/repetition/${id}`);
  },

  /**
   * 催单
   * @param {Long} id - 订单ID
   * @returns {Promise}
   */
  reminderOrder(id) {
    return http.get(`/user/order/reminder/${id}`);
  }
};

export default orderApi;
