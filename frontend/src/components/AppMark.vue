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

    <!-- 礼盒包装：圆角方标 + 等轴礼盒 -->
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
        <path d="M41 28 L47 28" fill="none" stroke="#7ad4f0" stroke-width="1.2" />
      </g>
    </g>

    <g v-else>
      <path :d="hex" :fill="url('plate')" />
      <path :d="hexInner" fill="none" stroke="#fff" stroke-opacity="0.38" stroke-width="1.6" />
      <path :d="hex" :fill="url('sheen')" />
      <g :filter="flt('soft')" fill="none" :stroke="url('ink')" stroke-linecap="round" stroke-linejoin="round">
        <!-- 设备故障预测 -->
        <g v-if="kind === 'pdm'">
          <circle cx="44" cy="30" r="5.2" stroke-width="2.2" />
          <path d="M44 35.4 V42" stroke-width="2.2" />
          <path d="M18 54 H30 L35.5 42 L41 62 L47.5 36 L54 54 H70" stroke-width="2.6" />
          <circle cx="44" cy="30" r="1.7" fill="#fff" stroke="none" />
        </g>
        <!-- CAD 拆解 -->
        <g v-else-if="kind === 'cad'">
          <path d="M25 58 L25 31 L43 21 L63 32 L63 58 L44 68 Z" stroke-width="2.4" />
          <path d="M25 31 L44 42 L63 32 M44 42 V68" stroke-width="2" stroke-opacity="0.8" />
          <circle cx="44" cy="42" r="4" stroke-width="2" />
          <path d="M19 62 H31 M57 62 H69" stroke-width="1.8" />
        </g>
        <!-- 周计划 -->
        <g v-else-if="kind === 'week'">
          <rect x="24" y="25" width="40" height="38" rx="4" stroke-width="2.4" />
          <path d="M24 37 H64 M33 20 V30 M55 20 V30" stroke-width="2.6" />
          <path d="M31 46 H37 M43 46 H49 M55 46 H59 M31 54 H37 M43 54 H49" stroke-width="2.4" />
          <path d="M31 60 H57" stroke-width="1.5" stroke-opacity="0.7" />
        </g>
        <!-- 售后管理：耳机 -->
        <g v-else-if="kind === 'after'">
          <path d="M26 46 V40 C26 30 34 23 44 23 C54 23 62 30 62 40 V46" stroke-width="2.6" />
          <rect x="22" y="44" width="10" height="16" rx="4" stroke-width="2.2" />
          <rect x="56" y="44" width="10" height="16" rx="4" stroke-width="2.2" />
          <path d="M62 56 H68 V50" stroke-width="2.2" />
        </g>
        <!-- 轻工单：工单夹 -->
        <g v-else-if="kind === 'work'">
          <rect x="27" y="24" width="34" height="42" rx="4" stroke-width="2.4" />
          <path d="M34 22 H54 V28 H34 Z" stroke-width="2.2" />
          <path d="M34 40 H54 M34 48 H50 M34 56 H46" stroke-width="2.2" />
        </g>
        <!-- 设备监控平台：屏幕波形 -->
        <g v-else-if="kind === 'monitor'">
          <rect x="22" y="24" width="44" height="30" rx="4" stroke-width="2.4" />
          <path d="M36 62 H52 M44 54 V62" stroke-width="2.4" />
          <path d="M28 40 H36 L40 32 L46 46 L50 38 H60" stroke-width="2.4" />
        </g>
        <!-- 轻仓储：货箱堆叠 -->
        <g v-else-if="kind === 'wms'">
          <path d="M30 36 L44 28 L58 36 L44 44 Z" stroke-width="2.2" />
          <path d="M30 36 V52 L44 60 V44 M58 36 V52 L44 60" stroke-width="2.2" />
          <path d="M26 58 H62" stroke-width="2.4" />
        </g>
        <!-- 在线追溯：扫码框 -->
        <g v-else-if="kind === 'trace'">
          <path d="M26 34 V26 H34 M54 26 H62 V34 M62 54 V62 H54 M34 62 H26 V54" stroke-width="2.6" />
          <path d="M34 36 H38 V40 H34 Z M42 36 H54 V40 H42 Z M34 44 H46 V54 H34 Z M50 44 H54 V54 H50 Z" stroke-width="1.8" />
        </g>
        <!-- 能源管理：闪电 -->
        <g v-else-if="kind === 'energy'">
          <path d="M50 20 L32 46 H44 L38 68 L60 40 H48 Z" stroke-width="2.4" />
        </g>
        <!-- 设备维保：扳手齿轮 -->
        <g v-else-if="kind === 'maint'">
          <circle cx="38" cy="50" r="10" stroke-width="2.4" />
          <circle cx="38" cy="50" r="3.4" stroke-width="2" />
          <path d="M46 42 L62 26 L68 32 L52 48" stroke-width="2.6" />
          <path d="M60 24 L70 34" stroke-width="2.2" />
        </g>
        <!-- AI设备知识库：书页 -->
        <g v-else-if="kind === 'eqkb'">
          <path d="M24 28 C32 24 40 24 44 28 C48 24 56 24 64 28 V62 C56 58 48 58 44 62 C40 58 32 58 24 62 Z" stroke-width="2.4" />
          <path d="M44 28 V62" stroke-width="2.2" />
          <path d="M54 22 L58 18 L62 24" stroke-width="2.2" />
        </g>
        <!-- 图档管理：图纸折角 -->
        <g v-else-if="kind === 'draw'">
          <path d="M28 24 H52 L64 36 V64 H28 Z" stroke-width="2.4" />
          <path d="M52 24 V36 H64" stroke-width="2.2" />
          <path d="M36 46 H56 M36 54 H50" stroke-width="2.2" />
        </g>
        <!-- ESOP：双页工序 -->
        <g v-else-if="kind === 'esop'">
          <rect x="24" y="26" width="28" height="36" rx="3" stroke-width="2.2" />
          <rect x="36" y="22" width="28" height="36" rx="3" stroke-width="2.4" />
          <path d="M42 34 H56 M42 42 H52" stroke-width="2.1" />
        </g>
        <!-- 安全生产：摄像头 -->
        <g v-else-if="kind === 'safety'">
          <rect x="22" y="30" width="28" height="22" rx="4" stroke-width="2.4" />
          <path d="M50 36 L64 28 V54 L50 46 Z" stroke-width="2.4" />
          <circle cx="36" cy="41" r="5" stroke-width="2.2" />
          <path d="M30 62 H58" stroke-width="2.2" />
        </g>
        <!-- 入场预约：门禁人 -->
        <g v-else-if="kind === 'visit'">
          <circle cx="36" cy="30" r="7" stroke-width="2.4" />
          <path d="M24 58 C24 46 30 42 36 42 C42 42 48 46 48 58" stroke-width="2.4" />
          <path d="M54 34 H68 M62 26 V42" stroke-width="2.6" />
        </g>
        <!-- 企业终端安全：盾锁 -->
        <g v-else-if="kind === 'edlp'">
          <path d="M44 20 L64 28 V46 C64 58 52 66 44 70 C36 66 24 58 24 46 V28 Z" stroke-width="2.4" />
          <rect x="36" y="42" width="16" height="12" rx="2" stroke-width="2.1" />
          <path d="M40 42 V38 C40 35 42 33 44 33 C46 33 48 35 48 38 V42" stroke-width="2.1" />
        </g>
        <!-- 知识库研发辅助：烧瓶 -->
        <g v-else-if="kind === 'rdkb'">
          <path d="M36 22 H52 M40 22 V38 L28 60 H60 L48 38 V22" stroke-width="2.4" />
          <path d="M32 52 H56" stroke-width="2.2" />
          <circle cx="58" cy="28" r="4" stroke-width="2" />
        </g>
        <!-- AI排程：甘特条 -->
        <g v-else-if="kind === 'aps'">
          <path d="M24 24 V64 M24 64 H66" stroke-width="2.2" />
          <path d="M30 32 H52 M30 44 H62 M30 56 H44" stroke-width="3.2" />
        </g>
        <!-- AI凭证：票据章 -->
        <g v-else-if="kind === 'voucher'">
          <rect x="26" y="24" width="36" height="42" rx="3" stroke-width="2.4" />
          <path d="M34 36 H54 M34 44 H48" stroke-width="2.2" />
          <circle cx="54" cy="56" r="8" stroke-width="2.2" />
          <path d="M50 56 L53 59 L58 52" stroke-width="2.2" />
        </g>
        <!-- AI自动对账：双列对齐 -->
        <g v-else-if="kind === 'recon'">
          <rect x="22" y="24" width="18" height="40" rx="3" stroke-width="2.2" />
          <rect x="48" y="24" width="18" height="40" rx="3" stroke-width="2.2" />
          <path d="M42 36 H46 M42 44 H46 M42 52 H46" stroke-width="2.4" />
        </g>
        <!-- 协同办公：三人 -->
        <g v-else-if="kind === 'oa'">
          <circle cx="44" cy="28" r="6.5" stroke-width="2.3" />
          <path d="M32 50 C32 40 38 38 44 38 C50 38 56 40 56 50" stroke-width="2.3" />
          <circle cx="26" cy="36" r="5" stroke-width="2.1" />
          <path d="M16 58 C16 50 20 48 26 48 C32 48 36 50 36 56" stroke-width="2.1" />
          <circle cx="62" cy="36" r="5" stroke-width="2.1" />
          <path d="M52 56 C52 50 56 48 62 48 C68 48 72 50 72 58" stroke-width="2.1" />
        </g>
        <!-- 未登记模块 -->
        <g v-else>
          <path d="M32 50 L44 28 L56 50 Z" stroke-width="2.2" />
          <circle cx="44" cy="28" r="3.2" fill="#fff" stroke="none" />
          <circle cx="32" cy="50" r="3.2" fill="#fff" stroke="none" />
          <circle cx="56" cy="50" r="3.2" fill="#fff" stroke="none" />
        </g>
      </g>
    </g>
  </svg>
</template>

<script setup>
import { computed } from 'vue'

const KIND = {
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

const props = defineProps({
  code: { type: String, default: '' },
  size: { type: String, default: 'md' }
})

const kind = computed(() => KIND[(props.code || '').trim()] || 'mesh')

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
