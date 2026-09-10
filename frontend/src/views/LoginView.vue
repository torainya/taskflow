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
    <div class="login-card glass">
      <div class="brand-mark">
        <svg viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="#fff" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round">
          <path d="M4 12.5l5 5L20 6.5" />
        </svg>
      </div>
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
  padding: 24px;
}

.login-card {
  width: 420px;
  max-width: 100%;
  padding: 44px 40px 30px;
  border-radius: 28px;
}

.brand-mark {
  width: 52px;
  height: 52px;
  margin: 0 auto;
  border-radius: 16px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #6aa2ff 0%, #2f6bff 100%);
  box-shadow: 0 8px 20px rgba(47, 107, 255, 0.38), inset 0 1px 0 rgba(255, 255, 255, 0.45);
}

.title {
  margin: 16px 0 0;
  text-align: center;
  font-size: 28px;
  font-weight: 600;
  letter-spacing: 0.3px;
  color: #101223;
}

.subtitle {
  margin: 6px 0 0;
  text-align: center;
  color: #7a7f93;
  font-size: 13px;
}

/* iOS 分段控件样式 */
.tabs {
  margin: 22px 0 6px;
}

.tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.tabs :deep(.el-tabs__nav-scroll) {
  display: flex;
  justify-content: center;
}

.tabs :deep(.el-tabs__nav) {
  float: none;
  display: inline-flex;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.75);
  border-radius: 999px;
  padding: 4px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.tabs :deep(.el-tabs__item) {
  border-radius: 999px;
  padding: 0 28px;
  height: 34px;
  line-height: 34px;
  color: #5b6072;
  transition: all 0.2s ease;
}

.tabs :deep(.el-tabs__item.is-active) {
  background: #fff;
  color: #2f6bff;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(60, 64, 120, 0.16);
}

.tabs :deep(.el-tabs__active-bar) {
  display: none;
}

.submit-btn {
  width: 100%;
  margin-top: 8px;
  height: 44px;
  font-size: 15px;
  letter-spacing: 2px;
}

.hint {
  margin-top: 20px;
  text-align: center;
  font-size: 12px;
  color: #7a7f93;
  background: rgba(255, 255, 255, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 14px;
  padding: 10px 12px;
}

.hint code {
  background: rgba(255, 255, 255, 0.7);
  padding: 1px 8px;
  border-radius: 6px;
  color: #3d4152;
}

.hint-action {
  margin-left: 8px;
  color: #2f6bff;
  font-weight: 500;
  cursor: pointer;
}

.hint-action:hover {
  text-decoration: underline;
}
</style>
