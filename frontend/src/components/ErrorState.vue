<script setup lang="ts">
withDefaults(
  defineProps<{
    message: string
    retryable?: boolean
  }>(),
  { retryable: true },
)

defineEmits<{
  retry: []
}>()
</script>

<template>
  <div class="error-state" role="alert">
    <strong>加载失败</strong>
    <p>{{ message }}</p>
    <el-button v-if="retryable" @click="$emit('retry')">重新加载</el-button>
  </div>
</template>
