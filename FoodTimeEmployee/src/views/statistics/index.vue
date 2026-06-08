<template>
  <div class="dashboard-container home">
    <TitleIndex @sendTitleInd="getTitleNum" :flag="flag" :tateData="tateData" />
    <div class="homeMain">
      <TurnoverStatistics :turnoverdata="turnoverData" />
      <UserStatistics :userdata="userData" />
    </div>
    <div class="homeMain homecon">
      <OrderStatistics :orderdata="orderData" :overviewData="overviewData" />
      <Top :top10data="top10Data" />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import {
  get1stAndToday,
  past7Day,
  past30Day,
  pastWeek,
  pastMonth,
} from '@/utils/formValidate'
import {
  getDataOverView,
  getTurnoverStatistics,
  getUserStatistics,
  getOrderStatistics,
  getTop,
} from '@/api/index'
import TitleIndex from './components/titleIndex.vue'
import TurnoverStatistics from './components/turnoverStatistics.vue'
import UserStatistics from './components/userStatistics.vue'
import OrderStatistics from './components/orderStatistics.vue'
import Top from './components/top10.vue'
import { getShopId } from '@/utils/cookies'

export default defineComponent({
  name: 'StatisticsDashboard',
  components: {
    TitleIndex,
    TurnoverStatistics,
    UserStatistics,
    OrderStatistics,
    Top,
  },

  data() {
    return {
      overviewData: {} as any,
      flag: 2,
      tateData: [] as any[],
      turnoverData: {} as any,
      userData: {} as any,
      orderData: { data: {} } as any,
      top10Data: {} as any
    }
  },

  created() {
    this.getTitleNum(2)
  },

  methods: {
    init(begin: any, end: any) {
      this.$nextTick(() => {
        this.getTurnoverStatisticsData(begin, end)
        this.getUserStatisticsData(begin, end)
        this.getOrderStatisticsData(begin, end)
        this.getTopData(begin, end)
      })
    },

    async getTurnoverStatisticsData(begin: any, end: any) {
      const data = await getTurnoverStatistics({ begin: begin, end: end, shopId: getShopId() })
      const turnoverData = data.data.data
      this.turnoverData = {
        dateList: turnoverData.dateList.split(','),
        turnoverList: turnoverData.turnoverList.split(',')
      }
    },

    async getUserStatisticsData(begin: any, end: any) {
      const data = await getUserStatistics({ begin: begin, end: end, shopId: getShopId() })
      const userData = data.data.data
      this.userData = {
        dateList: userData.dateList.split(','),
        totalUserList: userData.totalUserList.split(','),
        newUserList: userData.newUserList.split(','),
      }
    },

    async getOrderStatisticsData(begin: any, end: any) {
      const data = await getOrderStatistics({ begin: begin, end: end, shopId: getShopId() })
      const orderData = data.data.data
      this.orderData = {
        data: {
          dateList: orderData.dateList.split(','),
          orderCountList: orderData.orderCountList.split(','),
          validOrderCountList: orderData.validOrderCountList.split(','),
        },
        totalOrderCount: orderData.totalOrderCount,
        validOrderCount: orderData.validOrderCount,
        orderCompletionRate: orderData.orderCompletionRate
      }
    },

    async getTopData(begin: any, end: any) {
      const data = await getTop({ begin: begin, end: end, shopId: getShopId() })
      const top10Data = data.data.data
      this.top10Data = {
        nameList: top10Data.nameList.split(',').reverse(),
        numberList: top10Data.numberList.split(',').reverse(),
      }
    },

    getTitleNum(data: any) {
      switch (data) {
        case 1:
          this.tateData = get1stAndToday()
          break
        case 2:
          this.tateData = past7Day()
          break
        case 3:
          this.tateData = past30Day()
          break
        case 4:
          this.tateData = pastWeek()
          break
        case 5:
          this.tateData = pastMonth()
          break
      }
      this.init(this.tateData[0], this.tateData[1])
    }
  }
})
</script>

<style lang="scss"></style>
