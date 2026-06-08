<template>
  <div class="shop-container">
    <!-- 标签筛选区域 -->
    <div class="filter-area">
      <div class="tabs-container">
        <!--activeTab绑定name-->
        <el-tabs v-model="activeTab" @tab-click="handleTabChange">
          <el-tab-pane label="全部店铺" name="all">
            <template #label>
              <span>全部店铺</span>
            </template>
          </el-tab-pane>
          <el-tab-pane label="待审核" name="pending">
            <template #label>
              <span>待审核</span>
            </template>
          </el-tab-pane>
          <el-tab-pane label="已通过" name="approved">
            <template #label>
              <span>已通过</span>
            </template>
          </el-tab-pane>
          <el-tab-pane label="已拒绝" name="rejected">
            <template #label>
              <span>已拒绝</span>
            </template>
          </el-tab-pane>
        </el-tabs>
      </div>
      
      <!--搜索栏-->
      <div class="search-container">
        <el-input v-model="searchQuery" placeholder="搜索店铺名称/店主/电话" clearable class="search-input">
          <template #prefix>
            <el-icon>
              <Search />
            </el-icon>
          </template>
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>

        <el-button type="success" plain size="default" @click="exportData" class="export-btn">
          <el-icon>
            <Download />
          </el-icon>导出数据
        </el-button>
      </div>
    </div>

    <!-- 表格区域 -->
    <el-card class="table-card">
      <el-table :data="tableData" style="width: 100%" :stripe="true" border v-loading="loading"
        element-loading-text="加载中...">
<!--prop="id"：用来绑定当前行数据对象里的 id 字段，表格会自动读取 tableData 每一行的 id 值，渲染到这一列。-->
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="店铺Logo" width="100">
          <template #default="scope">
            <el-avatar :size="50" :src="scope.row.image" />
          </template>
        </el-table-column>
        <el-table-column prop="shopName" label="店铺名称" min-width="120" />
        <el-table-column prop="owner" label="店主姓名" width="120" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="address" label="店铺地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="auditStatus" label="审核状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.auditStatus)" effect="light">
              {{ getStatusText(scope.row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button size="small" type="primary" @click="handleView(scope.row)">
                <el-icon>
                  <View />
                </el-icon>查看
              </el-button>
              <el-button v-if="scope.row.auditStatus !== 1" size="small" type="success"
                @click="handleApprove(scope.row)">
                <el-icon>
                  <Check />
                </el-icon>{{ scope.row.auditStatus === 0 ? '通过' : '重新审核通过' }}
              </el-button>
              <el-button v-if="scope.row.auditStatus !== 2" size="small" type="danger" @click="handleReject(scope.row)">
                <el-icon>
                  <Close />
                </el-icon>{{ scope.row.auditStatus === 0 ? '拒绝' : '重新拒绝' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination background layout="total, sizes, prev, pager, next, jumper" :total="total" :page-size="pageSize"
          :current-page="currentPage" :page-sizes="[10, 20, 50, 100]" @size-change="handleSizeChange"
          @current-change="handleCurrentChange" />
      </div>
    </el-card>

    <!-- 店铺详情对话框 -->
    <el-dialog title="店铺详情" v-model="dialogVisible" width="60%" top="5vh" :destroy-on-close="true"
      class="shop-detail-dialog">
      <div v-if="currentShop" class="shop-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="店铺名称" label-class-name="label-bold">
            {{ currentShop.shopName }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话" label-class-name="label-bold">
            {{ currentShop.phone }}
          </el-descriptions-item>
          <el-descriptions-item label="店铺地址" label-class-name="label-bold">
            {{ currentShop.address }}
          </el-descriptions-item>
          <el-descriptions-item label="申请时间" label-class-name="label-bold">
            {{ currentShop.createTime }}
          </el-descriptions-item>
          <el-descriptions-item label="审核状态" label-class-name="label-bold">
            <el-tag :type="getStatusType(currentShop.auditStatus)" effect="light">
              {{ getStatusText(currentShop.auditStatus) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-section">
          <h3 class="section-title">店铺描述</h3>
          <p class="description-text">{{ currentShop.des }}</p>
        </div>

        <div class="detail-section">
          <h3 class="section-title">店铺照片</h3>
          <div class="shop-images">
            <el-image v-for="(img, index) in currentShop.shopImages" :key="index" :src="img" fit="cover"
              :preview-src-list="currentShop.shopImages" class="shop-image" />
          </div>
        </div>

        <div class="detail-section">
          <h3 class="section-title">营业执照</h3>
          <el-image :src="currentShop.businessLicense" style="width: 300px; height: auto"
            :preview-src-list="[currentShop.businessLicense]" class="license-image" />
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button v-if="currentShop && currentShop.auditStatus !== 1" type="success"
            @click="handleApprove(currentShop)">
            <el-icon>
              <Check />
            </el-icon>{{ currentShop.auditStatus === 0 ? '通过审核' : '重新审核通过' }}
          </el-button>
          <el-button v-if="currentShop && currentShop.auditStatus !== 2" type="danger"
            @click="handleReject(currentShop)">
            <el-icon>
              <Close />
            </el-icon>{{ currentShop.auditStatus === 0 ? '拒绝申请' : '重新拒绝' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 拒绝原因对话框 -->
    <el-dialog title="拒绝原因" v-model="rejectDialogVisible" width="30%" :destroy-on-close="true" class="reject-dialog">
      <el-form :model="rejectForm" :rules="rejectRules" ref="rejectFormRef">
        <el-form-item label="拒绝原因" prop="reason">
          <el-input v-model="rejectForm.reason" type="textarea" rows="4" placeholder="请输入拒绝原因" maxlength="200"
            show-word-limit></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmReject">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Shop,
  Timer,
  CircleCheck,
  CircleClose,
  Search,
  Refresh,
  Download,
  View,
  Check,
  Close
} from '@element-plus/icons-vue'

// 引入API函数
import { getShopAuditList, auditShop, exportShopData } from '@/api/shop'

// 加载状态
const loading = ref(false)

// 标签页和搜索
const activeTab = ref('all')
const searchQuery = ref('')

// 分页
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)

// 表格数据
const tableData = ref([])

// 当前选中的店铺
const currentShop = ref(null)
// 对话框显示状态
const dialogVisible = ref(false)
// 拒绝原因对话框显示状态
const rejectDialogVisible = ref(false)
// 拒绝表单
const rejectForm = reactive({
  reason: ''
})
// 拒绝表单规则 trigger: 'blur'什么时候触发验证？→ 鼠标离开输入框时验证
const rejectRules = {
  reason: [
    { required: true, message: '请输入拒绝原因', trigger: 'blur' },
    { min: 5, message: '拒绝原因不能少于5个字符', trigger: 'blur' }
  ]
}
const rejectFormRef = ref(null)
// 待拒绝的店铺
const shopToReject = ref(null)

// 页面加载
onMounted(() => {
  fetchData()
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    // 获取审核状态值
    let auditStatus = null
    if (activeTab.value !== 'all') {
      switch (activeTab.value) {
        case 'pending':
          auditStatus = 0
          break
        case 'approved':
          auditStatus = 1
          break
        case 'rejected':
          auditStatus = 2
          break
      }
    }
    // 调用API函数获取数据
    const res = await getShopAuditList({
      page: currentPage.value,
      pageSize: pageSize.value,
      auditStatus: auditStatus,
      keyword: searchQuery.value || undefined
    })
    if (res && res.code === 1) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
      console.log(tableData.value)
    } else {
      ElMessage.error(res.msg || '获取数据失败')
    }
  } catch (error) {
    console.error('获取店铺列表失败', error)
    ElMessage.error('获取店铺列表失败')
  } finally {
    loading.value = false
  }
}

// 处理标签切换
const handleTabChange = () => {
  currentPage.value = 1
  // 确保在下一个事件循环中执行fetchData，避免可能的状态更新问题
  setTimeout(() => {
    fetchData()
  }, 0)
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

// 导出数据
const exportData = async () => {
  try {
    // 获取审核状态值
    let auditStatus = null
    if (activeTab.value !== 'all') {
      switch (activeTab.value) {
        case 'pending':
          auditStatus = 0
          break
        case 'approved':
          auditStatus = 1
          break
        case 'rejected':
          auditStatus = 2
          break
      }
    }

    const res = await exportShopData({
      auditStatus: auditStatus,
      keyword: searchQuery.value || undefined
    })

    // 处理文件下载
    const blob = new Blob([res], { type: 'application/vnd.ms-excel' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `店铺数据_${new Date().toLocaleDateString()}.xlsx`
    link.click()
    URL.revokeObjectURL(link.href)

    ElMessage.success('数据导出成功')
  } catch (error) {
    console.error('导出数据失败', error)
    ElMessage.error('导出数据失败')
  }
}

// 分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchData()
}

// 页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchData()
}

// 查看店铺详情
const handleView = (row) => {
  currentShop.value = row
  dialogVisible.value = true
}

// 通过审核
const handleApprove = (row) => {
  const isReaudit = row.auditStatus !== 0
  const confirmMessage = isReaudit
    ? `确定要将该店铺状态修改为"已通过"吗？`
    : '确定要通过该店铺的入驻申请吗？'

  ElMessageBox.confirm(confirmMessage, '确认信息', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        // 调用审核API
        const res = await auditShop({
          shopId: row.id,
          auditStatus: 1, // 通过状态
          auditOpinion: isReaudit ? '重新审核通过' : '审核通过'
        })

        if (res && res.code === 1) {
          ElMessage.success(isReaudit ? '已将店铺状态修改为"已通过"' : '审核已通过')
          dialogVisible.value = false
          fetchData() // 刷新数据
        } else {
          ElMessage.error(res.msg || '操作失败')
        }
      } catch (error) {
        console.error('审核操作失败', error)
        ElMessage.error('审核操作失败')
      }
    })
    .catch(() => {
      // 取消操作
    })
}

// 确认拒绝
const confirmReject = async () => {
  rejectFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 调用审核API
        const res = await auditShop({
          shopId: shopToReject.value.id,
          auditStatus: 2, // 拒绝状态
          auditOpinion: rejectForm.reason
        })

        if (res && res.code === 1) {
          ElMessage.success('已拒绝该店铺的申请')
          rejectDialogVisible.value = false
          dialogVisible.value = false
          fetchData() // 刷新数据
        } else {
          ElMessage.error(res.msg || '操作失败')
        }
      } catch (error) {
        console.error('拒绝操作失败', error)
        ElMessage.error('拒绝操作失败')
      }
    }
  })
}

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 0: // 待审核
      return 'warning'
    case 1: // 已通过
      return 'success'
    case 2: // 已拒绝
      return 'danger'
    default:
      return 'info'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 0:
      return '待审核'
    case 1:
      return '已通过'
    case 2:
      return '已拒绝'
    default:
      return '未知'
  }
}

// 拒绝审核
const handleReject = (row) => {
  shopToReject.value = row
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}
</script>

<style lang="scss" scoped>
.shop-container {
  background-color: #f5f7fa;
  // padding: 12px;

  .filter-area {
    background-color: #ffffff;
    border-radius: 4px;
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
    margin-bottom: 10px;
    padding: 0 0 16px 0;

    .tabs-container {
      padding: 0 16px;

      :deep(.el-tabs__header) {
        margin-bottom: 0;
      }

      :deep(.el-tabs__nav-wrap::after) {
        height: 1px;
      }
    }

    .search-container {
      display: flex;
      align-items: center;
      padding: 16px;
      border-top: 1px solid #f0f0f0;

      .search-input {
        max-width: 400px;
      }

      .export-btn {
        margin-left: 16px;
      }
    }
  }

  .table-card {
    margin-bottom: 12px;
    background-color: #ffffff;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-title {
        font-size: 16px;
        font-weight: bold;
      }
    }
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  :deep(.el-table) {
    border-radius: 4px;
    overflow: hidden;

    .el-table__header-wrapper {
      th {
        background-color: #f5f7fa;
        color: #606266;
        font-weight: bold;
      }
    }
  }

  .action-buttons {
    display: flex;
    flex-wrap: nowrap;
    gap: 5px;

    :deep(.el-button) {
      padding: 5px 10px;
      min-width: 70px;
    }
  }
}

.shop-detail-dialog {
  .shop-detail {
    .label-bold {
      font-weight: bold;
    }

    .detail-section {
      margin-top: 20px;

      .section-title {
        font-size: 16px;
        font-weight: bold;
        margin-bottom: 10px;
        padding-bottom: 8px;
        border-bottom: 1px solid #ebeef5;
      }

      .description-text {
        line-height: 1.6;
        color: #606266;
      }

      .shop-images {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;

        .shop-image {
          width: 120px;
          height: 120px;
          border-radius: 4px;
          object-fit: cover;
          cursor: pointer;
          transition: all 0.3s;

          &:hover {
            transform: scale(1.05);
            box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
          }
        }
      }

      .license-image {
        border-radius: 4px;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          transform: scale(1.02);
          box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        }
      }
    }
  }
}
</style>