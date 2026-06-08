<template>
  <div class="dashboard-container">
    <div class="container">
      <div class="tableBar">
        <label style="margin-right: 10px">套餐名称：</label>
        <el-input v-model="input" placeholder="请填写套餐名称" style="width: 14%" clearable @clear="init"
          @keyup.enter="initFun" />

        <label style="margin-right: 10px; margin-left: 20px">套餐分类：</label>
        <el-select v-model="categoryId" style="width: 14%" placeholder="请选择" clearable @clear="init">
          <el-option v-for="item in dishCategoryList" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>

        <label style="margin-right: 10px; margin-left: 20px">售卖状态：</label>
        <el-select v-model="dishStatus" style="width: 14%" placeholder="请选择" clearable @clear="init">
          <el-option v-for="item in saleStatus" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button class="normal-btn continue" @click="init(true)">
          查询
        </el-button>
        <div class="tableLab">
          <span class="delBut non" @click="deleteHandle('批量', null)">批量删除</span>
          <el-button type="primary" style="margin-left: 15px" @click="addSetMeal('add')">
            + 新建套餐
          </el-button>
        </div>
      </div>
      <el-table v-if="tableData.length" :data="tableData" stripe class="tableBox"
        @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="25" />
        <el-table-column prop="name" label="套餐名称" />
        <el-table-column prop="image" label="图片">
          <template v-slot="{ row }">
            <el-image style="width: 80px; height: 40px; border: none; cursor: pointer" :src="row.image">
              <template #error>
                <img src="./../../assets/noImg.png" style="width: auto; height: 40px; border: none">
              </template>
            </el-image>
          </template>
        </el-table-column>

        <el-table-column prop="categoryName" label="套餐分类" />
        <el-table-column prop="price" label="套餐价">
          <template v-slot="{ row }">
            <span>￥{{ ((row.price).toFixed(2) * 100) / 100 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="售卖状态">
          <template v-slot="{ row }">
            <div class="tableColumn-status" :class="{ 'stop-use': String(row.status) === '0' }">
              {{ String(row.status) === '0' ? '停售' : '启售' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="最后操作时间"></el-table-column>
        <el-table-column label="操作" width="250" align="center">
          <template v-slot="{ row }">
            <el-button type="text" size="small" class="blueBug" @click="addSetMeal(row)">
              修改
            </el-button>
            <el-button type="text" size="small" class="delBut" @click="deleteHandle('单删', row.id)">
              删除
            </el-button>
            <el-button type="text" size="small" class="blueBug non" :class="{
              blueBug: row.status == '0',
              delBut: row.status != '0'
            }" @click="statusHandle(row)">
              {{ row.status == '0' ? '启售' : '停售' }}
            </el-button>
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
import HeadLable from '@/components/HeadLable/index.vue'
import {
  getSetmealPage,
  editSetmeal,
  deleteSetmeal,
  setmealStatusByStatus,
  dishCategoryList
} from '@/api/setMeal'
import InputAutoComplete from '@/components/InputAutoComplete/index.vue'
import Empty from '@/components/Empty/index.vue'
import { getShopId } from '@/utils/cookies'

export default defineComponent({
  name: 'Setmeal',
  components: {
    HeadLable,
    InputAutoComplete,
    Empty
  },

  data() {
    return {
      input: '' as any,
      counts: 0,
      page: 1,
      pageSize: 10,
      checkList: [] as any[],
      tableData: [] as any[],
      dishCategoryList: [] as any[],
      categoryId: '',
      dishStatus: '',
      isSearch: false,
      saleStatus: [
        { value: 0, label: '停售' },
        { value: 1, label: '启售' }
      ]
    }
  },

  created() {
    this.init()
    this.getDishCategoryList()
  },

  methods: {
    initProp(val: any) {
      this.input = val
      this.initFun()
    },

    initFun() {
      this.page = 1
      this.init()
    },

    async init(isSearch?: any) {
      this.isSearch = isSearch
      await getSetmealPage({
        page: this.page,
        pageSize: this.pageSize,
        name: this.input || undefined,
        categoryId: this.categoryId || undefined,
        status: this.dishStatus as any,
        shopId: getShopId()
      })
        .then(res => {
          if (res && res.data && res.data.code === 1) {
            this.tableData = res.data.data.records
            this.counts = Number(res.data.data.total)
          } else {
            this.$message.error(res.data.msg)
          }
        })
        .catch(err => {
          this.$message.error('请求出错了：' + err.message)
        })
    },

    addSetMeal(st: any) {
      if (st === 'add') {
        this.$router.push({ path: '/setmeal/add' })
      } else {
        this.$router.push({ path: '/setmeal/add', query: { id: st.id } })
      }
    },

    deleteHandle(type: string, id: any) {
      if (type === '批量' && id === null) {
        if (this.checkList.length === 0) {
          return this.$message.error('请选择删除对象')
        }
      }
      this.$confirm('确定删除该套餐?', '确定删除', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteSetmeal(type === '批量' ? this.checkList.join(',') : id)
          .then(res => {
            if (res.data.code === 1) {
              this.$message.success('删除成功！')
              this.init()
            } else {
              this.$message.error(res.data.msg)
            }
          })
          .catch(err => {
            this.$message.error('请求出错了：' + err.message)
          })
      })
    },

    statusHandle(row: any) {
      let params: any = {}
      params.shopId = getShopId()
      if (typeof row === 'string') {
        if (this.checkList.length == 0) {
          this.$message.error('批量操作，请先勾选操作菜品！')
          return false
        }
        params.ids = this.checkList.join(',')
        params.status = row
      } else {
        params.ids = row.id
        params.status = row.status ? '0' : '1'
      }

      this.$confirm('确认更改该套餐状态?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        setmealStatusByStatus(params)
          .then(res => {
            if (res.data.code === 1) {
              this.$message.success('套餐状态已经更改成功！')
              this.init()
            } else {
              this.$message.error(res.data.msg)
            }
          })
          .catch(err => {
            this.$message.error('请求出错了：' + err.message)
          })
      })
    },

    getDishCategoryList() {
      dishCategoryList({
        type: 2,
        shopId: getShopId()
      })
        .then(res => {
          if (res && res.data && res.data.code === 1) {
            this.dishCategoryList = (
              res.data && res.data.data
            ).map((item: any) => {
              return { value: item.id, label: item.name }
            })
          }
        })
        .catch(() => { })
    },

    handleSelectionChange(val: any) {
      let checkArr: string[] = []
      val.forEach((n: any) => {
        checkArr.push(n.id)
      })
      this.checkList = checkArr
    },

    handleSizeChange(val: any) {
      this.pageSize = val
      this.init()
    },

    handleCurrentChange(val: any) {
      this.page = val
      this.init()
    }
  }
})
</script>
<style lang="scss">
.el-table-column--selection .cell {
  padding-left: 10px;
}
</style>
<style lang="scss" scoped>
.dashboard {
  &-container {
    margin: 30px;

    .container {
      background: #fff;
      position: relative;
      z-index: 1;
      padding: 30px 28px;
      border-radius: 4px;

      .tableBar {
        margin-bottom: 20px;

        .tableLab {
          float: right;

          span {
            cursor: pointer;
            display: inline-block;
            font-size: 14px;
            padding: 0 20px;
            color: $gray-2;
          }
        }
      }

      .tableBox {
        width: 100%;
        border: 1px solid $gray-5;
        border-bottom: 0;
      }

      .pageList {
        text-align: center;
        margin-top: 30px;
      }

      .normal-btn {
        background: #333333;
        color: white;
        margin-left: 20px;
      }
    }
  }
}
</style>
