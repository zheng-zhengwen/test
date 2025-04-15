<template>
  <template v-if="route.path.startsWith('/user')">
    <router-view />
  </template>
  <template v-else>
    <BasicLayout />
  </template>
</template>

<style></style>
<script setup lang="ts">
import BasicLayout from "@/layouts/BasicLayout.vue";
import { onMounted } from "vue";
import { useRoute } from "vue-router";

const route = useRoute();

const doInit = () => {
  console.log("三更灯火五更鸡，正是男儿读书时");
};
onMounted(() => {
  doInit();
});
</script>
