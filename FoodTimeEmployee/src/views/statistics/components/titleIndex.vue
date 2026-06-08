<template>
  <div class="title-index">
    <div class="month">
      <ul class="tabs">
        <li
          class="li-tab"
          v-for="(item, index) in tabsParam"
          @click="toggleTabs(index)"
          :class="{ active: index === nowIndex }"
          :key="index"
        >
          {{ item }}
          <span></span>
        </li>
      </ul>
    </div>
    <div class="get-time">
      <p>
        已选时间：{{ tateData[0] }} 至
        {{ tateData[tateData.length - 1] }}
      </p>
    </div>
    <el-button
      class="right-el-button"
      @click="handleExport"
    >
      <template #icon>
        <i class="iconfont icon-download"></i>
      </template>
      数据导出
    </el-button>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { exportInfor } from '@/api/index'

export default defineComponent({
  name: 'TitleIndex',
  props: {
    flag: { type: Number, required: true },
    tateData: { type: Array, required: true },
    turnoverData: { type: Object, default: () => ({}) }
  },
  emits: ['sendTitleInd'],

  data() {
    return {
      nowIndex: 1,
      value: [] as any[],
      tabsParam: ['昨日', '近7日', '近30日', '本周', '本月']
    }
  },

  watch: {
    flag(val: number) {
      this.nowIndex = val
    }
  },

  methods: {
    toggleTabs(index: number) {
      this.nowIndex = index
      this.value = []
      this.$emit('sendTitleInd', index + 1)
    },

    handleExport() {
      this.$confirm('是否确认导出最近30天运营数据?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
        .then(async () => {
          const { data } = await exportInfor()
          let url = window.URL.createObjectURL(data)
          var a = document.createElement('a')
          document.body.appendChild(a)
          a.href = url
          a.download = '运营数据统计报表.xlsx'
          a.click()
          window.URL.revokeObjectURL(url)
        })
        .then((response: any) => {})
    }
  }
})
</script>
