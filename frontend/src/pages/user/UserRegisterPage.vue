<script setup lang="ts">
import { reactive } from 'vue'
import { message } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { useRoute, useRouter } from 'vue-router'
import { userRegister } from '@/api/yonghuxiangguanjiekou.ts'

interface RegisterFormState {
  userAccount: string
  userPassword: string
  confirmPassword: string
}

const router = useRouter()
const route = useRoute()

const formState = reactive<RegisterFormState>({
  userAccount: typeof route.query.account === 'string' ? route.query.account : '',
  userPassword: '',
  confirmPassword: '',
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

const validateConfirmPassword = async (_rule: Rule, value: string) => {
  if (!value) {
    throw new Error('请再次输入密码')
  }
  if (value !== formState.userPassword) {
    throw new Error('两次输入的密码不一致')
  }
}

const rules: Record<string, Rule[]> = {
  userAccount: [{ validator: validateAccount, trigger: 'change' }],
  userPassword: [{ validator: validatePassword, trigger: 'change' }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'change' }],
}

const handleFinish = async () => {
  try {
    const payload: API.UserRegisterDTO = {
      userAccount: formState.userAccount.trim(),
      userPassword: formState.userPassword,
      confirmPassword: formState.confirmPassword,
    }
    const res = await userRegister(payload)
    if (res.data.code !== 0) {
      message.error(res.data.message || '注册失败')
      return
    }
    message.success('注册成功，请登录')
    await router.push({
      path: '/user/login',
      query: {
        account: payload.userAccount,
        redirect: typeof route.query.redirect === 'string' ? route.query.redirect : undefined,
      },
    })
  } catch (error) {
    message.error(error instanceof Error ? error.message : '注册失败，请稍后重试')
  }
}

const goToLogin = () => {
  router.push({
    path: '/user/login',
    query: {
      account: formState.userAccount || undefined,
      redirect: typeof route.query.redirect === 'string' ? route.query.redirect : undefined,
    },
  })
}
</script>

<template>
  <div class="auth-page">
    <section class="auth-page__panel auth-page__panel--intro">
      <span class="auth-page__eyebrow">User Register</span>
      <h1>创建你的账号，进入更完整的应用体验</h1>
      <p>注册页已接入后端接口，并增加账号长度、密码长度和确认密码一致性校验，视觉上也与首页保持统一。</p>

      <div class="auth-page__feature-list">
        <div class="auth-feature">
          <strong>注册规则清晰</strong>
          <span>账号不少于 6 位，密码不少于 8 位</span>
        </div>
        <div class="auth-feature">
          <strong>流程更顺畅</strong>
          <span>注册成功后自动带上账号跳转到登录页</span>
        </div>
        <div class="auth-feature">
          <strong>品牌感统一</strong>
          <span>白色卡片、浅蓝背景和圆角阴影风格与首页一致</span>
        </div>
      </div>
    </section>

    <section class="auth-page__panel auth-page__panel--form">
      <div class="auth-form-header">
        <h2>创建账号</h2>
        <p>填写下面的信息，即可快速完成注册。</p>
      </div>

      <a-form layout="vertical" :model="formState" :rules="rules" @finish="handleFinish">
        <a-form-item label="账号" name="userAccount">
          <a-input
            v-model:value="formState.userAccount"
            size="large"
            placeholder="请设置账号"
            allow-clear
          />
        </a-form-item>

        <a-form-item label="密码" name="userPassword">
          <a-input-password
            v-model:value="formState.userPassword"
            size="large"
            placeholder="请设置密码"
          />
        </a-form-item>

        <a-form-item label="确认密码" name="confirmPassword">
          <a-input-password
            v-model:value="formState.confirmPassword"
            size="large"
            placeholder="请再次输入密码"
          />
        </a-form-item>

        <a-form-item class="auth-form__submit">
          <a-button html-type="submit" type="primary" size="large" block class="auth-form__button">
            注册
          </a-button>
        </a-form-item>
      </a-form>

      <div class="auth-form__footer">
        已有账号？
        <a @click="goToLogin">去登录</a>
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
    radial-gradient(circle at 20% 20%, rgba(126, 121, 255, 0.16), transparent 28%),
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
  background: rgba(122, 121, 255, 0.08);
  color: #6e6cff;
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
  box-shadow: 0 14px 30px rgba(110, 108, 255, 0.2);
}

.auth-form__footer {
  color: var(--text-secondary);
  text-align: center;
}

.auth-form__footer a {
  color: #6e6cff;
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
