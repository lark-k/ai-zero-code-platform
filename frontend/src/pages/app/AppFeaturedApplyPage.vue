<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'

import {
  addAppFeaturedApply,
  deleteAppFeaturedApply,
  getPageQueryFeatureApplyVoList,
  reAddApply,
  updateAppFeaturedApply,
} from '@/api/shenqingyingyongjingxuanxiangguanjiekou.ts'
import { getAppVoByPage } from '@/api/yingyongmokuaixiangguanjiekou.ts'
import {
  FEATURED_APPLY_STATUS,
  FEATURED_APPLY_STATUS_OPTIONS,
  formatDateTime,
  getFeaturedApplyStatusMeta,
} from '@/constants/featuredApply'

type ApplyModalMode = 'create' | 'edit' | 'reapply'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const appLoading = ref(false)
const submitting = ref(false)
const applyList = ref<API.PageQueryFeatureApplyVO[]>([])
const myApps = ref<API.AppVO[]>([])

const queryForm = reactive<API.PageQueryFeatureApplyDTO>({
  id: undefined,
  appId: undefined,
  applyReason: '',
  status: undefined,
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
})

const modalState = reactive<{
  open: boolean
  mode: ApplyModalMode
  id?: number
  appId?: number
  status?: string
  applyReason: string
}>({
  open: false,
  mode: 'create',
  id: undefined,
  appId: undefined,
  status: undefined,
  applyReason: '',
})

const appSelectOptions = computed(() =>
  myApps.value.map((app) => ({
    label: app.appName ? `${app.appName}（ID: ${app.id}）` : `应用 ${app.id}`,
    value: app.id as number,
  })),
)

const modalTitle = computed(() => {
  if (modalState.mode === 'edit') {
    return '编辑精选申请'
  }
  if (modalState.mode === 'reapply') {
    return '重新申请精选'
  }
  return '发起精选申请'
})

const modalSubmitText = computed(() => {
  if (modalState.mode === 'edit') {
    return '保存修改'
  }
  if (modalState.mode === 'reapply') {
    return '重新提交'
  }
  return '提交申请'
})

const getAppName = (appId?: number) => {
  if (!appId) {
    return '-'
  }
  const target = myApps.value.find((item) => item.id === appId)
  return target?.appName || `应用 ${appId}`
}

const canReapply = (status?: string) =>
  status === FEATURED_APPLY_STATUS.REJECTED || status === FEATURED_APPLY_STATUS.CANCELED

const parseQueryId = (value: unknown) => {
  const rawValue = Array.isArray(value) ? value[0] : value
  if (typeof rawValue !== 'string' || rawValue.trim() === '') {
    return undefined
  }
  const parsed = Number(rawValue)
  return Number.isFinite(parsed) ? parsed : undefined
}

const syncAppIdFromRoute = () => {
  const routeAppId = parseQueryId(route.query.appId)
  if (routeAppId) {
    queryForm.appId = routeAppId
    if (!modalState.appId) {
      modalState.appId = routeAppId
    }
  }
}

const resetModal = () => {
  modalState.open = false
  modalState.mode = 'create'
  modalState.id = undefined
  modalState.appId = parseQueryId(route.query.appId)
  modalState.status = undefined
  modalState.applyReason = ''
}

const openCreateModal = () => {
  modalState.mode = 'create'
  modalState.id = undefined
  modalState.appId = parseQueryId(route.query.appId)
  modalState.status = undefined
  modalState.applyReason = ''
  modalState.open = true
}

const openEditModal = (record: API.PageQueryFeatureApplyVO) => {
  modalState.mode = 'edit'
  modalState.id = record.id
  modalState.appId = record.appId
  modalState.status = record.status
  modalState.applyReason = record.applyReason || ''
  modalState.open = true
}

const openReapplyModal = (record: API.PageQueryFeatureApplyVO) => {
  if (!canReapply(record.status)) {
    return
  }
  modalState.mode = 'reapply'
  modalState.id = record.id
  modalState.appId = record.appId
  modalState.status = record.status
  modalState.applyReason = ''
  modalState.open = true
}

const loadMyApps = async () => {
  appLoading.value = true
  try {
    const res = await getAppVoByPage({
      pageNum: 1,
      pageSize: 20,
      sortField: 'updateTime',
      sortOrder: 'descend',
    })
    if (res.data.code !== 0) {
      message.error(res.data.message || '获取应用列表失败')
      return
    }
    myApps.value = res.data.data?.records ?? []
  } catch (error) {
    message.error(error instanceof Error ? error.message : '获取应用列表失败，请稍后重试')
  } finally {
    appLoading.value = false
  }
}

const loadApplyList = async () => {
  loading.value = true
  try {
    const res = await getPageQueryFeatureApplyVoList({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      id: queryForm.id,
      appId: queryForm.appId,
      applyReason: queryForm.applyReason?.trim() || undefined,
      status: queryForm.status,
      sortField: 'createTime',
      sortOrder: 'descend',
    })
    if (res.data.code !== 0) {
      message.error(res.data.message || '获取申请记录失败')
      return
    }
    applyList.value = res.data.data?.records ?? []
    pagination.total = res.data.data?.totalRow ?? 0
  } catch (error) {
    message.error(error instanceof Error ? error.message : '获取申请记录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  pagination.current = 1
  await loadApplyList()
}

const handleReset = async () => {
  queryForm.id = undefined
  queryForm.appId = parseQueryId(route.query.appId)
  queryForm.applyReason = ''
  queryForm.status = undefined
  pagination.current = 1
  await loadApplyList()
}

const handlePageChange = async (page: number, pageSize: number) => {
  pagination.current = page
  pagination.pageSize = pageSize
  await loadApplyList()
}

const handleSubmit = async () => {
  if (!modalState.appId) {
    message.warning('请选择要申请精选的应用')
    return
  }

  const applyReason = modalState.applyReason.trim()
  if (!applyReason) {
    message.warning('请填写申请理由')
    return
  }

  submitting.value = true
  try {
    const res =
      modalState.mode === 'create'
        ? await addAppFeaturedApply({
            appId: modalState.appId,
            applyReason,
          })
        : modalState.mode === 'reapply'
          ? await reAddApply({
              id: modalState.id,
              appId: modalState.appId,
              applyReason,
              status: modalState.status,
            })
          : await updateAppFeaturedApply({
              id: modalState.id,
              applyReason,
            })

    if (res.data.code !== 0) {
      const defaultMessage =
        modalState.mode === 'create'
          ? '提交申请失败'
          : modalState.mode === 'reapply'
            ? '重新申请失败'
            : '更新申请失败'
      message.error(res.data.message || defaultMessage)
      return
    }

    const successMessage =
      modalState.mode === 'create'
        ? '申请已提交'
        : modalState.mode === 'reapply'
          ? '已重新提交申请'
          : '申请已更新'
    message.success(successMessage)
    resetModal()
    await loadApplyList()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '操作失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (id?: number) => {
  if (!id) {
    return
  }
  try {
    const res = await deleteAppFeaturedApply({ id })
    if (res.data.code !== 0) {
      message.error(res.data.message || '删除申请失败')
      return
    }
    message.success('申请已删除')
    if (applyList.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1
    }
    await loadApplyList()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '删除申请失败，请稍后重试')
  }
}

const goToAppDetail = (appId?: number) => {
  if (!appId) {
    return
  }
  void router.push(`/app/detail/${appId}`)
}

watch(
  () => route.query.appId,
  () => {
    syncAppIdFromRoute()
  },
)

onMounted(async () => {
  syncAppIdFromRoute()
  await loadMyApps()
  if (route.query.action === 'create') {
    openCreateModal()
  }
  await loadApplyList()
})
</script>

<template>
  <main class="apply-page">
    <section class="apply-page__hero">
      <div>
        <span class="apply-page__eyebrow">Featured Apply</span>
        <h1>精选应用申请</h1>
        <p>在这里管理你提交给平台的精选申请，支持新增、修改、删除、重新申请和分页查询，也能随时查看审核结果与管理员备注。</p>
      </div>
      <a-button type="primary" size="large" :loading="appLoading" @click="openCreateModal">发起申请</a-button>
    </section>

    <section class="apply-panel">
      <div class="apply-panel__header">
        <div>
          <h2>申请记录</h2>
          <p>如果申请被管理员拒绝或撤销，你可以直接重新申请并补充新的申请理由。</p>
        </div>
      </div>

      <a-form layout="inline" class="apply-form">
        <a-form-item label="申请 ID">
          <a-input-number v-model:value="queryForm.id" placeholder="申请 ID" style="width: 140px" />
        </a-form-item>
        <a-form-item label="应用">
          <a-select
            v-model:value="queryForm.appId"
            show-search
            allow-clear
            placeholder="选择应用"
            style="width: 280px"
            option-filter-prop="label"
            :options="appSelectOptions"
            :loading="appLoading"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="queryForm.status"
            allow-clear
            placeholder="全部状态"
            style="width: 160px"
            :options="FEATURED_APPLY_STATUS_OPTIONS"
          />
        </a-form-item>
        <a-form-item label="申请理由">
          <a-input v-model:value="queryForm.applyReason" allow-clear placeholder="按申请理由搜索" />
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
        class="apply-table"
        :loading="loading"
        :data-source="applyList"
        :pagination="false"
        :scroll="{ x: 1360 }"
      >
        <a-table-column title="申请 ID" data-index="id" width="100" />
        <a-table-column title="应用" width="220">
          <template #default="{ record }">
            <div class="cell-stack">
              <a-button type="link" class="cell-stack__link" @click="goToAppDetail(record.appId)">
                {{ getAppName(record.appId) }}
              </a-button>
              <span class="cell-stack__sub">应用 ID：{{ record.appId ?? '-' }}</span>
            </div>
          </template>
        </a-table-column>
        <a-table-column title="申请理由" data-index="applyReason" ellipsis />
        <a-table-column title="状态" width="120">
          <template #default="{ record }">
            <a-tag :color="getFeaturedApplyStatusMeta(record.status).color">
              {{ getFeaturedApplyStatusMeta(record.status).label }}
            </a-tag>
          </template>
        </a-table-column>
        <a-table-column title="审核备注" width="240">
          <template #default="{ record }">
            <span>{{ record.reviewRemark || '-' }}</span>
          </template>
        </a-table-column>
        <a-table-column title="审核时间" width="190">
          <template #default="{ record }">
            {{ formatDateTime(record.reviewTime) }}
          </template>
        </a-table-column>
        <a-table-column title="创建时间" width="190">
          <template #default="{ record }">
            {{ formatDateTime(record.createTime) }}
          </template>
        </a-table-column>
        <a-table-column title="操作" width="260" fixed="right">
          <template #default="{ record }">
            <a-space wrap>
              <a-button type="link" @click="openEditModal(record)">编辑</a-button>
              <a-button v-if="canReapply(record.status)" type="link" @click="openReapplyModal(record)">
                重新申请
              </a-button>
              <a-popconfirm title="确定删除这条申请吗？" @confirm="handleDelete(record.id)">
                <a-button type="link" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </a-table-column>
      </a-table>

      <div class="apply-panel__pagination">
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
      v-model:open="modalState.open"
      :title="modalTitle"
      :confirm-loading="submitting"
      destroy-on-close
      :ok-text="modalSubmitText"
      cancel-text="取消"
      @ok="handleSubmit"
      @cancel="resetModal"
    >
      <a-form layout="vertical">
        <a-form-item label="选择应用" required>
          <a-select
            v-model:value="modalState.appId"
            show-search
            placeholder="请选择应用"
            option-filter-prop="label"
            :options="appSelectOptions"
            :loading="appLoading"
            :disabled="modalState.mode !== 'create'"
          />
        </a-form-item>
        <a-form-item v-if="modalState.mode === 'reapply'" label="当前状态">
          <a-tag :color="getFeaturedApplyStatusMeta(modalState.status).color">
            {{ getFeaturedApplyStatusMeta(modalState.status).label }}
          </a-tag>
        </a-form-item>
        <a-form-item label="申请理由" required>
          <a-textarea
            v-model:value="modalState.applyReason"
            :auto-size="{ minRows: 4, maxRows: 8 }"
            maxlength="300"
            show-count
            :placeholder="
              modalState.mode === 'reapply'
                ? '请填写新的申请理由，说明这次重新申请的补充点、优化点或管理员反馈后的改进情况。'
                : '请说明为什么这个应用值得进入精选，例如完成度、稳定性、适用场景或视觉表现。'
            "
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </main>
</template>

<style scoped>
.apply-page {
  padding: 22px 0 40px;
}

.apply-page__hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  padding: 34px;
  border: 1px solid rgba(118, 151, 195, 0.14);
  border-radius: 30px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: var(--shadow-soft);
}

.apply-page__eyebrow {
  display: inline-flex;
  padding: 8px 14px;
  color: #0b8f82;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  border-radius: 999px;
  background: rgba(15, 183, 166, 0.1);
}

.apply-page__hero h1 {
  margin: 18px 0 12px;
  font-size: clamp(38px, 5vw, 58px);
}

.apply-page__hero p {
  max-width: 760px;
  margin: 0;
  color: var(--text-secondary);
  font-size: 17px;
  line-height: 1.8;
}

.apply-panel {
  margin-top: 24px;
  padding: 28px;
  border: 1px solid rgba(118, 151, 195, 0.14);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: var(--shadow-soft);
}

.apply-panel__header {
  margin-bottom: 20px;
}

.apply-panel__header h2 {
  margin: 0 0 8px;
  font-size: 28px;
}

.apply-panel__header p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

.apply-form {
  gap: 8px 0;
  margin-bottom: 20px;
}

.apply-table {
  overflow: hidden;
  border-radius: 20px;
}

.cell-stack {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.cell-stack__link {
  width: fit-content;
  padding: 0;
}

.cell-stack__sub {
  color: var(--text-secondary);
  font-size: 12px;
}

.apply-panel__pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

@media (max-width: 900px) {
  .apply-page__hero {
    align-items: flex-start;
    flex-direction: column;
  }

  .apply-panel {
    padding: 20px 16px;
  }

  .apply-panel__pagination {
    justify-content: center;
  }
}
</style>
