/**
 * 商品相关接口
 */
import http from '@/utils/request';

export const goodsApi = {
  // 获取商品列表
  getGoodsList(params) {
    return http.get('/goods/list', params);
  },

  // 获取商品详情
  getGoodsDetail(id) {
    return http.get(`/goods/detail/${id}`);
  },

  // 获取菜品列表
  getDishList(categoryId) {
    return http.get('/user/dish/list', { categoryId });
  },

  // 获取菜品详情
  getDishDetail(id) {
    return http.get(`/user/dish/detail/${id}`);
  },

  // 获取套餐列表
  getSetmealList(categoryId) {
    return http.get('/user/setmeal/list', { categoryId });
  },

  // 获取套餐详情
  getSetmealDetail(id) {
    return http.get(`/user/setmeal/detail/${id}`);
  },

  // 获取分类列表
  getCategoryList(shopId) {
    return http.get('/user/category/list', { shopId });
  }
};

export default goodsApi;