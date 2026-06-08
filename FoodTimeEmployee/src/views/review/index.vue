<template>
  <div class="app-container">
    <div class="search-container">
      <el-form :model="searchForm" :inline="true" size="small">
        <el-form-item>
          <el-select v-model="searchForm.rating" placeholder="请选择评分" clearable style="width: 200px">
            <el-option label="全部评分" value="" />
            <el-option label="1星" :value="1" />
            <el-option label="2星" :value="2" />
            <el-option label="3星" :value="3" />
            <el-option label="4星" :value="4" />
            <el-option label="5星" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="searchForm.status" placeholder="评论状态" clearable style="width: 150px">
            <el-option label="所有状态" value="" />
            <el-option label="活跃" value="active" />
            <el-option label="隐藏" value="hidden" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table v-loading="loading" :data="reviewList" row-key="id" stripe border style="width: 100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="用户信息" width="150">
        <template v-slot="{ row }">
          <div class="user-info">
            <el-avatar v-if="row.userAvatar" :size="30" :src="row.userAvatar"></el-avatar>
            <el-avatar v-else :size="30" icon="el-icon-user"></el-avatar>
            <span class="user-id">ID: {{ row.userId }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="rating" label="评分" width="80">
        <template v-slot="{ row }">
          <el-rate v-model="row.rating" disabled show-score text-color="#ff9900" />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" width="120" />
      <el-table-column prop="content" label="评论内容" min-width="180" show-overflow-tooltip />
      <el-table-column label="图片" width="100">
        <template v-slot="{ row }">
          <el-image v-if="row.photoUrls && row.photoUrls.length > 0" style="width: 50px; height: 50px"
            :src="row.photoUrls[0]" :preview-src-list="row.photoUrls" />
          <span v-else>无图片</span>
        </template>
      </el-table-column>
      <el-table-column label="回复状态" width="100">
        <template v-slot="{ row }">
          <el-tag v-if="row.replies && row.replies.length > 0" type="success">已回复</el-tag>
          <el-tag v-else type="warning">待回复</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="150" />
      <el-table-column prop="status" label="状态" width="80">
        <template v-slot="{ row }">
          <el-tag v-if="row.status === 'active'" type="success">活跃</el-tag>
          <el-tag v-else-if="row.status === 'hidden'" type="info">隐藏</el-tag>
          <el-tag v-else type="info">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template v-slot="{ row }">
          <el-button type="text" size="small" @click="handleReply(row)">
            {{ row.replies && row.replies.length > 0 ? '查看/编辑回复' : '回复' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination :current-page="pagination.page" :page-sizes="[10, 20, 50, 100]"
        :page-size="pagination.pageSize" layout="total, sizes, prev, pager, next, jumper" :total="pagination.total"
        @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </div>

    <el-dialog :title="currentReview.replies && currentReview.replies.length > 0 ? '查看/编辑回复' : '回复评论'"
      v-model="replyDialogVisible" width="600px">
      <el-form :model="replyForm" ref="replyFormRef" :rules="replyRules" label-width="100px">
        <el-form-item label="用户评论">
          <div class="review-content">
            <div class="user-header">
              <el-avatar v-if="currentReview.userAvatar" :size="40" :src="currentReview.userAvatar"></el-avatar>
              <el-avatar v-else :size="40" icon="el-icon-user"></el-avatar>
              <div class="user-info-detail">
                <span>用户ID: {{ currentReview.userId }}</span>
                <span>时间: {{ currentReview.createTime }}</span>
              </div>
            </div>
            <div class="rating-info">
              <span>评分: </span>
              <el-rate v-model="currentReview.rating" disabled show-score text-color="#ff9900" />
            </div>
            <p v-if="currentReview.title"><strong>标题：</strong>{{ currentReview.title }}</p>
            <p><strong>内容：</strong>{{ currentReview.content }}</p>
            <div v-if="currentReview.photoUrls && currentReview.photoUrls.length > 0" class="photo-list">
              <p><strong>评论图片：</strong></p>
              <div class="photos">
                <el-image v-for="(url, index) in currentReview.photoUrls" :key="index"
                  style="width: 100px; height: 100px; margin-right: 10px;" :src="url"
                  :preview-src-list="currentReview.photoUrls" />
              </div>
            </div>
          </div>
        </el-form-item>
        <el-form-item v-if="currentReview.replies && currentReview.replies.length > 0" label="已有回复">
          <div class="existing-replies">
            <div v-for="(reply, index) in currentReview.replies" :key="index" class="reply-item">
              <p><strong>回复内容：</strong>{{ reply.content }}</p>
              <p><strong>回复时间：</strong>{{ reply.createTime }}</p>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="回复内容" prop="content">
          <el-input v-model="replyForm.content" type="textarea" :rows="4" placeholder="请输入回复内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="replyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReply">确认回复</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { getReviewPage, replyReview } from '@/api/review'
import { getShopId } from '@/utils/cookies'

export default defineComponent({
  name: 'ReviewManagement',

  data() {
    return {
      loading: false,
      reviewList: [] as any[],
      currentReview: {} as any,
      replyDialogVisible: false,
      searchForm: { rating: '', status: '' },
      replyForm: { reviewId: '', content: '' },
      replyRules: {
        content: [
          { required: true, message: '请输入回复内容', trigger: 'blur' },
          { min: 1, max: 500, message: '长度在 1 到 500 个字符', trigger: 'blur' }
        ]
      },
      pagination: { page: 1, pageSize: 10, total: 0 }
    }
  },

  mounted() { this.getReviewList() },

  methods: {
    async getReviewList() {
      this.loading = true
      try {
        const params = {
          page: this.pagination.page,
          pageSize: this.pagination.pageSize,
          rating: this.searchForm.rating,
          status: this.searchForm.status,
          shopId: getShopId()
        }
        const res = await getReviewPage(params)
        let apiData = res
        if (res?.status === 200 && res.data) apiData = res.data
        if (apiData?.code === 1 && apiData.data) {
          if (apiData.data.records && Array.isArray(apiData.data.records)) {
            this.reviewList = apiData.data.records
            this.pagination.total = apiData.data.total || 0
          } else if (Array.isArray(apiData.data)) {
            this.reviewList = apiData.data
            this.pagination.total = apiData.data.length
          }
        } else {
          this.reviewList = []
          this.pagination.total = 0
        }
      } catch (error) {
        console.error('获取评论列表异常:', error)
        this.reviewList = []
        this.pagination.total = 0
      } finally { this.loading = false }
    },

    handleSearch() { this.pagination.page = 1; this.getReviewList() },

    resetSearch() { this.searchForm = { rating: '', status: '' }; this.handleSearch() },

    handleSizeChange(val: number) { this.pagination.pageSize = val; this.getReviewList() },
    handleCurrentChange(val: number) { this.pagination.page = val; this.getReviewList() },

    handleReply(row: any) {
      this.currentReview = row
      this.replyForm.reviewId = row.id
      this.replyForm.content = ''
      this.replyDialogVisible = true
    },

    async submitReply() {
      const ref = this.$refs.replyFormRef as any
      ref.validate(async (valid: boolean) => {
        if (valid) {
          try {
            const res = await replyReview(this.replyForm)
            let apiData = res
            if (res?.status === 200 && res.data) apiData = res.data
            if (apiData?.code === 1) {
              this.$message.success('回复成功')
              this.replyDialogVisible = false
              this.getReviewList()
            } else {
              this.$message.error(apiData?.msg || '回复失败')
            }
          } catch (error) { this.$message.error('回复失败') }
        }
      })
    }
  }
})
</script>

<style lang="scss" scoped>
.app-container { padding: 20px; }
.search-container { margin-bottom: 20px; }
.pagination-container { margin-top: 20px; text-align: right; }
.user-info { display: flex; align-items: center; .user-id { margin-left: 8px; } }
.user-header { display: flex; align-items: center; margin-bottom: 10px; .user-info-detail { margin-left: 10px; display: flex; flex-direction: column; span { line-height: 1.5; } } }
.rating-info { display: flex; align-items: center; margin-bottom: 10px; span { margin-right: 5px; } }
.review-content { background-color: #f9f9f9; padding: 15px; border-radius: 4px; p { margin: 5px 0; } .photo-list { margin-top: 10px; .photos { display: flex; flex-wrap: wrap; margin-top: 10px; } } }
.existing-replies { background-color: #f5f7fa; padding: 10px; border-radius: 4px; .reply-item { padding: 8px; border-bottom: 1px solid #ebeef5; &:last-child { border-bottom: none; } p { margin: 5px 0; } } }
</style>
