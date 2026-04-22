<script setup lang="ts">
import { RouterView } from 'vue-router'

import GlobalFooter from '@/components/GlobalFooter.vue'
import GlobalHeader from '@/components/GlobalHeader.vue'
</script>

<template>
  <a-layout class="basic-layout">
    <GlobalHeader />

    <a-layout-content class="basic-layout__content">
      <div class="basic-layout__content-inner">
        <RouterView />
      </div>
    </a-layout-content>

    <GlobalFooter />
  </a-layout>
</template>

<style scoped>
.basic-layout {
  min-height: 100vh;
  background: transparent;
}

.basic-layout__content {
  flex: 1;
  padding: 28px 20px 40px;
  background: transparent;
}

.basic-layout__content-inner {
  width: min(var(--page-max-width), 100%);
  min-height: 100%;
  margin: 0 auto;
}

@media (max-width: 767px) {
  .basic-layout__content {
    padding: 16px 12px 24px;
  }
}
</style>
