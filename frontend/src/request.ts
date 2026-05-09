import axios from 'axios'
import { message } from 'ant-design-vue'

const myAxios = axios.create({
  baseURL: '/api',
  timeout: 60000,
  withCredentials: true,
})

myAxios.interceptors.request.use(
  function (config) {
    return config
  },
  function (error) {
    return Promise.reject(error)
  },
)

myAxios.interceptors.response.use(
  function (response) {
    const { data } = response
    const requestUrl = response.config.url ?? ''
    const isAuthPage =
      window.location.pathname.includes('/user/login') ||
      window.location.pathname.includes('/user/register')

    if (data.code === 40100) {
      if (
        !requestUrl.includes('/user/getCurrentUser') &&
        !requestUrl.includes('/user/login') &&
        !requestUrl.includes('/user/register') &&
        !isAuthPage
      ) {
        message.warning('请先登录')
        window.location.href = `/user/login?redirect=${window.location.href}`
      }
    }
    return response
  },
  function (error) {
    return Promise.reject(error)
  },
)

export default myAxios
