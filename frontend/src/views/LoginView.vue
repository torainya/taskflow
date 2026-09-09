<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const mode = ref('login') // login | register
const loading = ref(false)

const formRef = ref()
const form = reactive({
  username: '',
  password: '',
  nickname: '',
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_]{3,20}$/,
      message: '3~20 位字母、数字或下划线',
      trigger: 'blur',
    },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度 6~32 位', trigger: 'blur' },
  ],
  nickname: [{ max: 20, message: '昵称最长 20 字', trigger: 'blur' }],
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    if (mode.value === 'login') {
      await auth.login({ username: form.username, password: form.password })
      ElMessage.success('欢迎回来！')
    } else {
      await auth.register({
        username: form.username,
        password: form.password,
        nickname: form.nickname || undefined,
      })
      ElMessage.success('注册成功，已自动登录')
    }
    router.push('/')
  } catch {
    // 错误提示已由 http 拦截器统一弹出
  } finally {
    loading.value = false
  }
}

function fillDemo() {
  form.username = 'demo'
  form.password = 'Demo123456'
  mode.value = 'login'
  formRef.value?.clearValidate()
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="title">TaskFlow</h1>
      <p class="subtitle">任务待办 · 全生命周期示例项目</p>

      <el-tabs v-model="mode" class="tabs">
        <el-tab-pane label="登录" name="login" />
        <el-tab-pane label="注册" name="register" />
      </el-tabs>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
        @submit.prevent="submit"
      >
        <el-form-item v-if="mode === 'register'" label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="怎么称呼你？（可选）" clearable />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" autocomplete="username" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="请输入密码"
            autocomplete="current-password"
            @keyup.enter="submit"
          />
        </el-form-item>
        <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="submit">
          {{ mode === 'login' ? '登 录' : '注册并登录' }}
        </el-button>
      </el-form>

      <div class="hint">
        演示账号：<code>demo</code> / <code>Demo123456</code>
        <span class="hint-action" @click="fillDemo">一键填入</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eff6ff 0%, #f5f3ff 50%, #fdf2f8 100%);
}

.login-card {
  width: 400px;
  padding: 40px 36px 28px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(37, 99, 235, 0.08);
}

.title {
  margin: 0;
  text-align: center;
  font-size: 28px;
  color: #111827;
}

.subtitle {
  margin: 6px 0 0;
  text-align: center;
  color: #9ca3af;
  font-size: 13px;
}

.tabs {
  margin: 18px 0 4px;
}

.submit-btn {
  width: 100%;
  margin-top: 6px;
}

.hint {
  margin-top: 18px;
  text-align: center;
  font-size: 12px;
  color: #9ca3af;
}

.hint code {
  background: #f3f4f6;
  padding: 1px 6px;
  border-radius: 4px;
  color: #4b5563;
}

.hint-action {
  margin-left: 8px;
  color: #2563eb;
  cursor: pointer;
}

.hint-action:hover {
  text-decoration: underline;
}
</style>
