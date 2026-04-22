<script setup lang="ts">
import { onMounted } from 'vue'
import BasicLayout from '@/layouts/BasicLayout.vue'
import { antdLocale } from '@/config/antdLocale'
import { useLoginUserStore } from '@/stores/loginUser.ts'

const loginUserStore = useLoginUserStore()

onMounted(() => {
  if (!loginUserStore.hasFetched) {
    void loginUserStore.fetchLoginUser()
  }
})
</script>

<template>
  <a-config-provider :locale="antdLocale">
    <BasicLayout />
  </a-config-provider>
</template>
