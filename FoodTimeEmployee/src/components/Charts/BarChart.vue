<template>
  <div
    :id="id"
    :class="className"
    :style="{height: height, width: width}"
  />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import * as echarts from 'echarts'
import ResizeMixin from './mixins/resize'

export default defineComponent({
  name: 'BarChart',
  mixins: [ResizeMixin],
  props: {
    className: { type: String, default: 'chart' },
    id: { type: String, default: 'BarChart' },
    width: { type: String, default: '100%' },
    height: { type: String, default: '250px' },
    title: { type: String, default: 'Requests' },
    chartData: { type: Object, default: () => ({}) }
  },

  mounted() {
    this.init()
  },

  watch: {
    chartData() {
      this.init()
    }
  },

  beforeUnmount() {
    if (!this.chart) {
      return
    }
    this.chart.dispose()
    this.chart = null
  },

  methods: {
    init() {
      this.$nextTick(() => {
        this.initChart()
      })
    },

    initChart() {
      this.chart = echarts.init(document.getElementById(this.id) as HTMLDivElement)
      const data = this.chartData as any
      this.chart.setOption({
        title: { text: this.title, left: 'left' },
        tooltip: { trigger: 'item', formatter: '{a} <br/>{b} : {c} ({d}%)' },
        legend: {
          type: 'scroll',
          orient: 'vertical',
          left: 0,
          top: 50,
          bottom: 20,
          data: data.legendData,
          selected: data.selected
        },
        series: [{
          name: '占比',
          type: 'pie',
          radius: '65%',
          left: 80,
          center: ['40%', '50%'],
          data: data.seriesData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          },
          itemStyle: {
            normal: {
              color: function (params: any) {
                var colorList = ['#389BFF', '#FFC200', '#52C41A', '#08979C', '#597EF7', '#B37FEB', '#FF7875', '#5CDBD3', '#FFC53D']
                return colorList[params.dataIndex]
              }
            }
          }
        }]
      } as any)
    }
  }
})
</script>
