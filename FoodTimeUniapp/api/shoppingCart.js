/**
 * 购物车相关接口
 */
import http from '@/utils/request';

export const shoppingCartApi = {
  /**
   * 添加购物车
   * @param {Object} data - 购物车商品数据
   * @param {Long} data.shopId - 店铺ID
   * @param {Long} data.dishId - 菜品ID（添加菜品时必传）
   * @param {Long} data.setmealId - 套餐ID（添加套餐时必传）
   * @param {String} data.dishFlavor - 口味信息（JSON字符串）
   * @returns {Promise}
   */
  addShoppingCart(data) {
    return http.post('/user/shoppingCart/add', data);
  },

  /**
   * 查看当前用户的全部购物车列表（跨店铺）
   * @returns {Promise}
   */
  listShoppingCart() {
    return http.get('/user/shoppingCart/list');
  },

  /**
   * 查看当前用户在指定店铺的购物车列表
   * @param {Long} shopId - 店铺ID
   * @returns {Promise}
   */
  listShoppingCartByShopId(shopId) {
    return http.get('/user/shoppingCart/listByShopId', { shopId });
  },

  /**
   * 清空当前用户的全部购物车
   * @returns {Promise}
   */
  cleanShoppingCart() {
    return http.delete('/user/shoppingCart/clean');
  },

  /**
   * 删除购物车中的一个商品
   * @param {Object} data - 购物车商品数据
   * @param {Long} data.shopId - 店铺ID
   * @param {Long} data.dishId - 菜品ID（删除菜品时必传）
   * @param {Long} data.setmealId - 套餐ID（删除套餐时必传）
   * @param {String} data.dishFlavor - 口味信息（可选，JSON字符串）
   * @returns {Promise}
   */
  removeShoppingCartItem(data) {
    return http.post('/user/shoppingCart/sub', data);
  }
}; 