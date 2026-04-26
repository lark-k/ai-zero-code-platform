<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  cancelTop,
  deleteAppByAdmin,
  queryPageByAdmin,
  stickToTop,
  updateAppByAdmin,
} from '@/api/yingyongmokuaixiangguanjiekou.ts'
import { useRouter } from 'vue-router'

const APP_GOOD_PRIORITY = 99
const APP_TOP_PRIORITY = 999

const router = useRouter()
const loading = ref(false)
const appList = ref<API.AppVO[]>([])

const queryForm = reactive<API.QueryAppDTO>({
  pageNum: 1,
  pageSize: 10,
  id: undefined,
  appName: '',
  initPrompt: '',
  codeGenType: undefined,
  deployKey: '',
  priority: undefined,
  userId: undefined,
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
})

const getSortTimestamp = (app: API.AppVO) => {
  const time = app.updateTime || app.createTime
  if (!time) {
    return 0
  }
  const timestamp = new Date(time).getTime()
  return Number.isNaN(timestamp) ? 0 : timestamp
}

const sortAppsByPriority = (apps: API.AppVO[] = []) =>
  [...apps].sort((prev, next) => {
    const priorityDiff = (next.priority ?? 0) - (prev.priority ?? 0)
    if (priorityDiff !== 0) {
      return priorityDiff
    }
    return getSortTimestamp(next) - getSortTimestamp(prev)
  })

const loadApps = async () => {
  loading.value = true
  try {
    const res = await queryPageByAdmin({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      id: queryForm.id || undefined,
      appName: queryForm.appName || undefined,
      initPrompt: queryForm.initPrompt || undefined,
      codeGenType: queryForm.codeGenType || undefined,
      deployKey: queryForm.deployKey || undefined,
      priority: queryForm.priority,
      userId: queryForm.userId || undefined,
      sortField: 'priority',
      sortOrder: 'descend',
    })
    if (res.data.code !== 0) {
      message.error(res.data.message || '获取应用列表失败')
      return
    }
    appList.value = sortAppsByPriority(res.data.data?.records ?? [])
    pagination.total = res.data.data?.totalRow ?? 0
  } catch (error) {
    message.error(error instanceof Error ? error.message : '获取应用列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  pagination.current = 1
  await loadApps()
}

const handleReset = async () => {
  queryForm.id = undefined
  queryForm.appName = ''
  queryForm.initPrompt = ''
  queryForm.codeGenType = undefined
  queryForm.deployKey = ''
  queryForm.priority = undefined
  queryForm.userId = undefined
  pagination.current = 1
  await loadApps()
}

const handlePageChange = async (page: number, pageSize: number) => {
  pagination.current = page
  pagination.pageSize = pageSize
  await loadApps()
}

const handleDelete = async (id?: number | string) => {
  if (!id) {
    return
  }
  try {
    const res = await deleteAppByAdmin({ id: id as unknown as number })
    if (res.data.code !== 0) {
      message.error(res.data.message || '删除应用失败')
      return
    }
    message.success('删除应用成功')
    if (appList.value.length === 1 && pagination.current > 1) {
      pagination.current -= 1
    }
    await loadApps()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '删除应用失败，请稍后重试')
  }
}

const markAsGood = async (record: API.AppVO) => {
  if (!record.id) {
    return
  }
  try {
    const res = await updateAppByAdmin({
      id: record.id as unknown as number,
      appName: record.appName,
      cover: record.cover,
      priority: APP_GOOD_PRIORITY,
    })
    if (res.data.code !== 0) {
      message.error(res.data.message || '设为精选失败')
      return
    }
    message.success('已设为精选应用')
    await loadApps()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '设为精选失败，请稍后重试')
  }
}

const topGoodApp = async (record: API.AppVO) => {
  if (!record.id) {
    return
  }
  try {
    const res = await stickToTop({
      appId: record.id as unknown as number,
    })
    if (res.data.code !== 0) {
      message.error(res.data.message || '置顶精选应用失败')
      return
    }
    message.success('已置顶精选应用')
    await loadApps()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '置顶精选应用失败，请稍后重试')
  }
}

const cancelTopGoodApp = async (record: API.AppVO) => {
  if (!record.id) {
    return
  }
  try {
    const res = await cancelTop({
      appId: record.id as unknown as number,
    })
    if (res.data.code !== 0) {
      message.error(res.data.message || '取消置顶失败')
      return
    }
    message.success('已取消置顶')
    await loadApps()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '取消置顶失败，请稍后重试')
  }
}

const editApp = (id?: number | string) => {
  if (id) {
    router.push(`/app/edit/${id}`)
  }
}

const viewApp = (id?: number | string) => {
  if (id) {
    router.push(`/app/detail/${id}`)
  }
}

onMounted(() => {
  void loadApps()
})
</script>

<template>
  <main class="manage-page">
    <section class="manage-page__hero">
      <div>
        <span class="manage-page__eyebrow">Admin Only</span>
        <h1>应用管理</h1>
        <p>管理员可以筛选应用、查看详情、编辑、删除，并完成精选与置顶管理。</p>
      </div>
    </section>

    <section class="manage-panel">
      <div class="manage-panel__header">
        <div>
          <h2>筛选与列表</h2>
          <p>列表按优先级从高到低展示：置顶精选优先，其次是精选应用，再到普通应用。</p>
        </div>
      </div>

      <a-form layout="inline" class="manage-form">
        <a-form-item label="ID">
          <a-input-number v-model:value="queryForm.id" placeholder="应用 ID" style="width: 130px" />
        </a-form-item>
        <a-form-item label="名称">
          <a-input v-model:value="queryForm.appName" placeholder="按应用名称搜索" allow-clear />
        </a-form-item>
        <a-form-item label="提示词">
          <a-input v-model:value="queryForm.initPrompt" placeholder="按提示词搜索" allow-clear />
        </a-form-item>
        <a-form-item label="代码类型">
          <a-input v-model:value="queryForm.codeGenType" placeholder="MULTI_FILE" allow-clear />
        </a-form-item>
        <a-form-item label="部署标识">
          <a-input v-model:value="queryForm.deployKey" placeholder="deployKey" allow-clear />
        </a-form-item>
        <a-form-item label="优先级">
          <a-input-number v-model:value="queryForm.priority" placeholder="优先级" style="width: 120px" />
        </a-form-item>
        <a-form-item label="用户 ID">
          <a-input-number v-model:value="queryForm.userId" placeholder="用户 ID" style="width: 130px" />
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
        :data-source="appList"
        :pagination="false"
        :scroll="{ x: 1440 }"
      >
        <a-table-column title="ID" data-index="id" width="80" />
        <a-table-column title="封面" width="110">
          <template #default="{ record }">
            <a-avatar shape="square" :size="64" :src="record.cover">
              {{ (record.appName || 'A').slice(0, 1) }}
            </a-avatar>
          </template>
        </a-table-column>
        <a-table-column title="应用名称" data-index="appName" width="180" />
        <a-table-column title="初始提示词" data-index="initPrompt" ellipsis />
        <a-table-column title="代码类型" data-index="codeGenType" width="140" />
        <a-table-column title="部署标识" data-index="deployKey" width="140" />
        <a-table-column title="优先级" width="120">
          <template #default="{ record }">
            <a-tag
              :color="
                record.priority === APP_TOP_PRIORITY
                  ? 'volcano'
                  : record.priority === APP_GOOD_PRIORITY
                    ? 'gold'
                    : 'default'
              "
            >
              {{
                record.priority === APP_TOP_PRIORITY
                  ? `置顶 ${record.priority}`
                  : record.priority === APP_GOOD_PRIORITY
                    ? `精选 ${record.priority}`
                    : (record.priority ?? 0)
              }}
            </a-tag>
          </template>
        </a-table-column>
        <a-table-column title="作者" width="160">
          <template #default="{ record }">
            {{ record.userVo?.userName || record.userVo?.userAccount || record.userId || '-' }}
          </template>
        </a-table-column>
        <a-table-column title="创建时间" data-index="createTime" width="190" />
        <a-table-column title="操作" width="340" fixed="right">
          <template #default="{ record }">
            <a-space wrap>
              <a-button type="link" @click="viewApp(record.id)">详情</a-button>
              <a-button type="link" @click="editApp(record.id)">编辑</a-button>
              <a-button v-if="record.priority === APP_TOP_PRIORITY" type="link" @click="cancelTopGoodApp(record)">
                取消置顶
              </a-button>
              <a-button v-else-if="record.priority === APP_GOOD_PRIORITY" type="link" @click="topGoodApp(record)">
                置顶
              </a-button>
              <a-button v-else type="link" @click="markAsGood(record)">设为精选</a-button>
              <a-popconfirm title="确定删除该应用吗？" @confirm="handleDelete(record.id)">
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
  border: 1px solid rgba(118, 151, 195, 0.14);
  border-radius: 30px;
  background: rgba(255, 255, 255, 0.86);
  box-shadow: var(--shadow-soft);
}

.manage-page__eyebrow {
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

.manage-panel {
  margin-top: 24px;
  padding: 28px;
  border: 1px solid rgba(118, 151, 195, 0.14);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.9);
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
