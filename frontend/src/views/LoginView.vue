<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

import { useAuthStore } from '@/stores/auth'
import type { LoginPayload } from '@/types/auth'
import { errorMessage } from '@/utils/error'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const serverError = ref('')
const model = reactive<LoginPayload>({ username: '', password: '' })

const rules: FormRules<LoginPayload> = {
  username: [
    { required: true, whitespace: true, message: '请输入用户名', trigger: 'blur' },
    { max: 50, message: '用户名最多50个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { max: 72, message: '密码最多72个字符', trigger: 'blur' },
  ],
}

function safeRedirect(): string {
  const redirect = route.query.redirect
  return typeof redirect === 'string' && redirect.startsWith('/') && !redirect.startsWith('//')
    ? redirect
    : '/articles'
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid || submitting.value) return

  submitting.value = true
  serverError.value = ''
  try {
    await authStore.login({
      username: model.username.trim(),
      password: model.password,
    })
    ElMessage.success('登录成功')
    await router.replace(safeRedirect())
  } catch (error) {
    serverError.value = errorMessage(error)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <section class="auth-page page-container">
    <div class="auth-card">
      <div class="auth-heading">
        <span class="eyebrow">欢迎回来</span>
        <h1>登录饺子猫博客</h1>
        <p>继续记录你的思考与成长。</p>
      </div>

      <el-alert
        v-if="serverError"
        :title="serverError"
        type="error"
        show-icon
        :closable="false"
        class="form-alert"
      />

      <el-form
        ref="formRef"
        :model="model"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleSubmit"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="model.username"
            maxlength="50"
            autocomplete="username"
            placeholder="请输入用户名"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="model.password"
            type="password"
            maxlength="72"
            show-password
            autocomplete="current-password"
            placeholder="请输入密码"
            @keyup.enter="handleSubmit"
          />
        </el-form-item>
        <el-button
          class="full-button"
          type="primary"
          native-type="submit"
          :loading="submitting"
        >
          登录
        </el-button>
      </el-form>

      <p class="auth-switch">
        还没有账号？<RouterLink to="/register">立即注册</RouterLink>
      </p>
    </div>
  </section>
</template>
