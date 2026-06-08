<template>
  <div class="HeadLable">
    <span
      v-if="goback"
      class="goBack"
      @click="goBack()"
    ><img
      src="@/assets/icons/btn_back@2x.png"
      alt=""
    > 返回</span>
    <span v-if="!butList">{{ title }}</span>
    <div v-if="butList">
      <slot />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'HeadLable',
  props: {
    goback: { type: Boolean, default: false },
    butList: { type: Boolean, default: false },
    title: { type: String, default: '集团管理' }
  },
  emits: ['toggleClick'],
  methods: {
    toggleClick() {
      this.$emit('toggleClick')
    },
    goBack() {
      this.$router.go(-1)
    }
  }
})
</script>

<style lang="scss" scoped>
  .HeadLable{
    background: #fff;
    color: #333333;
    height: 64px;
    font-size: 16px;
    padding-left: 22px;
    line-height: 64px;
    font-weight: 700;
    margin-bottom: 15px;
    top:0px;
    left: 0px;
    opacity: 0;
    animation: opacity 500ms ease-out 800ms forwards;
    .goBack{
      border-right: solid 1px #d8dde3;
      padding-right: 14px;
      margin-right: 14px;
      font-size: 16px;
      color: #333333;
      cursor: pointer;
      font-weight: 400;
      img{
        position: relative;
        top:24px;
        margin-right: 5px;
        width: 18px;
        height: 18px;
        float: left;
      }
    }
  }
  @keyframes opacity {
     0% {
       opacity: 0;
       left: 80px;
     }
     100% {
       opacity: 1;
       left: 0;
     }
   }
</style>
