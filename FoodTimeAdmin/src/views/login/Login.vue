<template>
  <div class="login-container">
    <div class="login-left">
      <img src="../../assets/images/background/background.png" alt="Login Background" class="login-image">
    </div>
    <div class="login-right">
      <div class="login-form">
        <h2>欢迎登录</h2>
        <form @submit.prevent="handleLogin">
          <div class="form-group">
            <input type="text" v-model="loginForm.username" placeholder="用户名" required>
          </div>
          <div class="form-group">
            <input type="password" v-model="loginForm.password" placeholder="密码" required>
          </div>
          <div class="form-options">
            <a href="#" @click.prevent="handleForgotPassword" class="forgot-password">
              忘记密码？
            </a>
          </div>
          <button type="submit" class="login-button">登录</button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loginForm = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  try {
    const response = await login(loginForm)
    if (response.code === 1) {
      // 使用新的login方法保存用户信息
      userStore.login({
        token: response.data.token,
        userInfo: {
          id: response.data.id,
          username: response.data.username,
          name: response.data.name
        }
      })
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error(response.msg || '登录失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  }
}

const handleForgotPassword = () => {
  // 实现忘记密码逻辑
  console.log('忘记密码')
}
</script>

<style lang="scss" scoped>
.login-container {
  display: flex;
  height: 100vh;
  width: 100vw;

  .login-left {
    flex: 6; // 修改这里，从 1 改为 6
    overflow: hidden;

    .login-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .login-right {
    flex: 4; // 修改这里，从 1 改为 4
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #fff;

    .login-form {
      width: 400px;
      padding: 40px;

      h2 {
        text-align: center;
        margin-bottom: 30px;
        color: #333;
        font-size: 24px;
      }

      .form-group {
        margin-bottom: 20px;

        input {
          width: 100%;
          padding: 12px;
          border: 1px solid #ddd;
          border-radius: 4px;
          font-size: 14px;

          &:focus {
            outline: none;
            border-color: #1890ff;
          }
        }
      }

      .form-options {
        text-align: right;
        margin-bottom: 20px;

        .forgot-password {
          color: #1890ff;
          text-decoration: none;
          font-size: 14px;

          &:hover {
            text-decoration: underline;
          }
        }
      }

      .login-button {
        width: 100%;
        padding: 12px;
        background-color: #1890ff;
        color: white;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        font-size: 16px;

        &:hover {
          background-color: #40a9ff;
        }
      }
    }
  }
}
</style>