<template>
  <svg
    class="app-mark"
    :class="size"
    viewBox="0 0 88 88"
    xmlns="http://www.w3.org/1999/svg"
    aria-hidden="true"
  >
    <defs>
      <linearGradient :id="gid('plate')" x1="18%" y1="8%" x2="86%" y2="96%">
        <stop offset="0" stop-color="#7ad4f0" />
        <stop offset="0.42" stop-color="#14afe7" />
        <stop offset="1" stop-color="#0d6f96" />
      </linearGradient>
      <linearGradient :id="gid('sheen')" x1="50%" y1="0" x2="50%" y2="55%">
        <stop offset="0" stop-color="#fff" stop-opacity="0.42" />
        <stop offset="1" stop-color="#fff" stop-opacity="0" />
      </linearGradient>
      <linearGradient :id="gid('ink')" x1="20%" y1="10%" x2="80%" y2="90%">
        <stop offset="0" stop-color="#f7fdff" />
        <stop offset="1" stop-color="#d7eef8" />
      </linearGradient>
      <filter :id="gid('soft')" x="-20%" y="-20%" width="140%" height="140%">
        <feDropShadow dx="0" dy="1.2" stdDeviation="1.1" flood-color="#08384c" flood-opacity="0.22" />
      </filter>
    </defs>

    <!-- 设备故障预测：尖顶六边形底板 + 心跳波形 -->
    <g v-if="kind === 'pdm'">
      <path :d="hex" :fill="url('plate')" />
      <path :d="hexInner" fill="none" stroke="#fff" stroke-opacity="0.38" stroke-width="1.6" />
      <path :d="hex" :fill="url('sheen')" />
      <g :filter="flt('soft')" fill="none" :stroke="url('ink')" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="44" cy="30" r="5.2" stroke-width="2.2" />
        <path d="M44 35.4 V42" stroke-width="2.2" />
        <path d="M18 54 H30 L35.5 42 L41 62 L47.5 36 L54 54 H70" stroke-width="2.6" />
        <circle cx="44" cy="30" r="1.7" fill="#fff" stroke="none" />
      </g>
    </g>

    <!-- 礼盒包装：圆角方标 + 等轴礼盒 -->
    <g v-else-if="kind === 'pack'">
      <rect x="8" y="8" width="72" height="72" rx="20" :fill="url('plate')" />
      <rect x="14" y="14" width="60" height="60" rx="16" fill="none" stroke="#fff" stroke-opacity="0.32" stroke-width="1.5" />
      <rect x="8" y="8" width="72" height="36" rx="20" :fill="url('sheen')" />
      <g :filter="flt('soft')">
        <path d="M28 42 L44 50 L44 68 L28 60 Z" fill="#e8f6fb" fill-opacity="0.92" />
        <path d="M44 50 L60 42 L60 60 L44 68 Z" fill="#9fd4ea" />
        <path d="M28 42 L44 34 L60 42 L44 50 Z" fill="#fff" fill-opacity="0.95" />
        <path d="M44 34 V68" fill="none" stroke="#1a7eab" stroke-opacity="0.45" stroke-width="2" />
        <path d="M36 31 C36 26 41 24 44 28 C47 24 52 26 52 31 C49 30 46 33 44 36 C42 33 39 30 36 31 Z" fill="#fff" />
        <path d="M41 28 L47 28" fill="none" stroke="#7ad4f0" stroke-width="1.2" />
      </g>
    </g>

    <!-- CAD 拆解：折线图纸与圆形定位点 -->
    <g v-else-if="kind === 'cad'">
      <path :d="hex" :fill="url('plate')" />
      <path :d="hexInner" fill="none" stroke="#fff" stroke-opacity="0.38" stroke-width="1.6" />
      <path :d="hex" :fill="url('sheen')" />
      <g :filter="flt('soft')" fill="none" :stroke="url('ink')" stroke-linecap="round" stroke-linejoin="round">
        <path d="M25 58 L25 31 L43 21 L63 32 L63 58 L44 68 Z" stroke-width="2.4" />
        <path d="M25 31 L44 42 L63 32 M44 42 V68" stroke-width="2" stroke-opacity="0.8" />
        <circle cx="44" cy="42" r="4" stroke-width="2" />
        <path d="M19 62 H31 M57 62 H69" stroke-width="1.8" />
      </g>
    </g>

    <!-- 周计划：手绘日历与周节拍 -->
    <g v-else-if="kind === 'week'">
      <path :d="hex" :fill="url('plate')" />
      <path :d="hexInner" fill="none" stroke="#fff" stroke-opacity="0.38" stroke-width="1.6" />
      <path :d="hex" :fill="url('sheen')" />
      <g :filter="flt('soft')" fill="none" :stroke="url('ink')" stroke-linecap="round" stroke-linejoin="round">
        <rect x="24" y="25" width="40" height="38" rx="4" stroke-width="2.4" />
        <path d="M24 37 H64 M33 20 V30 M55 20 V30" stroke-width="2.6" />
        <path d="M31 46 H37 M43 46 H49 M55 46 H59 M31 54 H37 M43 54 H49" stroke-width="2.4" />
        <path d="M31 60 H57" stroke-width="1.5" stroke-opacity="0.7" />
      </g>
    </g>

    <!-- 未登记模块：六边形底板 + 三节点，不用汉字 -->
    <g v-else>
      <path :d="hex" :fill="url('plate')" />
      <path :d="hexInner" fill="none" stroke="#fff" stroke-opacity="0.38" stroke-width="1.6" />
      <path :d="hex" :fill="url('sheen')" />
      <g :filter="flt('soft')" fill="none" :stroke="url('ink')" stroke-width="2.2" stroke-linecap="round">
        <path d="M32 50 L44 28 L56 50 Z" />
        <circle cx="44" cy="28" r="3.2" fill="#fff" stroke="none" />
        <circle cx="32" cy="50" r="3.2" fill="#fff" stroke="none" />
        <circle cx="56" cy="50" r="3.2" fill="#fff" stroke="none" />
      </g>
    </g>
  </svg>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  code: { type: String, default: '' },
  size: { type: String, default: 'md' }
})

const kind = computed(() => {
  const code = (props.code || '').trim()
  if (code === 'EXT_PDM') return 'pdm'
  if (code === 'EXT_PACK_AI') return 'pack'
  if (code === 'EXT_CAD') return 'cad'
  if (code === 'EXT_WEEK') return 'week'
  return 'mesh'
})

const uid = Math.random().toString(36).slice(2, 8)
const hex = 'M44 6 L76 24.5 L76 63.5 L44 82 L12 63.5 L12 24.5 Z'
const hexInner = 'M44 14 L68 28 L68 60 L44 74 L20 60 L20 28 Z'

function gid(name) {
  return `am-${name}-${uid}`
}

function url(name) {
  return `url(#${gid(name)})`
}

function flt(name) {
  return `url(#${gid(name)})`
}
</script>

<style scoped>
.app-mark {
  display: block;
  overflow: visible;
}
.app-mark.md { width: 88px; height: 88px; }
.app-mark.sm { width: 64px; height: 64px; }
</style>
