/**
 * 用户相关接口
 */
import http from '@/utils/request';

export const userApi = {
  // 用户账号密码登录
  login(data) {
    return http.post('/user/user/login/account', data);
  },

  // 验证码登录
  smsLogin(data) {
    return http.post('/user/user/login/sms', data);
  },

  // 微信登录
  wechatLogin(data) {
    return http.post('/user/user/login/wechat', data);
  },

  /**
   * 获取用户信息
   * @returns {Promise} 用户信息
   */
  getUserInfo() {
    console.log('获取用户信息');
    return http.get('/user/user/get/userInfo');
  },

  /**
   * 更新用户信息
   * @param {Object} data - 用户信息
   * @param {string} data.id - 用户ID
   * @param {string} data.username - 用户名
   * @param {string} data.phone - 手机号
   * @param {string} data.sex - 性别
   * @param {string} data.avatar - 头像
   * @param {string} data.newPassword - 新密码（可选）
   * @param {string} data.confirmPassword - 确认密码（可选）
   * @returns {Promise} 更新结果
   */
  updateUserInfo(data) {
    console.log('更新用户信息, 数据:', data);
    return http.put('/user/user/update', data);
  },

  /**
   * 发送验证码
   * @param {Object} data - 发送验证码数据
   * @param {string} data.phone - 手机号
   * @returns {Promise}
   */
  sendCode(data) {
    return http.post('/user/user/sendCode', data);
  },

  /**
   * 用户注册
   * @param {Object} data - 注册数据
   * @param {string} data.username - 用户名
   * @param {string} data.phone - 手机号
   * @param {string} data.password - 密码
   * @returns {Promise}
   */
  register(data) {
    return http.post('/user/user/register', data);
  },

  /**
   * 重置密码
   * @param {Object} data - 重置密码数据
   * @param {string} data.phone - 手机号
   * @param {string} data.newPassword - 新密码
   * @returns {Promise}
   */
  resetPassword(data) {
    return http.post('/user/user/resetPassword', data);
  },

  /**
   * 修改密码
   * @param {Object} data - 修改密码数据
   * @param {string} data.oldPassword - 旧密码
   * @param {string} data.newPassword - 新密码
   * @returns {Promise}
   */
  changePassword(data) {
    return http.post('/user/user/changePassword', data);
  }
};

export default userApi;