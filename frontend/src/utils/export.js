// 表格数据导出工具

// 将单个单元格值转义为 CSV 安全字符串
function escapeCell(value) {
  if (value == null) return ''
  const str = String(value)
  if (/[",\n\r]/.test(str)) {
    return `"${str.replace(/"/g, '""')}"`
  }
  return str
}

/**
 * 导出 CSV 文件（带 UTF-8 BOM，便于 Excel 正确识别中文）
 * @param {string} filename 文件名（自动追加 .csv）
 * @param {Array<{label:string, prop:string, formatter?:Function}>} columns 列定义
 * @param {Array<Object>} rows 数据行（对象数组）
 */
export function exportCsv(filename, columns, rows) {
  const header = columns.map((c) => escapeCell(c.label)).join(',')
  const lines = (rows || []).map((row) =>
    columns
      .map((c) => {
        const raw = row[c.prop]
        const val = typeof c.formatter === 'function' ? c.formatter(raw, row) : raw
        return escapeCell(val)
      })
      .join(',')
  )
  const content = '﻿' + [header, ...lines].join('\r\n')

  const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename.endsWith('.csv') ? filename : `${filename}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}
