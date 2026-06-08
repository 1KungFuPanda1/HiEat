<template>
  <div class="dashboard-container">
    <div class="container">
      <div class="tableBar">
        <label style="margin-right: 10px">菜品名称：</label>
        <el-input v-model="input" placeholder="请填写菜品名称" style="width: 14%" clearable @clear="init()"
          @keyup.enter="initFun" />
        <label style="margin-right: 10px; margin-left: 20px">菜品分类：</label>
        <el-select v-model="categoryId" style="width: 14%" placeholder="请选择" clearable @clear="init()">
          <el-option v-for="item in dishCategoryList" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <label style="margin-right: 10px; margin-left: 20px">售卖状态：</label>
        <el-select v-model="dishStatus" style="width: 14%" placeholder="请选择" clearable @clear="init()">
          <el-option v-for="item in saleStatus" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button class="normal-btn continue" @click="init(true)">查询</el-button>
        <div class="tableLab">
          <span class="delBut non" @click="deleteHandle('批量', null)">批量删除</span>
          <el-button type="primary" style="margin-left: 15px" @click="addDishtype('add')">+ 新建菜品</el-button>
        </div>
      </div>
      <el-table v-if="tableData.length" :data="tableData" stripe class="tableBox"
        @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="25" />
        <el-table-column prop="name" label="菜品名称" />
        <el-table-column prop="image" label="图片">
          <template v-slot="{ row }">
            <el-image style="width: 80px; height: 40px; border: none; cursor: pointer" :src="row.image">
              <template #error>
                <img src="./../../assets/noImg.png" style="width: auto; height: 40px; border: none">
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="菜品分类" />
        <el-table-column label="售价">
          <template v-slot="{ row }">
            <span style="margin-right: 10px">￥{{ (row.price).toFixed(2) * 100 / 100 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="售卖状态">
          <template v-slot="{ row }">
            <div class="tableColumn-status" :class="{ 'stop-use': String(row.status) === '0' }">
              {{ String(row.status) === '0' ? '停售' : '启售' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="最后操作时间" />
        <el-table-column label="操作" width="250" align="center">
          <template v-slot="{ row }">
            <el-button type="text" size="small" class="blueBug" @click="addDishtype(row.id)">修改</el-button>
            <el-button type="text" size="small" class="delBut" @click="deleteHandle('单删', row.id)">删除</el-button>
            <el-button type="text" size="small" class="non" :class="{
              blueBug: row.status == '0', delBut: row.status != '0'
            }" @click="statusHandle(row)">{{ row.status == '0' ? '启售' : '停售' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <Empty v-else :is-search="isSearch" />
      <el-pagination v-if="counts > 10" class="pageList" :page-sizes="[10, 20, 30, 40]" :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper" :total="counts" @size-change="handleSizeChange"
        @current-change="handleCurrentChange" />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { getDishPage, deleteDish, dishStatusByStatus, dishCategoryList } from '@/api/dish'
import Empty from '@/components/Empty/index.vue'
import { getShopId } from '@/utils/cookies'

export default defineComponent({
  name: 'DishType',
  components: { Empty },

  data() {
    return {
      input: '',
      counts: 0, page: 1, pageSize: 10,
      checkList: [] as string[],
      tableData: [] as any[],
      dishState: '' as any,
      dishCategoryList: [] as any[],
      categoryId: '', dishStatus: '',
      isSearch: false,
      saleStatus: [
        { value: 0, label: '停售' },
        { value: 1, label: '启售' }
      ]
    }
  },

  created() { this.init(); this.getDishCategoryListFn() },

  methods: {
    initFun() { this.page = 1; this.init() },

    async init(isSearch?: boolean) {
      this.isSearch = isSearch || false
      await getDishPage({
        page: this.page, pageSize: this.pageSize,
        name: this.input || undefined,
        categoryId: this.categoryId || undefined,
        status: this.dishStatus,
        shopId: getShopId()
      }).then(res => {
        if (res.data.code === 1) {
          this.tableData = res.data?.data?.records || []
          this.counts = Number(res.data.data.total)
        }
      }).catch(err => { this.$message.error('请求出错了：' + err.message) })
    },

    addDishtype(st: string) {
      if (st === 'add') { this.$router.push({ path: '/dish/add' }) }
      else { this.$router.push({ path: '/dish/add', query: { id: st } }) }
    },

    deleteHandle(type: string, id: any) {
      if (type === '批量' && id === null) {
        if (this.checkList.length === 0) return this.$message.error('请选择删除对象')
      }
      this.$confirm('确认删除该菜品, 是否继续?', '确定删除', {
        confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning'
      }).then(() => {
        deleteDish(type === '批量' ? this.checkList.join(',') : id)
          .then(res => {
            if (res?.data?.code === 1) { this.$message.success('删除成功！'); this.init() }
            else { this.$message.error(res.data.msg) }
          })
          .catch(err => { this.$message.error('请求出错了：' + err.message) })
      })
    },

    getDishCategoryListFn() {
      dishCategoryList({ type: 1, shopId: getShopId() })
        .then(res => {
          if (res?.data?.code === 1) {
            this.dishCategoryList = (res.data.data || []).map((item: any) => ({ value: item.id, label: item.name }))
          }
        })
    },

    statusHandle(row: any) {
      let params: any = {}
      if (typeof row === 'string') {
        if (this.checkList.length === 0) {
          this.$message.error('批量操作，请先勾选操作菜品！')
          return false
        }
        params.id = this.checkList.join(',')
        params.status = row
      } else {
        params.id = row.id
        params.status = row.status ? '0' : '1'
      }
      this.dishState = params
      this.$confirm('确认更改该菜品状态?', '提示', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
      }).then(() => {
        dishStatusByStatus(this.dishState)
          .then(res => {
            if (res?.data?.code === 1) { this.$message.success('菜品状态已经更改成功！'); this.init() }
            else { this.$message.error(res.data.msg) }
          })
          .catch(err => { this.$message.error('请求出错了：' + err.message) })
      })
    },

    handleSelectionChange(val: any) {
      this.checkList = val.map((n: any) => n.id)
    },
    handleSizeChange(val: any) { this.pageSize = val; this.init() },
    handleCurrentChange(val: any) { this.page = val; this.init() }
  }
})
</script>

<style lang="scss">
.el-table-column--selection .cell { padding-left: 10px; }
</style>
<style lang="scss" scoped>
.dashboard-container {
  margin: 30px;
  .container {
    background: #fff; position: relative; z-index: 1; padding: 30px 28px; border-radius: 4px;
    .normal-btn { background: #333333; color: white; margin-left: 20px; }
    .tableBar { margin-bottom: 20px;
      .tableLab { display: inline-block;
        span { cursor: pointer; display: inline-block; font-size: 14px; padding: 0 20px; color: $gray-2; }
      }
    }
    .tableBox { width: 100%; border: 1px solid $gray-5; border-bottom: 0; }
    .pageList { text-align: center; margin-top: 30px; }
  }
}
</style>
