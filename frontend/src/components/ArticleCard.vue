<script setup lang="ts">
import type { ArticleListItem } from '@/types/article'
import { formatDate } from '@/utils/format'

defineProps<{
  article: ArticleListItem
}>()
</script>

<template>
  <article class="article-card">
    <RouterLink class="article-card-link" :to="`/articles/${article.id}`">
      <h2>{{ article.title }}</h2>
      <p class="article-summary">{{ article.summary }}</p>
      <div class="article-meta">
        <span>{{ formatDate(article.createTime) }}</span>
        <span>浏览 {{ article.viewCount }}</span>
      </div>
    </RouterLink>
    <slot name="actions" />
  </article>
</template>
