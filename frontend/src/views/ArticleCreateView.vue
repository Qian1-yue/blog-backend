<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { createArticle } from '@/api/article'
import ArticleForm from '@/components/ArticleForm.vue'
import type { ArticlePayload } from '@/types/article'
import { errorMessage } from '@/utils/error'

const router = useRouter()
const submitting = ref(false)
const error = ref('')

async function handleSubmit(payload: ArticlePayload) {
  if (submitting.value) return
  submitting.value = true
  error.value = ''
  try {
    const article = await createArticle(payload)
    ElMessage.success('文章发布成功')
    await router.replace(`/articles/${article.id}`)
  } catch (submitError) {
    error.value = errorMessage(submitError)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <section class="page-container editor-page">
    <div class="page-heading">
      <span class="eyebrow">新文章</span>
      <h1>发布文章</h1>
      <p>先专注内容，排版会保持清晰易读。</p>
    </div>
    <el-alert
      v-if="error"
      :title="error"
      type="error"
      show-icon
      :closable="false"
      class="form-alert"
    />
    <div class="editor-card">
      <ArticleForm
        :submitting="submitting"
        submit-text="发布文章"
        @submit="handleSubmit"
      />
    </div>
  </section>
</template>
