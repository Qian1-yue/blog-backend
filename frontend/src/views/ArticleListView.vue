<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { listArticles, listHotArticles } from '@/api/article'
import ArticleCard from '@/components/ArticleCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import ErrorState from '@/components/ErrorState.vue'
import LoadingState from '@/components/LoadingState.vue'
import type { PageResult } from '@/types/api'
import type { ArticleListItem } from '@/types/article'
import { errorMessage } from '@/utils/error'
import { positiveInteger } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const error = ref('')
const hotLoading = ref(false)
const hotError = ref('')
const hotArticles = ref<ArticleListItem[]>([])
const pageData = ref<PageResult<ArticleListItem>>({
  current: 1,
  size: 10,
  total: 0,
  pages: 0,
  records: [],
})

async function load() {
  const page = positiveInteger(route.query.page)
  loading.value = true
  error.value = ''
  try {
    pageData.value = await listArticles(page)

    if (pageData.value.pages > 0 && page > pageData.value.pages) {
      await router.replace({ query: { ...route.query, page: pageData.value.pages } })
    }
  } catch (loadError) {
    error.value = errorMessage(loadError)
  } finally {
    loading.value = false
  }
}

async function loadHot() {
  hotLoading.value = true
  hotError.value = ''
  try {
    hotArticles.value = await listHotArticles(10)
  } catch (loadError) {
    hotError.value = errorMessage(loadError)
  } finally {
    hotLoading.value = false
  }
}

function changePage(page: number) {
  void router.push({ query: { ...route.query, page } })
}

watch(() => route.query.page, load, { immediate: true })
onMounted(loadHot)
</script>

<template>
  <section class="page-container list-page">
    <div class="page-hero">
      <div>
        <span class="eyebrow">最新发布</span>
        <h1>发现值得阅读的文章</h1>
        <p>这里记录想法、经验和持续成长的轨迹。</p>
      </div>
      <RouterLink class="hero-action" to="/articles/create">写一篇文章</RouterLink>
    </div>

    <section class="hot-ranking" aria-labelledby="hot-ranking-title">
      <div class="hot-ranking-head">
        <div>
          <span class="eyebrow">本期热门</span>
          <h2 id="hot-ranking-title">热度文章排行榜</h2>
        </div>
        <button
          v-if="hotError"
          class="ranking-retry"
          type="button"
          @click="loadHot"
        >
          重新加载
        </button>
      </div>

      <div v-if="hotLoading" class="ranking-message">正在统计热度…</div>
      <div v-else-if="hotError" class="ranking-message ranking-error">
        {{ hotError }}
      </div>
      <div v-else-if="hotArticles.length === 0" class="ranking-message">
        暂无热度数据，多浏览几篇文章后这里就会出现排行。
      </div>
      <ol v-else class="ranking-list">
        <li v-for="(article, index) in hotArticles" :key="article.id">
          <RouterLink :to="`/articles/${article.id}`">
            <span class="ranking-number">{{ String(index + 1).padStart(2, '0') }}</span>
            <span class="ranking-title">{{ article.title }}</span>
            <span class="ranking-views">{{ article.viewCount }} 次浏览</span>
          </RouterLink>
        </li>
      </ol>
    </section>

    <LoadingState v-if="loading" />
    <ErrorState v-else-if="error" :message="error" @retry="load" />
    <EmptyState
      v-else-if="pageData.records.length === 0"
      title="还没有文章"
      description="成为第一个分享内容的人吧。"
    >
      <RouterLink class="text-link" to="/articles/create">发布第一篇文章</RouterLink>
    </EmptyState>
    <template v-else>
      <div class="article-grid">
        <ArticleCard
          v-for="article in pageData.records"
          :key="article.id"
          :article="article"
        />
      </div>
      <div v-if="pageData.pages > 1" class="pagination-wrap">
        <el-pagination
          background
          layout="prev, pager, next"
          :current-page="pageData.current"
          :page-size="pageData.size"
          :total="pageData.total"
          @current-change="changePage"
        />
      </div>
    </template>
  </section>
</template>
