/**
 * API接口统一管理入口
 */
import { userApi } from './user';
import { goodsApi } from './goods';
import { orderApi } from './order';
import { addressApi } from './address';
import { shopApi } from './shop';
import { shoppingCartApi } from './shoppingCart';
import { reviewApi } from './review';
import { commonApi } from './common';

// 导出所有API模块
export {
  userApi,
  goodsApi,
  orderApi,
  addressApi,
  shopApi,
  shoppingCartApi,
  reviewApi,
  commonApi
};