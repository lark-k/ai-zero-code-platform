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
  display: flex;
  flex-direction: column;
  background: transparent;
}

.basic-layout__content {
  flex: 1;
  padding: 24px 16px 32px;
  background: transparent;
}

.basic-layout__content-inner {
  width: min(1280px, 100%);
  min-height: 100%;
  margin: 0 auto;
  padding: clamp(20px, 3vw, 32px);
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(111, 150, 188, 0.18);
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(31, 51, 74, 0.12);
  backdrop-filter: blur(14px);
}

@media (max-width: 767px) {
  .basic-layout__content {
    padding: 20px 12px 24px;
  }

  .basic-layout__content-inner {
    border-radius: 20px;
  }
}
</style>
