<template>
  <div id="manageQuestionView">
    <h2 class="question-title">题目管理</h2>
    <a-table
      v-if="dataList && dataList.length > 0"
      :ref="tableRef"
      :columns="columns"
      :data="dataList"
      :pagination="searchParams"
      @page-change="onPageChange"
    >
      <template #optional="{ record }">
        <a-space>
          <a-button status="success" type="primary" @click="doUpdate(record)">
            修改</a-button
          >
          <a-button status="danger" @click="confirmDelete(record)"
            >删除
          </a-button>
        </a-space>
      </template>
      <template #judgeConfig="{ record }">
        <a-popover>
          <template #content>
            <div v-html="getFormattedObject(record.judgeConfig)"></div>
          </template>
          <span style="cursor: pointer; color: #42a263">查看详情</span>
        </a-popover>
      </template>

      <template #judgeCase="{ record }">
        <a-popover>
          <template #content>
            <div v-html="getFormattedArray(record.judgeCase)"></div>
          </template>
          <span style="cursor: pointer; color: #42a263">查看详情</span>
        </a-popover>
      </template>
      <template #tags="{ record }">
        <a-space wrap>
          <a-tag
            v-for="(tag, index) of JSON.parse(record.tags) || ''"
            :key="index"
            color="green"
            >{{ tag }}
          </a-tag>
        </a-space>
      </template>
      <template #createTime="{ record }">
        {{
          moment(record.createTime).parseZone().format("YYYY-MM-DD hh:mm:ss")
        }}
      </template>
      <template #updateTime="{ record }">
        {{
          moment(record.createTime).parseZone().format("YYYY-MM-DD hh:mm:ss")
        }}
      </template>
      <template #answer="{ record }">
        <a-popover>
          <template #content>
            <MdViewer :value="record.answer" />
          </template>
          <span style="cursor: pointer; color: #42a263">
            {{
              record.answer?.length > 20
                ? record.answer.slice(0, 20) + "..."
                : record.answer
            }}
          </span>
        </a-popover>
      </template>
      <template #content="{ record }">
        <a-popover>
          <template #content>
            <MdViewer :value="record.content" />
          </template>
          <span style="cursor: pointer; color: #42a263">
            {{
              record.content?.length > 20
                ? record.content.slice(0, 20) + "..."
                : record.content
            }}
          </span>
        </a-popover>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watchEffect } from "vue";
import { Question, QuestionControllerService } from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import { Modal } from "@arco-design/web-vue"; // 导入 Modal 组件
import moment from "moment";
import MdViewer from "@/components/MdViewer.vue";

const tableRef = ref();
const dataList = ref([]);
const total = ref(0);
const searchParams = ref({
  current: 1,
  pageSize: 10,
});
const loadData = async () => {
  const res = await QuestionControllerService.listQuestionByPageUsingPost(
    searchParams.value
  );
  if (res.code === 0) {
    dataList.value = res.data.records;
    total.value = res.data.total;
  } else {
    message.error("加载失败", res.message);
  }
};

const getFormattedObject = (jsonData) => {
  try {
    const parsedJson =
      typeof jsonData === "string" ? JSON.parse(jsonData) : jsonData;
    return `<div style='max-width: 300px; white-space: pre-wrap; word-wrap: break-word;'>${Object.entries(
      parsedJson
    )
      .map(([key, value]) => `<div>${key}: ${value}</div>`)
      .join("")}</div>`;
  } catch (e) {
    return "<span style='color: red;'>Invalid JSON</span>";
  }
};

const getFormattedArray = (jsonData) => {
  try {
    const parsedJson =
      typeof jsonData === "string" ? JSON.parse(jsonData) : jsonData;
    if (Array.isArray(parsedJson)) {
      return `<ul style='max-width: 300px; white-space: pre-wrap; word-wrap: break-word;'>${parsedJson
        .map(
          (item) =>
            `<li> ${
              typeof item === "object"
                ? Object.entries(item)
                    .map(([key, value]) => `${key}: ${value}`)
                    .join(", ")
                : item
            }</li>`
        )
        .join("")}</ul>`;
    }
    return getFormattedObject(parsedJson); // 如果不是数组则按照对象处理
  } catch (e) {
    return "<span style='color: red;'>Invalid JSON</span>";
  }
};

/**
 * 监听 searchParams 变量，改变时触发页面的重新加载
 */
watchEffect(() => {
  loadData();
});

/**
 * 页面加载时请求数据
 */
onMounted(() => {
  loadData();
});

const columns = [
  {
    title: "标题",
    dataIndex: "title",
  },
  {
    title: "内容",
    dataIndex: "content",
    slotName: "content",
  },
  {
    title: "标签",
    slotName: "tags",
  },
  {
    title: "答案",
    dataIndex: "answer",
    slotName: "answer",
  },
  {
    title: "提交数",
    dataIndex: "submitNum",
  },
  {
    title: "通过数",
    dataIndex: "acceptedNum",
  },
  {
    title: "判题配置",
    dataIndex: "judgeConfig",
    slotName: "judgeConfig",
  },
  {
    title: "判题用例",
    dataIndex: "judgeCase",
    slotName: "judgeCase",
  },
  {
    title: "用户id",
    dataIndex: "userId",
  },
  {
    title: "创建时间",
    dataIndex: "createTime",
    slotName: "createTime",
  },
  {
    title: "更新时间",
    dataIndex: "updateTime",
    slotName: "updateTime",
  },
  {
    title: "操作",
    slotName: "optional",
  },
];
const onPageChange = (page: number) => {
  searchParams.value = {
    ...searchParams.value,
    current: page,
  };
};
// 删除操作的确认弹框
const confirmDelete = (question: Question) => {
  Modal.confirm({
    title: "确认删除",
    content: `确定删除题目: ${question.title} 吗？`,
    onOk: async () => {
      const res = await QuestionControllerService.deleteQuestionUsingPost({
        id: question.id,
      });
      if (res.code === 0) {
        message.success("删除成功");
        loadData();
      } else {
        message.error("删除失败" + res.message);
      }
    },
    onCancel: () => {
      message.info("取消删除");
    },
  });
};
const router = useRouter();
const doUpdate = (question: Question) => {
  router.push({
    path: "/update/question",
    query: {
      id: question.id,
    },
  });
};
</script>

<style scoped>
#manageQuestionView {
}

.question-title {
  color: #2c6545;
  font-size: 28px;
  border-bottom: 3px solid #2c6545;
  padding-bottom: 12px;
  margin-bottom: 32px;
}
</style>
