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
  name: 'BasicChart',
  mixins: [ResizeMixin],
  props: {
    className: { type: String, default: 'chart' },
    id: { type: String, default: 'mixedChart' },
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
        backgroundColor: '#fff',
        title: {
          text: this.title,
          top: '0',
          textStyle: { color: '#000', fontSize: 18 },
          subtextStyle: { color: '#90979c', fontSize: 16 }
        },
        tooltip: { trigger: 'axis' },
        grid: {
          left: '50',
          right: '5%',
          borderWidth: 0,
          top: 60,
          bottom: 34,
          textStyle: { color: '#fff' }
        },
        xAxis: [{
          type: 'category',
          axisLine: { lineStyle: { color: '#90979c' } },
          splitLine: { show: false },
          axisTick: { show: true },
          splitArea: { show: false },
          axisLabel: { interval: 0, rotate: data.xData && data.xData.length > 10 ? -25 : 0 },
          data: data.xData
        }],
        yAxis: [{
          type: 'value',
          splitLine: { show: false },
          axisLine: { lineStyle: { color: '#90979c' } },
          axisTick: { show: false },
          axisLabel: { interval: 0 },
          splitArea: { show: false }
        }],
        series: [{
          name: '店内',
          type: 'bar',
          stack: 'total',
          barMaxWidth: 15,
          barGap: '10%',
          itemStyle: {
            normal: {
              barBorderRadius: [10, 10, 0, 0],
              color: new echarts.graphic.LinearGradient(
                0, 0, 0, 1,
                [
                  { offset: 0, color: '#FFA868' },
                  { offset: 1, color: '#FF9240' }
                ]
              )
            },
            label: {
              show: true,
              textStyle: { color: '#fff' },
              position: 'insideTop',
              formatter(p: any) {
                return p.value > 0 ? p.value : ''
              }
            }
          },
          data: data.yData
        }]
      } as any)
    }
  }
})
</script>
