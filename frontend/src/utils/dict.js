// 通用字典与格式化工具

export const licenseStatusMap = {
  NORMAL: { text: '正常', type: 'success' },
  EXPIRING: { text: '临期预警', type: 'warning' },
  EXPIRED: { text: '已过期', type: 'danger' }
}

export const auditStatusMap = {
  PENDING: { text: '待审核', type: 'warning' },
  APPROVED: { text: '审核通过', type: 'success' },
  REJECTED: { text: '已驳回', type: 'danger' }
}

export const regulationTypeMap = {
  REGULATION: { text: '法规', type: 'primary' },
  POLICY: { text: '政策', type: 'success' },
  NEWS: { text: '资讯', type: 'info' }
}

export const questionTypeMap = {
  SINGLE: '单选题',
  MULTI: '多选题',
  JUDGE: '判断题'
}

export const trainingStatusMap = {
  TRAINING: { text: '培训中', type: 'primary' },
  GRADUATED: { text: '已结业', type: 'success' },
  SUSPENDED: { text: '已暂停', type: 'info' }
}

// 安全解析 JSON 字符串
export function parseJSON(str, fallback) {
  if (str == null) return fallback
  if (typeof str !== 'string') return str
  try {
    return JSON.parse(str)
  } catch {
    return fallback
  }
}
