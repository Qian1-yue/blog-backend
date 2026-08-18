<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loggingOut = ref(false)
const displayName = computed(
  () => authStore.user?.nickname || authStore.user?.username || '用户',
)

async function handleLogout() {
  if (loggingOut.value) return
  loggingOut.value = true
  try {
    await authStore.logout()
    ElMessage.success('已安全退出')
    await router.push({ name: 'articles' })
  } finally {
    loggingOut.value = false
  }
}
</script>

<template>
  <header class="site-header">
    <div class="header-inner">
      <RouterLink class="brand" to="/articles" aria-label="饺子猫博客首页">
        <span class="brand-mark">饺</span>
        <span>饺子猫博客</span>
      </RouterLink>

      <nav class="main-nav" aria-label="主导航">
        <RouterLink to="/articles">首页</RouterLink>
        <RouterLink v-if="authStore.isLoggedIn" to="/articles/create">
          发布文章
        </RouterLink>
        <RouterLink v-if="authStore.isLoggedIn" to="/my/articles">
          我的文章
        </RouterLink>

        <template v-if="authStore.isLoggedIn">
          <span class="nav-user">你好，{{ displayName }}</span>
          <button
            class="nav-button"
            type="button"
            :disabled="loggingOut"
            @click="handleLogout"
          >
            {{ loggingOut ? '退出中…' : '退出' }}
          </button>
        </template>
        <template v-else>
          <RouterLink to="/login">登录</RouterLink>
          <RouterLink class="nav-register" to="/register">注册</RouterLink>
        </template>
      </nav>
    </div>
  </header>
</template>
