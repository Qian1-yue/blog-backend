import { createApp } from 'vue'
import { createPinia } from 'pinia'
import {
  ElAlert,
  ElButton,
  ElForm,
  ElFormItem,
  ElInput,
  ElPagination,
} from 'element-plus'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'
import { setUnauthorizedHandler } from '@/api/request'
import { useAuthStore } from '@/stores/auth'
import './style.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.component('ElAlert', ElAlert)
app.component('ElButton', ElButton)
app.component('ElForm', ElForm)
app.component('ElFormItem', ElFormItem)
app.component('ElInput', ElInput)
app.component('ElPagination', ElPagination)

const authStore = useAuthStore(pinia)

setUnauthorizedHandler(() => {
  const redirect = router.currentRoute.value.fullPath
  authStore.clearSession()

  if (router.currentRoute.value.name !== 'login') {
    void router.replace({ name: 'login', query: { redirect } })
  }
})

await authStore.restoreSession()
app.mount('#app')
