<template>
  <div class="container top10">
    <h2 class="homeTitle">销量排名TOP10</h2>
    <div class="charBox">
      <div id="top" style="width: 100%; height: 380px"></div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import * as echarts from 'echarts'

export default defineComponent({
  name: 'Top',
  props: {
    top10data: { type: Object, required: true }
  },

  watch: {
    top10data: {
      handler() {
        this.$nextTick(() => {
          this.initChart()
        })
      },
      deep: true
    }
  },

  methods: {
    initChart() {
      const chartDom = document.getElementById('top') as any
      if (!chartDom) return
      const myChart = echarts.init(chartDom)
      var option: any
      option = {
        tooltip: {
          trigger: 'axis',
          backgroundColor: '#fff',
          borderRadius: 2,
          textStyle: {
            color: '#333',
            fontSize: 12,
            fontWeight: 300,
          },
        },
        grid: {
          top: '-10px',
          left: '0',
          right: '0',
          bottom: '0',
          containLabel: true,
        },
        xAxis: {
          show: false,
        },
        yAxis: {
          axisLine: {
            show: false,
          },
          axisTick: {
            show: false,
            alignWithLabel: true,
          },
          type: 'category',
          axisLabel: {
            textStyle: {
              color: '#666',
              fontSize: '12px',
            },
          },
          data: (this.top10data as any).nameList || [],
        },
        series: [
          {
            data: (this.top10data as any).numberList || [],
            type: 'bar',
            showBackground: true,
            backgroundStyle: {
              color: '#F3F4F7',
            },
            barWidth: 20,
            barGap: '80%',
            barCategoryGap: '80%',
            itemStyle: {
              emphasis: {
                barBorderRadius: 30,
              },
              normal: {
                barBorderRadius: [0, 10, 10, 0],
                color: new echarts.graphic.LinearGradient(
                  1, 0, 0, 0,
                  [
                    { offset: 0, color: '#FFBD00' },
                    { offset: 1, color: '#FFD000' },
                  ]
                ),
                label: {
                  show: true,
                  formatter: '{@score}',
                  color: '#333',
                  position: ['8', '5'],
                },
              },
            },
          },
        ],
      }
      option && myChart.setOption(option)
    }
  }
})
</script>
