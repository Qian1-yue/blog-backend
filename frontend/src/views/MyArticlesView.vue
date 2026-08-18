<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

import { deleteArticle, listMyArticles } from '@/api/article'
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
const deletingId = ref<number>()
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
    pageData.value = await listMyArticles(page)

    if (pageData.value.pages > 0 && page > pageData.value.pages) {
      await router.replace({ query: { ...route.query, page: pageData.value.pages } })
    }
  } catch (loadError) {
    error.value = errorMessage(loadError)
  } finally {
    loading.value = false
  }
}

function changePage(page: number) {
  void router.push({ query: { ...route.query, page } })
}

async function handleDelete(article: ArticleListItem) {
  try {
    await ElMessageBox.confirm(
      `确定删除《${article.title}》吗？删除后无法在页面中恢复。`,
      '删除文章',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
  } catch {
    return
  }

  deletingId.value = article.id
  try {
    await deleteArticle(article.id)
    ElMessage.success('文章已删除')

    if (pageData.value.records.length === 1 && pageData.value.current > 1) {
      await router.push({ query: { page: pageData.value.current - 1 } })
    } else {
      await load()
    }
  } catch (deleteError) {
    ElMessage.error(errorMessage(deleteError))
  } finally {
    deletingId.value = undefined
  }
}

watch(() => route.query.page, load, { immediate: true })
</script>

<template>
  <section class="page-container list-page">
    <div class="page-heading heading-with-action">
      <div>
        <span class="eyebrow">个人创作</span>
        <h1>我的文章</h1>
        <p>管理已经发布的内容。</p>
      </div>
      <RouterLink to="/articles/create">
        <el-button type="primary">发布新文章</el-button>
      </RouterLink>
    </div>

    <LoadingState v-if="loading" />
    <ErrorState v-else-if="error" :message="error" @retry="load" />
    <EmptyState
      v-else-if="pageData.records.length === 0"
      title="你还没有发布文章"
      description="从一个小想法开始记录吧。"
    >
      <RouterLink class="text-link" to="/articles/create">去发布文章</RouterLink>
    </EmptyState>
    <template v-else>
      <div class="article-grid">
        <ArticleCard
          v-for="article in pageData.records"
          :key="article.id"
          :article="article"
        >
          <template #actions>
            <div class="card-actions">
              <RouterLink :to="`/articles/${article.id}/edit`">
                <el-button size="small">编辑</el-button>
              </RouterLink>
              <el-button
                size="small"
                type="danger"
                plain
                :loading="deletingId === article.id"
                @click="handleDelete(article)"
              >
                删除
              </el-button>
            </div>
          </template>
        </ArticleCard>
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
