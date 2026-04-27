import { message } from 'ant-design-vue'
import { createRouter, createWebHistory } from 'vue-router'

import pinia from '@/plugins/pinia'
import { useLoginUserStore } from '@/stores/loginUser.ts'

const HomePage = () => import('@/pages/HomePage.vue')
const UserLoginPage = () => import('@/pages/user/UserLoginPage.vue')
const UserRegisterPage = () => import('@/pages/user/UserRegisterPage.vue')
const UserManagePage = () => import('@/pages/admin/UserManagePage.vue')
const AppManagePage = () => import('@/pages/admin/AppManagePage.vue')
const AppFeaturedApplyManagePage = () => import('@/pages/admin/AppFeaturedApplyManagePage.vue')
const AppChatPage = () => import('@/pages/app/AppChatPage.vue')
const AppDetailPage = () => import('@/pages/app/AppDetailPage.vue')
const AppEditPage = () => import('@/pages/app/AppEditPage.vue')
const AppFeaturedApplyPage = () => import('@/pages/app/AppFeaturedApplyPage.vue')

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior(to, from, savedPosition) {
    if (to.hash) {
      return {
        el: to.hash,
        behavior: 'smooth',
      }
    }

    if (to.path === '/') {
      return {
        left: 0,
        top: 0,
      }
    }

    if (savedPosition) {
      return savedPosition
    }

    return {
      left: 0,
      top: 0,
    }
  },
  routes: [
    {
      path: '/',
      name: '首页',
      component: HomePage,
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: UserLoginPage,
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: UserRegisterPage,
    },
    {
      path: '/app/chat/:id',
      name: '应用生成对话',
      component: AppChatPage,
      meta: {
        loginOnly: true,
      },
    },
    {
      path: '/app/detail/:id',
      name: '应用详情',
      component: AppDetailPage,
    },
    {
      path: '/app/edit/:id',
      name: '应用信息修改',
      component: AppEditPage,
      meta: {
        loginOnly: true,
      },
    },
    {
      path: '/app/featured/apply',
      name: '精选申请',
      component: AppFeaturedApplyPage,
      meta: {
        loginOnly: true,
      },
    },
    {
      path: '/admin/userManage',
      name: '用户管理',
      component: UserManagePage,
      meta: {
        adminOnly: true,
      },
    },
    {
      path: '/admin/appManage',
      name: '应用管理',
      component: AppManagePage,
      meta: {
        adminOnly: true,
      },
    },
    {
      path: '/admin/appFeaturedApplyManage',
      name: '精选申请审核',
      component: AppFeaturedApplyManagePage,
      meta: {
        adminOnly: true,
      },
    },
  ],
})

router.beforeEach(async (to) => {
  const loginUserStore = useLoginUserStore(pinia)

  if (to.meta.adminOnly || to.meta.loginOnly) {
    if (!loginUserStore.hasFetched || !loginUserStore.isLoggedIn) {
      await loginUserStore.fetchLoginUser()
    }

    if (!loginUserStore.isLoggedIn) {
      message.warning('请先登录')
      return {
        path: '/user/login',
        query: {
          redirect: to.fullPath,
        },
      }
    }
  }

  if (to.meta.adminOnly && !loginUserStore.isAdmin) {
    message.warning('仅管理员可以访问该页面')
    return {
      path: '/',
    }
  }

  return true
})

export default router
