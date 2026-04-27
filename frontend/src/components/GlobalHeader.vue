<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'

import logoUrl from '@/assets/logo.png'
import { adminPageQueryApply } from '@/api/shenqingyingyongjingxuanxiangguanjiekou.ts'
import { userLogout } from '@/api/yonghuxiangguanjiekou.ts'
import {
  FEATURED_APPLY_PENDING_CHANGED_EVENT,
  FEATURED_APPLY_STATUS,
} from '@/constants/featuredApply'
import { headerMenuItems } from '@/config/menu'
import { useLoginUserStore } from '@/stores/loginUser.ts'

const loginUserStore = useLoginUserStore()
const route = useRoute()
const router = useRouter()

const pendingApplyCount = ref(0)
let pendingCountTimer: number | undefined

const visibleMenuItems = computed(() =>
  headerMenuItems.filter((item) => {
    if (item.adminOnly) {
      return loginUserStore.isAdmin
    }
    if (item.loginOnly) {
      return loginUserStore.isLoggedIn
    }
    return true
  }),
)

const displayName = computed(
  () => loginUserStore.loginUser.userName || loginUserStore.loginUser.userAccount || '访客用户',
)
const displayInitial = computed(() => displayName.value.slice(0, 1).toUpperCase())
const pendingApplyBadgeCount = computed(() =>
  pendingApplyCount.value > 99 ? '99+' : pendingApplyCount.value,
)

const shouldShowPendingBadge = (menuKey: string) =>
  menuKey === 'featured-apply-manage' && pendingApplyCount.value > 0

const fetchPendingApplyCount = async () => {
  if (!loginUserStore.isAdmin) {
    pendingApplyCount.value = 0
    return
  }

  try {
    const res = await adminPageQueryApply({
      pageNum: 1,
      pageSize: 1,
      status: FEATURED_APPLY_STATUS.PENDING,
    })
    if (res.data.code === 0) {
      pendingApplyCount.value = res.data.data?.totalRow ?? 0
    }
  } catch {
    pendingApplyCount.value = 0
  }
}

const clearPendingCountTimer = () => {
  if (pendingCountTimer) {
    window.clearInterval(pendingCountTimer)
    pendingCountTimer = undefined
  }
}

const startPendingCountTimer = () => {
  clearPendingCountTimer()
  if (!loginUserStore.isAdmin) {
    return
  }
  pendingCountTimer = window.setInterval(() => {
    void fetchPendingApplyCount()
  }, 30000)
}

const handlePendingChanged = () => {
  void fetchPendingApplyCount()
}

const goToLogin = () => {
  void router.push({
    path: '/user/login',
    query: route.fullPath !== '/' ? { redirect: route.fullPath } : undefined,
  })
}

const goToRegister = () => {
  void router.push({
    path: '/user/register',
    query: route.fullPath !== '/' ? { redirect: route.fullPath } : undefined,
  })
}

const navigateTo = (path: string) => {
  if (route.path !== path) {
    void router.push(path)
  }
}

const handleLogout = async () => {
  try {
    const res = await userLogout()
    if (res.data.code !== 0) {
      message.error(res.data.message || '退出登录失败')
      return
    }
    clearPendingCountTimer()
    pendingApplyCount.value = 0
    loginUserStore.resetLoginUser()
    message.success('已退出登录')
    await router.push('/')
  } catch (error) {
    message.error(error instanceof Error ? error.message : '退出登录失败，请稍后重试')
  }
}

watch(
  () => [loginUserStore.isAdmin, route.fullPath],
  () => {
    if (!loginUserStore.isAdmin) {
      clearPendingCountTimer()
      pendingApplyCount.value = 0
      return
    }
    void fetchPendingApplyCount()
    startPendingCountTimer()
  },
  { immediate: true },
)

onMounted(() => {
  window.addEventListener(FEATURED_APPLY_PENDING_CHANGED_EVENT, handlePendingChanged)
})

onBeforeUnmount(() => {
  clearPendingCountTimer()
  window.removeEventListener(FEATURED_APPLY_PENDING_CHANGED_EVENT, handlePendingChanged)
})
</script>

<template>
  <a-layout-header class="global-header">
    <div class="global-header__inner">
      <RouterLink class="global-header__brand" to="/">
        <img :src="logoUrl" alt="AI 应用生成平台" class="global-header__logo" />
        <div class="global-header__brand-text">
          <span class="global-header__title">AI 零代码生成平台</span>
          <span class="global-header__subtitle">Create your app with one prompt</span>
        </div>
      </RouterLink>

      <nav class="global-header__nav">
        <button
          v-for="item in visibleMenuItems"
          :key="item.key"
          type="button"
          class="global-header__nav-item"
          :class="{ 'global-header__nav-item--active': route.path === item.path }"
          @click="navigateTo(item.path)"
        >
          <a-badge
            v-if="shouldShowPendingBadge(item.key)"
            :count="pendingApplyBadgeCount"
            :overflow-count="99"
            class="global-header__nav-badge"
          >
            <span>{{ item.label }}</span>
          </a-badge>
          <span v-else>{{ item.label }}</span>
        </button>
      </nav>

      <div class="global-header__actions">
        <template v-if="loginUserStore.isLoggedIn">
          <div class="global-header__user">
            <a-avatar :src="loginUserStore.loginUser.userAvatar" :size="40" class="global-header__avatar">
              {{ displayInitial }}
            </a-avatar>
            <div class="global-header__user-text">
              <span class="global-header__user-name">{{ displayName }}</span>
              <span class="global-header__user-role">
                {{ loginUserStore.isAdmin ? '管理员账户' : '已登录用户' }}
              </span>
            </div>
          </div>
          <a-button class="global-header__ghost-btn" @click="handleLogout">退出登录</a-button>
        </template>
        <template v-else>
          <a-button class="global-header__ghost-btn" @click="goToRegister">注册</a-button>
          <a-button type="primary" class="global-header__primary-btn" @click="goToLogin">登录</a-button>
        </template>
      </div>
    </div>
  </a-layout-header>
</template>

<style scoped>
.global-header {
  position: sticky;
  top: 0;
  z-index: 100;
  height: auto;
  padding: 0;
  line-height: normal;
  background:
    linear-gradient(90deg, rgba(244, 255, 233, 0.5), rgba(151, 244, 231, 0.36), rgba(100, 173, 255, 0.32));
  border-bottom: 1px solid rgba(255, 255, 255, 0.34);
  backdrop-filter: blur(18px);
  box-shadow: 0 8px 32px rgba(42, 70, 110, 0.06);
}

.global-header__inner {
  width: min(var(--page-max-width), calc(100% - 40px));
  margin: 0 auto;
  padding: 14px 0;
  display: flex;
  align-items: center;
  gap: 24px;
}

.global-header__brand {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.global-header__logo {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  object-fit: cover;
  box-shadow: 0 12px 24px rgba(61, 110, 192, 0.14);
}

.global-header__brand-text {
  display: flex;
  flex-direction: column;
}

.global-header__title {
  color: #2582ff;
  font-size: 18px;
  font-weight: 700;
}

.global-header__subtitle {
  color: var(--text-secondary);
  font-size: 12px;
}

.global-header__nav {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-left: 12px;
}

.global-header__nav-item {
  position: relative;
  padding: 12px 14px;
  border: none;
  background: transparent;
  color: rgba(32, 48, 71, 0.86);
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: color 0.2s ease;
}

.global-header__nav-item::after {
  content: '';
  position: absolute;
  left: 14px;
  right: 14px;
  bottom: -14px;
  height: 3px;
  border-radius: 999px;
  background: transparent;
  transition: background 0.2s ease;
}

.global-header__nav-item:hover,
.global-header__nav-item--active {
  color: #2582ff;
}

.global-header__nav-item--active::after {
  background: linear-gradient(90deg, #2d7ff9, #6e84ff);
}

.global-header__nav-badge :deep(.ant-badge-count) {
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.92);
}

.global-header__actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
}

.global-header__user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 14px 8px 8px;
  background: rgba(255, 255, 255, 0.54);
  border: 1px solid rgba(255, 255, 255, 0.42);
  border-radius: 999px;
}

.global-header__avatar {
  box-shadow: 0 8px 18px rgba(61, 110, 192, 0.14);
}

.global-header__user-text {
  display: flex;
  flex-direction: column;
}

.global-header__user-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-main);
}

.global-header__user-role {
  font-size: 12px;
  color: var(--text-secondary);
}

.global-header__ghost-btn {
  height: 42px;
  padding: 0 18px;
  border-radius: 999px;
  border-color: rgba(120, 151, 193, 0.22);
  color: var(--text-main);
  box-shadow: none;
}

.global-header__primary-btn {
  height: 42px;
  padding: 0 18px;
  border-radius: 999px;
  box-shadow: 0 10px 24px rgba(45, 127, 249, 0.22);
}

@media (max-width: 1080px) {
  .global-header__inner {
    flex-wrap: wrap;
  }

  .global-header__nav {
    order: 3;
    width: 100%;
    margin-left: 0;
    overflow-x: auto;
  }
}

@media (max-width: 767px) {
  .global-header__inner {
    width: min(100%, calc(100% - 24px));
    gap: 14px;
  }

  .global-header__brand {
    width: 100%;
  }

  .global-header__actions {
    width: 100%;
    justify-content: space-between;
    flex-wrap: wrap;
  }

  .global-header__user {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
