import { request } from '@/api/request'
import type { PageResult } from '@/types/api'
import type {
  ArticleDetail,
  ArticleListItem,
  ArticlePayload,
  CreatedArticle,
} from '@/types/article'

export function listArticles(page: number, size = 10) {
  return request<PageResult<ArticleListItem>>({
    method: 'GET',
    url: '/api/articles',
    params: { page, size },
  })
}

export function listMyArticles(page: number, size = 10) {
  return request<PageResult<ArticleListItem>>({
    method: 'GET',
    url: '/api/articles/mine',
    params: { page, size },
  })
}

export function listHotArticles(limit = 10) {
  return request<ArticleListItem[]>({
    method: 'GET',
    url: '/api/articles/hot',
    params: { limit },
  })
}

export function getArticle(id: number) {
  return request<ArticleDetail>({
    method: 'GET',
    url: `/api/articles/${id}`,
  })
}

export function createArticle(payload: ArticlePayload) {
  return request<CreatedArticle>({
    method: 'POST',
    url: '/api/articles',
    data: payload,
  })
}

export function updateArticle(id: number, payload: ArticlePayload) {
  return request<ArticleDetail>({
    method: 'PUT',
    url: `/api/articles/${id}`,
    data: payload,
  })
}

export function deleteArticle(id: number) {
  return request<Record<string, never>>({
    method: 'DELETE',
    url: `/api/articles/${id}`,
  })
}
