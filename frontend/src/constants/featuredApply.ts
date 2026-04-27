export const FEATURED_APPLY_STATUS = {
  PENDING: 'pending',
  APPROVED: 'approved',
  REJECTED: 'rejected',
  CANCELED: 'canceled',
} as const

export const FEATURED_APPLY_PENDING_CHANGED_EVENT = 'featured-apply-pending-changed'

export const FEATURED_APPLY_STATUS_OPTIONS = [
  {
    label: '待审核',
    value: FEATURED_APPLY_STATUS.PENDING,
    color: 'processing',
  },
  {
    label: '已通过',
    value: FEATURED_APPLY_STATUS.APPROVED,
    color: 'success',
  },
  {
    label: '已拒绝',
    value: FEATURED_APPLY_STATUS.REJECTED,
    color: 'error',
  },
  {
    label: '已撤销',
    value: FEATURED_APPLY_STATUS.CANCELED,
    color: 'default',
  },
] as const

export const getFeaturedApplyStatusMeta = (status?: string) =>
  FEATURED_APPLY_STATUS_OPTIONS.find((item) => item.value === status) ?? {
    label: status || '未知状态',
    value: status || '',
    color: 'default',
  }

export const formatDateTime = (time?: string) => {
  if (!time) {
    return '-'
  }

  const date = new Date(time)
  if (Number.isNaN(date.getTime())) {
    return time
  }

  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}
