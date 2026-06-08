<template>
  <div class="register">
    <div class="register-box">
      <img src="@/assets/login/login_background.png" alt="" />
      <div class="register-form">
        <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules">
          <el-form-item prop="username">
            <el-input v-model="registerForm.username" type="text" auto-complete="off" placeholder="用户名">
              <template #prefix><i class="iconfont icon-user"></i></template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="registerForm.password" type="password" placeholder="密码">
              <template #prefix><i class="iconfont icon-lock"></i></template>
            </el-input>
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码">
              <template #prefix><i class="iconfont icon-lock"></i></template>
            </el-input>
          </el-form-item>
          <el-form-item prop="name">
            <el-input v-model="registerForm.name" type="text" placeholder="责任人姓名">
              <template #prefix><i class="iconfont icon-id"></i></template>
            </el-input>
          </el-form-item>
          <el-form-item prop="phone">
            <el-input v-model="registerForm.phone" type="text" auto-complete="off" placeholder="联系电话">
              <template #prefix><i class="iconfont icon-phone"></i></template>
            </el-input>
          </el-form-item>
          <el-form-item prop="idNumber">
            <el-input v-model="registerForm.idNumber" type="text" placeholder="责任人身份证号">
              <template #prefix><i class="iconfont icon-id"></i></template>
            </el-input>
          </el-form-item>
          <el-form-item prop="categoryId">
            <el-select v-model="registerForm.categoryId" placeholder="请选择店铺分类" style="width: 100%">
              <el-option v-for="item in categoryList" :key="item.id" :label="item.name" :value="item.id">
                <div style="display: flex; align-items: center;">
                  <img v-if="item.image" :src="item.image" style="width: 20px; height: 20px; margin-right: 5px;">
                  <span>{{ item.name }}</span>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item prop="shopName">
            <el-input v-model="registerForm.shopName" type="text" placeholder="店铺名称">
              <template #prefix><i class="iconfont icon-shop"></i></template>
            </el-input>
          </el-form-item>
          <el-form-item prop="shopAddress">
            <el-input v-model="registerForm.shopAddress" type="text" placeholder="店铺地址">
              <template #prefix><i class="iconfont icon-address"></i></template>
            </el-input>
          </el-form-item>
          <div class="upload-container">
            <el-form-item prop="shopLogo" class="upload-item">
              <el-upload class="avatar-uploader" action="/api/common/upload/register" :show-file-list="false"
                :on-success="handleLogoSuccess" :before-upload="beforeLogoUpload">
                <img v-if="registerForm.shopLogo" :src="registerForm.shopLogo" class="avatar">
                <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                <div class="el-upload__text">店铺Logo</div>
              </el-upload>
            </el-form-item>
            <el-form-item prop="businessLicense" class="upload-item">
              <el-upload class="avatar-uploader" action="/api/common/upload/register" :show-file-list="false"
                :on-success="handleLicenseSuccess" :before-upload="beforeLicenseUpload">
                <img v-if="registerForm.businessLicense" :src="registerForm.businessLicense" class="avatar">
                <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                <div class="el-upload__text">营业执照</div>
              </el-upload>
            </el-form-item>
          </div>
          <el-form-item prop="shopPhotos">
            <el-upload action="/api/common/upload/register" list-type="picture-card"
              :on-preview="handlePictureCardPreview" :on-remove="handleRemove" :on-success="handleShopPhotoSuccess"
              :before-upload="beforeShopPhotoUpload" multiple>
              <i class="el-icon-plus"></i>
            </el-upload>
            <div class="upload-label">店铺照片</div>
            <el-dialog v-model="dialogVisible">
              <img width="100%" :src="dialogImageUrl" alt="">
            </el-dialog>
          </el-form-item>
          <el-form-item style="width: 100%">
            <el-button :loading="loading" class="reg-btn" size="medium" type="primary" style="width: 100%"
              @click.prevent="handleRegister">
              <span v-if="!loading">提交审核</span>
              <span v-else>提交中...</span>
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { register } from '@/api/employee'
import { getShopCategoryList } from '@/api/shop'

export default defineComponent({
  name: 'Register',

  data() {
    const validateUsername = (_rule: any, value: string, callback: Function) => {
      if (!value) { callback(new Error('请输入用户名')) }
      else if (value.length < 3 || value.length > 20) { callback(new Error('用户名长度应为3-20个字符')) }
      else { callback() }
    }
    const validatePassword = (_rule: any, value: string, callback: Function) => {
      if (!value) { callback(new Error('请输入密码')) }
      else if (value.length < 6) { callback(new Error('密码必须在6位以上')) }
      else {
        if ((this as any).registerForm.confirmPassword) {
          ((this as any).$refs.registerFormRef as any).validateField('confirmPassword')
        }
        callback()
      }
    }
    const validateConfirmPassword = (_rule: any, value: string, callback: Function) => {
      if (!value) { callback(new Error('请再次输入密码')) }
      else if (value !== (this as any).registerForm.password) { callback(new Error('两次输入密码不一致')) }
      else { callback() }
    }
    const validatePhone = (_rule: any, value: string, callback: Function) => {
      if (!value) { callback(new Error('请输入手机号')) }
      else if (!/^1[3-9]\d{9}$/.test(value)) { callback(new Error('请输入正确的手机号格式')) }
      else { callback() }
    }
    const validateName = (_rule: any, value: string, callback: Function) => {
      if (!value) { callback(new Error('请输入姓名')) }
      else if (value.length < 2 || value.length > 10) { callback(new Error('姓名长度应为2-10个字符')) }
      else { callback() }
    }
    const validateIdNumber = (_rule: any, value: string, callback: Function) => {
      if (!value) { callback(new Error('请输入身份证号')) }
      else if (!/^[1-9]\d{5}(19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/.test(value)) { callback(new Error('请输入正确的身份证号格式')) }
      else { callback() }
    }
    const validateShopName = (_rule: any, value: string, callback: Function) => {
      if (!value) { callback(new Error('请输入店铺名称')) }
      else if (value.length < 2 || value.length > 20) { callback(new Error('店铺名称长度应为2-20个字符')) }
      else { callback() }
    }
    const validateShopAddress = (_rule: any, value: string, callback: Function) => {
      if (!value) { callback(new Error('请输入店铺地址')) }
      else if (value.length < 5 || value.length > 50) { callback(new Error('店铺地址长度应为5-50个字符')) }
      else { callback() }
    }
    const validateShopLogo = (_rule: any, value: string, callback: Function) => {
      if (!value) { callback(new Error('请上传店铺Logo')) }
      else { callback() }
    }
    const validateBusinessLicense = (_rule: any, value: string, callback: Function) => {
      if (!value) { callback(new Error('请上传营业执照')) }
      else { callback() }
    }
    const validateCategoryId = (_rule: any, value: string, callback: Function) => {
      if (!value) { callback(new Error('请选择店铺分类')) }
      else { callback() }
    }
    const validateShopPhotos = (_rule: any, _value: any, callback: Function) => {
      if ((this as any).registerForm.shopPhotos && (this as any).registerForm.shopPhotos.length > 0) { callback() }
      else { callback(new Error('请上传至少一张店铺照片')) }
    }

    return {
      registerForm: {
        username: '', password: '', confirmPassword: '', name: '', phone: '',
        idNumber: '', shopName: '', shopAddress: '', shopLogo: '', businessLicense: '',
        shopPhotos: [] as string[], code: '', categoryId: ''
      },
      registerRules: {
        username: [{ validator: validateUsername, trigger: 'blur' }],
        password: [{ validator: validatePassword, trigger: 'blur' }],
        confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }],
        name: [{ validator: validateName, trigger: 'blur' }],
        phone: [{ validator: validatePhone, trigger: 'blur' }],
        idNumber: [{ validator: validateIdNumber, trigger: 'blur' }],
        shopName: [{ validator: validateShopName, trigger: 'blur' }],
        shopAddress: [{ validator: validateShopAddress, trigger: 'blur' }],
        shopLogo: [{ validator: validateShopLogo, trigger: 'change' }],
        businessLicense: [{ validator: validateBusinessLicense, trigger: 'change' }],
        shopPhotos: [{ validator: validateShopPhotos, trigger: 'change' }],
        categoryId: [{ validator: validateCategoryId, trigger: 'change' }]
      },
      loading: false,
      categoryList: [] as any[],
      dialogImageUrl: '',
      dialogVisible: false,
      isSend: false,
      waitTime: 60
    }
  },

  created() { this.loadShopCategories() },

  methods: {
    async loadShopCategories() {
      try {
        const res = await getShopCategoryList()
        if (res?.data?.code === 1) { this.categoryList = res.data.data || [] }
        else { this.$message.error('获取店铺分类失败') }
      } catch (error) { console.error('获取店铺分类出错:', error) }
    },

    handleLogoSuccess(response: any) {
      if (response?.code === 1) { this.registerForm.shopLogo = response.data; this.$message.success('Logo上传成功') }
      else { this.$message.error(response.msg || 'Logo上传失败') }
    },

    beforeLogoUpload(file: File) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
      const isLt2M = file.size / 1024 / 1024 < 2
      if (!isJPG) { this.$message.error('上传Logo图片只能是JPG或PNG格式!') }
      if (!isLt2M) { this.$message.error('上传Logo图片大小不能超过2MB!') }
      return isJPG && isLt2M
    },

    handleLicenseSuccess(response: any) {
      if (response?.code === 1) { this.registerForm.businessLicense = response.data; this.$message.success('LICENSE上传成功') }
      else { this.$message.error(response.msg || 'LICENSE上传失败') }
    },

    beforeLicenseUpload(file: File) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
      const isLt5M = file.size / 1024 / 1024 < 5
      if (!isJPG) { this.$message.error('上传LICENSE只能是JPG或PNG格式!') }
      if (!isLt5M) { this.$message.error('上传LICENSE大小不能超过5MB!') }
      return isJPG && isLt5M
    },

    handleShopPhotoSuccess(response: any) {
      if (response?.code === 1) {
        if (!this.registerForm.shopPhotos) this.registerForm.shopPhotos = []
        this.registerForm.shopPhotos.push(response.data)
        this.$nextTick(() => {
          ((this.$refs.registerFormRef as any)).validateField('shopPhotos')
        })
        this.$message.success('店铺照片上传成功')
      } else { this.$message.error(response.msg || '店铺照片上传失败') }
    },

    handleRemove(_file: any, fileList: any[]) {
      this.registerForm.shopPhotos = fileList.map((f: any) => f.url || f.response?.data)
    },

    beforeShopPhotoUpload(file: File) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
      const isLt2M = file.size / 1024 / 1024 < 2
      if (!isJPG) { this.$message.error('上传店铺照片只能是JPG或PNG格式!') }
      if (!isLt2M) { this.$message.error('上传店铺照片大小不能超过2MB!') }
      return isJPG && isLt2M
    },

    handlePictureCardPreview(file: any) {
      this.dialogImageUrl = file.url
      this.dialogVisible = true
    },

    handleRegister() {
      (this.$refs.registerFormRef as any).validate(async (valid: boolean) => {
        if (valid) {
          this.loading = true
          try {
            const { confirmPassword, ...submitData } = this.registerForm
            const result = await register(submitData)
            if (result?.data?.code === 1) {
              this.$message.success('注册成功，请等待管理员审核')
              this.$router.push('/login')
            } else {
              this.$message.error(result.data.msg || '注册失败，请重试')
            }
          } catch (error) {
            this.$message.error('注册失败，请重试')
          } finally { this.loading = false }
        }
      })
    }
  }
})
</script>

<style lang="scss">
.register {
  display: flex; justify-content: center; align-items: center;
  height: 100vh; width: 100vw; background-color: #333;
  margin: 0; padding: 0; position: fixed; top: 0; left: 0; overflow: hidden;
}
.register-box {
  width: 100%; height: 100vh; border-radius: 8px; display: flex; box-shadow: 0 0 20px rgba(0,0,0,0.2);
  img { width: 60%; height: 100%; object-fit: cover; border-radius: 8px 0 0 8px; }
}
.register-form {
  background: #ffffff; width: 40%; height: 100%; border-radius: 0px 8px 8px 0px;
  display: flex; flex-direction: column; justify-content: flex-start;
  align-items: center; padding: 20px; box-sizing: border-box; overflow-y: auto;
  .el-form { width: 80%; max-width: 300px; height: auto; margin-top: 20px; }
  .el-form-item { margin-bottom: 20px; }
  .el-form-item.is-error .el-input__inner { border: 0 !important; border-bottom: 1px solid #fd7065 !important; background: #fff !important; }
  .el-input__inner { border: 0; border-bottom: 1px solid #e9e9e8; border-radius: 0; font-size: 12px; color: #333333; height: 32px; line-height: 32px; }
  .el-input__prefix { left: 0; }
  .el-input--prefix .el-input__inner { padding-left: 26px; }
  .el-input__inner::placeholder { color: #aeb5c4; }
}
.reg-btn {
  border-radius: 17px; padding: 11px 20px !important; margin-top: 10px;
  font-weight: 500; font-size: 12px; border: 0; color: #333333; background-color: #918acd;
  &:hover, &:focus { background-color: #918acd; color: #f5f5f3; }
}
.upload-container { display: flex; justify-content: space-between; width: 100%; margin-bottom: 15px; }
.upload-item { width: 48%; margin-bottom: 0 !important; }
.upload-label { color: #606266; font-size: 12px; text-align: center; margin-top: 5px; }
.avatar-uploader {
  width: 100%; text-align: center;
  .el-upload { border: 1px dashed #d9d9d9; border-radius: 6px; cursor: pointer; overflow: hidden; width: 100%; height: 100px; display: flex; flex-direction: column; justify-content: center; align-items: center; }
  .el-upload:hover { border-color: #409EFF; }
  .avatar { width: 70%; height: 60px; object-fit: contain; margin: 0 auto; }
  .el-upload__text { color: #606266; font-size: 12px; text-align: center; margin-top: 5px; }
}
</style>
