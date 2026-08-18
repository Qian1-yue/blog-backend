<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

import { useAuthStore } from '@/stores/auth'
import { errorMessage } from '@/utils/error'

interface RegisterForm {
  username: string
  nickname: string
  password: string
  confirmPassword: string
}

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const serverError = ref('')
const model = reactive<RegisterForm>({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
})

const rules: FormRules<RegisterForm> = {
  username: [
    { required: true, whitespace: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度必须为3到50个字符', trigger: 'blur' },
  ],
  nickname: [
    { required: true, whitespace: true, message: '请输入昵称', trigger: 'blur' },
    { max: 50, message: '昵称最多50个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 72, message: '密码长度必须为6到72个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== model.password) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur',
    },
  ],
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid || submitting.value) return

  submitting.value = true
  serverError.value = ''
  try {
    await authStore.register({
      username: model.username.trim(),
      nickname: model.nickname.trim(),
      password: model.password,
    })
    ElMessage.success('注册成功，请登录')
    await router.replace({ name: 'login' })
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
        <span class="eyebrow">加入饺子猫</span>
        <h1>创建你的账号</h1>
        <p>几步即可开始发布文章。</p>
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
          <el-input v-model="model.username" maxlength="50" autocomplete="username" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="model.nickname" maxlength="50" autocomplete="nickname" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="model.password"
            type="password"
            maxlength="72"
            show-password
            autocomplete="new-password"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="model.confirmPassword"
            type="password"
            maxlength="72"
            show-password
            autocomplete="new-password"
            @keyup.enter="handleSubmit"
          />
        </el-form-item>
        <el-button
          class="full-button"
          type="primary"
          native-type="submit"
          :loading="submitting"
        >
          注册
        </el-button>
      </el-form>

      <p class="auth-switch">
        已有账号？<RouterLink to="/login">返回登录</RouterLink>
      </p>
    </div>
  </section>
</template>
