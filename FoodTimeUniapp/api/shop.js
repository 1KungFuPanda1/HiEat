/**
 * 店铺相关接口
 */
import http from '@/utils/request';

export const shopApi = {
	
  // 获取店铺列表（综合排序）
  getShopList(params) {
    console.log('获取店铺列表, 参数:', params);
    return http.get('/user/shop/page', params);
  },

  // 获取店铺列表（按销量排序）
  getShopListByCount(params) {
    console.log('按销量获取店铺列表, 参数:', params);
    return http.get('/user/shop/count', params);
  },

  // 获取店铺列表（按评分排序）
  getShopListByScore(params) {
    console.log('按评分获取店铺列表, 参数:', params);
    return http.get('/user/shop/score', params);
  },

  // 获取店铺详情
  getShopDetail(id) {
    console.log('获取店铺详情, ID:', id);
    return http.get(`/user/shop/${id}`).then(res => {
      console.log('店铺详情返回数据:', res);
      return res;
    }).catch(err => {
      console.error('获取店铺详情失败:', err);
      throw err;
    });
  },

  /**
   * 获取所有启用的店铺分类列表
   * @returns {Promise} 分类列表数据
   */
  getShopCategories() {
    console.log('获取店铺分类列表');
    return http.get('/user/shopCategory/list');
  },

  /**
   * 根据分类获取店铺列表
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.pageSize - 每页条数
   * @param {string} params.rule - 排序规则
   * @param {number} params.categoryId - 分类ID
   * @returns {Promise} 店铺列表数据
   */
  getShopsByCategory(params) {
    console.log('根据分类获取店铺列表, 参数:', params);
    return http.get('/user/shop/category/page', params);
  },

  /**
   * 搜索店铺
   * @param {Object} params - 搜索参数
   * @param {number} params.page - 页码
   * @param {number} params.pageSize - 每页条数
   * @param {string} params.keyword - 搜索关键词
   * @param {string} [params.rule] - 排序规则：default/score/sales
   * @returns {Promise} 搜索结果
   */
  searchShops(params) {
    console.log('搜索店铺, 参数:', params);
    return http.get('/user/shop/search', params);
  },

  /**
   * 获取店铺营业状态
   * @param {number} shopId - 店铺ID
   * @returns {Promise} 营业状态：1=营业中，0=打烊中
   */
  getShopStatus(shopId) {
    return http.get(`/user/shop/status/${shopId}`);
  }
};

export default shopApi;