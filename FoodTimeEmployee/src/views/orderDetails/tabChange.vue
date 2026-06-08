<template>
  <div class="tab-change">
    <div v-for="item in changedOrderList"
         :key="item.value"
         class="tab-item"
         :class="{ active: item.value === activeIndex }"
         @click="tabChange(item.value)">
      <el-badge :class="{ 'special-item': item.num < 10 }"
                class="item"
                :value="item.num > 99 ? '99+' : item.num"
                :hidden="!([2, 3, 4].includes(item.value) && item.num)">
        {{ item.label }}
      </el-badge>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'TabChange',
  props: {
    orderStatics: { default: () => ({}) },
    defaultActivity: { default: '' }
  },
  emits: ['tabChange'],

  data() {
    return {
      activeIndex: Number((this as any).defaultActivity) || 0
    }
  },

  computed: {
    changedOrderList() {
      const statics = this.orderStatics as any
      return [
        { label: '全部订单', value: 0 },
        { label: '待接单', value: 2, num: statics.toBeConfirmed },
        { label: '待派送', value: 3, num: statics.confirmed },
        { label: '派送中', value: 4, num: statics.deliveryInProgress },
        { label: '已完成', value: 5 },
        { label: '已取消', value: 6 }
      ]
    }
  },

  watch: {
    defaultActivity(val: any) {
      this.activeIndex = Number(val)
    }
  },

  methods: {
    tabChange(activeIndex: number) {
      this.activeIndex = activeIndex
      this.$emit('tabChange', activeIndex)
    }
  }
})
</script>

<style lang="scss">
.tab-change {
  display: flex;
  border-radius: 4px;
  margin-bottom: 20px;

  .tab-item {
    width: 120px; height: 40px; text-align: center; line-height: 40px;
    color: #333; border: 1px solid #e5e4e4; background-color: white;
    border-left: none; cursor: pointer;

    .special-item {
      .el-badge__content { width: 20px; padding: 0 5px; }
    }
    .item {
      .el-badge__content {
        background-color: #fd3333 !important; line-height: 18px;
        height: auto; min-width: 18px; min-height: 18px;
      }
      .el-badge__content.is-fixed { top: 14px; right: 2px; }
    }
  }
  .active { background-color: #cadefc; font-weight: bold; }
  .tab-item:first-child { border-left: 1px solid #e5e4e4; }
}
</style>
