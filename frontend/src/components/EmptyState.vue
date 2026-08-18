<script setup lang="ts">
withDefaults(
  defineProps<{
    title?: string
    description?: string
  }>(),
  {
    title: '这里还没有内容',
    description: '稍后再来看看吧。',
  },
)
</script>

<template>
  <div class="empty-state">
    <div class="empty-symbol">○</div>
    <h2>{{ title }}</h2>
    <p>{{ description }}</p>
    <slot />
  </div>
</template>
