<template>
  <div id="userLayout" ref="vantaRef">
    <a-layout style="min-height: 100vh" class="centered-layout">
      <a-layout-header class="header">
        <a-space>
          <img src="../assets/thisLogo.png" class="logo" />
          <div class="title">在线编程系统</div>
        </a-space>
      </a-layout-header>

      <a-layout-content class="content">
        <router-view />
      </a-layout-content>

      <a-layout-footer class="footer">
        <div class="footer-content">
          <div class="footer-info">
            <span>© 2025 WEN </span>
            <span class="divider">|</span>
            <span class="platform-name"> 在线编程平台</span>
          </div>
        </div>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from "vue";
import * as THREE from "three";
import FOG from "vanta/src/vanta.fog";

const vantaRef = ref(null);
let vantaEffect: any = null;

onMounted(() => {
  vantaEffect = FOG({
    el: vantaRef.value,
    THREE,
    mouseControls: true,
    touchControls: true,
    gyroControls: false,
    minHeight: 200.0,
    minWidth: 200.0,
    highlightColor: 0x4bb77e,
    midtoneColor: 0x6dbd93,
    lowlightColor: 0xa5dbc2,
    baseColor: 0xffffff,
    blurFactor: 0.4,
    speed: 1.2,
    zoom: 0.5,
  });
});

onBeforeUnmount(() => {
  if (vantaEffect) vantaEffect.destroy();
});
</script>

<style scoped>
#userLayout {
  text-align: center;
  position: relative;
  overflow: hidden;
}

#userLayout .logo {
  width: 110px;
  height: 110px;
}

#userLayout .header {
  margin-top: 110px;
  position: relative;
  z-index: 10;
}

#userLayout .content {
  margin-bottom: 16px;
  padding: 20px;
  position: relative;
  z-index: 10;
}

#userLayout .footer {
  background: rgba(255, 255, 255, 0.9);
  padding: 14px;
  position: sticky;
  bottom: 0;
  right: 0;
  left: 0;
  backdrop-filter: blur(10px);
  z-index: 10;
  color: #2c6545;
  font-weight: bold;
}

.footer-info .platform-name {
  font-size: 16px;
  color: #2c6545;
  margin-left: 5px;
}

#userLayout .title {
  font-size: 30px;
  font-weight: bold;
  animation: rainbow 8s linear infinite;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-fill-color: transparent;
}

@keyframes rainbow {
  0%,
  100% {
    background-image: linear-gradient(to right, #02863c, #116466);
  }
  50% {
    background-image: linear-gradient(to right, #116466, #02863c);
  }
}
</style>
