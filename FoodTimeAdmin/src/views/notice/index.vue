<template>
  <div class="notice-container">
    <div class="page-header">
      <h2>系统通知管理</h2>
      <el-button type="primary" @click="handleAdd">新增通知</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="noticeList" border style="width: 100%">
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="content" label="通知内容" min-width="200" show-overflow-tooltip />
      <el-table-column label="生效时间" width="160" align="center">
        <template #default="{ row }">
          <span>{{ formatDate(row.effectiveTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="失效时间" width="160" align="center">
        <template #default="{ row }">
          <span>{{ formatDate(row.expireTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="duration" label="持续时间(分钟)" width="130" align="center" />
      <el-table-column label="创建时间" width="160" align="center">
        <template #default="{ row }">
          <span>{{ formatDate(row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleEdit(row)" circle>
            <el-icon>
              <Edit />
            </el-icon>
          </el-button>
          <el-popconfirm title="确定要删除该通知吗？" @confirm="handleDelete(row.id)">
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑通知' : '新增通知'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="通知内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入通知内容" />
        </el-form-item>
        <el-form-item label="生效时间" prop="effectiveTime">
          <el-date-picker v-model="form.effectiveTime" type="datetime" placeholder="选择生效时间" format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>

        <el-form-item label="时间设置">
          <el-radio-group v-model="timeType" @change="handleTimeTypeChange">
            <el-radio :label="1">指定失效时间</el-radio>
            <el-radio :label="2">指定持续时间</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="timeType === 1" label="失效时间" prop="expireTime">
          <el-date-picker v-model="form.expireTime" type="datetime" placeholder="选择失效时间" format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>

        <el-form-item v-if="timeType === 2" label="持续时间" prop="duration">
          <el-input-number v-model="form.duration" :min="1" :step="10" placeholder="请输入持续时间(分钟)" style="width: 100%" />
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete } from '@element-plus/icons-vue'
import {
  getNoticePage,
  getNoticeById,
  addNotice,
  updateNotice,
  deleteNotice
} from '@/api/notice'

// 查询参数
const queryParams = reactive({
  page: 1,
  pageSize: 10
})

// 数据列表
const noticeList = ref([])
const total = ref(0)
const loading = ref(false)

// 弹窗表单相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const timeType = ref(1) // 1-失效时间，2-持续时间

const form = reactive({
  id: null,
  content: '',
  effectiveTime: '',
  expireTime: '',
  duration: 60
})

const rules = {
  content: [
    { required: true, message: '请输入通知内容', trigger: 'blur' }
  ],
  effectiveTime: [
    { required: true, message: '请选择生效时间', trigger: 'change' }
  ],
  expireTime: [
    { required: timeType.value === 1, message: '请选择失效时间', trigger: 'change' }
  ],
  duration: [
    { required: timeType.value === 2, message: '请输入持续时间', trigger: 'change' }
  ]
}

// 根据timeType计算验证规则
const computedRules = computed(() => {
  return {
    ...rules,
    expireTime: [
      { required: timeType.value === 1, message: '请选择失效时间', trigger: 'change' }
    ],
    duration: [
      { required: timeType.value === 2, message: '请输入持续时间', trigger: 'change' }
    ]
  }
})

// 初始化
onMounted(() => {
  fetchNoticeList()
})

// 获取通知列表
const fetchNoticeList = async () => {
  loading.value = true
  try {
    const res = await getNoticePage(queryParams)
    noticeList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('获取通知列表失败', error)
    ElMessage.error('获取通知列表失败')
  } finally {
    loading.value = false
  }
}

// 处理分页大小变化
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  fetchNoticeList()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  queryParams.page = val
  fetchNoticeList()
}

// 新增通知
const handleAdd = () => {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
  timeType.value = 1 // 默认使用失效时间
}

// 编辑通知
const handleEdit = async (row) => {
  resetForm()
  isEdit.value = true
  dialogVisible.value = true

  // 查询详情
  try {
    const res = await getNoticeById(row.id)
    const noticeData = res.data

    // 设置时间类型
    if (noticeData.expireTime && !noticeData.duration) {
      timeType.value = 1
    } else if (noticeData.duration) {
      timeType.value = 2
    }

    Object.assign(form, noticeData)
  } catch (error) {
    console.error('获取通知详情失败', error)
    ElMessage.error('获取通知详情失败')
  }
}

// 删除通知
const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该通知吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteNotice(id)
      ElMessage.success('删除成功')
      fetchNoticeList()
    } catch (error) {
      console.error('删除通知失败', error)
      ElMessage.error('删除通知失败')
    }
  }).catch(() => { })
}

// 切换时间类型
const handleTimeTypeChange = (val) => {
  if (val === 1) {
    form.duration = null
  } else {
    form.expireTime = null
    if (!form.duration) {
      form.duration = 60
    }
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return

  // 根据timeType清空不需要的字段
  if (timeType.value === 1) {
    form.duration = null
  } else {
    form.expireTime = null
  }

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updateNotice(form)
        ElMessage.success('编辑成功')
      } else {
        await addNotice(form)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      fetchNoticeList()
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
    content: '',
    effectiveTime: '',
    expireTime: '',
    duration: 60
  })
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr
}
</script>

<style lang="scss" scoped>
.notice-container {
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
}
</style>