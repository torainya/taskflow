<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import http from '../api/http'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const stats = ref({ todo: 0, inProgress: 0, done: 0, total: 0 })

const page = ref(1)
const size = ref(10)
const keyword = ref('')
const statusFilter = ref('')

const STATUS_META = {
  TODO: { label: '待开始', type: 'info' },
  IN_PROGRESS: { label: '进行中', type: 'warning' },
  DONE: { label: '已完成', type: 'success' },
}

const STATUS_OPTIONS = Object.entries(STATUS_META).map(([value, meta]) => ({ value, label: meta.label }))

function todayStr() {
  const d = new Date()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${m}-${day}`
}

function isOverdue(row) {
  return row.dueDate && row.dueDate < todayStr() && row.status !== 'DONE'
}

async function load() {
  loading.value = true
  try {
    const [listResp, statsResp] = await Promise.all([
      http.get('/todos', {
        params: {
          page: page.value,
          size: size.value,
          status: statusFilter.value || undefined,
          keyword: keyword.value.trim() || undefined,
        },
      }),
      http.get('/todos/stats'),
    ])
    rows.value = listResp.data.items || []
    total.value = listResp.data.total || 0
    stats.value = statsResp.data
  } finally {
    loading.value = false
  }
}

async function search() {
  page.value = 1
  await load()
}

async function resetFilter() {
  keyword.value = ''
  statusFilter.value = ''
  page.value = 1
  await load()
}

// ---------- 新建 / 编辑 ----------
const dialogVisible = ref(false)
const editingId = ref(null)
const saving = ref(false)
const formRef = ref()
const form = reactive({ title: '', description: '', dueDate: '', status: 'TODO' })

const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { max: 200, message: '标题最长 200 字', trigger: 'blur' },
  ],
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { title: '', description: '', dueDate: '', status: 'TODO' })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    title: row.title,
    description: row.description || '',
    dueDate: row.dueDate || '',
    status: row.status,
  })
  dialogVisible.value = true
}

async function submitForm() {
  await formRef.value.validate()
  saving.value = true
  try {
    const payload = {
      title: form.title.trim(),
      description: form.description.trim() || null,
      dueDate: form.dueDate || null,
    }
    if (editingId.value) {
      await http.put(`/todos/${editingId.value}`, { ...payload, status: form.status })
      ElMessage.success('已更新')
    } else {
      await http.post('/todos', payload)
      ElMessage.success('已创建')
    }
    dialogVisible.value = false
    page.value = 1
    await load()
  } finally {
    saving.value = false
  }
}

// ---------- 状态流转 / 删除 ----------
async function changeStatus(row, status) {
  await http.patch(`/todos/${row.id}/status`, { status })
  ElMessage.success(`已标记为「${STATUS_META[status].label}」`)
  await load()
}

async function removeTodo(row) {
  await ElMessageBox.confirm(`确定删除「${row.title}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
  })
  await http.delete(`/todos/${row.id}`)
  ElMessage.success('已删除')
  if (rows.value.length === 1 && page.value > 1) page.value -= 1
  await load()
}

const greeting = computed(() => {
  const h = new Date().getHours()
  return h < 6 ? '夜深了' : h < 12 ? '早上好' : h < 14 ? '中午好' : h < 18 ? '下午好' : '晚上好'
})

onMounted(load)
</script>

<template>
  <div>
    <!-- 问候 + 快捷操作 -->
    <div class="page-head">
      <div>
        <h2>{{ greeting }}，{{ auth.displayName }}</h2>
        <p class="page-sub">把要做的事写下来，一件件完成它。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新建待办</el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-num">{{ stats.todo }}</div>
        <div class="stat-label">待开始</div>
      </div>
      <div class="stat-card">
        <div class="stat-num">{{ stats.inProgress }}</div>
        <div class="stat-label">进行中</div>
      </div>
      <div class="stat-card done">
        <div class="stat-num">{{ stats.done }}</div>
        <div class="stat-label">已完成</div>
      </div>
      <div class="stat-card total">
        <div class="stat-num">{{ stats.total }}</div>
        <div class="stat-label">总计</div>
      </div>
    </div>

    <!-- 筛选区 -->
    <div class="toolbar">
      <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width: 140px" @change="search">
        <el-option v-for="opt in STATUS_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-input
        v-model="keyword"
        placeholder="按标题 / 描述搜索"
        clearable
        style="width: 240px"
        :prefix-icon="Search"
        @keyup.enter="search"
        @clear="search"
      />
      <el-button type="primary" :icon="Search" @click="search">搜索</el-button>
      <el-button :icon="Refresh" @click="resetFilter">重置</el-button>
    </div>

    <!-- 待办表格 -->
    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="rows" empty-text="还没有待办，点击右上角新建一个吧">
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="{ 'done-title': row.status === 'DONE' }">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.description" class="desc">{{ row.description }}</span>
            <span v-else class="empty-desc">—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="130">
          <template #default="{ row }">
            <el-select
              :model-value="row.status"
              size="small"
              class="status-select"
              @change="(v) => changeStatus(row, v)"
            >
              <el-option v-for="opt in STATUS_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="截止日期" width="130">
          <template #default="{ row }">
            <el-tag v-if="row.dueDate" :type="isOverdue(row) ? 'danger' : 'info'" effect="plain" size="small">
              {{ row.dueDate }}
            </el-tag>
            <span v-else class="empty-desc">—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="removeTodo(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="load"
          @size-change="search"
        />
      </div>
    </el-card>

    <!-- 新建 / 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑待办' : '新建待办'"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="200" show-word-limit placeholder="要做点什么？" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            maxlength="1000"
            show-word-limit
            placeholder="补充细节（可选）"
          />
        </el-form-item>
        <div class="dialog-row">
          <el-form-item label="截止日期">
            <el-date-picker
              v-model="form.dueDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选择日期"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item v-if="editingId" label="状态">
            <el-select v-model="form.status" style="width: 100%">
              <el-option v-for="opt in STATUS_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 18px;
}

.page-head h2 {
  margin: 0 0 4px;
  font-size: 22px;
  color: #111827;
}

.page-sub {
  margin: 0;
  color: #9ca3af;
  font-size: 13px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px 20px;
  border-left: 4px solid #94a3b8;
}

.stat-card.done {
  border-left-color: #22c55e;
}

.stat-card.total {
  border-left-color: #2563eb;
}

.stat-num {
  font-size: 26px;
  font-weight: 700;
  color: #111827;
}

.stat-label {
  margin-top: 2px;
  font-size: 12px;
  color: #6b7280;
}

.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}

.table-card {
  border-radius: 10px;
  border-color: #eef0f4;
}

.done-title {
  text-decoration: line-through;
  color: #9ca3af;
}

.desc {
  font-size: 13px;
  color: #6b7280;
}

.empty-desc {
  color: #d1d5db;
}

.status-select {
  width: 110px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}

.dialog-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
</style>
