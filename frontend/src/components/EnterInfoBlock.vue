<template>
  <div>
    <section class="org-block">
      <div class="org-block-h">企业信息</div>
      <div class="org-enter-head">
        <div class="org-avatar">企</div>
        <div>
          <div class="org-enter-name">{{ enter.enterName || '—' }}</div>
          <div class="org-enter-sub">{{ blank(enter.industryName) === '待补充' ? '待补充' : enter.industryName }}</div>
        </div>
        <button v-if="editable" class="org-gold" type="button" @click="$emit('edit')">完善企业资料</button>
      </div>
      <hr class="org-dash">
      <div class="org-fields">
        <div><span>企业性质</span><b :class="{ mute: !enter.enterNature }">{{ blank(enter.enterNature) }}</b></div>
        <div><span>成立时间</span><b :class="{ mute: !enter.foundDate }">{{ blank(enter.foundDate) }}</b></div>
        <div><span>公司规模</span><b :class="{ mute: !enter.enterScale }">{{ blank(enter.enterScale) }}</b></div>
        <div><span>所属行业</span><b :class="{ mute: !enter.industryName }">{{ blank(enter.industryName) }}</b></div>
      </div>
    </section>
    <section class="org-block">
      <div class="org-block-h">联系信息</div>
      <div class="org-contact">
        <div><span>联系人</span><b>{{ blank(enter.contactName) }}</b></div>
        <div><span>手机</span><b>{{ blank(enter.contactMobile) }}</b></div>
        <div><span>联系电话</span><b :class="{ mute: !enter.contactPhone }">{{ blank(enter.contactPhone) }}</b></div>
        <div><span>email</span><b :class="{ mute: !enter.contactMail }">{{ blank(enter.contactMail) }}</b></div>
      </div>
      <div class="org-contact wide">
        <div><span>企业地址</span><b :class="{ mute: !enter.addressText }">{{ blank(enter.addressText) }}</b></div>
      </div>
    </section>
  </div>
</template>

<script setup>
defineProps({
  enter: { type: Object, default: () => ({}) },
  editable: { type: Boolean, default: false }
})
defineEmits(['edit'])

function blank(value) {
  return value && String(value).trim() ? value : '待补充'
}
</script>
