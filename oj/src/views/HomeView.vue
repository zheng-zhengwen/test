<template>
  <div class="home-page">
    <!-- 欢迎区域 -->
    <a-card class="welcome-section">
      <div class="welcome-content">
        <div class="welcome-text">
          <h1>欢迎来到在线编程系统</h1>
          <p>在这里，你可以挑战各种编程题目，提升你的编程能力</p>
          <a-space>
            <a-button
              type="primary"
              status="success"
              size="large"
              @click="router.push('/questions')"
            >
              开始刷题
            </a-button>
          </a-space>
        </div>
        <div class="welcome-stats">
          <a-statistic title="已解决题目" :value="userStats.solvedCount" />
          <a-statistic title="提交次数" :value="userStats.submitCount" />
          <a-space size="large">
            通过率：
            <a-progress type="circle" :percent="userStats.passRate" />
          </a-space>
        </div>
      </div>
    </a-card>

    <!-- 快速开始区域 -->
    <div class="section-grid">
      <!-- 最近做题 -->
      <a-card class="section-card" title="今日热题">
        <template #extra>
          <a-link @click="router.push('/questions')">查看更多</a-link>
        </template>
        <a-list :data="recentQuestions" :bordered="false">
          <template #item="{ item }">
            <a-list-item>
              <div class="list-item">
                <a-link @click="toQuestionPage(item)">{{ item.title }}</a-link>
                <div class="info-container">
                  <a-space>
                    <div class="tag-container">
                      <a-tag v-for="tag in item.tags" :key="tag" color="green">
                        {{ tag }}
                      </a-tag>
                    </div>
                  </a-space>
                </div>
              </div>
            </a-list-item>
          </template>
        </a-list>
      </a-card>

      <!-- 最近提交 -->
      <a-card class="section-card" title="最近提交">
        <template #extra>
          <a-link @click="router.push('/homepage')">查看更多</a-link>
        </template>
        <a-list :data="recentSubmits" :bordered="false">
          <template #item="{ item }">
            <a-list-item>
              <div class="list-item">
                <a-link @click="toQuestionPage(item)">{{ item.title }}</a-link>
                <div class="info-container">
                  <a-space>
                    <div class="tag-container">
                      <a-tag :color="getStatusColor(item.status)">
                        {{ getStatusText(item.status) }}
                      </a-tag>
                    </div>
                    <div class="tag-container">
                      <a-tag>{{ item.language }}</a-tag>
                    </div>
                    <span class="submit-time">
                      {{ moment(item.createTime).fromNow() }}
                    </span>
                  </a-space>
                </div>
              </div>
            </a-list-item>
          </template>
        </a-list>
      </a-card>
    </div>

    <!-- 排行榜区域 -->
    <a-card class="leaderboard-section" title="排行榜">
      <a-table
        :data="leaderboard"
        :pagination="false"
        :bordered="false"
        :stripe="true"
      >
        <template #columns>
          <a-table-column title="排名" align="center">
            <template #cell="{ rowIndex }">
              <template v-if="rowIndex < 3">
                <icon-trophy :style="getTrophyStyle(rowIndex)" />
              </template>
              <template v-else>
                {{ rowIndex + 1 }}
              </template>
            </template>
          </a-table-column>
          <a-table-column title="用户" data-index="userName" align="left">
            <template #cell="{ record }">
              <a-space>
                <a-avatar :size="24" :image-url="record.userAvatar">
                  {{ record.userName?.[0]?.toUpperCase() }}
                </a-avatar>
                <span>{{ record.userName }}</span>
              </a-space>
            </template>
          </a-table-column>
          <a-table-column
            title="解题数"
            data-index="solvedCount"
            align="center"
          />
          <a-table-column
            title="提交数"
            data-index="submitCount"
            align="center"
          />
          <a-table-column title="通过率" align="center">
            <template #cell="{ record }">
              {{
                ((record.solvedCount / record.submitCount) * 100).toFixed(1)
              }}%
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { Message } from "@arco-design/web-vue";
import moment from "moment";
import { IconTrophy } from "@arco-design/web-vue/es/icon";
import { QuestionControllerService } from "../../generated";
import { colors } from "@arco-design/web-vue/es/color-picker/colors";

moment.locale("zh-cn"); // 设置为中文

const router = useRouter();
const percent = ref(0);
// 用户统计数据
const userStats = ref({
  solvedCount: 0,
  submitCount: 0,
  passRate: 0,
});

// 最近做题记录
const recentQuestions = ref([]);

// 最近提交记录
const recentSubmits = ref([]);

// 排行榜数据
const leaderboard = ref([]);

// 获取状态颜色
const getStatusColor = (status: number) => {
  const statusMap = {
    0: "orange",
    1: "blue",
    2: "green",
    3: "red",
  };
  return statusMap[status] || "grey";
};
// 获取状态文本
const getStatusText = (status: number) => {
  const statusMap = {
    0: "待判题",
    1: "判题中",
    2: "成功",
    3: "失败",
  };
  return statusMap[status] || "未知状态";
};

// 获取奖杯样式
const getTrophyStyle = (index: number) => {
  const colors = ["#FFD700", "#C0C0C0", "#CD7F32"];
  return {
    fontSize: "24px",
    color: colors[index],
  };
};

// 跳转到题目页面
const toQuestionPage = (question: any) => {
  if (!question?.id) return;
  router.push(`/view/question/${question.id}`);
};

// 加载数据
const loadData = async () => {
  try {
    // 加载用户统计数据
    const statsRes = await QuestionControllerService.getUserStatsUsingGet();
    if (statsRes.code === 0) {
      userStats.value = statsRes.data;
      userStats.value.passRate = (statsRes.data.passRate / 100).toFixed(2);
    }
    // 加载排行榜数据
    const leaderboardRes =
      await QuestionControllerService.getLeaderboardUsingGet();
    if (leaderboardRes.code === 0) {
      leaderboard.value = leaderboardRes.data;
    }
    // 加载最近提交记录
    const submitsRes =
      await QuestionControllerService.listQuestionMySubmitByPageUsingPost({
        pageSize: 5,
        current: 1,
        sortField: "createTime",
        sortOrder: "descend",
      });
    if (submitsRes.code === 0) {
      recentSubmits.value = submitsRes.data.records;
    }
    // 加载最近热题记录
    const questionsRes =
      await QuestionControllerService.getHotQuestionSubmitListUsingPost({
        pageSize: 5,
        current: 1,
      });
    if (questionsRes.code === 0) {
      recentQuestions.value = questionsRes.data.records;
    }
  } catch (error) {
    Message.error("数据加载失败");
  }
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
/* 全局样式 */
body {
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
  background-color: #f4f6f8;
  color: #333;
}

.home-page {
  padding: 40px;
  max-width: 1400px;
  margin: 0 auto;
}

.welcome-section {
  --color-primary-light-1: #c8e6c9;
  --color-primary-light-4: #2c6545;
  margin-bottom: 40px;
  background: linear-gradient(
    135deg,
    var(--color-primary-light-4) 0%,
    var(--color-primary-light-1) 100%
  );
  border-radius: 16px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40px;
}

.welcome-text {
  color: white;
  flex: 1;
}

.welcome-text h1 {
  font-size: 48px;
  margin-bottom: 24px;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.welcome-text p {
  font-size: 20px;
  margin-bottom: 32px;
  line-height: 1.6;
}

.welcome-stats {
  display: flex;
  gap: 40px;
}

.section-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 40px;
  margin-bottom: 40px;
}

.section-card {
  height: 100%;
  border-radius: 16px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.section-card:hover {
  transform: translateY(-8px);
}

.list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 16px 0;
}

.info-container {
  display: flex;
  align-items: center;
}

.tag-container {
  display: flex;
  align-items: center;
  height: 32px;
}

:deep(.arco-tag) {
  display: inline-flex;
  align-items: center;
  height: 28px;
  margin: 0 8px;
  border-radius: 8px;
  padding: 0 12px;
  font-size: 14px;
}

.submit-time {
  color: #777;
  font-size: 14px;
}

.leaderboard-section {
  margin-bottom: 40px;
  border-radius: 16px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .section-grid {
    grid-template-columns: 1fr;
  }

  .welcome-content {
    flex-direction: column;
    text-align: center;
    gap: 40px;
  }

  .welcome-stats {
    justify-content: center;
  }
}

:deep(.arco-list-item) {
  padding: 16px 0;
}

:deep(.arco-table) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.arco-table-thead) {
  background-color: #f0f5f9;
}

:deep(.arco-table-tbody tr:nth-child(even)) {
  background-color: #fafafa;
}
</style>
