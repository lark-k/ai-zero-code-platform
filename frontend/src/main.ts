import './styles/global.css'

import { createApp } from 'vue'

import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'

import App from './App.vue'
import pinia from './plugins/pinia'
import router from './router'
import { useLoginUserStore } from './stores/loginUser.ts'

const resetHomeScroll = () => {
  if (window.location.pathname !== '/') {
    return
  }
  if (window.location.hash) {
    return
  }
  window.scrollTo({
    left: 0,
    top: 0,
    behavior: 'auto',
  })
}

const warmRouteComponent = async (path: string) => {
  const resolvedRoute = router.resolve(path)
  const loaders = resolvedRoute.matched
    .map((record) => record.components?.default)
    .filter((component): component is () => Promise<unknown> => typeof component === 'function')

  await Promise.all(loaders.map((loader) => loader()))
}

const warmProtectedRoutes = async (isAdmin: boolean) => {
  const pathsToWarm = ['/app/featured/apply']
  if (isAdmin) {
    pathsToWarm.push('/admin/appManage', '/admin/appFeaturedApplyManage', '/admin/userManage')
  }

  await Promise.all(pathsToWarm.map((path) => warmRouteComponent(path)))
}

const bootstrapUserContext = async () => {
  const loginUserStore = useLoginUserStore(pinia)

  // Use cached identity to warm likely-next routes before the first click.
  if (loginUserStore.isLoggedIn) {
    void warmProtectedRoutes(loginUserStore.isAdmin)
  }

  // Start session validation as early as possible so route guards are less likely to block later.
  const user = await loginUserStore.fetchLoginUser()
  if (user.id) {
    void warmProtectedRoutes(user.userRole === 'admin')
  }
}

if ('scrollRestoration' in window.history) {
  window.history.scrollRestoration = 'manual'
}

window.addEventListener('pageshow', resetHomeScroll)

void bootstrapUserContext()

const app = createApp(App)

app.use(pinia)
app.use(router)
app.use(Antd)

app.mount('#app')

requestAnimationFrame(() => {
  resetHomeScroll()
  setTimeout(resetHomeScroll, 0)
  setTimeout(resetHomeScroll, 120)
})
