<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'

import { appDeploy, getAppVoById } from '@/api/yingyongmokuaixiangguanjiekou.ts'
import { formatDateTime } from '@/constants/featuredApply'
import { useLoginUserStore } from '@/stores/loginUser.ts'

const API_BASE_URL = 'http://localhost:8123/api'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()
const app = ref<API.AppVO>()
const loading = ref(false)
const deploying = ref(false)

const appId = computed(() => String(route.params.id || ''))
const previewUrl = computed(() => `${API_BASE_URL}/static/${appId.value}?t=${Date.now()}`)
const isOwner = computed(
  () => Boolean(loginUserStore.loginUser.id) && app.value?.userId === loginUserStore.loginUser.id,
)
const canManage = computed(() => loginUserStore.isAdmin || isOwner.value)
const deployedUrl = computed(() => (app.value?.deployKey ? `http://localhost/${app.value.deployKey}` : ''))

const loadApp = async () => {
  if (!appId.value) {
    message.error('应用 ID 不正确')
    await router.push('/')
    return
  }

  loading.value = true
  try {
    const res = await getAppVoById({ id: appId.value as unknown as number })
    if (res.data.code !== 0 || !res.data.data) {
      message.error(res.data.message || '获取应用详情失败')
      await router.push('/')
      return
    }
    app.value = res.data.data
  } catch (error) {
    message.error(error instanceof Error ? error.message : '获取应用详情失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const deployApp = async () => {
  if (!canManage.value) {
    message.warning('只有应用作者或管理员可以部署该应用')
    return
  }

  deploying.value = true
  try {
    const res = await appDeploy({ appId: appId.value as unknown as number })
    if (res.data.code !== 0 || !res.data.data) {
      message.error(res.data.message || '部署失败')
      return
    }

    Modal.success({
      title: '部署成功',
      content: res.data.data,
      okText: '知道了',
    })
    await loadApp()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '部署失败，请稍后重试')
  } finally {
    deploying.value = false
  }
}

const goToFeaturedApply = () => {
  void router.push({
    path: '/app/featured/apply',
    query: {
      appId: appId.value,
      action: 'create',
    },
  })
}

const goToFeaturedReview = () => {
  void router.push({
    path: '/admin/appFeaturedApplyManage',
    query: {
      appId: appId.value,
    },
  })
}

onMounted(() => {
  void loadApp()
})
</script>

<template>
  <main class="detail-page">
    <a-spin :spinning="loading">
      <section class="detail-header">
        <div>
          <span class="detail-header__eyebrow">Application</span>
          <h1>{{ app?.appName || '应用详情' }}</h1>
          <p>{{ app?.initPrompt || '暂无初始提示词' }}</p>
        </div>
        <a-space wrap>
          <a-button @click="router.push('/')">返回首页</a-button>
          <a-button v-if="isOwner" @click="router.push(`/app/chat/${appId}`)">继续生成</a-button>
          <a-button v-if="isOwner" @click="goToFeaturedApply">申请精选</a-button>
          <a-button v-if="loginUserStore.isAdmin" @click="goToFeaturedReview">审核申请</a-button>
          <a-button v-if="canManage" @click="router.push(`/app/edit/${appId}`)">编辑信息</a-button>
          <a-button v-if="canManage" type="primary" :loading="deploying" @click="deployApp">部署</a-button>
        </a-space>
      </section>

      <section class="detail-layout">
        <div class="preview-card">
          <iframe :src="previewUrl" title="应用预览"></iframe>
        </div>

        <aside class="info-card">
          <h2>应用信息</h2>
          <a-descriptions :column="1" bordered size="small">
            <a-descriptions-item label="应用 ID">{{ app?.id }}</a-descriptions-item>
            <a-descriptions-item label="作者">
              {{ app?.userVo?.userName || app?.userVo?.userAccount || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="代码类型">{{ app?.codeGenType || '-' }}</a-descriptions-item>
            <a-descriptions-item label="优先级">{{ app?.priority ?? 0 }}</a-descriptions-item>
            <a-descriptions-item label="创建时间">{{ formatDateTime(app?.createTime) }}</a-descriptions-item>
            <a-descriptions-item label="更新时间">{{ formatDateTime(app?.updateTime) }}</a-descriptions-item>
            <a-descriptions-item label="部署时间">{{ formatDateTime(app?.deployedTime) }}</a-descriptions-item>
            <a-descriptions-item label="部署地址">
              <a v-if="deployedUrl" :href="deployedUrl" target="_blank" rel="noreferrer">{{ deployedUrl }}</a>
              <span v-else>-</span>
            </a-descriptions-item>
          </a-descriptions>
        </aside>
      </section>
    </a-spin>
  </main>
</template>

<style scoped>
.detail-page {
  padding: 22px 0 44px;
}

.detail-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  padding: 34px;
  border: 1px solid rgba(112, 142, 178, 0.14);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: var(--shadow-soft);
}

.detail-header__eyebrow {
  display: inline-flex;
  padding: 7px 13px;
  color: #0b8f82;
  font-size: 13px;
  font-weight: 700;
  border-radius: 999px;
  background: rgba(15, 183, 166, 0.1);
}

.detail-header h1 {
  margin: 18px 0 12px;
  color: #111827;
  font-size: clamp(36px, 4vw, 56px);
}

.detail-header p {
  max-width: 760px;
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.8;
}

.detail-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 22px;
  margin-top: 22px;
}

.preview-card,
.info-card {
  border: 1px solid rgba(112, 142, 178, 0.14);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: var(--shadow-soft);
}

.preview-card {
  min-height: 680px;
  overflow: hidden;
  padding: 14px;
}

.preview-card iframe {
  width: 100%;
  height: 660px;
  border: 0;
  border-radius: 16px;
  background: #ffffff;
}

.info-card {
  align-self: start;
  padding: 24px;
}

.info-card h2 {
  margin: 0 0 18px;
  color: #111827;
  font-size: 24px;
}

@media (max-width: 1020px) {
  .detail-header,
  .detail-layout {
    grid-template-columns: 1fr;
  }

  .detail-header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
