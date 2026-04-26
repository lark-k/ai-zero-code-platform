<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import {
  getAppVoById,
  updateApp,
  updateAppByAdmin,
} from '@/api/yingyongmokuaixiangguanjiekou.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()
const loading = ref(false)
const submitting = ref(false)
const app = ref<API.AppVO>()

const formState = reactive({
  appName: '',
  cover: '',
  priority: 0,
})

const appId = computed(() => String(route.params.id || ''))
const isAdminMode = computed(() => loginUserStore.isAdmin)
const canEdit = computed(() => isAdminMode.value || app.value?.userId === loginUserStore.loginUser.id)

const loadApp = async () => {
  loading.value = true
  try {
    const res = await getAppVoById({ id: appId.value as unknown as number })
    if (res.data.code !== 0 || !res.data.data) {
      message.error(res.data.message || '获取应用信息失败')
      await router.push('/')
      return
    }
    app.value = res.data.data
    formState.appName = res.data.data.appName || ''
    formState.cover = res.data.data.cover || ''
    formState.priority = res.data.data.priority ?? 0
    if (!canEdit.value) {
      message.warning('普通用户只能编辑自己的应用')
      await router.push(`/app/detail/${appId.value}`)
    }
  } catch (error) {
    message.error(error instanceof Error ? error.message : '获取应用信息失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  if (!formState.appName.trim()) {
    message.warning('请输入应用名称')
    return
  }
  if (!canEdit.value) {
    message.warning('无权限编辑该应用')
    return
  }

  submitting.value = true
  try {
    const res = isAdminMode.value
      ? await updateAppByAdmin({
          id: appId.value as unknown as number,
          appName: formState.appName.trim(),
          cover: formState.cover.trim() || undefined,
          priority: formState.priority,
        })
      : await updateApp({
          id: appId.value as unknown as number,
          appName: formState.appName.trim(),
        })

    if (res.data.code !== 0) {
      message.error(res.data.message || '更新应用失败')
      return
    }
    message.success('更新应用成功')
    await router.push(`/app/detail/${appId.value}`)
  } catch (error) {
    message.error(error instanceof Error ? error.message : '更新应用失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  void loadApp()
})
</script>

<template>
  <main class="edit-page">
    <section class="edit-panel">
      <div class="edit-panel__header">
        <div>
          <span class="edit-panel__eyebrow">{{ isAdminMode ? 'Admin Edit' : 'Owner Edit' }}</span>
          <h1>应用信息修改</h1>
          <p>
            普通用户只支持修改自己的应用名称；管理员可更新应用名称、封面和精选优先级。
          </p>
        </div>
      </div>

      <a-spin :spinning="loading">
        <a-form layout="vertical" class="edit-form">
          <a-form-item label="应用名称" required>
            <a-input
              v-model:value="formState.appName"
              size="large"
              placeholder="请输入应用名称"
              allow-clear
            />
          </a-form-item>

          <template v-if="isAdminMode">
            <a-form-item label="应用封面">
              <a-input
                v-model:value="formState.cover"
                size="large"
                placeholder="请输入封面 URL"
                allow-clear
              />
            </a-form-item>
            <a-form-item label="优先级">
              <a-input-number
                v-model:value="formState.priority"
                :min="0"
                :max="99"
                size="large"
                style="width: 180px"
              />
              <span class="edit-form__hint">设置为 99 即精选应用</span>
            </a-form-item>
          </template>

          <a-form-item>
            <a-space wrap>
              <a-button @click="router.back()">取消</a-button>
              <a-button type="primary" :loading="submitting" @click="submit">保存修改</a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </a-spin>
    </section>
  </main>
</template>

<style scoped>
.edit-page {
  padding: 26px 0 48px;
}

.edit-panel {
  max-width: 820px;
  margin: 0 auto;
  padding: 34px;
  border: 1px solid rgba(112, 142, 178, 0.14);
  border-radius: 30px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: var(--shadow-soft);
}

.edit-panel__header {
  margin-bottom: 28px;
}

.edit-panel__eyebrow {
  display: inline-flex;
  padding: 8px 14px;
  color: #2d7ff9;
  font-size: 13px;
  font-weight: 700;
  border-radius: 999px;
  background: rgba(45, 127, 249, 0.08);
}

.edit-panel h1 {
  margin: 18px 0 12px;
  color: #111827;
  font-size: clamp(34px, 4vw, 52px);
}

.edit-panel p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.8;
}

.edit-form__hint {
  margin-left: 12px;
  color: #667085;
}

@media (max-width: 767px) {
  .edit-panel {
    padding: 24px 18px;
  }
}
</style>
