<template>
  <el-dialog
    title="修改密码"
    v-model="dialogFormVisible"
    width="568px"
    class="pwdCon"
    @close="handlePwdClose()"
  >
    <el-form :model="form" label-width="85px" :rules="rules" ref="formRef">
      <el-form-item label="原始密码：" prop="oldPassword">
        <el-input
          v-model="form.oldPassword"
          type="password"
          placeholder="请输入"
        />
      </el-form-item>
      <el-form-item label="新密码：" prop="newPassword">
        <el-input
          v-model="form.newPassword"
          type="password"
          placeholder="6 - 20位密码，数字或字母，区分大小写"
        />
      </el-form-item>
      <el-form-item label="确认密码：" prop="affirmPassword">
        <el-input
          v-model="form.affirmPassword"
          type="password"
          placeholder="请输入"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handlePwdClose()">取 消</el-button>
        <el-button type="primary" @click="handleSave()">保 存</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { ElMessage } from 'element-plus'
import { editPassword } from '@/api/users'

export default defineComponent({
  name: 'Password',
  props: {
    dialogFormVisible: { type: Boolean, default: false }
  },
  emits: ['handleclose'],

  data() {
    const validatePwd = (_rule: any, value: any, callback: Function) => {
      const reg = /^[0-9A-Za-z]{6,20}$/
      if (!value) {
        callback(new Error('请输入'))
      } else if (!reg.test(value)) {
        callback(new Error('6 - 20位密码，数字或字母，区分大小写'))
      } else {
        callback()
      }
    }

    const validatePass2 = (_rule: any, value: any, callback: Function) => {
      if (!value) {
        callback(new Error('请再次输入密码'))
      } else if (value !== (this as any).form.newPassword) {
        callback(new Error('密码不一致，请重新输入密码'))
      } else {
        callback()
      }
    }

    return {
      form: {} as any,
      affirmPassword: '',
      rules: {
        oldPassword: [{ validator: validatePwd, trigger: 'blur' }],
        newPassword: [{ validator: validatePwd, trigger: 'blur' }],
        affirmPassword: [{ validator: validatePass2, trigger: 'blur' }]
      }
    }
  },

  methods: {
    handleSave() {
      const formRef = this.$refs.formRef as any
      if (!formRef) return
      formRef.validate(async (valid: boolean) => {
        if (valid) {
          const parnt = {
            oldPassword: (this as any).form.oldPassword,
            newPassword: (this as any).form.newPassword
          }
          await editPassword(parnt)
          this.$emit('handleclose')
          formRef.resetFields()
        } else {
          return false
        }
      })
    },

    handlePwdClose() {
      const formRef = this.$refs.formRef as any
      if (formRef) {
        formRef.resetFields()
      }
      this.$emit('handleclose')
    }
  }
})
</script>

<style lang="scss">
.navbar {
  .pwdCon {
    .el-dialog__body {
      padding-top: 60px;
      padding: 60px 100px 0;
    }
    .el-input__inner {
      padding: 0 12px;
    }
    .el-form-item {
      margin-bottom: 26px;
    }
    .el-form-item__label {
      text-align: left;
    }
    .el-dialog__footer {
      padding-top: 14px;
    }
  }
}
</style>
