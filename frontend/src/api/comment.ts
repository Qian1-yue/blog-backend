import { request } from '@/api/request'
import type { PageResult } from '@/types/api'
import type { CommentItem } from '@/types/comment'

export function listComments(articleId: number, page: number, size = 10) {
  return request<PageResult<CommentItem>>({
    method: 'GET',
    url: `/api/articles/${articleId}/comments`,
    params: { page, size },
  })
}

export function createComment(articleId: number, content: string) {
  return request<CommentItem>({
    method: 'POST',
    url: `/api/articles/${articleId}/comments`,
    data: { content },
  })
}
