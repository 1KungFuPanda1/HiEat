<template>
  <div class="dashboard-container home">
    <Overview :overviewData="overviewData" />
    <Orderview :orderviewData="orderviewData" />
    <div class="homeMain">
      <CuisineStatistics :dishesData="dishesData" />
      <SetMealStatistics :setMealData="setMealData" />
    </div>
    <OrderList
      :order-statics="orderStatics"
      @getOrderListBy3Status="getOrderListBy3Status"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import {
  getBusinessData,
  getOrderData,
  getOverviewDishes,
  getSetMealStatistics
} from '@/api/index'
import { getOrderListBy } from '@/api/order'
import Overview from './components/overview.vue'
import Orderview from './components/orderview.vue'
import CuisineStatistics from './components/cuisineStatistics.vue'
import SetMealStatistics from './components/setMealStatistics.vue'
import OrderList from './components/orderList.vue'

export default defineComponent({
  name: 'Dashboard',
  components: {
    Overview,
    Orderview,
    CuisineStatistics,
    SetMealStatistics,
    OrderList
  },

  data() {
    return {
      overviewData: {} as any,
      orderviewData: {} as any,
      dishesData: {} as any,
      setMealData: {} as any,
      orderStatics: {} as any
    }
  },

  created() {
    this.init()
  },

  methods: {
    init() {
      this.$nextTick(() => {
        this.getBusinessDataFn()
        this.getOrderStatisticsData()
        this.getOverStatisticsData()
        this.getSetMealStatisticsData()
      })
    },

    async getBusinessDataFn() {
      const data = await getBusinessData()
      this.overviewData = data.data.data
    },

    async getOrderStatisticsData() {
      const data = await getOrderData()
      this.orderviewData = data.data.data
    },

    async getOverStatisticsData() {
      const data = await getOverviewDishes()
      this.dishesData = data.data.data
    },

    async getSetMealStatisticsData() {
      const data = await getSetMealStatistics()
      this.setMealData = data.data.data
    },

    getOrderListBy3Status() {
      getOrderListBy({})
        .then((res) => {
          if (res.data.code === 1) {
            this.orderStatics = res.data.data
          } else {
            this.$message.error(res.data.msg)
          }
        })
        .catch((err) => {
          this.$message.error('请求出错了：' + err.message)
        })
    }
  }
})
</script>
