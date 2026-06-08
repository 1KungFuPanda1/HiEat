/**
 * 地址相关接口
 */
import http from '@/utils/request';

export const addressApi = {
  /**
   * 获取地址列表
   * @returns {Promise}
   */
  getAddressList() {
    return http.get('/user/addressBook/list');
  },

  /**
   * 获取默认地址
   * @returns {Promise}
   */
  getDefaultAddress() {
    return http.get('/user/addressBook/default');
  },

  /**
   * 新增地址
   * @param {Object} data - 地址信息
   * @returns {Promise}
   */
  addAddress(data) {
    return http.post('/user/addressBook', data);
  },

  /**
   * 修改地址
   * @param {Object} data - 地址信息
   * @returns {Promise}
   */
  updateAddress(data) {
    return http.put('/user/addressBook', data);
  },

  /**
   * 删除地址
   * @param {Long} id - 地址ID
   * @returns {Promise}
   */
  deleteAddress(id) {
    return http.get(`/user/addressBook/delete/${id}`);
  },

  /**
   * 获取地址详情
   * @param {Long} id - 地址ID
   * @returns {Promise} 
   */
  getAddressById(id) {
    return http.get(`/user/addressBook/${id}`);
  },

  /** 设置默认地址
  /**
   * 设置默认地址
   * @param {Long} id - 地址ID
   * @returns {Promise}
   */
  setDefaultAddress(id) {
    return http.put(`/user/addressBook/default?id=${id}`);
  },

  /**
   * 获取省市区行政规划
   * @returns {Promise}
   */
  getAreaTree() {
    return http.get('/user/area/tree');
  }
};

export default addressApi;