<template xmlns:height="http://www.w3.org/1999/xhtml">
  <div
    id="code-editor"
    ref="codeEditorRef"
    style="min-height: 400px; height: 70vh"
  />
</template>
<script setup lang="ts">
import * as monaco from "monaco-editor";
import { defineProps, onMounted, ref, toRaw, watch, withDefaults } from "vue";

const codeEditorRef = ref();
const codeEditor = ref();

/**
 * 定义组件属性类型
 * */
interface Props {
  value: string;
  language?: string;
  handleChange: (v: string) => void;
}

/**
 * 给组件指定初始值
 * */
const props = withDefaults(defineProps<Props>(), {
  value: () => "",
  language: () => "java",
  handleChange: (v: string) => {
    console.log(v);
  },
});
/**
 * 修复代码编辑器没有更改语言
 *  */
watch(
  () => props.language,
  () => {
    if (codeEditor.value) {
      //官方实例 1、当前编辑器的实例，2、要切换的编程语言
      monaco.editor.setModelLanguage(
        toRaw(codeEditor.value).getModel(),
        props.language
      );
    }
  }
);

// const fillValue = () => {
//   if (!codeEditor.value) return;
//   //改变值
//   toRaw(codeEditor.value).setValue("新的值");
// };

// watch([props.language], () => {
//   console.log(props.language);
//   codeEditor.value = monaco.editor.create(codeEditorRef.value, {
//     value: props.value,
//     language: props.language,
//     automaticLayout: true,
//     colorDecorators: true,
//     minimap: {
//       enabled: true,
//     },
//     // lineNumbers: "off",
//     // roundedSelection: false,
//     // scrollBeyondLastLine: false,
//     readOnly: false,
//     theme: "vs-dark",
//   });
// });

onMounted(() => {
  if (!codeEditorRef.value) {
    return;
  }
  // Hover on each property to see its docs!
  codeEditor.value = monaco.editor.create(codeEditorRef.value, {
    value: props.value,
    language: props.language,
    automaticLayout: true,
    colorDecorators: true,
    minimap: {
      enabled: false,
    },
    // lineNumbers: "off",
    // roundedSelection: false,
    scrollBeyondLastLine: false,
    readOnly: false,
    theme: "vs-dark",
  });
  /**
   * 编辑监听内容变化
   * */
  codeEditor.value.onDidChangeModelContent(() => {
    // console.log("目前内容为:", toRaw(codeEditor.value).getValue());
    props.handleChange(toRaw(codeEditor.value).getValue());
  });
});
</script>
<style scoped></style>
