export function formatDateTime(value) {
  if (!value) return ''
  const d = new Date(Array.isArray(value) ? joinArray(value) : value)
  if (Number.isNaN(d.getTime())) return String(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

export function relativeTime(value) {
  if (!value) return ''
  const d = new Date(Array.isArray(value) ? joinArray(value) : value)
  if (Number.isNaN(d.getTime())) return String(value)
  const diff = Date.now() - d.getTime()
  const minutes = Math.floor(diff / 60000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes} 分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours} 小时前`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days} 天前`
  return formatDateTime(value)
}

// 后端 LocalDateTime 序列化可能为数组 [y,m,d,h,mi,s]
function joinArray(arr) {
  const [y, m = 1, d = 1, h = 0, mi = 0, s = 0] = arr
  return new Date(y, m - 1, d, h, mi, s)
}

export function formatHours(hours) {
  if (hours === null || hours === undefined) return '0'
  const n = Number(hours)
  return Number.isInteger(n) ? String(n) : n.toFixed(1)
}
