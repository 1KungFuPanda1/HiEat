import request from '@/utils/request'

export const getShopData = (data: any) =>
  request({
    'url': `/shop/getInfo/` + data,
    'method': 'get',
    'data': data
  })

export const updateShopInfo = (data: any) =>
  request({
    'url': '/shop/update',
    'method': 'post',
    'data': data
  })

/**
 * 获取所有启用的店铺分类列表
 * @returns {Promise<any>} 分类列表数据
 */
export const getShopCategoryList = () => {
  return request({
    url: '/shopCategory/list',
    method: 'get'
  });
};
