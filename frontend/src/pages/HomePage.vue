<script setup lang="ts">
import { computed, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { userLogout } from '@/api/yonghuxiangguanjiekou.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const promptText = ref('')

const promptExamples = ['个人博客网站', '企业官网', '在线商城', '作品展示网站']

const displayName = computed(
  () => loginUserStore.loginUser.userName || loginUserStore.loginUser.userAccount || '访客用户',
)

const fillPrompt = (value: string) => {
  promptText.value = `帮我创建一个${value}`
}

const submitPrompt = () => {
  if (!promptText.value.trim()) {
    message.info('先输入一句你的站点想法，比如“帮我创建一个个人博客网站”')
    return
  }
  message.success(`已记录你的想法：${promptText.value}`)
}

const goToLogin = () => {
  router.push('/user/login')
}

const goToRegister = () => {
  router.push('/user/register')
}

const handleLogout = async () => {
  try {
    const res = await userLogout()
    if (res.data.code !== 0) {
      message.error(res.data.message || '退出登录失败')
      return
    }
    loginUserStore.resetLoginUser()
    message.success('已退出登录')
  } catch (error) {
    message.error(error instanceof Error ? error.message : '退出登录失败，请稍后重试')
  }
}
</script>

<template>
  <main class="home-page">
    <template v-if="loginUserStore.isLoggedIn">
      <section class="hero">
        <div class="hero__eyebrow">
          <span class="hero__eyebrow-dot"></span>
          AI 应用生成平台
        </div>
        <h1>AI 应用生成平台</h1>
        <p>一句话轻松创建网站应用，登录后即可进入平台界面并继续扩展你的 AI 应用工作流。</p>

        <div class="hero__composer">
          <a-textarea
            v-model:value="promptText"
            :auto-size="{ minRows: 5, maxRows: 7 }"
            class="hero__textarea"
            placeholder="帮我创建一个个人博客网站"
            :bordered="false"
          />
          <button type="button" class="hero__submit" @click="submitPrompt">↑</button>
        </div>

        <div class="hero__chips">
          <button
            v-for="item in promptExamples"
            :key="item"
            type="button"
            class="hero__chip"
            @click="fillPrompt(item)"
          >
            {{ item }}
          </button>
        </div>

        <div class="hero__status-card">
          <a-avatar :src="loginUserStore.loginUser.userAvatar" :size="56">
            {{ displayName.slice(0, 1).toUpperCase() }}
          </a-avatar>
          <div class="hero__status-copy">
            <strong>{{ displayName }}</strong>
            <span>
              {{ loginUserStore.isAdmin ? '当前管理员已登录，可进入用户管理。' : '当前已登录，可直接使用 AI 应用生成平台。' }}
            </span>
          </div>
          <a-button class="hero__status-btn" @click="handleLogout">退出登录</a-button>
        </div>
      </section>
    </template>

    <template v-else>
      <section class="guest-home">
        <div class="guest-home__content">
          <div class="guest-home__eyebrow">Welcome</div>
          <h1>先登录或注册，再进入 AI 应用生成平台</h1>
          <p>首页在未登录状态下只展示登录和注册入口；完成登录后，会自动进入 AI 应用生成平台界面。</p>

          <div class="guest-home__actions">
            <a-button size="large" class="guest-home__ghost-btn" @click="goToRegister">注册账号</a-button>
            <a-button type="primary" size="large" class="guest-home__primary-btn" @click="goToLogin">
              立即登录
            </a-button>
          </div>
        </div>

        <div class="guest-home__cards">
          <article class="guest-card">
            <h2>注册</h2>
            <p>账号不能少于 6 位，密码不能少于 8 位，注册成功后可直接跳转到登录页。</p>
            <a-button block size="large" @click="goToRegister">去注册</a-button>
          </article>
          <article class="guest-card guest-card--primary">
            <h2>登录</h2>
            <p>登录成功后顶部会展示头像与退出入口，首页自动切换为 AI 应用生成平台界面。</p>
            <a-button block type="primary" size="large" @click="goToLogin">去登录</a-button>
          </article>
        </div>
      </section>
    </template>
  </main>
</template>

<style scoped>
.home-page {
  padding: 18px 0 32px;
}

.hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: clamp(40px, 7vw, 92px) 0 24px;
}

.hero__eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(108, 145, 193, 0.16);
  color: #4d6b90;
  font-size: 14px;
  font-weight: 600;
  box-shadow: 0 14px 30px rgba(57, 88, 132, 0.08);
}

.hero__eyebrow-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: linear-gradient(135deg, #2d7ff9, #7a79ff);
}

.hero h1 {
  margin: 28px 0 18px;
  font-size: clamp(56px, 8vw, 88px);
  line-height: 1.04;
  letter-spacing: -0.03em;
  background: linear-gradient(135deg, #4f88ff 10%, #7a79ff 50%, #6f94ff 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.hero p {
  max-width: 760px;
  margin: 0;
  color: var(--text-secondary);
  font-size: clamp(18px, 2vw, 24px);
  line-height: 1.8;
}

.hero__composer {
  position: relative;
  width: min(100%, 1020px);
  margin-top: 42px;
  padding: 18px 20px 20px;
  border-radius: 30px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(118, 151, 195, 0.16);
  box-shadow: var(--shadow-card);
}

.hero__textarea {
  width: 100%;
  font-size: 18px;
  line-height: 1.8;
}

.hero__submit {
  position: absolute;
  right: 18px;
  bottom: 18px;
  width: 56px;
  height: 56px;
  border: none;
  border-radius: 18px;
  background: linear-gradient(135deg, #2d7ff9 0%, #6b8cff 100%);
  color: #ffffff;
  font-size: 28px;
  cursor: pointer;
  box-shadow: 0 16px 30px rgba(45, 127, 249, 0.24);
}

.hero__chips {
  margin-top: 26px;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 14px;
}

.hero__chip {
  padding: 14px 24px;
  border: 1px solid rgba(111, 146, 194, 0.16);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.86);
  color: #495f7d;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 14px 24px rgba(57, 88, 132, 0.08);
}

.hero__status-card {
  width: min(100%, 1020px);
  margin-top: 24px;
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 18px 20px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.76);
  border: 1px solid rgba(118, 151, 195, 0.14);
  box-shadow: 0 14px 28px rgba(57, 88, 132, 0.08);
}

.hero__status-copy {
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: flex-start;
  text-align: left;
  gap: 6px;
}

.hero__status-copy strong {
  font-size: 18px;
}

.hero__status-copy span {
  color: var(--text-secondary);
  line-height: 1.7;
}

.hero__status-btn {
  height: 42px;
  padding: 0 18px;
  border-radius: 999px;
  border-color: rgba(117, 149, 192, 0.22);
}

.guest-home {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, 440px);
  gap: 30px;
  align-items: stretch;
  padding: clamp(40px, 7vw, 88px) 0 16px;
}

.guest-home__content,
.guest-home__cards {
  min-width: 0;
}

.guest-home__content {
  padding: 26px 12px 26px 0;
}

.guest-home__eyebrow {
  display: inline-flex;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(45, 127, 249, 0.08);
  color: #2d7ff9;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.guest-home__content h1 {
  margin: 22px 0 16px;
  font-size: clamp(42px, 5vw, 68px);
  line-height: 1.06;
}

.guest-home__content p {
  max-width: 720px;
  margin: 0;
  color: var(--text-secondary);
  font-size: 18px;
  line-height: 1.8;
}

.guest-home__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 30px;
}

.guest-home__ghost-btn,
.guest-home__primary-btn {
  min-width: 148px;
  height: 46px;
  border-radius: 999px;
}

.guest-home__cards {
  display: grid;
  gap: 18px;
}

.guest-card {
  padding: 28px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(118, 151, 195, 0.14);
  box-shadow: var(--shadow-soft);
}

.guest-card--primary {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(240, 247, 255, 0.92));
}

.guest-card h2 {
  margin: 0 0 12px;
  font-size: 28px;
}

.guest-card p {
  margin: 0 0 20px;
  color: var(--text-secondary);
  line-height: 1.8;
}

@media (max-width: 980px) {
  .guest-home {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 767px) {
  .home-page {
    padding-top: 8px;
  }

  .hero {
    padding-top: 28px;
  }

  .hero h1 {
    font-size: 46px;
  }

  .hero__composer {
    padding: 14px 14px 74px;
    border-radius: 24px;
  }

  .hero__submit {
    width: 52px;
    height: 52px;
    border-radius: 16px;
  }

  .hero__status-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .guest-home__content {
    padding-right: 0;
  }
}
</style>
