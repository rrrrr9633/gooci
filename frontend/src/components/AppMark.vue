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

    <g v-if="kind === 'pack'">
      <rect x="8" y="8" width="72" height="72" rx="20" :fill="url('plate')" />
      <rect x="14" y="14" width="60" height="60" rx="16" fill="none" stroke="#fff" stroke-opacity="0.32" stroke-width="1.5" />
      <rect x="8" y="8" width="72" height="36" rx="20" :fill="url('sheen')" />
      <g :filter="flt('soft')">
        <path d="M28 42 L44 50 L44 68 L28 60 Z" fill="#e8f6fb" fill-opacity="0.92" />
        <path d="M44 50 L60 42 L60 60 L44 68 Z" fill="#9fd4ea" />
        <path d="M28 42 L44 34 L60 42 L44 50 Z" fill="#fff" fill-opacity="0.95" />
        <path d="M44 34 V68" fill="none" stroke="#1a7eab" stroke-opacity="0.45" stroke-width="2" />
        <path d="M36 31 C36 26 41 24 44 28 C47 24 52 26 52 31 C49 30 46 33 44 36 C42 33 39 30 36 31 Z" fill="#fff" />
      </g>
    </g>

    <g v-else>
      <path :d="hex" :fill="url('plate')" />
      <path :d="hexInner" fill="none" stroke="#fff" stroke-opacity="0.38" stroke-width="1.6" />
      <path :d="hex" :fill="url('sheen')" />
      <g :filter="flt('soft')" :fill="url('ink')" :stroke="url('ink')" stroke-linecap="round" stroke-linejoin="round">
        <g v-if="kind === 'pdm'" fill="none">
          <circle cx="44" cy="30" r="5.2" stroke-width="2.4" />
          <path d="M44 35.4 V42" stroke-width="2.4" />
          <path d="M18 54 H30 L35.5 42 L41 62 L47.5 36 L54 54 H70" stroke-width="2.8" />
          <circle cx="44" cy="30" r="1.8" stroke="none" />
        </g>
        <g v-else-if="kind === 'cad'" fill="none">
          <path d="M25 58 L25 31 L43 21 L63 32 L63 58 L44 68 Z" stroke-width="2.5" />
          <path d="M25 31 L44 42 L63 32 M44 42 V68" stroke-width="2.1" />
          <circle cx="44" cy="42" r="4" stroke-width="2.1" />
        </g>
        <g v-else-if="kind === 'week'" fill="none">
          <rect x="24" y="25" width="40" height="38" rx="4" stroke-width="2.5" />
          <path d="M24 37 H64 M33 20 V30 M55 20 V30" stroke-width="2.6" />
          <path d="M31 46 H37 M43 46 H49 M55 46 H59 M31 54 H37 M43 54 H49" stroke-width="2.6" />
        </g>
        <g v-else-if="kind === 'after'" fill="none">
          <path d="M27 46 V39 C27 29 34.5 22 44 22 C53.5 22 61 29 61 39 V46" stroke-width="3" />
          <rect x="21" y="43" width="12" height="18" rx="5" stroke-width="2.6" />
          <rect x="55" y="43" width="12" height="18" rx="5" stroke-width="2.6" />
          <path d="M61 58 C66 58 70 54 70 49" stroke-width="2.6" />
        </g>
        <g v-else-if="kind === 'work'">
          <path d="M30 28 H58 L58 68 H30 Z" stroke="none" />
          <path d="M36 22 H52 L54 30 H34 Z" stroke="none" />
          <path d="M36 42 H52 M36 50 H48 M36 58 H44" fill="none" stroke="#0d6f96" stroke-width="2.4" />
        </g>
        <g v-else-if="kind === 'monitor'">
          <path d="M20 24 H68 V52 H20 Z" stroke="none" />
          <path d="M36 56 H52 L56 66 H32 Z" stroke="none" />
          <path d="M28 38 H36 L40 30 L46 44 L52 34 H62" fill="none" stroke="#0d6f96" stroke-width="2.6" />
        </g>
        <g v-else-if="kind === 'wms'">
          <path d="M24 58 H64 V66 H24 Z" stroke="none" />
          <path d="M28 36 L44 26 L60 36 L44 46 Z" stroke="none" />
          <path d="M28 36 V54 L44 64 V46 M60 36 V54 L44 64" fill="none" stroke-width="2.3" />
        </g>
        <g v-else-if="kind === 'trace'" fill="none">
          <path d="M24 34 V24 H34 M54 24 H64 V34 M64 54 V64 H54 M34 64 H24 V54" stroke-width="3.2" />
          <path d="M32 34 H40 V42 H32 Z M46 34 H56 V42 H46 Z M32 46 H44 V56 H32 Z M48 46 H56 V56 H48 Z" stroke-width="2.2" />
        </g>
        <g v-else-if="kind === 'energy'">
          <path d="M50 18 L30 46 H44 L36 70 L64 38 H50 Z" stroke="none" />
        </g>
        <g v-else-if="kind === 'maint'" fill="none">
          <circle cx="36" cy="52" r="12" stroke-width="3" />
          <circle cx="36" cy="52" r="4" stroke="none" />
          <path d="M46 42 L64 24 L72 32 L54 50" stroke-width="3.2" />
        </g>
        <g v-else-if="kind === 'eqkb'">
          <path d="M22 26 C32 20 40 20 44 26 C48 20 56 20 66 26 V64 C56 58 48 58 44 64 C40 58 32 58 22 64 Z" stroke="none" />
          <path d="M44 26 V64" fill="none" stroke="#0d6f96" stroke-width="2.2" />
        </g>
        <g v-else-if="kind === 'draw'">
          <path d="M26 22 H52 L66 36 V68 H26 Z" stroke="none" />
          <path d="M52 22 V36 H66" fill="none" stroke="#0d6f96" stroke-width="2.2" />
          <path d="M34 46 H58 M34 54 H50" fill="none" stroke="#0d6f96" stroke-width="2.4" />
        </g>
        <g v-else-if="kind === 'esop'">
          <path d="M22 30 H50 V68 H22 Z" opacity="0.7" stroke="none" />
          <path d="M36 20 H64 V58 H36 Z" stroke="none" />
          <path d="M44 32 H56 M44 40 H52" fill="none" stroke="#0d6f96" stroke-width="2.4" />
        </g>
        <g v-else-if="kind === 'safety'">
          <path d="M20 32 H50 V54 H20 Z" stroke="none" />
          <path d="M50 36 L68 26 V60 L50 50 Z" stroke="none" />
          <circle cx="35" cy="43" r="6" fill="none" stroke="#0d6f96" stroke-width="2.4" />
        </g>
        <g v-else-if="kind === 'visit'">
          <circle cx="34" cy="28" r="8" stroke="none" />
          <path d="M20 62 C20 46 26 42 34 42 C42 42 48 46 48 62 Z" stroke="none" />
          <path d="M54 32 H72 M63 23 V41" fill="none" stroke-width="3.4" />
        </g>
        <g v-else-if="kind === 'edlp'">
          <path d="M44 18 L68 28 V48 C68 62 52 72 44 76 C36 72 20 62 20 48 V28 Z" stroke="none" />
          <rect x="36" y="44" width="16" height="12" rx="2" fill="none" stroke="#0d6f96" stroke-width="2.2" />
          <path d="M40 44 V39 C40 36 42 34 44 34 C46 34 48 36 48 39 V44" fill="none" stroke="#0d6f96" stroke-width="2.2" />
        </g>
        <g v-else-if="kind === 'rdkb'" fill="none">
          <path d="M34 20 H54 M38 20 V38 L24 64 H64 L50 38 V20" stroke-width="2.8" />
          <path d="M30 52 H58" stroke-width="2.6" />
          <circle cx="60" cy="26" r="5" stroke-width="2.4" />
        </g>
        <g v-else-if="kind === 'aps'">
          <path d="M26 56 H38 V68 H26 Z" stroke="none" />
          <path d="M42 36 H54 V68 H42 Z" stroke="none" />
          <path d="M58 24 H70 V68 H58 Z" stroke="none" />
        </g>
        <g v-else-if="kind === 'voucher'">
          <path d="M26 20 H62 V70 L54 64 L46 70 L38 64 L26 70 Z" stroke="none" />
          <path d="M34 34 H54 M34 42 H48" fill="none" stroke="#0d6f96" stroke-width="2.4" />
          <circle cx="54" cy="54" r="8" fill="none" stroke="#0d6f96" stroke-width="2.3" />
        </g>
        <g v-else-if="kind === 'recon'">
          <path d="M18 24 H40 V64 H18 Z" stroke="none" />
          <path d="M48 24 H70 V64 H48 Z" stroke="none" />
          <path d="M40 36 H48 M40 44 H48 M40 52 H48" fill="none" stroke-width="2.8" />
        </g>
        <g v-else-if="kind === 'oa'">
          <circle cx="44" cy="26" r="8" stroke="none" />
          <path d="M30 54 C30 42 36 38 44 38 C52 38 58 42 58 54 Z" stroke="none" />
          <circle cx="24" cy="34" r="6" stroke="none" />
          <path d="M14 62 C14 52 18 49 24 49 C30 49 34 52 34 60 Z" stroke="none" />
          <circle cx="64" cy="34" r="6" stroke="none" />
          <path d="M54 60 C54 52 58 49 64 49 C70 49 74 52 74 62 Z" stroke="none" />
        </g>
        <g v-else fill="none">
          <path d="M32 50 L44 28 L56 50 Z" stroke-width="2.2" />
          <circle cx="44" cy="28" r="3.2" stroke="none" />
          <circle cx="32" cy="50" r="3.2" stroke="none" />
          <circle cx="56" cy="50" r="3.2" stroke="none" />
        </g>
      </g>
    </g>
  </svg>
</template>

<script setup>
import { computed } from 'vue'

const KIND_BY_CODE = {
  EXT_PDM: 'pdm',
  EXT_PACK_AI: 'pack',
  EXT_CAD: 'cad',
  EXT_WEEK: 'week',
  APP_AFTER: 'after',
  APP_WORK: 'work',
  APP_MONITOR: 'monitor',
  APP_WMS: 'wms',
  APP_TRACE: 'trace',
  APP_ENERGY: 'energy',
  APP_MAINT: 'maint',
  APP_EQKB: 'eqkb',
  APP_DRAW: 'draw',
  APP_ESOP: 'esop',
  APP_SAFETY: 'safety',
  APP_VISIT: 'visit',
  APP_EDLP: 'edlp',
  APP_RDKB: 'rdkb',
  APP_APS: 'aps',
  APP_VOUCHER: 'voucher',
  APP_RECON: 'recon',
  APP_OA: 'oa'
}

const KIND_BY_NAME = {
  设备故障预测: 'pdm',
  礼盒包装AI: 'pack',
  CAD拆解: 'cad',
  周计划: 'week',
  售后管理: 'after',
  轻工单: 'work',
  设备监控平台: 'monitor',
  轻仓储: 'wms',
  在线追溯: 'trace',
  能源管理: 'energy',
  设备维保: 'maint',
  AI设备知识库: 'eqkb',
  图档管理: 'draw',
  ESOP管理: 'esop',
  安全生产AI行为监控: 'safety',
  入场预约: 'visit',
  企业终端安全管理: 'edlp',
  知识库研发辅助: 'rdkb',
  AI排程预测: 'aps',
  AI凭证自动处理: 'voucher',
  AI自动对账: 'recon',
  协同办公: 'oa'
}

const props = defineProps({
  code: { type: String, default: '' },
  name: { type: String, default: '' },
  size: { type: String, default: 'md' }
})

const kind = computed(() => {
  const code = (props.code || '').trim()
  if (KIND_BY_CODE[code]) return KIND_BY_CODE[code]
  const name = (props.name || '').replace(/\s/g, '')
  if (KIND_BY_NAME[name]) return KIND_BY_NAME[name]
  for (const [label, value] of Object.entries(KIND_BY_NAME)) {
    if (name && (name.includes(label) || label.includes(name))) return value
  }
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
