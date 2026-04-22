import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { getCurrentUserInfo } from '@/api/yonghuxiangguanjiekou.ts'

const LOGIN_USER_STORAGE_KEY = 'ai-zero-code-login-user'

const defaultLoginUser: API.UserLoginVO = {
  id: undefined,
  userAccount: '',
  userName: '未登录',
  userAvatar: '',
  userProfile: '',
  userRole: '',
}

const readCachedLoginUser = (): API.UserLoginVO => {
  if (typeof window === 'undefined') {
    return { ...defaultLoginUser }
  }

  try {
    const raw = window.localStorage.getItem(LOGIN_USER_STORAGE_KEY)
    if (!raw) {
      return { ...defaultLoginUser }
    }
    return {
      ...defaultLoginUser,
      ...JSON.parse(raw),
    }
  } catch {
    return { ...defaultLoginUser }
  }
}

export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<API.UserLoginVO>(readCachedLoginUser())
  const hasFetched = ref(false)
  const isAdmin = computed(() => loginUser.value.userRole === 'admin')
  const isLoggedIn = computed(() => Boolean(loginUser.value.id))
  let fetchPromise: Promise<API.UserLoginVO> | null = null

  const persistLoginUser = (user: API.UserLoginVO) => {
    if (typeof window === 'undefined') {
      return
    }

    if (!user.id) {
      window.localStorage.removeItem(LOGIN_USER_STORAGE_KEY)
      return
    }

    window.localStorage.setItem(LOGIN_USER_STORAGE_KEY, JSON.stringify(user))
  }

  async function fetchLoginUser(force = false) {
    if (fetchPromise && !force) {
      return fetchPromise
    }
    if (hasFetched.value && !force) {
      return loginUser.value
    }

    fetchPromise = (async () => {
      try {
        const res = await getCurrentUserInfo()
        if (res.data.code === 0 && res.data.data) {
          setLoginUser(res.data.data)
          return loginUser.value
        }
        resetLoginUser()
      } catch {
        resetLoginUser()
      } finally {
        hasFetched.value = true
        fetchPromise = null
      }

      return loginUser.value
    })()

    return fetchPromise
  }

  function setLoginUser(newLoginUser: API.UserLoginVO) {
    loginUser.value = {
      ...defaultLoginUser,
      ...newLoginUser,
    }
    hasFetched.value = true
    persistLoginUser(loginUser.value)
  }

  function resetLoginUser() {
    loginUser.value = { ...defaultLoginUser }
    hasFetched.value = true
    persistLoginUser(loginUser.value)
  }

  return { loginUser, hasFetched, isAdmin, isLoggedIn, setLoginUser, resetLoginUser, fetchLoginUser }
})
