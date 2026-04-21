<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import { antdLocale } from '@/config/antdLocale'
</script>

<template>
  <a-config-provider :locale="antdLocale">
    <BasicLayout />
  </a-config-provider>
</template>
