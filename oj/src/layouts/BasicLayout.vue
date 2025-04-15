<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
      <a-layout-footer class="footer">
        <div class="footer-content">
          <div class="footer-info">
            <span>© 2025 WEN </span>
            <span class="divider">|</span>
            <span> 在线编程平台</span>
          </div>
        </div>
      </a-layout-footer>
    </a-layout>
  </div>
</template>
<script setup lang="ts">
import GlobalHeader from "@/components/GlobalHeader.vue";
</script>
<style scoped>
#basicLayout {
}

#basicLayout .header {
  margin-bottom: 16px;
  box-shadow: 1px 1px 5px #eee;
}

#basicLayout .content {
  /* 水平方向的渐变背景 */
  background: linear-gradient(to right, #fefefe, #fff);
  margin-bottom: 16px;
  padding: 20px;
}

#basicLayout .footer {
  background: #f6f9fa;
  padding: 14px 0;
  bottom: 0;
  right: 0;
  left: 0;
  text-align: center;
}
</style>
