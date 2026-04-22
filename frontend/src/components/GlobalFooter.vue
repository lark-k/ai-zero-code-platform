<template>
  <a-layout-footer class="global-footer">
    <div class="global-footer__inner">
      <span>AI 应用生成平台</span>
      <span class="global-footer__divider">·</span>
      <span>让注册、登录和应用创建体验更轻盈</span>
      <span class="global-footer__divider">·</span>
      <a class="global-footer__link" href="mailto:1573002354@qq.com">1573002354@qq.com</a>
    </div>
  </a-layout-footer>
</template>

<style scoped>
.global-footer {
  padding: 10px 20px 28px;
  background: transparent;
}

.global-footer__inner {
  width: min(var(--page-max-width), 100%);
  margin: 0 auto;
  padding: 0 8px;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
  color: rgba(74, 97, 126, 0.86);
  font-size: 14px;
}

.global-footer__divider {
  color: rgba(74, 97, 126, 0.4);
}

.global-footer__link {
  color: var(--brand-primary);
}

@media (max-width: 767px) {
  .global-footer {
    padding: 4px 12px 20px;
  }

  .global-footer__inner {
    gap: 6px;
    font-size: 13px;
  }
}
</style>
