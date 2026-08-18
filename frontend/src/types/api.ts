export interface ApiResult<T> {
  code: number
  msg: string
  data: T
}

export interface PageResult<T> {
  current: number
  size: number
  total: number
  pages: number
  records: T[]
}
