/**
 * 网络请求封装
 */
import { useUserStore } from '@/store/user';

// 基础URL，可以根据环境变量等动态设置
const baseURL = uni.getStorageSync('baseURL') || 'http://localhost:8080';

// 请求拦截器
const requestInterceptor = (config) => {
  // 从 Pinia Store 获取 token
  const userStore = useUserStore();
  const token = userStore.getToken;

  // 如果有token，添加到请求头
  if (token) {
    config.header = {
      ...config.headers,//...保留原始请求头中的其他字段
      'authentication': token
    };
  }

  // 添加其他通用请求头
  config.header = {
    ...config.header,
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  };

  // 拼接完整URL
  //判断http是不是开头，是相对路径还是服务器根地址
  if (config.url.indexOf('http') !== 0) {
    config.url = baseURL + config.url;
  }

  return config;
};

// 响应拦截器
const responseInterceptor = (response) => {
  // 请求成功
  if (response.statusCode >= 200 && response.data.code == 1) {
    return response.data;
  }

  // 401: 未授权，token过期或无效
  if (response.statusCode === 401) {
    // 清除本地存储的登录信息
    uni.removeStorageSync('token');
    uni.removeStorageSync('userInfo');

    // 直接显示提示并跳转
    uni.showModal({
      title: '提示',
      content: '登录已过期，请重新登录',
      showCancel: false,//showCancel:false 是用来控制弹窗不显示取消按钮，
      success: () => {
        // 跳转到登录页
        uni.navigateTo({// 保留当前页面，跳转到新页面
          url: '/pages/login/index'
        });
      }
    });
  //返回一个失败的异步结果。进行中（pending）成功（fulfilled）失败（rejected）
    return Promise.reject(new Error('登录已过期'));
  }

  // 其他错误状态码处理
  uni.showToast({
    title: response.data.msg || '请求失败',
    icon: 'none'
  });

  return Promise.reject(response);
};

// 错误处理
const errorHandler = (error) => {
  // 网络错误
  uni.showToast({
    title: '网络异常，请检查网络连接',
    icon: 'none'
  });

  return Promise.reject(error);
};

// 封装请求方法
// options = {
//   url: "/api/shop/list",   // 要请求哪个接口
//   method: "GET",           // GET / POST
//   data: { id: 1 },         // 传给后端的参数
//   header: {}               // 请求头
// }
const request = (options) => {
  // 应用请求拦截器
  const config = requestInterceptor(options);

  // 返回Promise，resolve 是成功回调，reject 是失败回调
  return new Promise((resolve, reject) => {
    uni.request({// 我要发请求了，成功用 resolve，失败用 reject
      ...config,// ...config 是把地址、参数、请求头都展开带过去
      success: (res) => {
        try {
          // 应用响应拦截器
          const result = responseInterceptor(res);
          resolve(result);
        } catch (error) {
          reject(error);
        }
      },
      fail: (err) => {
        errorHandler(err);
        reject(err);
      }
    });
  });
};

// 封装常用请求方法
const http = {
  // GET请求，地址栏带参数从服务器拿东西
  get(url, params = {}, options = {}) {
    return request({
      url,
      method: 'GET',
      data: params,
      ...options
    });
  },

  // POST请求给服务器发数据、新增、登录请求体（body）里传数据，安全
  post(url, data = {}, options = {}) {
    return request({
      url,
      method: 'POST',
      data,
      ...options
    });
  },

  // PUT请求修改已有的数据body 里传
  put(url, data = {}, options = {}) {
    return request({
      url,
      method: 'PUT',
      data,
      ...options
    });
  },

  // DELETE请求删除数据地址栏带 ID
  delete(url, data = {}, options = {}) {
    return request({
      url,
      method: 'DELETE',
      data,
      ...options
    });
  }
};

export default http;