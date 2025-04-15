<template>
  <div id="viewQuestionView">
    <a-row :gutter="[24, 24]" class="section-grid">
      <a-col :md="12" :xs="24">
        <a-tabs default-active-key="question" class="leaderboard-section">
          <a-tab-pane key="question" title="题目">
            <a-card
              v-if="question"
              :title="question.title"
              class="arco-card animated"
            >
              <a-descriptions
                title="判题条件"
                :column="{ xs: 1, md: 2, lg: 3 }"
                class="arco-descriptions"
              >
                <a-descriptions-item label="时间限制">
                  {{ question.judgeConfig?.timeLimit ?? 0 }}
                </a-descriptions-item>
                <a-descriptions-item label="内存限制">
                  {{ question.judgeConfig?.memoryLimit ?? 0 }}
                </a-descriptions-item>
                <a-descriptions-item label="堆栈限制">
                  {{ question.judgeConfig?.stackLimit ?? 0 }}
                </a-descriptions-item>
              </a-descriptions>
              <MdViewer
                :value="question.content || ''"
                class="arco-typography"
              />
              <template #extra>
                <a-space wrap class="tag-container">
                  <a-tag
                    v-for="(tag, index) of question.tags"
                    :key="index"
                    color="green"
                    class="arco-tag"
                  >
                    {{ tag }}
                  </a-tag>
                </a-space>
              </template>
            </a-card>
          </a-tab-pane>
          <!--          <a-tab-pane key="comment" title="评论" disabled> 评论区</a-tab-pane>-->
          <a-tab-pane key="answer" title="答案">
            <!-- 添加加载状态和空值检查 -->
            <!-- 如果 loading 为 true，说明数据正在加载中，显示加载图标 -->
            <template v-if="loading">
              <a-spin class="arco-spin animated" />
            </template>
            <!-- 如果 loading 为 false，且 question 存在并且 question.answer 也存在，渲染 MdViewer 组件 -->
            <template v-else-if="question && question.answer">
              <MdViewer
                :value="question.answer"
                class="arco-typography md-viewer-custom"
              />
            </template>
            <!-- 如果以上条件都不满足，即数据加载完成但没有答案，显示占位提示 -->
            <template v-else>
              <a-empty description="暂无答案" class="arco-empty animated" />
            </template>
          </a-tab-pane>
          <a-tab-pane key="submits" title="提交记录">
            <QuestionSubmitList
              v-if="question?.id"
              :question-id="question.id"
              class="question-submit-list"
            />
          </a-tab-pane>
          <a-tab-pane key="ai" title="AI 答题">
            <a-card
              v-if="question"
              :title="`题目：${question.title}`"
              class="arco-card animated"
            >
              <template #extra>
                <a-button
                  type="primary"
                  @click="handleAiSubmit(form.language)"
                  :loading="aiLoading"
                  class="arco-btn arco-btn-primary animated"
                >
                  AI 解答
                </a-button>
              </template>
              <div v-if="aiResult">
                <a-typography-title :heading="6" class="arco-typography-title">
                  AI 解答结果：
                </a-typography-title>
                <a-divider class="arco-divider" />
                <MdViewer
                  :value="aiResult.result || ''"
                  disabled="disabled"
                  class="arco-typography"
                />
                <template v-if="aiResult.code">
                  <a-typography-title
                    :heading="6"
                    class="arco-typography-title"
                  >
                    示例代码：
                  </a-typography-title>
                  <CodeEditor
                    :value="aiResult.code"
                    :language="form.language"
                    :readonly="true"
                    class="code-editor"
                  />
                </template>
              </div>
              <a-empty
                v-else
                description="点击 AI 解答获取答案"
                class="arco-empty animated"
              />
            </a-card>
          </a-tab-pane>
        </a-tabs>
      </a-col>
      <a-col :md="12" :xs="24">
        <a-form
          :model="form"
          layout="inline"
          class="arco-form animated"
          style="width: 96%"
        >
          <!-- 表单内容 -->
          <a-form-item
            field="language"
            label="编程语言"
            style="min-width: 140px"
            class="arco-form-item"
          >
            <a-select
              :style="{ width: '320px' }"
              placeholder="请选择编程语言"
              v-model="form.language"
              class="arco-select"
            >
              <a-option class="arco-option">java</a-option>
              <a-option class="arco-option">cpp</a-option>
              <a-option class="arco-option">python</a-option>
            </a-select>
          </a-form-item>
          <a-button
            type="primary"
            style="min-width: 140px"
            status="success"
            @click="doSubmit"
            class="arco-btn arco-btn-primary arco-btn-success animated"
          >
            提交代码
          </a-button>
        </a-form>
        <CodeEditor
          :key="form.language"
          :value="form.code as string"
          :language="form.language"
          :handle-change="changeCode"
          class="code-editor animated"
          style="width: 100%"
        />
        <a-divider size="0" class="arco-divider" />
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, withDefaults, defineProps, watch } from "vue";
import {
  AiQuestionVO,
  QuestionControllerService,
  QuestionSubmitAddRequest,
  QuestionVO,
} from "../../../generated";
import { Message } from "@arco-design/web-vue";
import CodeEditor from "@/components/CodeEditor.vue";
import MdViewer from "@/components/MdViewer.vue";
import message from "@arco-design/web-vue/es/message";
import { eventBus, EventType } from "@/utils/eventBus";
import QuestionSubmitList from "@/components/QuestionSubmitList.vue";

interface Props {
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: () => "",
});

const question = ref<QuestionVO>();
const loading = ref(true);

const loadData = async () => {
  try {
    const res = await QuestionControllerService.getQuestionVoByIdUsingGet(
      props.id as any
    );
    if (res.code === 0) {
      question.value = res.data;
    } else {
      Message.error("加载失败" + res.message);
    }
  } catch (error) {
    Message.error("加载失败");
  } finally {
    loading.value = false;
  }
};

const LANGUAGE_TEMPLATES = {
  java: `import java.util.*;
public class Main {
    public static void main(String[] args) {
        // 在这里写下你的代码
        int num1 = Integer.parseInt(args[0]);
        int num2 = Integer.parseInt(args[1]);
        // 进行加法运算
        int sum = num1 + num2;
        // 输出结果
        System.out.println(sum);
    }
}`,
  cpp: `#include <iostream>
#include <cstdlib> // 用于 atoi

int main(int argc, char* argv[]) {
    // 解析参数
    int a = std::atoi(argv[1]);
    int b = std::atoi(argv[2]);
    std::cout << a + b << std::endl;
    return 0;
}`,
  go: `package main

import "fmt"

func main() {
    // 在这里写下你的代码
    fmt.Println("Hello World!")
}`,
};

const form = ref<QuestionSubmitAddRequest>({
  language: "java",
  code: LANGUAGE_TEMPLATES.java,
});

watch(
  () => form.value.language,
  (newLanguage) => {
    const template =
      LANGUAGE_TEMPLATES[newLanguage as keyof typeof LANGUAGE_TEMPLATES];
    if (template !== undefined) form.value.code = template;
  }
);

/**
 * 提交代码
 * */
const doSubmit = async () => {
  if (!question.value?.id) {
    return;
  }
  const res = await QuestionControllerService.doQuestionSubmitUsingPost({
    ...form.value,
    questionId: question.value.id,
  });
  if (res.code === 0) {
    message.success("提交成功");
    eventBus.emit(EventType.REFRESH_SUBMITS);
    // 发送开始轮询的消息
    eventBus.emit(EventType.START_POLLING, question.value.id);
  } else {
    message.error("提交失败" + res.message);
  }
};

/**
 * 页加载时请求数据
 * */
onMounted(() => {
  loadData();
});

/**
 * 监听输入
 * */
const changeCode = (value: string) => {
  form.value.code = value;
};

/**
 AI 相关状态
 **/
const aiLoading = ref(false);
const aiResult = ref<AiQuestionVO>();

/**
 * 处理 AI 解答
 */
const handleAiSubmit = async (language: string) => {
  if (!question.value?.id) {
    Message.error("题目不存在");
    return;
  }

  try {
    aiLoading.value = true;
    const res = await QuestionControllerService.aiQuestionUsingPost({
      questionId: question.value.id,
      language: language,
    });

    if (res.code === 0 && res.data) {
      aiResult.value = res.data;
      Message.success("AI 解答成功");
    } else {
      Message.error("AI 解答失败：" + (res.message || "未知错误"));
    }
  } catch (error: any) {
    Message.error("AI 解答失败：" + (error.message || "未知错误"));
  } finally {
    aiLoading.value = false;
  }
};
</script>

<style scoped>
#viewQuestionView {
  margin: 0 auto;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

#viewQuestionView .arco-space-horizontal .arco-space-item {
  margin-bottom: 0 !important;
}

.arco-card {
  border-radius: 16px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  transition: transform 0.3s ease-in-out;
  position: relative; /* 添加相对定位，以便 z-index 生效 */
  z-index: 1; /* 默认的 z-index 值 */
  margin-top: 5px; /* 添加 margin-top，将整个卡片往下移动 */
}

.arco-card:hover {
  transform: translateY(-5px);
  z-index: 2; /* 悬停时的 z-index 值，确保在其他元素之上 */
}

.arco-descriptions {
  background-color: #f0f5f9;
  border-radius: 8px;
  padding: 16px;
}

.arco-tag {
  display: inline-flex;
  align-items: center;
  height: 18px;
  margin: 0 4px;
  border-radius: 8px;
  padding: 0 12px;
  font-size: 14px;
}

.arco-form {
  background: #f8f9fa;
  padding: 18px;
  border-radius: 12px;
  box-shadow: 0 2px 14px rgba(0, 0, 0, 0.15);
  margin-bottom: 20px;
  transition: transform 0.3s ease-in-out;
}

.arco-form:hover {
  transform: translateY(-5px);
}

.arco-btn {
  border-radius: 4px;
  transition: background-color 0.3s ease-in-out;
}

.arco-btn:hover {
  background-color: rgba(0, 0, 0, 0.1);
}

.code-editor {
  border-radius: 4px;
  border: 1px solid #e5e9ef;
  padding: 6px;
  transition: border-color 0.3s ease-in-out;
}

.code-editor:hover {
  border-color: #007bff;
}

.animated {
  animation-duration: 0.5s;
  animation-fill-mode: both;
}

/* 美化 MdViewer */
.md-viewer-custom {
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
  font-size: 16px;
  line-height: 1.6;
  color: #333;
  background-color: #f9f9f9;
  border: 1px solid #e5e9ef;
  border-radius: 8px;
  padding: 20px;
  margin-top: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.md-viewer-custom h1,
.md-viewer-custom h2,
.md-viewer-custom h3,
.md-viewer-custom h4,
.md-viewer-custom h5,
.md-viewer-custom h6 {
  color: #222;
  margin-top: 20px;
  margin-bottom: 10px;
}

.md-viewer-custom p {
  margin-bottom: 15px;
}

.md-viewer-custom pre {
  background-color: #f4f4f4;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 10px;
  overflow-x: auto;
}

.md-viewer-custom code {
  font-family: "Courier New", Courier, monospace;
  background-color: #f4f4f4;
  padding: 2px 4px;
  border-radius: 4px;
}

.md-viewer-custom blockquote {
  border-left: 4px solid #ddd;
  padding-left: 15px;
  color: #777;
  margin: 20px 0;
}
</style>
