export interface ArticleListItem {
  id: number
  title: string
  summary: string
  authorId: number
  viewCount: number
  createTime: string
}

export interface ArticleDetail extends ArticleListItem {
  content: string
  authorNickname: string
  updateTime: string
}

export interface ArticlePayload {
  title: string
  summary: string
  content: string
}

export interface CreatedArticle extends ArticleListItem {
  content: string
  status: number
  updateTime: string
}
