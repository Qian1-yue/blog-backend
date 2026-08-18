<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ElMessage,
  ElMessageBox,
  type FormInstance,
  type FormRules,
} from 'element-plus'

import { deleteArticle, getArticle } from '@/api/article'
import { createComment, listComments } from '@/api/comment'
import CommentList from '@/components/CommentList.vue'
import EmptyState from '@/components/EmptyState.vue'
import ErrorState from '@/components/ErrorState.vue'
import LoadingState from '@/components/LoadingState.vue'
import { useAuthStore } from '@/stores/auth'
import type { PageResult } from '@/types/api'
import type { ArticleDetail } from '@/types/article'
import type { CommentItem } from '@/types/comment'
import { errorMessage } from '@/utils/error'
import { formatDate, positiveInteger } from '@/utils/format'

interface CommentForm {
  content: string
}

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const articleId = positiveInteger(route.params.id, 0)
const article = ref<ArticleDetail>()
const articleLoading = ref(true)
const articleError = ref('')
const commentsLoading = ref(true)
const commentsError = ref('')
const deleting = ref(false)
const submittingComment = ref(false)
const commentFormRef = ref<FormInstance>()
const commentModel = reactive<CommentForm>({ content: '' })
const comments = ref<PageResult<CommentItem>>({
  current: 1,
  size: 10,
  total: 0,
  pages: 0,
  records: [],
})

const canManage = computed(
  () => article.value?.authorId === authStore.user?.id,
)

const commentRules: FormRules<CommentForm> = {
  content: [
    { required: true, whitespace: true, message: '请输入评论内容', trigger: 'blur' },
    { max: 1000, message: '评论最多1000个字符', trigger: 'blur' },
  ],
}

async function loadArticle() {
  if (!articleId) {
    articleError.value = '文章地址不正确'
    articleLoading.value = false
    return
  }

  articleLoading.value = true
  articleError.value = ''
  try {
    article.value = await getArticle(articleId)
  } catch (loadError) {
    articleError.value = errorMessage(loadError)
  } finally {
    articleLoading.value = false
  }
}

async function loadComments(page = comments.value.current || 1) {
  if (!articleId) {
    commentsLoading.value = false
    return
  }

  commentsLoading.value = true
  commentsError.value = ''
  try {
    comments.value = await listComments(articleId, page)
  } catch (loadError) {
    commentsError.value = errorMessage(loadError)
  } finally {
    commentsLoading.value = false
  }
}

async function handleDelete() {
  if (!canManage.value || deleting.value) return

  try {
    await ElMessageBox.confirm(
      '删除后文章和评论将不再显示，确定继续吗？',
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

  deleting.value = true
  try {
    await deleteArticle(articleId)
    ElMessage.success('文章已删除')
    await router.replace({ name: 'my-articles' })
  } catch (deleteError) {
    ElMessage.error(errorMessage(deleteError))
  } finally {
    deleting.value = false
  }
}

async function submitComment() {
  const valid = await commentFormRef.value?.validate().catch(() => false)
  if (!valid || submittingComment.value) return

  submittingComment.value = true
  try {
    await createComment(articleId, commentModel.content.trim())
    commentModel.content = ''
    commentFormRef.value?.clearValidate()
    ElMessage.success('评论发表成功')
    await loadComments(1)
  } catch (submitError) {
    ElMessage.error(errorMessage(submitError))
  } finally {
    submittingComment.value = false
  }
}

onMounted(() => {
  void Promise.all([loadArticle(), loadComments(1)])
})
</script>

<template>
  <section class="page-container detail-page">
    <LoadingState v-if="articleLoading" />
    <ErrorState
      v-else-if="articleError || !article"
      :message="articleError || '文章不存在'"
      :retryable="Boolean(articleId)"
      @retry="loadArticle"
    />
    <template v-else>
      <article class="article-detail">
        <header class="article-detail-head">
          <span class="eyebrow">文章详情</span>
          <h1>{{ article.title }}</h1>
          <p class="article-lead">{{ article.summary }}</p>
          <div class="article-meta detail-meta">
            <span>作者 {{ article.authorNickname }}</span>
            <span>{{ formatDate(article.createTime) }}</span>
            <span>浏览 {{ article.viewCount }}</span>
          </div>
          <div v-if="canManage" class="article-actions">
            <RouterLink :to="`/articles/${article.id}/edit`">
              <el-button>编辑文章</el-button>
            </RouterLink>
            <el-button type="danger" plain :loading="deleting" @click="handleDelete">
              删除文章
            </el-button>
          </div>
        </header>
        <div class="article-body">{{ article.content }}</div>
      </article>

      <section class="comments-section">
        <div class="section-heading">
          <div>
            <span class="eyebrow">交流讨论</span>
            <h2>评论 {{ comments.total }}</h2>
          </div>
        </div>

        <div v-if="authStore.isLoggedIn" class="comment-form-card">
          <el-form
            ref="commentFormRef"
            :model="commentModel"
            :rules="commentRules"
            @submit.prevent="submitComment"
          >
            <el-form-item prop="content">
              <el-input
                v-model="commentModel.content"
                type="textarea"
                :rows="4"
                maxlength="1000"
                show-word-limit
                resize="vertical"
                placeholder="友善地分享你的看法…"
              />
            </el-form-item>
            <div class="comment-submit">
              <el-button
                type="primary"
                native-type="submit"
                :loading="submittingComment"
              >
                发表评论
              </el-button>
            </div>
          </el-form>
        </div>
        <div v-else class="login-prompt">
          <RouterLink :to="{ name: 'login', query: { redirect: route.fullPath } }">
            登录后参与评论
          </RouterLink>
        </div>

        <LoadingState v-if="commentsLoading" />
        <ErrorState
          v-else-if="commentsError"
          :message="commentsError"
          @retry="loadComments()"
        />
        <EmptyState
          v-else-if="comments.records.length === 0"
          title="还没有评论"
          description="期待你的第一条留言。"
        />
        <template v-else>
          <CommentList :comments="comments.records" />
          <div v-if="comments.pages > 1" class="pagination-wrap">
            <el-pagination
              background
              layout="prev, pager, next"
              :current-page="comments.current"
              :page-size="comments.size"
              :total="comments.total"
              @current-change="loadComments"
            />
          </div>
        </template>
      </section>
    </template>
  </section>
</template>
