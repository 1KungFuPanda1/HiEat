import { defineComponent } from 'vue'
import { getShopInfo, getToken } from '@/utils/cookies'
import { useUserStore } from '@/stores/user'
import { updateShopInfo } from '@/api/shop'

export default defineComponent({
  name: 'MyShop',

  data() {
    return {
      isEditing: false,
      originalShopInfo: {} as any,
      shopInfo: {} as any,
      headers: { token: '' },
      rules: {
        shopName: [
          { required: true, message: '请输入店铺名称', trigger: 'blur' },
          { min: 2, max: 30, message: '长度在 2 到 30 个字符', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入联系电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ],
        address: [{ required: true, message: '请输入店铺地址', trigger: 'blur' }],
        businessLicense: [{ required: true, message: '请上传营业执照', trigger: 'change' }],
        minFee: [{ required: true, message: '请输入起送费用', trigger: 'blur' }],
        deliverFee: [{ required: true, message: '请输入配送费', trigger: 'blur' }]
      },
      confirmDialogVisible: false,
      editDialogVisible: false,
      editForm: {} as any,
      imagePreviewVisible: false,
      previewImageUrl: ''
    }
  },

  computed: {
    displayMinFee: {
      get(): number { return this.shopInfo.minFee ? this.shopInfo.minFee / 100 : 0 },
      set(value: number) { this.shopInfo.minFee = Math.round(value * 100) }
    },
    displayDeliverFee: {
      get(): number { return this.shopInfo.deliverFee ? this.shopInfo.deliverFee / 100 : 0 },
      set(value: number) { this.shopInfo.deliverFee = Math.round(value * 100) }
    },
    displayEditMinFee: {
      get(): number { return this.editForm.minFee ? this.editForm.minFee / 100 : 0 },
      set(value: number) { this.editForm.minFee = Math.round(value * 100) }
    },
    displayEditDeliverFee: {
      get(): number { return this.editForm.deliverFee ? this.editForm.deliverFee / 100 : 0 },
      set(value: number) { this.editForm.deliverFee = Math.round(value * 100) }
    }
  },

  created() { this.fetchShopInfo() },

  methods: {
    fetchShopInfo() {
      this.shopInfo = JSON.parse(getShopInfo() || '{}')
      this.headers.token = getToken() || ''
      this.originalShopInfo = JSON.parse(getShopInfo() || '{}')
    },

    startEditing() {
      this.editForm = JSON.parse(JSON.stringify(this.shopInfo))
      if (!this.editForm.shopImages) this.editForm.shopImages = []
      this.editDialogVisible = true
    },

    cancelEditing() {
      this.isEditing = false
      this.shopInfo = JSON.parse(JSON.stringify(this.originalShopInfo))
    },

    cancelEdit() {
      this.editDialogVisible = false
      this.editForm = {}
    },

    submitEdit() {
      (this.$refs.editForm as any).validate((valid: boolean) => {
        if (valid) {
          this.shopInfo = JSON.parse(JSON.stringify(this.editForm))
          this.shopInfo.minFee = Math.round(this.displayEditMinFee * 100)
          this.shopInfo.deliverFee = Math.round(this.displayEditDeliverFee * 100)
          this.editDialogVisible = false
          this.saveShopInfoDirectly()
        } else { this.$message.warning('请填写完整的店铺信息') }
      })
    },

    async saveShopInfoDirectly() {
      updateShopInfo(this.shopInfo)
        .then(res => {
          if (res.data.code === 1) {
            this.$message.success('店铺信息修改成功！')
            useUserStore().ResetShopInfo()
            this.isEditing = false
          } else { this.$message.error('店铺信息修改失败！') }
        })
    },

    saveShopInfo() {
      this.confirmDialogVisible = false
      this.saveShopInfoDirectly()
    },

    confirmSave() {
      (this.$refs.shopInfoForm as any).validate((valid: boolean) => {
        if (valid) { this.confirmDialogVisible = true }
        else { this.$message.warning('请填写完整的店铺信息') }
      })
    },

    handleLogoSuccess(res: any) {
      if (res.code === 1) { this.shopInfo.image = res.data }
      else { this.$message.error(res.message || 'Logo上传失败') }
    },

    handleEditLogoSuccess(res: any) {
      if (res.code === 1) { this.editForm.image = res.data }
      else { this.$message.error(res.message || 'Logo上传失败') }
    },

    beforeLogoUpload(file: File) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
      const isLt2M = file.size / 1024 / 1024 < 2
      if (!isJPG) { this.$message.error('上传Logo只能是 JPG/PNG 格式!') }
      if (!isLt2M) { this.$message.error('上传Logo大小不能超过 2MB!') }
      return isJPG && isLt2M
    },

    handleLicenseSuccess(res: any) {
      if (res.code === 1) { this.shopInfo.businessLicense = res.data }
      else { this.$message.error(res.message || '营业执照上传失败') }
    },

    handleEditLicenseSuccess(res: any) {
      if (res.code === 1) { this.editForm.businessLicense = res.data }
      else { this.$message.error(res.message || '营业执照上传失败') }
    },

    beforeLicenseUpload(file: File) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
      const isLt5M = file.size / 1024 / 1024 < 5
      if (!isJPG) { this.$message.error('上传营业执照只能是 JPG/PNG 格式!') }
      if (!isLt5M) { this.$message.error('上传营业执照大小不能超过 5MB!') }
      return isJPG && isLt5M
    },

    previewImage(url: string) {
      if (!this.isEditing) { this.previewImageUrl = url; this.imagePreviewVisible = true }
    },

    beforePhotoUpload(file: File) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
      const isLt5M = file.size / 1024 / 1024 < 5
      if (!isJPG) { this.$message.error('上传照片只能是 JPG/PNG 格式!') }
      if (!isLt5M) { this.$message.error('上传照片大小不能超过 5MB!') }
      return isJPG && isLt5M
    },

    handleShopPhotoSuccess(res: any) {
      if (res.code === 1) {
        if (!this.editForm.shopImages) this.editForm.shopImages = []
        this.editForm.shopImages.push(res.data)
      } else { this.$message.error(res.message || '照片上传失败') }
    },

    removeShopPhoto(index: number) { this.editForm.shopImages.splice(index, 1) }
  }
})
