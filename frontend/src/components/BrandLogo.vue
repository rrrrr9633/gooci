<template>
  <span class="brand" :class="{ light }">
    <span class="mark">G</span>
    <span class="word">
      <b>GOOCI</b>
      <em>谷器</em>
    </span>
  </span>
</template>

<script setup>
defineProps({
  light: { type: Boolean, default: false }
})
</script>

<style scoped>
.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: #111;
}
.brand.light { color: #fff; }
.mark {
  width: 34px;
  height: 38px;
  clip-path: polygon(50% 0, 100% 25%, 100% 75%, 50% 100%, 0 75%, 0 25%);
  background: #111;
  color: #fff;
  display: grid;
  place-items: center;
  font-weight: 700;
  font-size: 16px;
}
.brand.light .mark { background: #fff; color: #2b8fb6; }
.word { display: flex; align-items: baseline; gap: 6px; line-height: 1; }
.word b { font-size: 20px; letter-spacing: 0.5px; }
.word em { font-style: normal; font-size: 18px; }
</style>
