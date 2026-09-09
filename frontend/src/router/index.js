import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { public: true, title: '登录' },
  },
  {
    path: '/',
    name: 'todos',
    component: () => import('../views/TodoView.vue'),
    meta: { title: '我的待办' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 登录守卫：未登录只能去 /login
router.beforeEach((to) => {
  const token = localStorage.getItem('tf_token')
  if (!to.meta.public && !token) {
    return { name: 'login', query: to.fullPath !== '/' ? { redirect: to.fullPath } : {} }
  }
  if (to.name === 'login' && token) {
    return { name: 'todos' }
  }
  return true
})

export default router
