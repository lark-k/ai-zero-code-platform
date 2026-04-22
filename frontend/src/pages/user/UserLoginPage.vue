<script setup lang="ts">
import { reactive } from 'vue'
import { message } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { useRoute, useRouter } from 'vue-router'
import { userLogin } from '@/api/yonghuxiangguanjiekou.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'

interface LoginFormState {
  userAccount: string
  userPassword: string
}

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()

const formState = reactive<LoginFormState>({
  userAccount: typeof route.query.account === 'string' ? route.query.account : '',
  userPassword: '',
})

const validateAccount = async (_rule: Rule, value: string) => {
  if (!value) {
    throw new Error('请输入账号')
  }
  if (value.trim().length < 6) {
    throw new Error('账号不能少于6位')
  }
}

const validatePassword = async (_rule: Rule, value: string) => {
  if (!value) {
    throw new Error('请输入密码')
  }
  if (value.length < 8) {
    throw new Error('密码不能少于8位')
  }
}

const rules: Record<string, Rule[]> = {
  userAccount: [{ validator: validateAccount, trigger: 'change' }],
  userPassword: [{ validator: validatePassword, trigger: 'change' }],
}

const resolveRedirect = () => {
  const redirect = route.query.redirect
  if (typeof redirect === 'string' && redirect) {
    window.location.href = redirect
    return
  }
  void router.push('/')
}

const handleFinish = async () => {
  try {
    const payload: API.UserLoginDTO = {
      userAccount: formState.userAccount.trim(),
      userPassword: formState.userPassword,
    }
    const res = await userLogin(payload)
    if (res.data.code !== 0 || !res.data.data) {
      message.error(res.data.message || '登录失败')
      return
    }
    loginUserStore.setLoginUser(res.data.data)
    message.success('登录成功')
    resolveRedirect()
  } catch (error) {
    message.error(error instanceof Error ? error.message : '登录失败，请稍后重试')
  }
}

const goToRegister = () => {
  router.push({
    path: '/user/register',
    query: {
      redirect: typeof route.query.redirect === 'string' ? route.query.redirect : undefined,
      account: formState.userAccount || undefined,
    },
  })
}
</script>

<template>
  <div class="auth-page">
    <section class="auth-page__panel auth-page__panel--intro">
      <span class="auth-page__eyebrow">User Login</span>
      <h1>欢迎回来，继续你的 AI 应用创作</h1>
      <p>登录后将同步后端登录态，在顶部和首页展示头像、昵称、角色信息，并支持一键退出登录。</p>

      <div class="auth-page__feature-list">
        <div class="auth-feature">
          <strong>安全校验</strong>
          <span>账号至少 6 位，密码至少 8 位</span>
        </div>
        <div class="auth-feature">
          <strong>登录回跳</strong>
          <span>登录后可自动返回原页面</span>
        </div>
        <div class="auth-feature">
          <strong>统一体验</strong>
          <span>视觉风格与首页、导航、用户状态保持一致</span>
        </div>
      </div>
    </section>

    <section class="auth-page__panel auth-page__panel--form">
      <div class="auth-form-header">
        <h2>账号登录</h2>
        <p>请输入账号和密码，立即进入你的工作台。</p>
      </div>

      <a-form layout="vertical" :model="formState" :rules="rules" @finish="handleFinish">
        <a-form-item label="账号" name="userAccount">
          <a-input
            v-model:value="formState.userAccount"
            size="large"
            placeholder="请输入账号"
            allow-clear
          />
        </a-form-item>

        <a-form-item label="密码" name="userPassword">
          <a-input-password
            v-model:value="formState.userPassword"
            size="large"
            placeholder="请输入密码"
          />
        </a-form-item>

        <a-form-item class="auth-form__submit">
          <a-button html-type="submit" type="primary" size="large" block class="auth-form__button">
            登录
          </a-button>
        </a-form-item>
      </a-form>

      <div class="auth-form__footer">
        还没有账号？
        <a @click="goToRegister">立即注册</a>
      </div>
    </section>
  </div>
</template>

<style scoped>
.auth-page {
  display: grid;
  grid-template-columns: minmax(0, 1.08fr) minmax(360px, 460px);
  gap: 30px;
  align-items: stretch;
  min-height: calc(100vh - 220px);
  padding: 24px 0 8px;
}

.auth-page__panel {
  border-radius: 34px;
  border: 1px solid rgba(118, 151, 195, 0.14);
  box-shadow: var(--shadow-soft);
}

.auth-page__panel--intro {
  padding: clamp(28px, 5vw, 54px);
  background:
    radial-gradient(circle at 20% 20%, rgba(96, 145, 255, 0.18), transparent 28%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.76), rgba(247, 251, 255, 0.94));
}

.auth-page__panel--form {
  padding: 30px;
  background: rgba(255, 255, 255, 0.94);
}

.auth-page__eyebrow {
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

.auth-page__panel--intro h1 {
  margin: 22px 0 16px;
  font-size: clamp(42px, 5vw, 64px);
  line-height: 1.06;
}

.auth-page__panel--intro p {
  max-width: 620px;
  margin: 0;
  color: var(--text-secondary);
  font-size: 17px;
  line-height: 1.9;
}

.auth-page__feature-list {
  display: grid;
  gap: 16px;
  margin-top: 34px;
}

.auth-feature {
  padding: 18px 20px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(118, 151, 195, 0.12);
}

.auth-feature strong {
  display: block;
  margin-bottom: 8px;
  font-size: 18px;
}

.auth-feature span {
  color: var(--text-secondary);
  line-height: 1.7;
}

.auth-form-header h2 {
  margin: 0 0 8px;
  font-size: 34px;
}

.auth-form-header p {
  margin: 0 0 26px;
  color: var(--text-secondary);
  line-height: 1.7;
}

.auth-form__submit {
  margin-top: 8px;
  margin-bottom: 12px;
}

.auth-form__button {
  height: 50px;
  border-radius: 16px;
  box-shadow: 0 14px 30px rgba(45, 127, 249, 0.22);
}

.auth-form__footer {
  color: var(--text-secondary);
  text-align: center;
}

.auth-form__footer a {
  color: var(--brand-primary);
}

@media (max-width: 980px) {
  .auth-page {
    grid-template-columns: 1fr;
    min-height: auto;
  }
}

@media (max-width: 767px) {
  .auth-page {
    padding-top: 8px;
  }

  .auth-page__panel--form {
    padding: 22px 18px;
  }
}
</style>
