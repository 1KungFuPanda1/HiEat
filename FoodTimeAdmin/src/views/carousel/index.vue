<template>
  <div class="carousel-container">
    <div class="page-header">
      <h2>轮播图管理</h2>
      <el-button type="primary" @click="handleAdd">新增轮播图</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="carouselList" border style="width: 100%">
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column label="轮播图" width="200" align="center">
        <template #default="{ row }">
          <el-image v-if="row.imageUrl" :src="row.imageUrl" fit="cover"
            style="width: 120px; height: 60px; border-radius: 4px;" :preview-src-list="[row.imageUrl]" />
          <el-icon v-else>
            <Picture />
          </el-icon>
        </template>
      </el-table-column>
      <el-table-column label="关联店铺" min-width="180">
        <template #default="{ row }">
          <div v-if="row.shop">
            <div class="shop-info">
              <el-image v-if="row.shop.image" :src="row.shop.image" class="shop-image" />
              <div class="shop-detail">
                <div class="shop-name">{{ row.shop.shopName }}</div>
                <div class="shop-address">{{ row.shop.address }}</div>
              </div>
            </div>
          </div>
          <span v-else>未关联店铺</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleEdit(row)" circle>
            <el-icon>
              <Edit />
            </el-icon>
          </el-button>
          <el-popconfirm title="确定要删除该轮播图吗？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button type="danger" size="small" circle>
                <el-icon>
                  <Delete />
                </el-icon>
              </el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination v-model:current-page="queryParams.page" v-model:page-size="queryParams.pageSize" :total="total"
        :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
        @current-change="handleCurrentChange" />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑轮播图' : '新增轮播图'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="轮播图" prop="imageUrl">
          <el-upload class="carousel-uploader" action="/api/admin/common/upload" :show-file-list="false"
            :on-success="handleUploadSuccess">
            <img v-if="form.imageUrl" :src="form.imageUrl" class="carousel-image" />
            <el-icon v-else class="carousel-uploader-icon">
              <Plus />
            </el-icon>
          </el-upload>
          <div class="upload-tip">点击上传图片，建议比例16:9</div>
        </el-form-item>

        <el-form-item label="关联店铺" prop="shopId">
          <div class="shop-id-input">
            <el-input-number v-model="form.shopId" :min="1" placeholder="请输入店铺ID" style="width: 100%"
              :controls="false" />
            <el-button type="default" @click="form.shopId = null" size="small">清空</el-button>
          </div>
          <div class="form-tip">如果需要关联店铺，请输入店铺ID，否则可不填</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete, Picture, Plus } from '@element-plus/icons-vue'
import {
  getCarouselPage,
  getCarouselById,
  addCarousel,
  updateCarousel,
  deleteCarousel
} from '@/api/carousel'

// 查询参数
const queryParams = reactive({
  page: 1,
  pageSize: 10
})

// 数据列表
const carouselList = ref([])
const total = ref(0)
const loading = ref(false)

// 弹窗表单相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  imageUrl: '',
  shopId: null
})

const rules = {
  imageUrl: [
    { required: true, message: '请上传轮播图片', trigger: 'change' }
  ]
}

// 初始化
onMounted(() => {
  fetchCarouselList()
})

// 获取轮播图列表
const fetchCarouselList = async () => {
  loading.value = true
  try {
    const res = await getCarouselPage(queryParams)
    carouselList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('获取轮播图列表失败', error)
    ElMessage.error('获取轮播图列表失败')
  } finally {
    loading.value = false
  }
}

// 处理分页大小变化
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  fetchCarouselList()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  queryParams.page = val
  fetchCarouselList()
}

// 新增轮播图
const handleAdd = () => {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

// 编辑轮播图
const handleEdit = async (row) => {
  resetForm()
  isEdit.value = true
  dialogVisible.value = true

  // 查询详情
  try {
    const res = await getCarouselById(row.id)
    Object.assign(form, res.data)
  } catch (error) {
    console.error('获取轮播图详情失败', error)
    ElMessage.error('获取轮播图详情失败')
  }
}

// 删除轮播图
const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该轮播图吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCarousel(id)
      ElMessage.success('删除成功')
      fetchCarouselList()
    } catch (error) {
      console.error('删除轮播图失败', error)
      ElMessage.error('删除轮播图失败')
    }
  }).catch(() => { })
}

// 上传成功回调
const handleUploadSuccess = (res) => {
  if (res.code === 1 && res.data) {
    form.imageUrl = res.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateCarousel(form)
        ElMessage.success('编辑成功')
      } else {
        await addCarousel(form)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      fetchCarouselList()
    } catch (error) {
      console.error('保存失败', error)
      // 后端返回的业务异常：店铺不存在，直接提示后端消息
      if (error && error.msg) {
        ElMessage.error(error.msg) // 会弹出：关联的店铺不存在
      } else {
        ElMessage.error('保存失败')
      }
    } finally {
      submitLoading.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(form, {
    id: null,
    imageUrl: '',
    shopId: null
  })
}
</script>

<style lang="scss" scoped>
.carousel-container {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  height: 100%;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }
  }

  .pagination-container {
    display: flex;
    justify-content: center;
    margin-top: 20px;
  }

  .shop-info {
    display: flex;
    align-items: center;

    .shop-image {
      width: 40px;
      height: 40px;
      border-radius: 4px;
      margin-right: 10px;
    }

    .shop-detail {
      .shop-name {
        font-weight: 500;
        line-height: 1.4;
      }

      .shop-address {
        font-size: 12px;
        color: #909399;
      }
    }
  }

  .carousel-uploader {
    width: 360px;
    height: 180px;
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;

    &:hover {
      border-color: #409EFF;
    }

    .carousel-image {
      width: 360px;
      height: 180px;
      display: block;
    }

    .carousel-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 360px;
      height: 180px;
      line-height: 180px;
      text-align: center;
    }
  }

  .upload-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 8px;
  }

  .shop-id-input {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 8px;
  }
}
</style>