import { defineStore } from 'pinia'
import { login, userLogout } from '@/api/employee'
import { getShopData } from '@/api/shop'
import {
  getToken, setToken, removeToken, getShopId, setShopId, setShopName,
  getShopName, getUserInfo, removeUserInfo,
  setUserInfo, removeShopId, removeShopInfo, removeShopName,
  setShopInfo, getShopInfo,
  getIsManager, setIsManager, removeIsManager
} from '@/utils/cookies'
import Cookies from 'js-cookie'
import { ElMessage } from 'element-plus'

export interface IUserState {
  token: string
  name: string
  avatar: string
  shopId: string
  shopInfo: any
  introduction: string
  userInfo: any
  roles: string[]
  username: string
  isManager: number
}

export const useUserStore = defineStore('user', {
  state: (): IUserState => ({
    token: getToken() || '',
    name: '',
    avatar: '',
    shopId: getShopId() || '',
    shopName: getShopName() || '',
    shopInfo: getShopInfo() || '',
    introduction: '',
    userInfo: {},
    roles: [],
    username: Cookies.get('username') || '',
    isManager: Number(getIsManager()) || 0
  }),

  actions: {
    SET_TOKEN(token: string) {
      this.token = token
      setToken(token)
    },

    SET_NAME(name: string) {
      this.name = name
    },

    SET_USERINFO(userInfo: any) {
      this.userInfo = { ...userInfo }
      setUserInfo(userInfo)
    },

    SET_AVATAR(avatar: string) {
      this.avatar = avatar
    },

    SET_INTRODUCTION(introduction: string) {
      this.introduction = introduction
    },

    SET_ROLES(roles: string[]) {
      this.roles = roles
    },

    SET_SHOPID(shopId: string) {
      this.shopId = shopId
      setShopId(shopId)
    },

    SET_SHOPNAME(shopName: string) {
      this.shopName = shopName
      setShopName(shopName)
    },

    SET_USERNAME(username: string) {
      this.username = username
      Cookies.set('username', username)
    },

    SET_SHOPINFO(shopInfo: any) {
      this.shopInfo = shopInfo
      setShopInfo(shopInfo)
    },

    SET_IS_MANAGER(isManager: number) {
      this.isManager = isManager
      setIsManager(isManager)
    },

    async Login(userInfo: { username: string, password: string }) {
      let { username, password } = userInfo
      username = username.trim()
      const { data } = await login({ username, password })
      if (String(data.code) === '1') {
        this.SET_USERNAME(data.data.username)
        this.SET_TOKEN(data.data.token)
        this.SET_USERINFO(data.data)
        this.SET_SHOPID(data.data.shopId)
        this.SET_SHOPNAME(data.data.shopName)
        if (data.data.isManager !== undefined) {
          this.SET_IS_MANAGER(data.data.isManager)
        }
        await getShopData(this.shopId).then((res: any) => {
          if (String(res.data.code) === '1') {
            this.SET_SHOPINFO(res.data.data)
          } else {
            console.log(res.data.msg)
          }
        })
        return data
      } else {
        return ElMessage.error(data.msg)
      }
    },

    async ResetShopInfo() {
      await getShopData(this.shopId).then((res: any) => {
        if (String(res.data.code) === '1') {
          this.SET_SHOPINFO(res.data.data)
          this.SET_SHOPNAME(res.data.data.shopName)
        } else {
          console.log(res.data.msg)
        }
      })
    },

    ResetToken() {
      removeToken()
      this.SET_TOKEN('')
      this.SET_ROLES([])
    },

    async changeStore(data: any) {
      this.SET_SHOPID(data.data.shopId)
      this.SET_TOKEN(data.authorization)
      setShopId(data.data.shopId)
      setToken(data.authorization)
    },

    async GetUserInfo() {
      if (this.token === '') {
        throw Error('GetUserInfo: token is undefined!')
      }
      const data = JSON.parse(getUserInfo() as string)
      if (!data) {
        throw Error('Verification failed, please Login again.')
      }
      const { roles, name, avatar, introduction, applicant, storeManagerName } = data
      if (!roles || roles.length <= 0) {
        throw Error('GetUserInfo: roles must be a non-null array!')
      }
      this.SET_ROLES(roles)
      this.SET_USERINFO(data)
      this.SET_NAME(name || applicant || storeManagerName)
      this.SET_AVATAR(avatar)
      this.SET_INTRODUCTION(introduction)
    },

    async LogOut() {
      await userLogout({})
      removeToken()
      this.SET_TOKEN('')
      this.SET_ROLES([])
      Cookies.remove('username')
      removeUserInfo()
      removeShopId()
      removeShopInfo()
      removeShopName()
      removeIsManager()
    }
  },

  persist: {
    key: 'user-store',
    storage: window.localStorage
  }
})
