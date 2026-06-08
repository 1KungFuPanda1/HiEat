<template>
  <div class="dashboard-container">
    <div class="container">
      <div class="tableBar">
        <label style="margin-right: 5px">员工姓名：</label>
        <el-input v-model="input" placeholder="请输入员工姓名" style="width: 15%" clearable @clear="init()"
          @keyup.enter="initFun" />
        <el-button class="normal-btn continue" @click="init(true)">查询</el-button>
        <el-button type="primary" style="float: right" @click="addEmployeeHandle('add')" :disabled="isManager !== 1">
          + 添加员工
        </el-button>
      </div>
      <el-table :data="tableData" stripe v-if="tableData.length" class="tableBox">
        <el-table-column prop="name" label="员工姓名" />
        <el-table-column prop="username" label="账号" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column label="账号状态">
          <template v-slot="{ row }">
            <div class="tableColumn-status" :class="{ 'stop-use': String(row.status) === '0' }">
              {{ String(row.status) === '0' ? '禁用' : '启用' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="最后操作时间" />
        <el-table-column label="操作" width="160" align="center">
          <template v-slot="{ row }">
            <el-button type="text" size="small" class="blueBug" :class="{ 'disabled-text': isManager !== 1 }"
              :disabled="isManager !== 1" @click="addEmployeeHandle(row.id, row.username)">
              修改
            </el-button>
            <el-button :disabled="row.username === 'admin' || isManager !== 1" type="text" size="small"
              class="non" :class="{
                'disabled-text': row.username === 'admin' || isManager !== 1,
                blueBug: row.status == '0',
                delBut: row.status != '0'
              }" @click="statusHandle(row)">
              {{ row.status == '1' ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <Empty v-else :is-search="isSearch" />
      <el-pagination class="pageList" :page-sizes="[10, 20, 30, 40]" :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper" :total="counts" @size-change="handleSizeChange"
        @current-change="handleCurrentChange" />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { getEmployeeList, enableOrDisableEmployee } from '@/api/employee'
import { useUserStore } from '@/stores/user'
import Empty from '@/components/Empty/index.vue'
import { getShopId } from '@/utils/cookies'

export default defineComponent({
  name: 'Employee',
  components: { Empty },

  data() {
    return {
      input: '',
      counts: 0,
      page: 1,
      pageSize: 10,
      tableData: [] as any[],
      id: '',
      status: '',
      isSearch: false
    }
  },

  computed: {
    userName() {
      return useUserStore().username
    },
    isManager() {
      return useUserStore().isManager
    }
  },

  created() { this.init() },

  methods: {
    initFun() {
      this.page = 1
      this.init()
    },

    async init(isSearch?: boolean) {
      this.isSearch = isSearch || false
      const params = {
        page: this.page,
        pageSize: this.pageSize,
        name: this.input || undefined,
        shopId: getShopId()
      }
      await getEmployeeList(params)
        .then((res: any) => {
          if (String(res.data.code) === '1') {
            this.tableData = res.data?.data?.records || []
            this.counts = res.data.data.total
          }
        })
        .catch((err) => {
          this.$message.error('请求出错了：' + err.message)
        })
    },

    addEmployeeHandle(st: string, username?: string) {
      if (st === 'add') {
        this.$router.push({ path: '/employee/add' })
      } else {
        if (username === 'admin') return
        this.$router.push({ path: '/employee/add', query: { id: st } })
      }
    },

    statusHandle(row: any) {
      if (row.username === 'admin') return
      this.id = row.id
      this.status = row.status
      this.$confirm('确认调整该账号的状态?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        enableOrDisableEmployee({ id: this.id, status: !this.status ? 1 : 0 })
          .then((res) => {
            if (String(res.status) === '200') {
              this.$message.success('账号状态更改成功！')
              this.init()
            }
          })
          .catch((err) => {
            this.$message.error('请求出错了：' + err.message)
          })
      })
    },

    handleSizeChange(val: any) { this.pageSize = val; this.init() },
    handleCurrentChange(val: any) { this.page = val; this.init() }
  }
})
</script>

<style lang="scss" scoped>
.disabled-text { color: #bac0cd !important; }
</style>
