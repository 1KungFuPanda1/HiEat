<template>
  <div class="login">
    <div class="login-box">
      <img src="@/assets/login/login_background.png" alt="" />
      <div class="login-form">
        <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules">
          <div class="login-form-title">
            <img src="@/assets/login/food_time_logo.png" style="width: 149px" alt="" />
          </div>
          <el-form-item prop="username">
            <el-input v-model="loginForm.username" type="text" auto-complete="off" placeholder="账号"
              @keyup.enter="handleLogin">
              <template #prefix>
                <i class="iconfont icon-user"></i>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="密码"
              @keyup.enter="handleLogin">
              <template #prefix>
                <i class="iconfont icon-lock"></i>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item style="width: 100%">
            <el-button :loading="loading" class="login-btn" size="medium" type="primary" style="width: 100%"
              @click.prevent="handleLogin">
              <span v-if="!loading">登录</span>
              <span v-else>登录中...</span>
            </el-button>
          </el-form-item>
        </el-form>
        <div class="join-area">
          <el-button type="primary" class="register-btn" @click="toRegister">我要入驻</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { useUserStore } from '@/stores/user'

export default defineComponent({
  name: 'Login',

  data() {
    const validateUsername = (_rule: any, value: string, callback: Function) => {
      if (!value) {
        callback(new Error('请输入用户名'))
      } else {
        callback()
      }
    }
    const validatePassword = (_rule: any, value: string, callback: Function) => {
      if (value.length < 6) {
        callback(new Error('密码必须在6位以上'))
      } else {
        callback()
      }
    }

    return {
      loginForm: {
        username: 'admin',
        password: '123456'
      },
      loginRules: {
        username: [{ validator: validateUsername, trigger: 'blur' }],
        password: [{ validator: validatePassword, trigger: 'blur' }]
      },
      loading: false
    }
  },

  methods: {
    handleLogin() {
      const loginFormRef = this.$refs.loginFormRef as any
      loginFormRef.validate(async (valid: boolean) => {
        if (valid) {
          this.loading = true
          const userStore = useUserStore()
          await userStore.Login(this.loginForm as any)
            .then((res: any) => {
              if (String(res.code) === '1') {
                this.$router.push('/')
              } else {
                this.loading = false
              }
            })
            .catch(() => {
              this.loading = false
            })
        } else {
          return false
        }
      })
    },

    toRegister() {
      this.$router.push('/register')
    }
  }
})
</script>

<style lang="scss">
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-color: #333;
}

.login-box {
  width: 1000px;
  height: 474.38px;
  border-radius: 8px;
  display: flex;
  img { width: 60%; height: auto; }
}

.login-form {
  background: #ffffff;
  width: 40%;
  border-radius: 0px 8px 8px 0px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  .el-form { width: 214px; height: 260px; }
  .el-form-item { margin-bottom: 30px; }
  .el-form-item.is-error .el-input__inner {
    border: 0 !important;
    border-bottom: 1px solid #fd7065 !important;
    background: #fff !important;
  }
  .el-input__inner {
    border: 0;
    border-bottom: 1px solid #e9e9e8;
    border-radius: 0;
    font-size: 12px;
    font-weight: 400;
    color: #333333;
    height: 32px;
    line-height: 32px;
  }
  .el-input__prefix { left: 0; }
  .el-input--prefix .el-input__inner { padding-left: 26px; }
  .el-input__inner::placeholder { color: #aeb5c4; }
  .el-form-item--medium .el-form-item__content { line-height: 32px; }
  .el-input--medium .el-input__icon { line-height: 32px; }

  .join-area {
    display: flex;
    margin-top: 10px;
    width: 214px;
    justify-content: space-around;

    .register-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 214px;
      height: 34px;
      border-radius: 17px;
      font-weight: 500;
      font-size: 12px;
      color: #333333;
    }
  }
}

.login-btn {
  border-radius: 17px;
  padding: 11px 20px !important;
  margin-top: 10px;
  font-weight: 500;
  font-size: 12px;
  border: 0;
  color: #333333;
  background-color: #918acd;
  &:hover, &:focus { background-color: #918acd; color: #f5f5f3; }
}

.login-form-title {
  height: 36px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 40px;
}
</style>
