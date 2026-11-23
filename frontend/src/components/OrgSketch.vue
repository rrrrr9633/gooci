<template>
  <div class="org-sketch">
    <svg viewBox="0 0 280 150" fill="none">
      <path d="M140 52 V70 M140 70 H57 M140 70 H223 M57 70 V88 M140 70 V88 M223 70 V88" stroke="#d5e0e8" stroke-width="2"/>
    </svg>
    <div class="box hq">{{ top }}</div>
    <div class="box a">{{ left }}</div>
    <div class="box b">{{ mid }}</div>
    <div class="box c">{{ right }}</div>
  </div>
</template>

<script setup>
defineProps({
  top: { type: String, default: '总公司' },
  left: { type: String, default: '分公司' },
  mid: { type: String, default: '分公司' },
  right: { type: String, default: '分公司' }
})
</script>
