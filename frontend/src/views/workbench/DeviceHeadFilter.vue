<template>
  <span class="org-head-cell">
    {{ label }}
    <el-popover placement="bottom" :width="220" trigger="click" @show="open">
      <div class="org-filter-pop">
        <template v-if="type === 'check'">
          <label v-for="opt in options" :key="opt.value" class="org-filter-check">
            <el-checkbox
              :model-value="checks.includes(opt.value)"
              @change="(on) => toggle(opt.value, on)"
            />
            {{ opt.label }}
          </label>
        </template>
        <el-input v-else v-model="draft" :placeholder="placeholder || ('请输入' + label)" />
        <div class="org-filter-actions">
          <button class="pri" type="button" @click="apply">筛选</button>
          <button type="button" @click="reset">重置</button>
        </div>
      </div>
      <template #reference>
        <button class="org-filter-btn" :class="{ on }" type="button" aria-label="筛选">
          <i class="org-funnel" />
        </button>
      </template>
    </el-popover>
  </span>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  label: String,
  col: String,
  type: { type: String, default: 'text' },
  placeholder: String,
  options: { type: Array, default: () => [] },
  query: { type: Object, required: true }
})
const emit = defineEmits(['apply'])
const draft = ref('')
const checks = ref([])
const on = computed(() => !!props.query[props.col])

function open() {
  if (props.type === 'check') {
    checks.value = props.query[props.col] ? props.query[props.col].split(',') : []
  } else {
    draft.value = props.query[props.col] || ''
  }
}

function toggle(value, selected) {
  checks.value = selected
    ? [...checks.value, value]
    : checks.value.filter((item) => item !== value)
}

function apply() {
  emit('apply', { [props.col]: props.type === 'check' ? checks.value.join(',') : draft.value })
}

function reset() {
  draft.value = ''
  checks.value = []
  emit('apply', { [props.col]: '' })
}
</script>
