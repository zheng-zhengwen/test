<template>
  <div class="question-submit-list">
    <a-table
      :data="submits"
      :columns="columns"
      :pagination="{
        pageSize: 5,
      }"
    >
      <template #createTime="{ record }">
        {{
          moment(record.createTime).parseZone().format("YYYY-MM-DD HH:mm:ss")
        }}
      </template>

      <template #status="{ record }">
        <a-tag :color="getStatusInfo(record.status).color">
          {{ getStatusInfo(record.status).text }}
        </a-tag>
      </template>
      <template #language="{ record }">
        <a-tag>{{ record.language }}</a-tag>
      </template>
      <template #runtime="{ record }">
        {{ record.judgeInfo.time ? record.judgeInfo.time + "ms" : "" }}
      </template>
      <template #message="{ record }">
        <a-tag
          >{{
            record.judgeInfo.message ? record.judgeInfo.message : "提交信息为空"
          }}
        </a-tag>
      </template>
      <template #code="{ record }">
        <a-popover
          trigger="click"
          position="right"
          :popup-container="popupContainer.value"
        >
          <template #content>
            <div class="code-viewer">
              <div class="code-header">
                <span>代码详情</span>
                <a-button
                  type="text"
                  size="mini"
                  @click="copyCode(record.code)"
                >
                  <template #icon>
                    <IconCopy />
                  </template>
                  复制代码
                </a-button>
              </div>
              <a-typography-paragraph>
                <pre class="code-content"><code>{{ record.code }}</code></pre>
              </a-typography-paragraph>
            </div>
          </template>
          <a-button type="text" size="small">
            <template #icon>
              <IconEye />
            </template>
            查看代码
          </a-button>
        </a-popover>
      </template>
    </a-table>
    <template v-if="submits.length === 0">
      <a-empty description="暂无提交记录" />
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, defineProps } from "vue";
import { Message } from "@arco-design/web-vue";
import { IconEye, IconCopy } from "@arco-design/web-vue/es/icon";
import moment from "moment";
import { UserControllerService } from "../../generated";
import { eventBus, EventType } from "@/utils/eventBus";

const props = defineProps<{
  questionId: string | number;
}>();

const submits = ref<any[]>([]);
const popupContainer = ref<HTMLElement | undefined>(undefined);
let intervalId: number | null = null;
const isPolling = ref(false); // 新增：用于标记是否正在轮询

// 加载提交记录
const loadSubmits = async () => {
  try {
    let questionId = props.questionId;
    console.log("真实数据：", props);
    console.log("questionId:", questionId); // 打印 questionId 确保其值正确

    // 确保 questionId 是有效的
    if (typeof questionId === "string" && isNaN(Number(questionId))) {
      Message.error("题目 ID 无效");
      return;
    }

    // 直接使用 questionId 进行 API 请求，不进行 parseInt 转换
    const res =
      await UserControllerService.getLoginUserQuestionByQuestionIdUsingGet(
        questionId
      );
    console.log("API响应:", res); // 打印 API 响应，检查数据结构和内容

    if (res.code === 0 && res.data?.questionSubmitList) {
      console.log("数据为：", res.data);
      // 按时间降序排序
      submits.value = res.data.questionSubmitList.sort(
        (a, b) =>
          moment(b.createTime).valueOf() - moment(a.createTime).valueOf()
      );
      console.log("排序后的提交记录:", submits.value); // 打印排序后的提交记录

      // 检查是否所有提交记录都已经是最终状态
      const allFinal = submits.value.every(
        (submit) => submit.status === 2 || submit.status === 3
      );
      if (allFinal) {
        stopPolling();
      }
    } else {
      console.error("API返回错误:", res.message);
      Message.error("加载提交记录失败：" + res.message);
    }
  } catch (error) {
    if (error instanceof Error) {
      console.error("加载提交记录时发生错误:", error);
      Message.error("加载提交记录失败：" + error.message);
    } else {
      console.error("加载提交记录时发生未知错误:", error);
      Message.error("加载提交记录失败：未知错误");
    }
  }
};

// 开始轮询
const startPolling = () => {
  if (!isPolling.value) {
    isPolling.value = true;
    loadSubmits();
    intervalId = window.setInterval(loadSubmits, 5000); // 每5秒请求一次
  }
};

// 停止轮询
const stopPolling = () => {
  if (isPolling.value) {
    isPolling.value = false;
    if (intervalId) {
      window.clearInterval(intervalId);
      intervalId = null;
      console.log("所有提交记录已完成，停止轮询");
    }
  }
};

// 复制代码
const copyCode = (code: string) => {
  navigator.clipboard
    .writeText(code)
    .then(() => {
      Message.success("代码已复制到剪贴板");
    })
    .catch(() => {
      Message.error("复制失败");
    });
};

// 获取状态文本和颜色
const getStatusInfo = (status: number) => {
  const statusMap: Record<number, { text: string; color: string }> = {
    0: { text: "待判题", color: "blue" },
    1: { text: "判题中", color: "orange" },
    2: { text: "成功", color: "green" },
    3: { text: "失败", color: "red" },
  };
  return statusMap[status] || { text: "未知", color: "grey" };
};

// 添加事件监听
onMounted(() => {
  popupContainer.value = document.body;
  // 监听提交事件
  eventBus.on(EventType.REFRESH_SUBMITS, loadSubmits);
  // 监听开始轮询的消息
  eventBus.on(EventType.START_POLLING, startPolling);
  // 页面加载时加载提交记录
  loadSubmits();
});

// 清理事件监听
onUnmounted(() => {
  eventBus.off(EventType.REFRESH_SUBMITS, loadSubmits);
  eventBus.off(EventType.START_POLLING, startPolling);
  stopPolling();
});

const columns = [
  {
    title: "提交时间",
    dataIndex: "createTime",
    slotName: "createTime",
    sortable: {
      sortDirections: ["ascend", "descend"],
    },
    defaultSortOrder: "descend",
  },
  {
    title: "语言",
    dataIndex: "language",
    slotName: "language",
  },
  {
    title: "状态",
    slotName: "status",
  },
  {
    title: "用时",
    dataIndex: "runtime",
    slotName: "runtime",
  },
  {
    title: "提交信息",
    dataIndex: "message",
    slotName: "message",
  },
  {
    title: "代码",
    slotName: "code",
  },
] as any; // 暂时使用 as any 绕过类型检查，确保类型一致
</script>

<style scoped>
.question-submit-list {
  margin-top: 16px;
}

.code-viewer {
  min-width: 500px;
  max-width: 800px;
  padding: 16px;
  background: var(--color-bg-1);
  border-radius: 4px;
}

.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--color-neutral-3);
}

.code-content {
  margin: 0;
  padding: 12px;
  background: var(--color-fill-1);
  border-radius: 4px;
  font-family: monospace;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 14px;
  line-height: 1.5;
  overflow-x: auto;
}

:deep(.arco-typography) {
  margin-bottom: 0;
}
</style>
