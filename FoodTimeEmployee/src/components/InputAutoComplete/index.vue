<template>
  <div class="input-auto-complete">
    <el-input
      v-model="input"
      :placeholder="placeholder"
      style="width: 250px"
      clearable
      @clear="init"
      @keyup.enter="init"
    >
      <template #prefix>
        <i
          class="el-input__icon el-icon-search"
          style="cursor: pointer"
          @click="init"
        />
      </template>
    </el-input>
  </div>
</template>

<script lang='ts'>
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'InputAutoComplete',
  props: {
    data: { type: Array, default: () => [] },
    placeholder: { type: String, default: '' },
    ObKey: { type: String, default: 'name' }
  },
  emits: ['init'],
  data() {
    return {
      input: '' as any
    }
  },
  methods: {
    init() {
      this.$emit('init', this.input)
    }
  }
})
</script>
<style scoped>
.input-auto-complete {
  display: inline-block;
}
</style>
