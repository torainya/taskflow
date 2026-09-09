<script setup>
import { useRoute } from 'vue-router'
import { useAuthStore } from './stores/auth'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

// 登录页全屏展示，不套壳
const isLoginPage = route.name === 'login'

function handleLogout() {
  auth.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<template>
  <div v-if="isLoginPage" class="fullscreen">
    <router-view />
  </div>

  <div v-else class="app-shell">
    <header class="topbar">
      <div class="brand">
        <el-icon :size="20" color="#2563eb"><Check /></el-icon>
        <span>TaskFlow</span>
      </div>
      <div class="user-area">
        <span class="nickname">你好，{{ auth.displayName || '用户' }}</span>
        <el-button text type="primary" @click="handleLogout">退出登录</el-button>
      </div>
    </header>
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<style>
* {
  box-sizing: border-box;
}
html,
body,
#app {
  margin: 0;
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', Arial, sans-serif;
  background: #f3f5f9;
  color: #1f2937;
}

.fullscreen {
  height: 100%;
}

.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.topbar {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  position: sticky;
  top: 0;
  z-index: 10;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  font-size: 18px;
  color: #111827;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nickname {
  font-size: 14px;
  color: #4b5563;
}

.main-content {
  flex: 1;
  max-width: 1100px;
  width: 100%;
  margin: 0 auto;
  padding: 24px 20px 48px;
}
</style>
