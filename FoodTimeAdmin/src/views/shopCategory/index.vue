<template>
  <div class="shop-category-container">
    <div class="page-header">
      <h2>店铺分类管理</h2>
      <el-button type="primary" @click="handleAdd">新增分类</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="categoryList" border style="width: 100%">
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="name" label="分类名称" min-width="120" />
      <el-table-column label="分类图片" width="120" align="center">
        <template #default="{ row }">
          <el-image v-if="row.image" :src="row.image" fit="cover" style="width: 60px; height: 60px; border-radius: 4px;"
            :preview-src-list="[row.image]" />
          <el-icon v-else>
            <Picture />
          </el-icon>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" align="center">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleEdit(row)" circle>
            <el-icon>
              <Edit />
            </el-icon>
          </el-button>
          <el-button :type="row.status === 1 ? 'danger' : 'success'" size="small" @click="handleStatusChange(row)"
            circle>
            <el-icon v-if="row.status === 1">
              <Close />
            </el-icon>
            <el-icon v-else>
              <Check />
            </el-icon>
          </el-button>
          <el-popconfirm title="确定要删除该分类吗？" @confirm="handleDelete(row.id)">
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

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类图片" prop="image">
          <el-upload class="avatar-uploader" action="/api/admin/common/upload" :show-file-list="false"
            :on-success="handleUploadSuccess">
            <img v-if="form.image" :src="form.image" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon">
              <Plus />
            </el-icon>
          </el-upload>
          <div class="upload-tip">点击上传图片，建议尺寸200x200像素</div>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
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
import { Edit, Delete, Picture, Plus, Check, Close } from '@element-plus/icons-vue'
import {
  getShopCategoryList,
  getShopCategoryById,
  addShopCategory,
  updateShopCategory,
  deleteShopCategory,
  changeShopCategoryStatus
} from '@/api/shopCategory'

// 数据列表
const categoryList = ref([])
const loading = ref(false)

// 弹窗表单相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  image: '',
  status: 1
})

const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  image: [
    { required: true, message: '请上传分类图片', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 初始化
onMounted(() => {
  fetchCategoryList()
})

// 获取分类列表
const fetchCategoryList = async () => {
  loading.value = true
  try {
    const res = await getShopCategoryList()
    categoryList.value = res.data || []
  } catch (error) {
    console.error('获取分类列表失败', error)
    ElMessage.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

// 新增分类
const handleAdd = () => {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

// 编辑分类
const handleEdit = async (row) => {
  resetForm()
  isEdit.value = true
  dialogVisible.value = true

  // 查询详情
  try {
    const res = await getShopCategoryById(row.id)
    Object.assign(form, res.data)
  } catch (error) {
    console.error('获取分类详情失败', error)
    ElMessage.error('获取分类详情失败')
  }
}

// 删除分类
const handleDelete = async (id) => {
  try {
    await deleteShopCategory(id)
    ElMessage.success('删除成功')
    fetchCategoryList()
  } catch (error) {
    console.error('删除分类失败', error)
    ElMessage.error('删除分类失败')
  }
}

// 修改状态
const handleStatusChange = (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '禁用'

  ElMessageBox.confirm(`确定要${statusText}该分类吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await changeShopCategoryStatus(row.id, newStatus)
      ElMessage.success(`${statusText}成功`)
      fetchCategoryList()
    } catch (error) {
      console.error(`${statusText}失败`, error)
      ElMessage.error(`${statusText}失败`)
    }
  }).catch(() => { })
}

// 上传成功回调
const handleUploadSuccess = (res) => {
  if (res.code === 1 && res.data) {
    form.image = res.data
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
        await updateShopCategory(form)
        ElMessage.success('编辑成功')
      } else {
        await addShopCategory(form)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      fetchCategoryList()
    } catch (error) {
      console.error('保存失败', error)
      ElMessage.error('保存失败')
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
    name: '',
    image: '',
    status: 1
  })
}
</script>

<style lang="scss" scoped>
.shop-category-container {
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

  .avatar-uploader {
    width: 178px;
    height: 178px;
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;

    &:hover {
      border-color: #409EFF;
    }

    .avatar {
      width: 178px;
      height: 178px;
      display: block;
    }

    .avatar-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 178px;
      height: 178px;
      line-height: 178px;
      text-align: center;
    }
  }

  .upload-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 8px;
  }
}
</style>