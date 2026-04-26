import './styles/global.css'

import { createApp } from 'vue'

import App from './App.vue'
import router from './router'

import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import pinia from './plugins/pinia'

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

if ('scrollRestoration' in window.history) {
  window.history.scrollRestoration = 'manual'
}

window.addEventListener('pageshow', resetHomeScroll)

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
