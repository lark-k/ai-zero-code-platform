<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'

import {
  adminPageQueryApply,
  agreeApply,
  cancelApply,
  disagreeApply,
} from '@/api/shenqingyingyongjingxuanxiangguanjiekou.ts'
import { getAppVoById } from '@/api/yingyongmokuaixiangguanjiekou.ts'
import { getUserVoById } from '@/api/yonghuxiangguanjiekou.ts'
import {
  FEATURED_APPLY_PENDING_CHANGED_EVENT,
  FEATURED_APPLY_STATUS_OPTIONS,
  formatDateTime,
  getFeaturedApplyStatusMeta,
} from '@/constants/featuredApply'

type ReviewAction = 'agree' | 'reject' | 'cancel'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const actionLoading = ref(false)
const applyList = ref<API.AdminCheckVO[]>([])
const appNameMap = reactive<Record<number, string>>({})
const userNameMap = reactive<Record<number, string>>({})

const queryForm = reactive<API.AdminPageQueryFeatureApplyDTO>({
  id: undefined,
  appId: undefined,
  userId: undefined,
  applyReason: '',
  status: undefined,
  reviewRemark: '',
  reviewUserId: undefined,
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
})

const reviewModal = reactive<{
  open: boolean
  action: ReviewAction
  id?: number
  appId?: number
  reviewRemark: string
}>({
  open: false,
  action: 'agree',
  id: undefined,
  appId: undefined,
  reviewRemark: '',
})

const reviewActionText: Record<ReviewAction, string> = {
  agree: '通过',
  reject: '拒绝',
  cancel: '撤销',
}

const parseQueryId = (value: unknown) => {
  const rawValue = Array.isArray(value) ? value[0] : value
  if (typeof rawValue !== 'string' || rawValue.trim() === '') {
    return undefined
  }
  const parsed = Number(rawValue)
  return Number.isFinite(parsed) ? parsed : undefined
}

const notifyPendingCountChanged = () => {
  window.dispatchEvent(new Event(FEATURED_APPLY_PENDING_CHANGED_EVENT))
}

const hydrateRelations = async (records: API.AdminCheckVO[]) => {
  const appIds = [...new Set(records.map((item) => item.appId).filter((item): item is number => Boolean(item)))]
  const userIds = [
    ...new Set(
      records
        .flatMap((item) => [item.userId, item.reviewUserId])
        .filter((item): item is number => Boolean(item)),
    ),
  ]

  const appTasks = appIds
    .filter((id) => !appNameMap[id])
    .map(async (id) => {
      try {
        const res = await getAppVoById({ id })
        if (res.data.code === 0 && res.data.data?.appName) {
          appNameMap[id] = res.data.data.appName
        }
      } catch {
        appNameMap[id] = `应用 ${id}`
      }
    })

  const userTasks = userIds
    .filter((id) => !userNameMap[id])
    .map(async (id) => {
      try {
        const res = await getUserVoById({ id })
        if (res.data.code === 0 && res.data.data) {
          userNameMap[id] = res.data.data.userName || res.data.data.userAccount || `用户 ${id}`
          return
        }
      } catch {
        userNameMap[id] = `用户 ${id}`
      }
    })

  await Promise.all([...appTasks, ...userTasks])
}

const loadApplyList = async () => {
  loading.value = true
  try {
    const res = await adminPageQueryApply({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      id: queryForm.id,
      appId: queryForm.appId,
      userId: queryForm.userId,
      applyReason: queryForm.applyReason?.trim() || undefined,
      status: queryForm.status,
      reviewRemark: queryForm.reviewRemark?.trim() || undefined,
      reviewUserId: queryForm.reviewUserId,
      sortField: 'createTime',
      sortOrder: 'descend',
    })
    if (res.data.code !== 0) {
      message.error(res.data.message || '获取申请记录失败')
      return
    }
    const records = res.data.data?.records ?? []
    applyList.value = records
    pagination.total = res.data.data?.totalRow ?? 0
    await hydrateRelations(records)
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
  queryForm.userId = undefined
  queryForm.applyReason = ''
  queryForm.status = undefined
  queryForm.reviewRemark = ''
  queryForm.reviewUserId = undefined
  pagination.current = 1
  await loadApplyList()
}

const handlePageChange = async (page: number, pageSize: number) => {
  pagination.current = page
  pagination.pageSize = pageSize
  await loadApplyList()
}

const openReviewModal = (action: ReviewAction, record: API.AdminCheckVO) => {
  reviewModal.open = true
  reviewModal.action = action
  reviewModal.id = record.id
  reviewModal.appId = record.appId
  reviewModal.reviewRemark = record.reviewRemark || ''
}

const resetReviewModal = () => {
  reviewModal.open = false
  reviewModal.action = 'agree'
  reviewModal.id = undefined
  reviewModal.appId = undefined
  reviewModal.reviewRemark = ''
}

const submitReview = async () => {
  if (!reviewModal.id) {
    return
  }

  actionLoading.value = true
  try {
    const payload = {
      id: reviewModal.id,
      appId: reviewModal.appId,
      reviewRemark: reviewModal.reviewRemark.trim() || undefined,
    }
    const actionMap = {
      agree: agreeApply,
      reject: disagreeApply,
      cancel: cancelApply,
    }
    const res = await actionMap[reviewModal.action](payload)
    if (res.data.code !== 0) {
      message.error(res.data.message || `${reviewActionText[reviewModal.action]}申请失败`)
      return
    }
    message.success(`已${reviewActionText[reviewModal.action]}申请`)
    resetReviewModal()
    notifyPendingCountChanged()
    await loadApplyList()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '审核操作失败，请稍后重试')
  } finally {
    actionLoading.value = false
  }
}

const getAppName = (appId?: number) => {
  if (!appId) {
    return '-'
  }
  return appNameMap[appId] || `应用 ${appId}`
}

const getUserName = (userId?: number) => {
  if (!userId) {
    return '-'
  }
  return userNameMap[userId] || `用户 ${userId}`
}

const goToAppDetail = (appId?: number) => {
  if (!appId) {
    return
  }
  void router.push(`/app/detail/${appId}`)
}

onMounted(async () => {
  queryForm.appId = parseQueryId(route.query.appId)
  await loadApplyList()
})
</script>

<template>
  <main class="review-page">
    <section class="review-page__hero">
      <div>
        <span class="review-page__eyebrow">Admin Only</span>
        <h1>精选申请审核</h1>
        <p>管理员可以在这里分页查看全部精选申请，并执行通过、拒绝、撤销等审核操作。</p>
      </div>
    </section>

    <section class="review-panel">
      <div class="review-panel__header">
        <div>
          <h2>审核列表</h2>
          <p>支持按申请、应用、用户、状态和审核信息筛选，适合日常运营快速处理精选入口。</p>
        </div>
      </div>

      <a-form layout="inline" class="review-form">
        <a-form-item label="申请 ID">
          <a-input-number v-model:value="queryForm.id" placeholder="申请 ID" style="width: 130px" />
        </a-form-item>
        <a-form-item label="应用 ID">
          <a-input-number v-model:value="queryForm.appId" placeholder="应用 ID" style="width: 130px" />
        </a-form-item>
        <a-form-item label="申请人 ID">
          <a-input-number v-model:value="queryForm.userId" placeholder="申请人 ID" style="width: 140px" />
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
        <a-form-item label="审核备注">
          <a-input v-model:value="queryForm.reviewRemark" allow-clear placeholder="按审核备注搜索" />
        </a-form-item>
        <a-form-item label="审核人 ID">
          <a-input-number
            v-model:value="queryForm.reviewUserId"
            placeholder="审核人 ID"
            style="width: 140px"
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
        class="review-table"
        :loading="loading"
        :data-source="applyList"
        :pagination="false"
        :scroll="{ x: 1680 }"
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
        <a-table-column title="申请人" width="200">
          <template #default="{ record }">
            <div class="cell-stack">
              <span>{{ getUserName(record.userId) }}</span>
              <span class="cell-stack__sub">用户 ID：{{ record.userId ?? '-' }}</span>
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
        <a-table-column title="审核备注" width="220">
          <template #default="{ record }">
            <span>{{ record.reviewRemark || '-' }}</span>
          </template>
        </a-table-column>
        <a-table-column title="审核人" width="180">
          <template #default="{ record }">
            <div class="cell-stack">
              <span>{{ getUserName(record.reviewUserId) }}</span>
              <span class="cell-stack__sub">用户 ID：{{ record.reviewUserId ?? '-' }}</span>
            </div>
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
        <a-table-column title="操作" width="240" fixed="right">
          <template #default="{ record }">
            <a-space wrap>
              <a-button type="link" @click="openReviewModal('agree', record)">通过</a-button>
              <a-button type="link" danger @click="openReviewModal('reject', record)">拒绝</a-button>
              <a-button type="link" @click="openReviewModal('cancel', record)">撤销</a-button>
            </a-space>
          </template>
        </a-table-column>
      </a-table>

      <div class="review-panel__pagination">
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
      v-model:open="reviewModal.open"
      :title="`${reviewActionText[reviewModal.action]}精选申请`"
      :confirm-loading="actionLoading"
      destroy-on-close
      ok-text="确认"
      cancel-text="取消"
      @ok="submitReview"
      @cancel="resetReviewModal"
    >
      <a-form layout="vertical">
        <a-form-item label="审核备注">
          <a-textarea
            v-model:value="reviewModal.reviewRemark"
            :auto-size="{ minRows: 4, maxRows: 8 }"
            maxlength="300"
            show-count
            placeholder="可填写通过说明、拒绝原因或撤销原因。"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </main>
</template>

<style scoped>
.review-page {
  padding: 22px 0 40px;
}

.review-page__hero {
  padding: 34px;
  border: 1px solid rgba(118, 151, 195, 0.14);
  border-radius: 30px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: var(--shadow-soft);
}

.review-page__eyebrow {
  display: inline-flex;
  padding: 8px 14px;
  color: #2d7ff9;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  border-radius: 999px;
  background: rgba(45, 127, 249, 0.08);
}

.review-page__hero h1 {
  margin: 18px 0 12px;
  font-size: clamp(38px, 5vw, 58px);
}

.review-page__hero p {
  max-width: 760px;
  margin: 0;
  color: var(--text-secondary);
  font-size: 17px;
  line-height: 1.8;
}

.review-panel {
  margin-top: 24px;
  padding: 28px;
  border: 1px solid rgba(118, 151, 195, 0.14);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: var(--shadow-soft);
}

.review-panel__header {
  margin-bottom: 20px;
}

.review-panel__header h2 {
  margin: 0 0 8px;
  font-size: 28px;
}

.review-panel__header p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

.review-form {
  gap: 8px 0;
  margin-bottom: 20px;
}

.review-table {
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

.review-panel__pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

@media (max-width: 900px) {
  .review-panel {
    padding: 20px 16px;
  }

  .review-panel__pagination {
    justify-content: center;
  }
}
</style>
