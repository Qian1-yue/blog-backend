<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { getArticle, updateArticle } from '@/api/article'
import ArticleForm from '@/components/ArticleForm.vue'
import ErrorState from '@/components/ErrorState.vue'
import LoadingState from '@/components/LoadingState.vue'
import { useAuthStore } from '@/stores/auth'
import type { ArticleDetail, ArticlePayload } from '@/types/article'
import { errorMessage } from '@/utils/error'
import { positiveInteger } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const articleId = positiveInteger(route.params.id, 0)
const article = ref<ArticleDetail>()
const loading = ref(true)
const submitting = ref(false)
const error = ref('')
const canEdit = computed(
  () => article.value?.authorId === authStore.user?.id,
)

async function load() {
  if (!articleId) {
    error.value = '文章地址不正确'
    loading.value = false
    return
  }

  loading.value = true
  error.value = ''
  try {
    article.value = await getArticle(articleId)
    if (!canEdit.value) error.value = '你无权编辑这篇文章'
  } catch (loadError) {
    error.value = errorMessage(loadError)
  } finally {
    loading.value = false
  }
}

async function handleSubmit(payload: ArticlePayload) {
  if (!canEdit.value || submitting.value) return
  submitting.value = true
  error.value = ''
  try {
    await updateArticle(articleId, payload)
    ElMessage.success('文章修改成功')
    await router.replace(`/articles/${articleId}`)
  } catch (submitError) {
    error.value = errorMessage(submitError)
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="page-container editor-page">
    <div class="page-heading">
      <span class="eyebrow">修改内容</span>
      <h1>编辑文章</h1>
      <p>保存后会立即更新文章内容。</p>
    </div>
    <LoadingState v-if="loading" />
    <ErrorState
      v-else-if="error && !article"
      :message="error"
      :retryable="Boolean(articleId)"
      @retry="load"
    />
    <div v-else-if="article && canEdit" class="editor-card">
      <el-alert
        v-if="error"
        :title="error"
        type="error"
        show-icon
        :closable="false"
        class="form-alert"
      />
      <ArticleForm
        :initial-value="article"
        :submitting="submitting"
        submit-text="保存修改"
        @submit="handleSubmit"
      />
    </div>
    <ErrorState v-else :message="error || '你无权编辑这篇文章'" :retryable="false" />
  </section>
</template>
