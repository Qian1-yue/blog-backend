import { createRouter, createWebHistory } from 'vue-router'

import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior: () => ({ top: 0 }),
  routes: [
    {
      path: '/',
      redirect: '/articles',
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { guestOnly: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { guestOnly: true },
    },
    {
      path: '/articles',
      name: 'articles',
      component: () => import('@/views/ArticleListView.vue'),
    },
    {
      path: '/articles/create',
      name: 'article-create',
      component: () => import('@/views/ArticleCreateView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/articles/:id/edit',
      name: 'article-edit',
      component: () => import('@/views/ArticleEditView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/articles/:id',
      name: 'article-detail',
      component: () => import('@/views/ArticleDetailView.vue'),
    },
    {
      path: '/my/articles',
      name: 'my-articles',
      component: () => import('@/views/MyArticlesView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/NotFoundView.vue'),
    },
  ],
})

router.beforeEach((to) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return {
      name: 'login',
      query: { redirect: to.fullPath },
    }
  }

  if (to.meta.guestOnly && authStore.isLoggedIn) {
    return { name: 'articles' }
  }
})

export default router
