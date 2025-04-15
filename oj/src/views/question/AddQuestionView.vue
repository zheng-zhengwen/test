<template>
  <div id="addQuestionView">
    <h2 class="question-title">创建题目</h2>
    <a-form
      :model="form"
      label-align="left"
      class="custom-form"
      @submit="handleSubmit"
    >
      <a-form-item field="title" label="标题">
        <a-input v-model="form.title" placeholder="请输入标题" />
      </a-form-item>
      <a-form-item field="tags" label="标签">
        <a-input-tag v-model="form.tags" placeholder="请输入标签" allow-clear />
      </a-form-item>
      <a-form-item field="content" label="题目内容: ">
        <md-editor
          @click="
            contentZIndex = 2;
            answerZIndex = 1;
          "
          :style="`z-index: ${contentZIndex}`"
          mode="split"
          :value="form.content"
          :handle-change="onContentChange"
        />
      </a-form-item>
      <a-form-item field="answer" label="标准答案: ">
        <md-editor
          @click="
            contentZIndex = 1;
            answerZIndex = 2;
          "
          :style="`z-index: ${answerZIndex}`"
          mode="split"
          :value="form.answer"
          :handle-change="onAnswerChange"
        />
      </a-form-item>
      <a-form-item label="判题配置" :content-flex="false" :merge-props="false">
        <a-space direction="vertical" style="min-width: 480px">
          <a-form-item field="judgeConfig.timeLimit" label="时间限制">
            <a-input-number
              v-model="form.judgeConfig.timeLimit"
              placeholder="请输入时间消耗"
              mode="button"
              size="large"
              min="0"
            />
          </a-form-item>
          <a-form-item field="judgeConfig.memoryLimit" label="内存限制">
            <a-input-number
              v-model="form.judgeConfig.memoryLimit"
              placeholder="请输入内存限制"
              mode="button"
              size="large"
              min="0"
            />
          </a-form-item>
          <a-form-item field="judgeConfig.stackLimit" label="堆栈限制">
            <a-input-number
              v-model="form.judgeConfig.stackLimit"
              placeholder="请输入堆栈限制"
              mode="button"
              size="large"
              min="0"
            />
          </a-form-item>
        </a-space>
      </a-form-item>
      <a-form-item
        label="测试用例配置"
        :content-flex="false"
        :merge-props="false"
      >
        <a-space direction="vertical" style="min-width: 640px">
          <template
            v-for="(judgeCaseItem, index) in form.judgeCase"
            :key="index"
          >
            <div class="test-case-unit">
              <a-form-item
                :field="`form.judgeCase[${index}].input`"
                :label="`输入样例-${index}`"
              >
                <a-input
                  v-model="judgeCaseItem.input"
                  placeholder="请输入测试输入样例"
                />
              </a-form-item>
              <a-form-item
                :field="`form.judgeCase[${index}].output`"
                :label="`输出样例-${index}`"
              >
                <a-input
                  v-model="judgeCaseItem.output"
                  placeholder="请输入测试输出样例"
                />
              </a-form-item>
              <a-button status="danger" @click="handleDelete(index)"
                >删除
              </a-button>
            </div>
          </template>
        </a-space>
        <a-row class="button-row">
          <a-col :span="24">
            <a-button @click="handleAdd" type="outline" status="success">
              新增测试样例
            </a-button>
            <a-button
              type="primary"
              status="success"
              style="margin-left: 16px; min-width: 120px"
              @click="doSubmit"
            >
              提交
            </a-button>
          </a-col>
        </a-row>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import MdEditor from "@/components/MdEditor.vue";
import { QuestionControllerService } from "../../../generated";
import { Message } from "@arco-design/web-vue";
import { useRoute } from "vue-router";

let form = ref({
  tags: [],
  title: "",
  answer: "",
  content: "",
  judgeCase: [
    {
      input: "",
      output: "",
    },
  ],
  judgeConfig: {
    memoryLimit: 1000,
    stackLimit: 1000,
    timeLimit: 1000,
  },
});
const route = useRoute();
const contentZIndex = ref(1);
const answerZIndex = ref(1);

/**
 * 如果页面路由地址包含update,视为更新页面
 */
const updatePage = route.path.includes("update");
/**
 * 根据题目id获取老的数据
 */
const loadData = async () => {
  const id = route.query.id;
  if (!id) return;
  const res = await QuestionControllerService.getQuestionByIdUsingGet(
    id as any
  );
  if (res.code === 0) {
    form.value = res.data as any;
    if (!form.value.judgeCase) {
      form.value.judgeCase = [
        {
          input: "",
          output: "",
        },
      ];
    } else {
      form.value.judgeCase = JSON.parse(form.value.judgeCase as any);
    }
    if (!form.value.judgeConfig) {
      form.value.judgeConfig = {
        memoryLimit: 1000,
        stackLimit: 1000,
        timeLimit: 1000,
      };
    } else {
      form.value.judgeConfig = JSON.parse(form.value.judgeConfig as any);
    }
    if (!form.value.tags) {
      form.value.tags = [];
    } else {
      //json转js对象
      form.value.tags = JSON.parse(form.value.tags as any);
    }
  } else {
    Message.error("加载失败" + res.message);
  }
};
onMounted(() => {
  loadData();
});
const doSubmit = async () => {
  console.log(form.value);
  //判断更新还是新增
  if (updatePage) {
    const res = await QuestionControllerService.updateQuestionUsingPost(
      form.value
    );
    if (res.code === 0) {
      Message.success("更新成功");
    } else {
      Message.error("更新失败" + res.message);
    }
  } else {
    const res = await QuestionControllerService.addQuestionUsingPost(
      form.value
    );
    if (res.code === 0) {
      Message.success("创建成功");
    } else {
      Message.error("创建失败" + res.message);
    }
  }
};
/**
 * 新增判题用例
 * */
const handleAdd = () => {
  form.value.judgeCase.push({
    input: "",
    output: "",
  });
};
/**
 * 删除判题用例
 * */
const handleDelete = (index: number) => {
  form.value.judgeCase.splice(index, 1);
};
const handleSubmit = () => {
  console.log(1);
};
const onContentChange = (value: string) => {
  form.value.content = value;
};
const onAnswerChange = (value: string) => {
  form.value.answer = value;
};
</script>

<style scoped>
#addQuestionView {
  padding: 10px; /* 进一步缩小左右留白 */
  margin: 0 auto;
}

.question-title {
  color: #2c6545;
  font-size: 28px;
  border-bottom: 3px solid #2c6545;
  padding-bottom: 12px;
  margin-bottom: 32px;
}

.custom-form {
  background: #f8f9fa;
  padding: 18px; /* 适当减少表单内边距 */
  border-radius: 12px;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.18);
  overflow: visible; /* 防止阴影在缩小时消失 */
  box-sizing: border-box;
}

a-form-item {
  margin-bottom: 24px;
}

.test-case-unit {
  border: 1px solid #e0e1e6;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 16px;
  background: #fff;
}

.button-row {
  margin-top: 24px;
}

/* 让 MdEditor 铺满容器 */
a-form-item:has(MdEditor) .arco-form-item-control-content {
  width: 100%;
}
</style>
