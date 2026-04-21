<script setup lang="ts">
import { computed } from 'vue'
import { Grid } from 'ant-design-vue'
import type { MenuProps } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'

import logoUrl from '@/assets/logo.png'
import { headerMenuItems } from '@/config/menu'

const route = useRoute()
const router = useRouter()

const { useBreakpoint } = Grid
const screens = useBreakpoint()

const menuItems = computed<MenuProps['items']>(() =>
  headerMenuItems.map((item) => ({
    key: item.path,
    label: item.label,
    title: item.label,
  })),
)

const selectedKeys = computed(() => {
  const currentMenu = [...headerMenuItems]
    .sort((left, right) => right.path.length - left.path.length)
    .find((item) => route.path === item.path || route.path.startsWith(`${item.path}/`))

  return currentMenu ? [currentMenu.path] : []
})

const menuMode = computed<MenuProps['mode']>(() => (screens.value.md ? 'horizontal' : 'inline'))

const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
  if (typeof key === 'string' && key !== route.path) {
    router.push(key)
  }
}
</script>

<template>
  <a-layout-header class="global-header">
    <div class="global-header__inner">
      <RouterLink class="global-header__brand" to="/">
        <img :src="logoUrl" alt="AI零代码生成平台" class="global-header__logo" />
        <div class="global-header__title-wrap">
          <span class="global-header__title">AI零代码生成平台</span>
          <span class="global-header__subtitle">AIGC Universal Workspace</span>
        </div>
      </RouterLink>

      <a-menu
        class="global-header__menu"
        :items="menuItems"
        :mode="menuMode"
        :selected-keys="selectedKeys"
        @click="handleMenuClick"
      />

      <div class="global-header__actions">
        <a-button type="primary" size="large">登录</a-button>
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
  background: rgba(6, 20, 38, 0.88);
  backdrop-filter: blur(16px);
  box-shadow: 0 10px 30px rgba(6, 20, 38, 0.14);
}

.global-header__inner {
  width: min(1280px, calc(100% - 32px));
  margin: 0 auto;
  padding: 16px 0;
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}

.global-header__brand {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 14px;
  text-decoration: none;
}

.global-header__logo {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  object-fit: cover;
  box-shadow: 0 8px 20px rgba(26, 111, 223, 0.3);
}

.global-header__title-wrap {
  display: flex;
  flex-direction: column;
}

.global-header__title {
  color: #f8fbff;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.global-header__subtitle {
  color: rgba(232, 241, 255, 0.68);
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.global-header__menu {
  flex: 1;
  min-width: 240px;
  background: transparent;
  border-bottom: none;
}

.global-header__actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

:deep(.global-header__menu.ant-menu) {
  color: rgba(232, 241, 255, 0.82);
  background: transparent;
}

:deep(.global-header__menu.ant-menu-horizontal) {
  justify-content: center;
  border-bottom: none;
}

:deep(.global-header__menu.ant-menu-horizontal::after) {
  display: none;
}

:deep(.global-header__menu.ant-menu-horizontal > .ant-menu-item),
:deep(.global-header__menu.ant-menu-inline > .ant-menu-item) {
  color: rgba(232, 241, 255, 0.82);
  border-radius: 999px;
  margin-inline: 8px;
}

:deep(.global-header__menu.ant-menu-horizontal > .ant-menu-item-selected),
:deep(.global-header__menu.ant-menu-inline > .ant-menu-item-selected) {
  color: #ffffff;
  background: linear-gradient(135deg, rgba(26, 111, 223, 0.88), rgba(15, 163, 177, 0.88));
}

:deep(.global-header__menu.ant-menu-horizontal > .ant-menu-item::after) {
  display: none;
}

@media (max-width: 767px) {
  .global-header__inner {
    width: min(100%, calc(100% - 24px));
    gap: 16px;
  }

  .global-header__brand,
  .global-header__menu,
  .global-header__actions {
    width: 100%;
  }

  .global-header__actions {
    margin-left: 0;
  }

  :deep(.global-header__menu.ant-menu-inline) {
    border-inline-end: none;
  }

  :deep(.global-header__menu.ant-menu-inline > .ant-menu-item) {
    margin: 4px 0;
  }
}
</style>
