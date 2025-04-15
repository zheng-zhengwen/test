<template>
  <div class="exampleView">
    <CodeEditor :value="codeValue" :handle-change="onCodeChange" />
    <MdEditor :value="mdValue" :handle-change="onMdChange"></MdEditor>
  </div>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import MdEditor from "@/components/MdEditor.vue";
import CodeEditor from "@/components/CodeEditor.vue"; // @ is an alias to /src
const codeValue = ref("");
const mdValue = ref("");
const onCodeChange = (v: string) => {
  codeValue.value = v;
  console.log(v);
};
const onMdChange = (v: string) => {
  mdValue.value = v;
};
</script>
