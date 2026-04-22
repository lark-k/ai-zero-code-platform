<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  deleteUser,
  getUserPage,
  saveUser,
  updateUser,
} from '@/api/yonghuxiangguanjiekou.ts'

type ManageMode = 'create' | 'edit'

interface UserFormState {
  id?: number
  userName: string
  userAccount: string
  userAvatar: string
  userProfile: string
  userRole: string
}

const loading = ref(false)
const submitting = ref(false)
const modalOpen = ref(false)
const modalMode = ref<ManageMode>('create')
const userList = ref<API.UserVO[]>([])

const queryForm = reactive<API.QueryUserDTO>({
  pageNum: 1,
  pageSize: 8,
  userName: '',
  userAccount: '',
  userRole: undefined,
})

const pagination = reactive({
  current: 1,
  pageSize: 8,
  total: 0,
})

const formState = reactive<UserFormState>({
  id: undefined,
  userName: '',
  userAccount: '',
  userAvatar: '',
  userProfile: '',
  userRole: 'user',
})

const roleOptions = [
  { label: '普通用户', value: 'user' },
  { label: '管理员', value: 'admin' },
]

const resetFormState = () => {
  formState.id = undefined
  formState.userName = ''
  formState.userAccount = ''
  formState.userAvatar = ''
  formState.userProfile = ''
  formState.userRole = 'user'
}

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getUserPage({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      userName: queryForm.userName || undefined,
      userAccount: queryForm.userAccount || undefined,
      userRole: queryForm.userRole || undefined,
    })
    if (res.data.code !== 0) {
      message.error(res.data.message || '获取用户列表失败')
      return
    }
    userList.value = res.data.data?.records ?? []
    pagination.total = res.data.data?.totalRow ?? 0
  } catch (error) {
    message.error(error instanceof Error ? error.message : '获取用户列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  pagination.current = 1
  await loadUsers()
}

const handleReset = async () => {
  queryForm.userName = ''
  queryForm.userAccount = ''
  queryForm.userRole = undefined
  pagination.current = 1
  await loadUsers()
}

const openCreateModal = () => {
  modalMode.value = 'create'
  resetFormState()
  modalOpen.value = true
}

const openEditModal = (record: API.UserVO) => {
  modalMode.value = 'edit'
  formState.id = record.id
  formState.userName = record.userName || ''
  formState.userAccount = record.userAccount || ''
  formState.userAvatar = record.userAvatar || ''
  formState.userProfile = record.userProfile || ''
  formState.userRole = record.userRole || 'user'
  modalOpen.value = true
}

const validateCreateForm = () => {
  if (!formState.userAccount.trim()) {
    message.warning('请输入账号')
    return false
  }
  if (formState.userAccount.trim().length < 6) {
    message.warning('账号不能少于6位')
    return false
  }
  if (!formState.userRole) {
    message.warning('请选择用户角色')
    return false
  }
  return true
}

const handleSubmit = async () => {
  if (!validateCreateForm()) {
    return
  }

  submitting.value = true
  try {
    if (modalMode.value === 'create') {
      const res = await saveUser({
        userName: formState.userName || undefined,
        userAccount: formState.userAccount.trim(),
        userAvatar: formState.userAvatar || undefined,
        userProfile: formState.userProfile || undefined,
        userRole: formState.userRole,
      })
      if (res.data.code !== 0) {
        message.error(res.data.message || '新增用户失败')
        return
      }
      message.success('新增用户成功，默认密码为 12345678')
    } else {
      const res = await updateUser({
        id: formState.id,
        userName: formState.userName || undefined,
        userAvatar: formState.userAvatar || undefined,
        userProfile: formState.userProfile || undefined,
        userRole: formState.userRole,
      })
      if (res.data.code !== 0) {
        message.error(res.data.message || '更新用户失败')
        return
      }
      message.success('更新用户成功')
    }
    modalOpen.value = false
    resetFormState()
    await loadUsers()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (id?: number) => {
  if (!id) {
    return
  }
  try {
    const res = await deleteUser({ id })
    if (res.data.code !== 0) {
      message.error(res.data.message || '删除用户失败')
      return
    }
    message.success('删除用户成功')
    if (userList.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1
    }
    await loadUsers()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '删除用户失败，请稍后重试')
  }
}

const handlePageChange = async (page: number, pageSize: number) => {
  pagination.current = page
  pagination.pageSize = pageSize
  await loadUsers()
}

onMounted(() => {
  void loadUsers()
})
</script>

<template>
  <main class="manage-page">
    <section class="manage-page__hero">
      <div>
        <span class="manage-page__eyebrow">Admin Only</span>
        <h1>用户管理</h1>
        <p>该页面仅管理员可见且可访问，支持分页查询、新增、编辑和删除用户。</p>
      </div>
      <a-button type="primary" size="large" class="manage-page__primary-btn" @click="openCreateModal">
        新增用户
      </a-button>
    </section>

    <section class="manage-panel">
      <div class="manage-panel__header">
        <div>
          <h2>筛选与列表</h2>
          <p>新增用户时后端会自动设置默认密码为 `12345678`。</p>
        </div>
      </div>

      <a-form layout="inline" class="manage-form">
        <a-form-item label="昵称">
          <a-input v-model:value="queryForm.userName" placeholder="按昵称搜索" allow-clear />
        </a-form-item>
        <a-form-item label="账号">
          <a-input v-model:value="queryForm.userAccount" placeholder="按账号搜索" allow-clear />
        </a-form-item>
        <a-form-item label="角色">
          <a-select
            v-model:value="queryForm.userRole"
            placeholder="全部角色"
            allow-clear
            style="width: 140px"
            :options="roleOptions"
          />
        </a-form-item>
        <a-form-item>
          <a-space wrap>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-table
        row-key="id"
        class="manage-table"
        :loading="loading"
        :data-source="userList"
        :pagination="false"
        :scroll="{ x: 980 }"
      >
        <a-table-column title="ID" data-index="id" width="80" />
        <a-table-column title="头像" width="90">
          <template #default="{ record }">
            <a-avatar :src="record.userAvatar">
              {{ (record.userName || record.userAccount || 'U').slice(0, 1).toUpperCase() }}
            </a-avatar>
          </template>
        </a-table-column>
        <a-table-column title="昵称" data-index="userName" width="160" />
        <a-table-column title="账号" data-index="userAccount" width="180" />
        <a-table-column title="简介" data-index="userProfile" ellipsis />
        <a-table-column title="角色" width="120">
          <template #default="{ record }">
            <a-tag :color="record.userRole === 'admin' ? 'gold' : 'blue'">
              {{ record.userRole === 'admin' ? '管理员' : '普通用户' }}
            </a-tag>
          </template>
        </a-table-column>
        <a-table-column title="创建时间" data-index="createTime" width="200" />
        <a-table-column title="操作" width="180" fixed="right">
          <template #default="{ record }">
            <a-space wrap>
              <a-button type="link" @click="openEditModal(record)">编辑</a-button>
              <a-popconfirm title="确定删除该用户吗？" @confirm="handleDelete(record.id)">
                <a-button type="link" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </a-table-column>
      </a-table>

      <div class="manage-panel__pagination">
        <a-pagination
          :current="pagination.current"
          :page-size="pagination.pageSize"
          :total="pagination.total"
          :show-size-changer="true"
          :show-total="(total: number) => `共 ${total} 条`"
          @change="handlePageChange"
          @showSizeChange="handlePageChange"
        />
      </div>
    </section>

    <a-modal
      v-model:open="modalOpen"
      :title="modalMode === 'create' ? '新增用户' : '编辑用户'"
      :confirm-loading="submitting"
      @ok="handleSubmit"
      @cancel="resetFormState"
    >
      <a-form layout="vertical">
        <a-form-item label="账号" required>
          <a-input
            v-model:value="formState.userAccount"
            :disabled="modalMode === 'edit'"
            placeholder="请输入账号，至少 6 位"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="昵称">
          <a-input v-model:value="formState.userName" placeholder="请输入昵称" allow-clear />
        </a-form-item>
        <a-form-item label="头像地址">
          <a-input v-model:value="formState.userAvatar" placeholder="请输入头像 URL" allow-clear />
        </a-form-item>
        <a-form-item label="个人简介">
          <a-textarea
            v-model:value="formState.userProfile"
            :auto-size="{ minRows: 3, maxRows: 5 }"
            placeholder="请输入个人简介"
          />
        </a-form-item>
        <a-form-item label="角色" required>
          <a-select v-model:value="formState.userRole" :options="roleOptions" />
        </a-form-item>
      </a-form>
    </a-modal>
  </main>
</template>

<style scoped>
.manage-page {
  padding: 22px 0 40px;
}

.manage-page__hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  padding: 34px;
  border-radius: 30px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(118, 151, 195, 0.14);
  box-shadow: var(--shadow-soft);
}

.manage-page__eyebrow {
  display: inline-flex;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(45, 127, 249, 0.08);
  color: #2d7ff9;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.manage-page__hero h1 {
  margin: 18px 0 12px;
  font-size: clamp(40px, 5vw, 60px);
}

.manage-page__hero p {
  max-width: 760px;
  margin: 0;
  color: var(--text-secondary);
  font-size: 17px;
  line-height: 1.8;
}

.manage-page__primary-btn {
  border-radius: 999px;
}

.manage-panel {
  margin-top: 24px;
  padding: 28px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(118, 151, 195, 0.14);
  box-shadow: var(--shadow-soft);
}

.manage-panel__header {
  margin-bottom: 20px;
}

.manage-panel__header h2 {
  margin: 0 0 8px;
  font-size: 28px;
}

.manage-panel__header p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

.manage-form {
  gap: 8px 0;
  margin-bottom: 20px;
}

.manage-table {
  overflow: hidden;
  border-radius: 20px;
}

.manage-panel__pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

@media (max-width: 900px) {
  .manage-page__hero {
    align-items: flex-start;
    flex-direction: column;
  }

  .manage-panel {
    padding: 20px 16px;
  }

  .manage-panel__pagination {
    justify-content: center;
  }
}
</style>
