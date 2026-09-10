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
  <!-- 全局液态玻璃背景：缓慢漂移的极光色块 -->
  <div class="aurora" aria-hidden="true">
    <span class="blob b1"></span>
    <span class="blob b2"></span>
    <span class="blob b3"></span>
  </div>

  <div v-if="isLoginPage" class="fullscreen">
    <router-view />
  </div>

  <div v-else class="app-shell">
    <header class="topbar glass">
      <div class="brand">
        <span class="brand-logo">
          <el-icon :size="16"><Check /></el-icon>
        </span>
        <span>TaskFlow</span>
      </div>
      <div class="user-area">
        <span class="nickname">你好，{{ auth.displayName || '用户' }}</span>
        <el-button text class="logout-btn" @click="handleLogout">退出登录</el-button>
      </div>
    </header>
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<style>
/* ============================================================
   Liquid Glass 设计系统（全局，非 scoped）
   ============================================================ */
:root {
  --el-color-primary: #3d7bff;
  --el-color-primary-light-3: #79a5ff;
  --el-color-primary-light-5: #9dbdff;
  --el-color-primary-light-7: #c2d5ff;
  --el-color-primary-light-8: #d5e2ff;
  --el-color-primary-light-9: #eaf1ff;
  --el-color-primary-dark-2: #2f6bff;
  --el-border-radius-base: 12px;
  --el-text-color-primary: #1d1d2b;
  --el-text-color-regular: #3d4152;
  --el-font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'PingFang SC',
    'Hiragino Sans GB', 'Microsoft YaHei', 'Segoe UI', sans-serif;
}

* {
  box-sizing: border-box;
}

html,
body,
#app {
  margin: 0;
  height: 100%;
  font-family: var(--el-font-family);
  background: linear-gradient(180deg, #eaf2ff 0%, #f2efff 48%, #fdf1f6 100%);
  color: #1d1d2b;
  -webkit-font-smoothing: antialiased;
}

/* ---------- 极光背景 ---------- */
.aurora {
  position: fixed;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  pointer-events: none;
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
  opacity: 0.5;
}

.b1 {
  width: 480px;
  height: 480px;
  background: #9ec1ff;
  top: -140px;
  left: -100px;
  animation: drift1 18s ease-in-out infinite alternate;
}

.b2 {
  width: 440px;
  height: 440px;
  background: #cbb4ff;
  top: 18%;
  right: -140px;
  animation: drift2 22s ease-in-out infinite alternate;
}

.b3 {
  width: 400px;
  height: 400px;
  background: #ffb7d0;
  bottom: -140px;
  left: 32%;
  animation: drift3 26s ease-in-out infinite alternate;
}

@keyframes drift1 {
  to {
    transform: translate(70px, 50px) scale(1.1);
  }
}

@keyframes drift2 {
  to {
    transform: translate(-60px, 60px) scale(0.94);
  }
}

@keyframes drift3 {
  to {
    transform: translate(50px, -50px) scale(1.12);
  }
}

@media (prefers-reduced-motion: reduce) {
  .blob {
    animation: none;
  }
}

/* ---------- 玻璃面板通用 ---------- */
.glass {
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(28px) saturate(160%);
  -webkit-backdrop-filter: blur(28px) saturate(160%);
  border: 1px solid rgba(255, 255, 255, 0.65);
  box-shadow: 0 8px 32px rgba(60, 64, 120, 0.12), inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.fullscreen {
  height: 100%;
  position: relative;
  z-index: 1;
}

.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
}

/* ---------- 悬浮玻璃顶栏 ---------- */
.topbar {
  position: sticky;
  top: 14px;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 58px;
  max-width: 1100px;
  width: calc(100% - 40px);
  margin: 14px auto 0;
  padding: 0 20px;
  border-radius: 20px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  font-size: 17px;
  letter-spacing: 0.2px;
  color: #101223;
}

.brand-logo {
  width: 30px;
  height: 30px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  color: #fff;
  background: linear-gradient(135deg, #6aa2ff 0%, #2f6bff 100%);
  box-shadow: 0 4px 12px rgba(47, 107, 255, 0.35);
}

.user-area {
  display: flex;
  align-items: center;
  gap: 10px;
}

.nickname {
  font-size: 13px;
  color: #3d4152;
  background: rgba(255, 255, 255, 0.55);
  border: 1px solid rgba(255, 255, 255, 0.7);
  padding: 6px 14px;
  border-radius: 999px;
}

.logout-btn {
  border-radius: 999px;
}

.main-content {
  flex: 1;
  max-width: 1100px;
  width: 100%;
  margin: 0 auto;
  padding: 26px 20px 48px;
}

/* ============================================================
   Element Plus 组件玻璃化
   ============================================================ */

/* 输入框 / 文本域：半透明玻璃 */
.el-input__wrapper,
.el-textarea__inner {
  background: rgba(255, 255, 255, 0.55);
  border-radius: 12px;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.8) inset,
    0 1px 2px rgba(31, 38, 135, 0.05);
  transition: box-shadow 0.2s ease, background 0.2s ease;
}

.el-input__wrapper.is-focus,
.el-textarea__inner:focus {
  background: rgba(255, 255, 255, 0.75);
  box-shadow: 0 0 0 1.5px rgba(61, 123, 255, 0.55) inset,
    0 2px 10px rgba(61, 123, 255, 0.12);
}

/* 按钮：胶囊形 */
.el-button {
  border-radius: 999px;
}

.el-button--large {
  border-radius: 999px;
}

.el-button--primary {
  background: linear-gradient(135deg, #6aa2ff 0%, #2f6bff 100%);
  border: none;
  box-shadow: 0 6px 18px rgba(47, 107, 255, 0.35);
  transition: transform 0.15s ease, box-shadow 0.2s ease;
}

.el-button--primary:hover {
  background: linear-gradient(135deg, #5c98ff 0%, #2661f5 100%);
  transform: translateY(-1px);
  box-shadow: 0 8px 22px rgba(47, 107, 255, 0.42);
}

.el-button--primary:active {
  transform: translateY(0);
}

.el-button:not(.el-button--primary):not(.is-text):not(.is-link) {
  background: rgba(255, 255, 255, 0.55);
  border-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

/* 卡片：透明容器，玻璃由外层负责 */
.el-card {
  --el-card-bg-color: transparent;
  border: none;
  box-shadow: none;
  border-radius: 24px;
}

/* 表格：完全透明，融入玻璃卡片 */
.el-table {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: transparent;
  --el-table-row-hover-bg-color: rgba(255, 255, 255, 0.5);
  --el-table-border-color: rgba(120, 125, 160, 0.16);
  background: transparent;
  color: #2a2d3a;
}

.el-table th.el-table__cell {
  background: transparent;
  color: #7a7f93;
  font-weight: 500;
}

.el-table .el-table__body tr:hover > td.el-table__cell {
  background: rgba(255, 255, 255, 0.5);
}

/* 固定列：加一层磨砂避免内容透出 */
.el-table .el-table-fixed-column--right,
.el-table .el-table-fixed-column--left {
  background: rgba(250, 251, 255, 0.72) !important;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

/* 标签：胶囊形 */
.el-tag {
  border-radius: 999px;
}

/* 弹窗：玻璃面板 */
.el-overlay {
  background: rgba(30, 34, 60, 0.28);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
}

.el-dialog {
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(32px) saturate(160%);
  -webkit-backdrop-filter: blur(32px) saturate(160%);
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 24px;
  box-shadow: 0 24px 64px rgba(50, 50, 93, 0.25);
}

.el-dialog__title {
  color: #101223;
  font-weight: 600;
}

/* 下拉 / 气泡：磨砂玻璃 */
.el-popper.is-light {
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(24px) saturate(160%);
  -webkit-backdrop-filter: blur(24px) saturate(160%);
  border: 1px solid rgba(255, 255, 255, 0.75);
  border-radius: 14px;
  box-shadow: 0 12px 36px rgba(50, 50, 93, 0.18);
}

.el-popper.is-light .el-popper__arrow::before {
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(255, 255, 255, 0.75);
}

/* 日期选择面板 */
.el-picker-panel {
  background: transparent;
  border: none;
}

/* 消息条 / 确认框 */
.el-message {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.75);
  border-radius: 999px;
  box-shadow: 0 8px 24px rgba(50, 50, 93, 0.16);
}

.el-message-box {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(28px) saturate(160%);
  -webkit-backdrop-filter: blur(28px) saturate(160%);
  border: 1px solid rgba(255, 255, 255, 0.75);
  border-radius: 20px;
}

/* 分页 */
.el-pagination.is-background .el-pager li,
.el-pagination.is-background .btn-prev,
.el-pagination.is-background .btn-next {
  background: rgba(255, 255, 255, 0.55);
  border-radius: 999px;
}

.el-pagination.is-background .el-pager li.is-active {
  background: linear-gradient(135deg, #6aa2ff 0%, #2f6bff 100%);
}
</style>
