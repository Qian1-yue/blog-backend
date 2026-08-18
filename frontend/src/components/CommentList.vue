<script setup lang="ts">
import type { CommentItem } from '@/types/comment'
import { formatDate } from '@/utils/format'

defineProps<{
  comments: CommentItem[]
}>()
</script>

<template>
  <div class="comment-list">
    <article v-for="comment in comments" :key="comment.id" class="comment-item">
      <div class="comment-head">
        <strong>{{ comment.userNickname }}</strong>
        <time>{{ formatDate(comment.createTime) }}</time>
      </div>
      <p>{{ comment.content }}</p>
    </article>
  </div>
</template>
