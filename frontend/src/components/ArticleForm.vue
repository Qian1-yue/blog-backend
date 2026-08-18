<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'

import type { ArticlePayload } from '@/types/article'

const props = withDefaults(
  defineProps<{
    initialValue?: Partial<ArticlePayload>
    submitting?: boolean
    submitText?: string
  }>(),
  {
    initialValue: () => ({}),
    submitting: false,
    submitText: '保存文章',
  },
)

const emit = defineEmits<{
  submit: [payload: ArticlePayload]
}>()

const formRef = ref<FormInstance>()
const model = reactive<ArticlePayload>({
  title: '',
  summary: '',
  content: '',
})

const rules: FormRules<ArticlePayload> = {
  title: [
    { required: true, whitespace: true, message: '请输入文章标题', trigger: 'blur' },
    { max: 200, message: '文章标题最多200个字符', trigger: 'blur' },
  ],
  summary: [
    { required: true, whitespace: true, message: '请输入文章摘要', trigger: 'blur' },
    { max: 500, message: '文章摘要最多500个字符', trigger: 'blur' },
  ],
  content: [
    { required: true, whitespace: true, message: '请输入文章正文', trigger: 'blur' },
  ],
}

watch(
  () => props.initialValue,
  (value) => {
    model.title = value.title ?? ''
    model.summary = value.summary ?? ''
    model.content = value.content ?? ''
  },
  { immediate: true, deep: true },
)

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  emit('submit', {
    title: model.title.trim(),
    summary: model.summary.trim(),
    content: model.content.trim(),
  })
}
</script>

<template>
  <el-form
    ref="formRef"
    :model="model"
    :rules="rules"
    label-position="top"
    class="article-form"
    @submit.prevent="handleSubmit"
  >
    <el-form-item label="文章标题" prop="title">
      <el-input
        v-model="model.title"
        maxlength="200"
        show-word-limit
        placeholder="用一句话概括文章主题"
        autocomplete="off"
      />
    </el-form-item>

    <el-form-item label="文章摘要" prop="summary">
      <el-input
        v-model="model.summary"
        type="textarea"
        :rows="3"
        maxlength="500"
        show-word-limit
        resize="vertical"
        placeholder="简要介绍文章内容"
      />
    </el-form-item>

    <el-form-item label="文章正文" prop="content">
      <el-input
        v-model="model.content"
        type="textarea"
        :rows="16"
        resize="vertical"
        placeholder="开始写作吧…"
      />
    </el-form-item>

    <div class="form-actions">
      <el-button native-type="button" @click="$router.back()">取消</el-button>
      <el-button
        type="primary"
        native-type="submit"
        :loading="submitting"
        :disabled="submitting"
      >
        {{ submitText }}
      </el-button>
    </div>
  </el-form>
</template>
