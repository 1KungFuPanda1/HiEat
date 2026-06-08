import Cookies from 'js-cookie';
// Cookie 读写、token 持久化工具
// App
const sidebarStatusKey = 'sidebar_status';
export const getSidebarStatus = () => Cookies.get(sidebarStatusKey);
export const setSidebarStatus = (sidebarStatus: string) => Cookies.set(sidebarStatusKey, sidebarStatus);

// User
const tokenKey = 'token';
export const getToken = () => Cookies.get(tokenKey);
export const setToken = (token: string) => Cookies.set(tokenKey, token);
export const removeToken = () => Cookies.remove(tokenKey);

// userInfo
const userInfoKey = 'userInfo';
export const getUserInfo = () => Cookies.get(userInfoKey);
export const setUserInfo = (useInfor: Object) => Cookies.set(userInfoKey, useInfor);
export const removeUserInfo = () => Cookies.remove(userInfoKey);

// shopId
const shopIdKey = 'shopId'
export const getShopId = () => Cookies.get(shopIdKey);
export const setShopId = (shopId: string) => Cookies.set(shopIdKey, shopId);
export const removeShopId = () => Cookies.remove(shopIdKey);

// shopName
const shopNameKey = 'shopName'
export const getShopName = () => Cookies.get(shopNameKey);
export const setShopName = (shopName: string) => Cookies.set(shopNameKey, shopName);
export const removeShopName = () => Cookies.remove(shopNameKey);

// shopInfo
const shopInfoKey = 'shopInfo'
export const getShopInfo = () => Cookies.get(shopInfoKey);
export const setShopInfo = (shopInfo: Object) => Cookies.set(shopInfoKey, shopInfo);
export const removeShopInfo = () => Cookies.remove(shopInfoKey);

// printinfo
const printKey = 'print';
export const getPrint = () => Cookies.get(printKey);
export const setPrint = (useInfor: Object) => Cookies.set(printKey, useInfor);
export const removePrint = () => Cookies.remove(printKey);

// 获取消息
const newData = 'new';
export const getNewData = () => Cookies.get(newData);
export const setNewData = (val: Object) => Cookies.set(newData, val);

// isManager相关的cookie操作
export const getIsManager = () => Cookies.get('isManager')
export const setIsManager = (isManager: number) => Cookies.set('isManager', isManager.toString())
export const removeIsManager = () => Cookies.remove('isManager')
