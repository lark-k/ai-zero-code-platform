import { message } from 'ant-design-vue'
import { createRouter, createWebHistory } from 'vue-router'
import pinia from '@/plugins/pinia'
import { useLoginUserStore } from '@/stores/loginUser.ts'

const HomePage = () => import('@/pages/HomePage.vue')
const UserLoginPage = () => import('@/pages/user/UserLoginPage.vue')
const UserRegisterPage = () => import('@/pages/user/UserRegisterPage.vue')
const UserManagePage = () => import('@/pages/admin/UserManagePage.vue')

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '主页',
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
      path: '/admin/userManage',
      name: '用户管理',
      component: UserManagePage,
      meta: {
        adminOnly: true,
      },
    },
  ],
})

router.beforeEach(async (to) => {
  const loginUserStore = useLoginUserStore(pinia)

  if (to.meta.adminOnly) {
    if (!loginUserStore.hasFetched || !loginUserStore.isLoggedIn || !loginUserStore.isAdmin) {
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
    if (!loginUserStore.isAdmin) {
      message.warning('仅管理员可以访问用户管理页面')
      return {
        path: '/',
      }
    }
  }

  return true
})

export default router
