<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import {
  addApp,
  deleteApp,
  getAppVoByPage,
  getAppVoPageForGood,
} from '@/api/yingyongmokuaixiangguanjiekou.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const promptText = ref('')
const creating = ref(false)
const myAppsLoading = ref(false)
const goodAppsLoading = ref(false)
const myApps = ref<API.AppVO[]>([])
const goodApps = ref<API.AppVO[]>([])
const previewReadyMap = reactive<Record<string, boolean>>({})

const myQuery = reactive({
  appName: '',
  current: 1,
  pageSize: 6,
  total: 0,
})

const goodQuery = reactive({
  appName: '',
  current: 1,
  pageSize: 6,
  total: 0,
})

const promptExamples = [
  {
    label: '个人博客网站',
    prompt:
      '帮我创建个人博客网站，包含首页、文章列表、文章详情、关于我和联系方式模块，整体风格清爽温暖，适合记录技术学习、生活随笔和项目经验，支持响应式布局。',
  },
  {
    label: '企业官网',
    prompt:
      '帮我创建企业官网，面向科技服务公司，包含品牌首页、公司介绍、核心服务、成功案例、客户评价和联系咨询区域，设计专业可信，突出业务价值和转化按钮。',
  },
  {
    label: '电商商品页',
    prompt:
      '帮我创建电商商品展示网站，用于售卖智能办公产品，包含商品卖点、高清展示区、规格参数、用户评价、常见问题和立即购买按钮，风格现代简洁，有促销氛围。',
  },
  {
    label: '作品集网站',
    prompt:
      '帮我创建设计师个人作品集网站，包含首屏个人介绍、精选作品网格、项目详情展示、技能标签、工作经历和联系入口，视觉有高级感，适合求职和对外展示。',
  },
]

const displayName = computed(
  () => loginUserStore.loginUser.userName || loginUserStore.loginUser.userAccount || '创作者',
)

const fillPrompt = (value: string) => {
  promptText.value = value
}

const formatTime = (time?: string) => {
  if (!time) {
    return '刚刚创建'
  }
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) {
    return time
  }
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const getStaticUrl = (id?: number | string) => (id ? `http://localhost:8123/api/static/${id}` : '')
const getDeployUrl = (deployKey?: string) => (deployKey ? `http://localhost/${deployKey}` : '')

const shouldRenderPreview = (id?: number | string) => {
  if (!id) {
    return false
  }
  return Boolean(previewReadyMap[String(id)])
}

const loadPreview = (id?: number | string) => {
  if (!id) {
    return
  }
  previewReadyMap[String(id)] = true
}

const getAuthorName = (app: API.AppVO) => {
  if (app.userVo?.userName || app.userVo?.userAccount) {
    return app.userVo.userName || app.userVo.userAccount || 'NoCode 官方'
  }
  if (app.userId === loginUserStore.loginUser.id) {
    return displayName.value
  }
  return 'NoCode 官方'
}

const getAuthorAvatar = (app: API.AppVO) => {
  if (app.userVo?.userAvatar) {
    return app.userVo.userAvatar
  }
  if (app.userId === loginUserStore.loginUser.id) {
    return loginUserStore.loginUser.userAvatar
  }
  return ''
}

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

const buildQueryPayload = (query: typeof myQuery): API.QueryAppDTO => ({
  pageNum: query.current,
  pageSize: query.pageSize,
  appName: query.appName.trim() || undefined,
  sortField: 'priority',
  sortOrder: 'descend',
})

const loadMyApps = async () => {
  if (!loginUserStore.isLoggedIn) {
    myApps.value = []
    myQuery.total = 0
    return
  }

  myAppsLoading.value = true
  try {
    const res = await getAppVoByPage(buildQueryPayload(myQuery))
    if (res.data.code !== 0) {
      message.error(res.data.message || '获取我的应用失败')
      return
    }
    myApps.value = sortAppsByPriority(res.data.data?.records ?? [])
    myQuery.total = res.data.data?.totalRow ?? 0
  } catch (error) {
    message.error(error instanceof Error ? error.message : '获取我的应用失败，请稍后重试')
  } finally {
    myAppsLoading.value = false
  }
}

const loadGoodApps = async () => {
  goodAppsLoading.value = true
  try {
    const res = await getAppVoPageForGood(buildQueryPayload(goodQuery))
    if (res.data.code !== 0) {
      message.error(res.data.message || '获取精选应用失败')
      return
    }
    goodApps.value = sortAppsByPriority(res.data.data?.records ?? [])
    goodQuery.total = res.data.data?.totalRow ?? 0
  } catch (error) {
    message.error(error instanceof Error ? error.message : '获取精选应用失败，请稍后重试')
  } finally {
    goodAppsLoading.value = false
  }
}

const createApp = async () => {
  const initPrompt = promptText.value.trim()
  if (!initPrompt) {
    message.info('先输入一句你的站点想法，比如“帮我创建一个个人博客网站”')
    return
  }
  if (!loginUserStore.isLoggedIn) {
    message.warning('登录后即可创建应用')
    await router.push({
      path: '/user/login',
      query: {
        redirect: '/',
      },
    })
    return
  }

  creating.value = true
  try {
    const res = await addApp({ initPrompt })
    if (res.data.code !== 0 || !res.data.data) {
      message.error(res.data.message || '创建应用失败')
      return
    }
    await router.push({
      path: `/app/chat/${res.data.data}`,
      query: {
        prompt: initPrompt,
      },
    })
  } catch (error) {
    message.error(error instanceof Error ? error.message : '创建应用失败，请稍后重试')
  } finally {
    creating.value = false
  }
}

const searchMyApps = async () => {
  myQuery.current = 1
  await loadMyApps()
}

const searchGoodApps = async () => {
  goodQuery.current = 1
  await loadGoodApps()
}

const handleMyPageChange = async (page: number, pageSize: number) => {
  myQuery.current = page
  myQuery.pageSize = Math.min(pageSize, 20)
  await loadMyApps()
}

const handleGoodPageChange = async (page: number, pageSize: number) => {
  goodQuery.current = page
  goodQuery.pageSize = Math.min(pageSize, 20)
  await loadGoodApps()
}

const viewApp = (id?: number | string) => {
  if (id) {
    router.push(`/app/detail/${id}`)
  }
}

const handleCoverClick = (app: API.AppVO) => {
  if (!app.id) {
    return
  }
  if (!app.cover && !shouldRenderPreview(app.id)) {
    loadPreview(app.id)
    return
  }
  viewApp(app.id)
}

const continueChat = (id?: number | string) => {
  if (id) {
    router.push(`/app/chat/${id}`)
  }
}

const openDeployedApp = (deployKey?: string) => {
  const deployUrl = getDeployUrl(deployKey)
  if (!deployUrl) {
    return
  }
  window.open(deployUrl, '_blank', 'noopener,noreferrer')
}

watch(
  () => loginUserStore.isLoggedIn,
  () => {
    void loadMyApps()
  },
)

onMounted(() => {
  void loadGoodApps()
  void loadMyApps()
})
</script>

<template>
  <main class="home-page">
    <section class="home-hero">
      <div class="home-hero__brand">
        <span>一句话</span>
        <img src="@/assets/logo.png" alt="logo" />
        <span>呈所想</span>
      </div>
      <p class="home-hero__subtitle">与 AI 对话轻松创建应用和网站</p>

      <div class="prompt-box">
        <a-textarea
          v-model:value="promptText"
          :auto-size="{ minRows: 5, maxRows: 8 }"
          :bordered="false"
          placeholder="帮我创建个人博客网站"
          @pressEnter.ctrl="createApp"
        />
        <div class="prompt-box__footer">
          <a-button
            type="primary"
            shape="circle"
            size="large"
            :loading="creating"
            class="prompt-box__submit"
            @click="createApp"
          >
            ↑
          </a-button>
        </div>
      </div>

      <div class="home-hero__chips">
        <button v-for="item in promptExamples" :key="item.label" type="button" @click="fillPrompt(item.prompt)">
          {{ item.label }}
        </button>
      </div>

      <p v-if="loginUserStore.isLoggedIn" class="home-hero__hello">
        {{ displayName }}，今天也可以把一个想法变成可访问的网站。
      </p>
    </section>

    <section v-if="loginUserStore.isLoggedIn" class="app-section">
      <div class="section-heading">
        <div>
          <h2>我的作品</h2>
        </div>
        <a-input-search
          v-model:value="myQuery.appName"
          placeholder="按应用名称搜索"
          allow-clear
          class="section-heading__search"
          @search="searchMyApps"
        />
      </div>

      <a-spin :spinning="myAppsLoading">
        <div v-if="myApps.length" class="app-grid">
          <article v-for="app in myApps" :key="app.id" class="app-card">
            <div class="app-card__cover-wrap">
              <button type="button" class="app-card__cover" @click="handleCoverClick(app)">
                <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
                <div v-else-if="app.id" class="app-card__cover-frame">
                  <iframe
                    v-if="shouldRenderPreview(app.id)"
                    :src="getStaticUrl(app.id)"
                    title="应用预览"
                    loading="lazy"
                    tabindex="-1"
                  ></iframe>
                  <div v-else class="app-card__cover-placeholder">
                    <span>{{ (app.appName || '应用').slice(0, 1) }}</span>
                    <strong class="app-card__preview-tip">点击加载预览</strong>
                  </div>
                </div>
                <div v-else class="app-card__cover-placeholder">
                  <span>{{ (app.appName || '应用').slice(0, 1) }}</span>
                </div>
              </button>
              <div class="app-card__hover-actions">
                <a-button type="primary" class="app-card__hover-btn" @click="continueChat(app.id)">
                  查看对话
                </a-button>
                <a-button
                  v-if="app.deployKey"
                  class="app-card__hover-btn app-card__hover-btn--ghost"
                  @click="openDeployedApp(app.deployKey)"
                >
                  查看作品
                </a-button>
              </div>
            </div>
            <div class="app-card__body">
              <div class="app-card__meta" @click="viewApp(app.id)">
                <a-avatar :src="getAuthorAvatar(app)" :size="56" class="app-card__avatar">
                  {{ getAuthorName(app).slice(0, 1).toUpperCase() }}
                </a-avatar>
                <div class="app-card__meta-text">
                  <h3>{{ app.appName || '未命名应用' }}</h3>
                  <p>{{ getAuthorName(app) }}</p>
                </div>
              </div>
            </div>
          </article>
        </div>
        <a-empty v-else description="暂无应用，先用上方输入框创建一个吧" />
      </a-spin>

      <div class="app-section__pagination">
        <a-pagination
          :current="myQuery.current"
          :page-size="myQuery.pageSize"
          :total="myQuery.total"
          :show-size-changer="true"
          :page-size-options="['6', '12', '20']"
          :show-total="(total: number) => `共 ${total} 个应用`"
          @change="handleMyPageChange"
          @showSizeChange="handleMyPageChange"
        />
      </div>
    </section>

    <section class="app-section">
      <div class="section-heading">
        <div>
          <h2>精选案例</h2>
        </div>
        <a-input-search
          v-model:value="goodQuery.appName"
          placeholder="按应用名称搜索"
          allow-clear
          class="section-heading__search"
          @search="searchGoodApps"
        />
      </div>

      <a-spin :spinning="goodAppsLoading">
        <div v-if="goodApps.length" class="app-grid">
          <article v-for="app in goodApps" :key="app.id" class="app-card app-card--good">
            <div class="app-card__cover-wrap">
              <button type="button" class="app-card__cover" @click="handleCoverClick(app)">
                <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
                <div v-else-if="app.id" class="app-card__cover-frame">
                  <iframe
                    v-if="shouldRenderPreview(app.id)"
                    :src="getStaticUrl(app.id)"
                    title="精选应用预览"
                    loading="lazy"
                    tabindex="-1"
                  ></iframe>
                  <div v-else class="app-card__cover-placeholder">
                    <span>{{ (app.appName || '精选').slice(0, 1) }}</span>
                    <strong class="app-card__preview-tip">点击加载预览</strong>
                  </div>
                </div>
                <div v-else class="app-card__cover-placeholder">
                  <span>{{ (app.appName || '精选').slice(0, 1) }}</span>
                </div>
              </button>
              <div class="app-card__hover-actions">
                <a-button type="primary" class="app-card__hover-btn" @click="continueChat(app.id)">
                  查看对话
                </a-button>
                <a-button
                  v-if="app.deployKey"
                  class="app-card__hover-btn app-card__hover-btn--ghost"
                  @click="openDeployedApp(app.deployKey)"
                >
                  查看作品
                </a-button>
              </div>
            </div>
            <div class="app-card__body">
              <div class="app-card__meta" @click="viewApp(app.id)">
                <a-avatar :src="getAuthorAvatar(app)" :size="56" class="app-card__avatar">
                  {{ getAuthorName(app).slice(0, 1).toUpperCase() }}
                </a-avatar>
                <div class="app-card__meta-text">
                  <h3>{{ app.appName || '精选应用' }}</h3>
                  <p>{{ getAuthorName(app) }}</p>
                </div>
              </div>
            </div>
          </article>
        </div>
        <a-empty v-else description="暂无精选应用" />
      </a-spin>

      <div class="app-section__pagination">
        <a-pagination
          :current="goodQuery.current"
          :page-size="goodQuery.pageSize"
          :total="goodQuery.total"
          :show-size-changer="true"
          :page-size-options="['6', '12', '20']"
          :show-total="(total: number) => `共 ${total} 个案例`"
          @change="handleGoodPageChange"
          @showSizeChange="handleGoodPageChange"
        />
      </div>
    </section>
  </main>
</template>

<style scoped>
.home-page {
  padding: 10px 0 46px;
}

.home-hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 520px;
  padding: clamp(42px, 7vw, 96px) 0 40px;
  text-align: center;
}

.home-hero__brand {
  display: inline-flex;
  align-items: center;
  gap: 26px;
  color: #101828;
  font-size: clamp(44px, 6vw, 76px);
  font-weight: 900;
  line-height: 1;
}

.home-hero__brand img {
  width: clamp(56px, 6vw, 78px);
  height: clamp(56px, 6vw, 78px);
  border-radius: 28px;
}

.home-hero__subtitle {
  margin: 28px 0 0;
  color: rgba(22, 50, 79, 0.58);
  font-size: clamp(18px, 2vw, 28px);
  letter-spacing: 0;
}

.prompt-box {
  width: min(100%, 1100px);
  margin-top: 68px;
  padding: 24px 24px 20px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 34px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.76), rgba(230, 255, 250, 0.58));
  box-shadow: 0 28px 80px rgba(31, 115, 160, 0.16);
  backdrop-filter: blur(18px);
}

.prompt-box :deep(textarea) {
  color: #253044;
  font-size: 22px;
  line-height: 1.8;
}

.prompt-box__footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 18px;
}

.prompt-box__submit {
  width: 56px;
  height: 56px;
  font-size: 26px;
  background: #9aa1a9;
  border-color: #9aa1a9;
  box-shadow: none;
}

.home-hero__chips {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 16px;
  margin-top: 34px;
}

.home-hero__chips button {
  padding: 12px 22px;
  color: #657181;
  font-size: 18px;
  font-weight: 600;
  border: 1px solid rgba(255, 255, 255, 0.52);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.62);
  backdrop-filter: blur(14px);
  cursor: pointer;
}

.home-hero__hello {
  margin: 22px 0 0;
  color: #60748b;
  font-size: 16px;
}

.app-section {
  margin-top: 34px;
  padding: clamp(28px, 4vw, 58px);
  border: 1px solid rgba(255, 255, 255, 0.48);
  border-radius: 34px;
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.48), rgba(218, 255, 248, 0.34), rgba(211, 235, 255, 0.3));
  box-shadow: 0 26px 80px rgba(30, 106, 170, 0.13);
  backdrop-filter: blur(18px);
}

.section-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 28px;
}

.section-heading h2 {
  margin: 0 0 8px;
  color: #101828;
  font-size: clamp(32px, 4vw, 48px);
}

.section-heading p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

.section-heading__search {
  width: min(100%, 320px);
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 28px;
}

.app-card {
  min-width: 0;
}

.app-card__cover-wrap {
  position: relative;
}

.app-card__cover {
  width: 100%;
  aspect-ratio: 16 / 9;
  padding: 0;
  overflow: hidden;
  border: 1px solid rgba(120, 145, 170, 0.16);
  border-radius: 18px;
  background: rgba(245, 247, 250, 0.72);
  cursor: pointer;
}

.app-card__hover-actions {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  padding: 20px;
  opacity: 0;
  pointer-events: none;
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(14, 23, 38, 0.12), rgba(14, 23, 38, 0.52));
  transition: opacity 0.22s ease;
}

.app-card__cover-wrap:hover .app-card__hover-actions {
  opacity: 1;
  pointer-events: auto;
}

.app-card__hover-btn {
  min-width: 112px;
  height: 42px;
  border-radius: 999px;
  box-shadow: 0 10px 24px rgba(17, 24, 39, 0.18);
}

.app-card__hover-btn--ghost {
  color: #0f172a;
  border-color: rgba(255, 255, 255, 0.68);
  background: rgba(255, 255, 255, 0.92);
}

.app-card__cover img,
.app-card__cover-frame,
.app-card__cover iframe {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: top center;
}

.app-card__cover-frame {
  overflow: hidden;
}

.app-card__cover iframe {
  border: 0;
  pointer-events: none;
  transform-origin: top left;
}

.app-card__cover-placeholder {
  display: grid;
  width: 100%;
  height: 100%;
  place-items: center;
  background:
    linear-gradient(135deg, rgba(45, 127, 249, 0.08), rgba(32, 205, 180, 0.1)),
    repeating-linear-gradient(0deg, rgba(120, 145, 170, 0.08) 0 1px, transparent 1px 18px);
}

.app-card__cover-placeholder span {
  display: grid;
  width: 68px;
  height: 68px;
  place-items: center;
  color: #ffffff;
  font-size: 30px;
  font-weight: 800;
  border-radius: 22px;
  background: #0fb7a6;
}

.app-card__preview-tip {
  margin-top: 16px;
  color: #355070;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.app-card__body {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  min-height: 82px;
  padding: 18px 4px 0;
}

.app-card__meta {
  display: flex;
  align-items: center;
  min-width: 0;
  gap: 16px;
  cursor: pointer;
}

.app-card__avatar {
  flex: 0 0 auto;
  color: #111827;
  font-size: 20px;
  font-weight: 800;
  background: linear-gradient(135deg, #30f2ca, #e8fff9);
  box-shadow: 0 10px 24px rgba(12, 130, 122, 0.16);
}

.app-card__meta-text {
  min-width: 0;
}

.app-card__meta-text h3 {
  margin: 0 0 6px;
  color: #101828;
  font-size: 24px;
  line-height: 1.25;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-card__meta-text p {
  margin: 0;
  color: #6b7280;
  font-size: 18px;
  line-height: 1.35;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-section__pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 28px;
}

@media (max-width: 1080px) {
  .app-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  .home-hero {
    min-height: auto;
  }

  .home-hero__brand {
    gap: 14px;
  }

  .prompt-box {
    margin-top: 38px;
    padding: 18px;
    border-radius: 24px;
  }

  .prompt-box :deep(textarea) {
    font-size: 18px;
  }

  .section-heading,
  .app-card__body {
    align-items: flex-start;
    flex-direction: column;
  }

  .app-card__body {
    min-height: auto;
  }

  .app-card__hover-actions {
    flex-direction: column;
  }

  .section-heading__search {
    width: 100%;
  }

  .app-grid {
    grid-template-columns: 1fr;
  }

  .app-section__pagination {
    justify-content: center;
  }
}
</style>
