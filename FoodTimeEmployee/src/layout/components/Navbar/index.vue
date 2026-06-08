<template>
  <div class="navbar">
    <div class="statusBox">
      <hamburger id="hamburger-container" :is-active="sidebar.opened" class="hamburger-container"
        @toggleClick="toggleSideBar" />
      <span class="middle-shop-name">{{ shopName }}</span>
      <span v-if="status === 1" class="businessBtn">营业中</span>
      <span v-else class="businessBtn closing">打烊中</span>
    </div>

    <div :key="restKey" class="right-menu">
      <div class="rightStatus">
        <audio ref="audioVo" hidden>
          <source src="./../../../assets/preview.mp3" type="audio/mp3" />
        </audio>
        <audio ref="audioVo2" hidden>
          <source src="./../../../assets/reminder.mp3" type="audio/mp3" />
        </audio>
        <span class="navicon operatingState" @click="handleStatus"><i />营业状态设置</span>
      </div>
      <div class="avatar-wrapper">
        <div :class="shopShow ? 'userInfo' : ''" @mouseenter="toggleShow" @mouseleave="mouseLeaves">
          <el-button type="primary" :class="shopShow ? 'active' : ''">
            {{ name }}<i class="el-icon-arrow-down" />
          </el-button>
          <div v-if="shopShow" class="userList">
            <p class="amendPwdIcon" @click="handlePwd">
              修改密码<i />
            </p>
            <p class="outLogin" @click="logout">
              退出登录<i />
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- 营业状态弹层 -->
    <el-dialog
      v-model="dialogVisible"
      width="440px"
      :show-close="false"
      class="status-dialog"
      :close-on-click-modal="false"
      center
    >
      <template #header>
        <div class="status-dialog-header">
          <div class="status-dialog-icon">
            <i class="iconfont icon-time"></i>
          </div>
          <span class="status-dialog-title">营业状态设置</span>
        </div>
      </template>

      <div class="status-options">
        <div
          class="status-card"
          :class="{ active: setStatusVal === 1 }"
          @click="setStatusVal = 1"
        >
          <div class="status-card-icon open-icon">
            <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20Z"/>
              <path d="M8 15c1.5 2 4 3 6 2.5s3-3 2.5-5"/>
              <circle cx="9" cy="9" r="1" fill="currentColor" stroke="none"/>
              <circle cx="15" cy="9" r="1" fill="currentColor" stroke="none"/>
            </svg>
          </div>
          <div class="status-card-body">
            <div class="status-card-title">营业中</div>
            <div class="status-card-desc">接收所有订单，餐厅正常接单中</div>
          </div>
          <div class="status-card-check" v-show="setStatusVal === 1">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
            </svg>
          </div>
        </div>

        <div
          class="status-card"
          :class="{ active: setStatusVal === 0 }"
          @click="setStatusVal = 0"
        >
          <div class="status-card-icon closed-icon">
            <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/>
              <circle cx="9" cy="9" r="1" fill="currentColor" stroke="none"/>
              <circle cx="15" cy="9" r="1" fill="currentColor" stroke="none"/>
              <path d="M9 15c0 2 3 3 6 0"/>
            </svg>
          </div>
          <div class="status-card-body">
            <div class="status-card-title">打烊中</div>
            <div class="status-card-desc">仅接受预定订单，手动恢复后可接单</div>
          </div>
          <div class="status-card-check" v-show="setStatusVal === 0">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
            </svg>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="status-dialog-footer">
          <el-button class="btn-cancel" @click="dialogVisible = false" round>取 消</el-button>
          <el-button class="btn-confirm" type="primary" @click="handleSave" round>确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 修改密码 -->
    <Password :dialog-form-visible="dialogFormVisible" @handleclose="handlePwdClose" />
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import Hamburger from '@/components/Hamburger/index.vue'
import { getStatus, setStatus } from '@/api/users'
import Cookies from 'js-cookie'
import { getUserInfo, getShopId, getShopName } from '@/utils/cookies'
import { getCountUnread } from '@/api/inform'
import Password from '../components/password.vue'

export default defineComponent({
  name: 'Navbar',
  components: {
    Hamburger,
    Password
  },

  data() {
    return {
      restKey: 0,
      websocket: null as WebSocket | null,
      audioIsPlaying: false,
      audioPaused: false,
      statusValue: true,
      shopShow: false,
      dialogVisible: false,
      status: 1,
      setStatusVal: 1,
      dialogFormVisible: false,
      ountUnread: 0
    }
  },

  computed: {
    sidebar() {
      return useAppStore().sidebar
    },
    device() {
      return useAppStore().device.toString()
    },
    userInfo() {
      return useUserStore().userInfo
    },
    name() {
      const userStore = useUserStore()
      return (userStore.userInfo as any).name
        ? (userStore.userInfo as any).name
        : JSON.parse((getUserInfo() as any) || '{}').name
    },
    shopId() {
      const userStore = useUserStore()
      return userStore.shopId ? userStore.shopId : getShopId()
    },
    shopName() {
      const userStore = useUserStore()
      return userStore.shopName ? userStore.shopName : getShopName()
    }
  },

  mounted() {
    document.addEventListener('click', this.handleClose)
    this.getStatusData()
  },

  created() {
    this.webSocket()
  },

  beforeUnmount() {
    // 关闭所有弹窗，防止路由切换时 el-dialog teleport 导致 parentNode 为 null 的错误
    this.dialogVisible = false
    this.dialogFormVisible = false
    this.shopShow = false

    // 移除事件监听
    document.removeEventListener('click', this.handleClose)

    // 关闭 WebSocket 连接
    if (this.websocket) {
      this.websocket.close()
      this.websocket = null
    }
  },

  methods: {
    webSocket() {
      const that = this as any
      let clientId = Math.random().toString(36).substr(2)
      let socketUrl = import.meta.env.VITE_APP_SOCKET_URL + clientId
      console.log(socketUrl, 'socketUrl')
      if (typeof WebSocket == 'undefined') {
        ;(that as any).$notify({
          title: '提示',
          message: '当前浏览器无法接收实时报警信息，请使用谷歌浏览器！',
          type: 'warning',
          duration: 0
        })
      } else {
        this.websocket = new WebSocket(socketUrl)
        this.websocket.onopen = function () {
          console.log('浏览器WebSocket已打开')
          const shopId = that.shopId
          if (shopId) {
            this.send(JSON.stringify({
              type: 'init',
              shopId: shopId
            }))
          }
        }
        this.websocket.onmessage = function (msg) {
          const audioVo = that.$refs.audioVo as HTMLAudioElement
          const audioVo2 = that.$refs.audioVo2 as HTMLAudioElement
          if (audioVo) audioVo.currentTime = 0
          if (audioVo2) audioVo2.currentTime = 0
          console.log(msg, JSON.parse(msg.data), 'msg')
          const jsonMsg = JSON.parse(msg.data)
          that.$notify({
            title: jsonMsg.type === 1 ? '待接单' : '催单',
            duration: 0,
            dangerouslyUseHTMLString: true,
            onClick: () => {
              that.$router
                .push(`/order?orderId=${jsonMsg.orderId}`)
                .catch((err: any) => {
                  console.log(err)
                })
              setTimeout(() => {
                location.reload()
              }, 100)
            },
            message: `${jsonMsg.type === 1
              ? `<span>您有1个<span style=color:#419EFF>订单待处理</span>,${jsonMsg.content},请及时接单</span>`
              : `${jsonMsg.content}<span style='color:#419EFF;cursor: pointer'>去处理</span>`
              }`
          })
        }
        this.websocket.onerror = function () {
          that.$notify({
            title: '错误',
            message: '服务器错误，无法接收实时报警信息',
            type: 'error',
            duration: 0
          })
        }
        this.websocket.onclose = function () {
          console.log('WebSocket已关闭')
        }
      }
    },

    toggleSideBar() {
      useAppStore().ToggleSideBar(false)
    },

    async logout() {
      // 在路由跳转前关闭所有弹窗，防止 el-dialog teleport 导致的 parentNode 错误
      this.dialogVisible = false
      this.dialogFormVisible = false
      this.shopShow = false

      const userStore = useUserStore()
      await userStore.LogOut()
      this.$router.replace({ path: '/login' }).catch((err: any) => {
        // 忽略 NavigationDuplicated 等路由错误
        console.log('路由跳转:', err)
      })
    },

    async getCountUnreadData() {
      try {
        const { data } = await getCountUnread()
        if (data.code === 1) {
          useAppStore().StatusNumber(data.data)
        } else {
          this.$message.error(data.msg)
        }
      } catch (error) {
        console.error('获取未读消息失败:', error)
      }
    },

    async getStatusData() {
      try {
        const { data } = await getStatus()
        this.status = data.data
        this.setStatusVal = this.status
      } catch (error) {
        console.error('获取营业状态失败:', error)
      }
    },

    toggleShow() {
      this.shopShow = true
    },

    mouseLeaves() {
      this.shopShow = false
    },

    handleClose() {
      // clearTimeout
    },

    handleStatus() {
      this.dialogVisible = true
    },

    async handleSave() {
      try {
        const params = {
          shopId: this.shopId,
          status: this.setStatusVal
        }
        const { data } = await setStatus(params)
        if (data.code === 1) {
          this.dialogVisible = false
          this.getStatusData()
        }
      } catch (error) {
        console.error('设置营业状态失败:', error)
      }
    },

    handlePwd() {
      this.dialogFormVisible = true
    },

    handlePwdClose() {
      this.dialogFormVisible = false
    }
  }
})
</script>

<style lang="scss" scoped>
.navbar {
  height: 60px;
  position: relative;
  background: #918acd;

  .statusBox {
    float: left;
    height: 100%;
    align-items: center;
    display: flex;
  }

  .hamburger-container {
    padding: 0 12px 0 20px;
    cursor: pointer;
    transition: background 0.3s;
    -webkit-tap-highlight-color: transparent;

    &:hover {
      background: rgba(0, 0, 0, 0.025);
    }
  }

  .right-menu {
    float: right;
    margin-right: 20px;
    color: #333333;
    font-size: 14px;

    span {
      padding: 0 10px;
      width: 130px;
      display: inline-block;
      cursor: pointer;

      &:hover {
        background: rgba(255, 255, 255, 0.52);
      }
    }

    .amendPwdIcon {
      i {
        width: 18px;
        height: 18px;
        background: url(./../../../assets/icons/btn_gaimi@2x.png) no-repeat;
        background-size: contain;
        margin-top: 8px;
      }
    }

    .outLogin {
      i {
        width: 18px;
        height: 18px;
        background: url(./../../../assets/icons/btn_close@2x.png) no-repeat 100% 100%;
        background-size: contain;
        margin-top: 8px;
      }
    }
  }

  .rightStatus {
    height: 100%;
    line-height: 60px;
    display: flex;
    align-items: center;
    float: left;
  }

  .avatar-wrapper {
    margin-top: 14px;
    margin-left: 18px;
    position: relative;
    float: right;
    width: 120px;
    text-align: left;

    .el-button--primary {
      background: rgba(255, 255, 255, 0.52);
      border-radius: 4px;
      padding-top: 0px;
      padding-bottom: 0px;
      position: relative;
      width: 120px;
      padding-left: 12px;
      text-align: left;
      border: 0 none;
      height: 32px;
      line-height: 32px;

      &.active {
        background: rgba(250, 250, 250, 0);
        border: 0 none;

        .el-icon-arrow-down {
          transform: rotate(-180deg);
        }
      }
    }
  }

  .businessBtn {
    height: 22px;
    line-height: 20px;
    background: #99ddcc;
    border: 1px solid #ffffff;
    border-radius: 4px;
    display: inline-block;
    padding: 0 6px;
    color: #fff;
  }

  .closing {
    background: #6a6a6a;
  }

  .middle-shop-name {
    display: inline-block;
    padding-right: 10px;
    padding-left: 40px;
    font-size: 16px;
  }

  .navicon {
    i {
      display: inline-block;
      width: 18px;
      height: 18px;
      vertical-align: sub;
      margin: 0 4px 0 0;
    }
  }

  .operatingState {
    i {
      background: url('./../../../assets/icons/time.png') no-repeat;
      background-size: contain;
    }
  }
}
</style>

<style lang="scss">
.el-notification {
  width: 419px !important;

  .el-notification__title {
    margin-bottom: 14px;
    color: #333;

    .el-notification__content {
      color: #333;
    }
  }
}

/* ========== 营业状态弹窗样式 ========== */
.status-dialog {
  .el-dialog {
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
  }

  .el-dialog__header {
    padding: 0;
    margin: 0;
    border: none;
  }

  .el-dialog__body {
    padding: 16px 28px 24px;
  }

  .el-dialog__footer {
    padding: 0 28px 28px;
  }
}

.status-dialog-header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 24px 28px 18px;
  background: linear-gradient(135deg, #faf9ff 0%, #f0edff 100%);
  border-bottom: 1px solid #ebe9f2;
}

.status-dialog-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, #918acd, #b5aef0);
  display: flex;
  align-items: center;
  justify-content: center;

  i {
    font-size: 22px;
    color: #fff;
    display: block;
    width: 22px;
    height: 22px;
    background: url('./../../../assets/icons/time.png') no-repeat center;
    background-size: contain;
    filter: brightness(0) invert(1);
  }
}

.status-dialog-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c2c2c;
  letter-spacing: 0.3px;
}

/* 状态卡片容器 */
.status-options {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-top: 8px;
}

/* 单个状态卡片 */
.status-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 12px;
  border: 2px solid #ebe9f2;
  background: #fafafa;
  cursor: pointer;
  transition: all 0.25s ease;
  position: relative;

  &:hover {
    border-color: #c8c1f0;
    background: #f8f7ff;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(145, 138, 205, 0.12);
  }

  &.active {
    border-color: #918acd;
    background: linear-gradient(135deg, #f9f8ff, #f2efff);
    box-shadow: 0 4px 16px rgba(145, 138, 205, 0.18);

    .status-card-title {
      color: #918acd;
    }
  }
}

.status-card-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.open-icon {
    background: linear-gradient(135deg, #e8f5e9, #c8e6c9);
    color: #4caf50;
  }

  &.closed-icon {
    background: linear-gradient(135deg, #fbe9e7, #ffccbc);
    color: #ff7043;
  }
}

.status-card-body {
  flex: 1;
  min-width: 0;
}

.status-card-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c2c2c;
  margin-bottom: 4px;
  transition: color 0.25s ease;
}

.status-card-desc {
  font-size: 13px;
  color: #999;
  line-height: 1.5;
}

.status-card-check {
  width: 24px;
  height: 24px;
  color: #918acd;
  flex-shrink: 0;
  animation: checkIn 0.3s ease;
}

@keyframes checkIn {
  from {
    transform: scale(0);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

/* 底部按钮 */
.status-dialog-footer {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding-top: 12px;

  .btn-cancel,
  .btn-confirm {
    width: 120px;
    height: 40px;
    border-radius: 20px;
    font-size: 14px;
    font-weight: 500;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;
  }

  .btn-cancel {
    border: 1.5px solid #dcdcdc;
    color: #666;
    background: #fff;

    &:hover {
      border-color: #bbb;
      color: #333;
      background: #f5f5f5;
    }
  }

  .btn-confirm {
    background: linear-gradient(135deg, #918acd, #a59de0);
    border: none;
    color: #fff;
    letter-spacing: 0.3px;
    box-shadow: 0 4px 12px rgba(145, 138, 205, 0.35);

    &:hover {
      background: linear-gradient(135deg, #867fc8, #9b92dc);
      box-shadow: 0 6px 16px rgba(145, 138, 205, 0.45);
      transform: translateY(-1px);
    }
  }
}

.navbar {
  .el-badge__content.is-fixed {
    top: 24px;
    right: 2px;
    width: 18px;
    height: 18px;
    font-size: 10px;
    line-height: 16px;
    border-radius: 50%;
    padding: 0;
  }

  .badgeW {
    .el-badge__content.is-fixed {
      width: 30px;
      border-radius: 20px;
    }
  }
}

.el-icon-arrow-down {
  background: url('./../../../assets/icons/up.png') no-repeat 50% 50%;
  background-size: contain;
  width: 8px;
  height: 8px;
  transform: rotate(0deg);
  margin-left: 16px;
  position: absolute;
  right: 16px;
  top: 12px;

  &:before {
    content: '';
  }
}

.userInfo {
  background: #fff;
  position: absolute;
  top: 0px;
  left: 0;
  z-index: 99;
  box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.14);
  width: 100%;
  border-radius: 4px;
  line-height: 32px;
  padding: 0 0 5px;
  height: 105px;

  .userList {
    width: 95%;
    padding-left: 5px;
  }

  p {
    cursor: pointer;
    height: 32px;
    line-height: 32px;
    padding: 0 5px 0 7px;

    i {
      margin-left: 10px;
      vertical-align: middle;
      margin-top: 4px;
      float: right;
    }

    &:hover {
      background: #f6f1e1;
    }
  }
}

.msgTip {
  color: #419eff;
  padding: 0 5px;
}
</style>
