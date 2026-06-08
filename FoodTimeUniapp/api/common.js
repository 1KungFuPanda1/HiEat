/**
 * 通用API接口
 */
import http from '@/utils/request';

export const commonApi = {
  /**
   * 上传文件
   * @param {string} filePath - 文件路径
   * @param {string} [name='file'] - 文件对应的 key
   * @param {Object} [formData={}] - 其他表单数据
   * @returns {Promise}
   */
  uploadFile(filePath, name = 'file', formData = {}) {
    return new Promise((resolve, reject) => {
      // 获取用户token
      const token = uni.getStorageSync('token');
      const header = token ? { 'authentication': token } : {};

      uni.uploadFile({
        url: (http.baseUrl || 'http://localhost:8080') + '/user/common/upload',
        filePath: filePath,
        name: name,
        formData: formData,
        header: header,
        success: (res) => {
          if (res.statusCode === 200) {
            try {
              const data = JSON.parse(res.data);
              if (data.code === 1) {
                resolve(data);
              } else {
                uni.showToast({
                  title: data.msg || '上传失败',
                  icon: 'none'
                });
                reject(new Error(data.msg || '上传失败'));
              }
            } catch (e) {
              uni.showToast({
                title: '解析上传结果失败',
                icon: 'none'
              });
              reject(new Error('解析上传结果失败'));
            }
          } else {
            uni.showToast({
              title: `上传失败：${res.statusCode}`,
              icon: 'none'
            });
            reject(new Error(`上传失败：${res.statusCode}`));
          }
        },
        fail: (err) => {
          uni.showToast({
            title: '网络异常，请检查网络连接',
            icon: 'none'
          });
          reject(err);
        }
      });
    });
  },

  /**
   * 获取公告列表
   * @returns {Promise} 公告列表数据
   */
  getNotices() {
    console.log('获取公告列表');
    return http.get('/user/notice/list');
  },

  /**
   * 获取轮播图列表
   * @returns {Promise} 轮播图列表数据
   */
  getCarousels() {
    console.log('获取轮播图列表');
    return http.get('/user/carousel/list');
  }
};

export default commonApi; 

// {
//   "code": 1,
//   "msg": "操作成功",
//   "data": [
//     {
//       "id": 1,
//       "title": "奶茶星期四四五折（活动持续两周）",
//       "content": "本周奶茶特惠，周四至周五45折",
//       "createTime": "2025-04-14 14:38:26",
//       "status": 1
//     },
//     {
//       "id": 2,
//       "title": "新店开业福利",
//       "content": "地道川菜馆开业，首单立减5元",
//       "createTime": "2025-01-30 21:25:51",
//       "status": 1
//     }
//   ]
// }