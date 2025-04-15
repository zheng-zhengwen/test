<template>
  <div id="questionSubmitView">
    <a-card>
      <template #title>提交记录</template>
      <!-- 搜索表单 -->
      <a-form :model="searchParams" layout="inline" class="search-form">
        <a-form-item field="title" label="题目名称">
          <a-input
            v-model="searchParams.title"
            placeholder="请输入题目名称"
            hide-button
            style="width: 160px"
          />
        </a-form-item>
        <a-form-item field="language" label="编程语言">
          <a-select
            v-model="searchParams.language"
            placeholder="选择编程语言"
            style="width: 160px"
            allow-clear
          >
            <a-option value="java">Java</a-option>
            <a-option value="cpp">C++</a-option>
            <a-option value="go">Go</a-option>
            <a-option value="python">Python</a-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="doSubmit" status="success">
              <template #icon>
                <icon-search />
              </template>
              搜索
            </a-button>
            <a-button @click="resetSearch">
              <template #icon>
                <icon-refresh />
              </template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <!-- 提交记录表格 -->
      <a-table
        :data="dataList"
        :pagination="searchParams"
        @page-change="onPageChange"
        :bordered="false"
        :stripe="true"
        :hoverable="true"
      >
        <template #columns>
          <a-table-column title="题目" align="left">
            <template #cell="{ record }">
              <a-link @click="toQuestionPage(record.questionVO)">
                {{ record.questionVO?.title }}
              </a-link>
            </template>
          </a-table-column>
          <a-table-column title="标签" align="left">
            <template #cell="{ record }">
              <a-space>
                <a-tag
                  v-for="tag in record.questionVO?.tags"
                  :key="tag"
                  color="green"
                  >{{ tag }}
                </a-tag>
              </a-space>
            </template>
          </a-table-column>
          <a-table-column title="语言" align="center">
            <template #cell="{ record }">
              <a-tag>{{ record.language }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="提交代码" align="center">
            <template #cell="{ record }">
              <a-popover
                trigger="click"
                position="right"
                :popup-container="popupContainer"
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
                          <icon-copy />
                        </template>
                        复制代码
                      </a-button>
                    </div>
                    <a-typography-paragraph>
                      <pre
                        class="code-content"
                      ><code>{{ record.code }}</code></pre>
                    </a-typography-paragraph>
                  </div>
                </template>
                <a-button type="text" size="small">
                  <template #icon>
                    <icon-eye />
                  </template>
                  查看代码
                </a-button>
              </a-popover>
            </template>
          </a-table-column>
          <a-table-column title="状态" align="center">
            <template #cell="{ record }">
              <a-tag :color="getStatusColor(record.status)">
                {{ getStatusText(record.status) }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="用时" align="center">
            <template #cell="{ record }">
              <a-tag> {{ record.judgeInfo.time }}ms</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="提交者" align="center">
            <template #cell="{ record }">
              <a-tag>
                {{ record.userVO?.userName }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column
            title="提交时间"
            align="center"
            :sortable="{ sortDirections: ['ascend', 'descend'] }"
          >
            <template #cell="{ record }">
              {{
                moment(record.createTime)
                  .parseZone()
                  .format("YYYY-MM-DD HH:mm:ss")
              }}
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { Message } from "@arco-design/web-vue";
import { useRouter } from "vue-router";
import moment from "moment";
import {
  IconSearch,
  IconRefresh,
  IconEye,
  IconCopy,
} from "@arco-design/web-vue/es/icon";
import { QuestionControllerService, QuestionVO } from "../../../generated";

const loading = ref(false);
const popupContainer = ref<HTMLElement | undefined>(undefined);

const dataList = ref([]);
const searchParams = ref({
  title: undefined,
  language: undefined,
  pageSize: 10,
  current: 1,
  total: 0, // 新增 total 属性
});

// 获取状态颜色
const getStatusColor = (status: number): string => {
  const statusMap: { [key: number]: string } = {
    0: "orange", // 待判题
    1: "blue", // 判题中
    2: "green", // 成功
    3: "red", // 失败
  };
  return statusMap[status] || "grey";
};

// 获取状态文本
const getStatusText = (status: number): string => {
  const statusMap: { [key: number]: string } = {
    0: "待判题",
    1: "判题中",
    2: "成功",
    3: "失败",
  };
  return statusMap[status] || "未知状态";
};

// 复制代码
const copyCode = async (code: string) => {
  try {
    await navigator.clipboard.writeText(code);
    Message.success("代码已复制到剪贴板");
  } catch (err) {
    Message.error("复制失败");
  }
};

const loadData = async () => {
  loading.value = true;
  try {
    const res =
      await QuestionControllerService.listQuestionSubmitByPageUsingPost({
        ...searchParams.value,
        sortField: "createTime",
        sortOrder: "descend",
      });
    console.log("请求题目数据：", res.data);
    if (res.code === 0) {
      dataList.value = res.data.records;
      searchParams.value.total = res.data.total; // 更新 total 属性
    } else {
      Message.error("加载失败：" + res.message);
    }
  } catch (error) {
    if (error instanceof Error) {
      Message.error("加载失败：" + error.message);
    } else {
      Message.error("加载失败：未知错误");
    }
  } finally {
    loading.value = false;
  }
};

const onPageChange = (page: number) => {
  searchParams.value = {
    ...searchParams.value,
    current: page,
  };
  console.log("page的值==", page);
  loadData(); // 调用 loadData 方法重新加载数据
};

const router = useRouter();

const toQuestionPage = (question: QuestionVO) => {
  if (!question?.id) return;
  router.push({
    path: `/view/question/${question.id}`,
  });
};

const doSubmit = () => {
  searchParams.value.current = 1;
  loadData();
};

const resetSearch = () => {
  searchParams.value = {
    title: undefined,
    language: undefined,
    pageSize: 10,
    current: 1,
    total: 0,
  };
  loadData();
};

onMounted(() => {
  loadData();
  popupContainer.value = document.body;
});
</script>

<style scoped>
#questionSubmitView {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.search-form {
  margin-bottom: 16px;
}

.code-viewer {
  min-width: 400px;
  max-width: 800px;
}

.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding: 8px;
  background: #f5f6f7;
  border-radius: 4px;
}

.code-content {
  margin: 0;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 4px;
  max-height: 400px;
  overflow: auto;
}

:deep(.arco-table-th) {
  background-color: var(--color-fill-2) !important;
}

:deep(.arco-table-tr:hover) {
  background-color: var(--color-fill-2) !important;
}

:deep(.arco-tag) {
  margin: 0 4px;
}
</style>
