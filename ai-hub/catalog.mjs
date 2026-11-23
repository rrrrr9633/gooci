// 已开放功能。编码与控制台门口一致，这里不读控制台库。
export const catalog = [
  { code: 'APP_EQKB', name: 'AI设备知识库', left: '设备', right: '知识' },
  { code: 'APP_SAFETY', name: '安全生产AI行为监控', left: '区域', right: '行为' },
  { code: 'APP_RDKB', name: '知识库研发辅助', left: '主题', right: '要点' },
  { code: 'APP_APS', name: 'AI排程预测', left: '订单', right: '窗口' },
  { code: 'APP_VOUCHER', name: 'AI凭证自动处理', left: '摘要', right: '金额' },
  { code: 'APP_RECON', name: 'AI自动对账', left: '单号', right: '差异' }
]

export function known(code) {
  if (!code) return null
  return catalog.find((item) => item.code === String(code).trim()) || null
}
