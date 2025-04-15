<template>
  <div class="comment-item">
    <!-- 评论主体 -->
    <div class="comment-main">
      <div class="comment-user">
        <a-avatar :size="32" :image-url="comment.userVO?.userAvatar">
          {{ comment.userVO?.userName?.[0]?.toUpperCase() || "?" }}
        </a-avatar>
        <div class="user-info">
          <span class="username">{{ comment.userVO?.userName }}</span>
          <span class="time">{{ formatDate(comment.createTime) }}</span>
        </div>
      </div>

      <div class="comment-content">
        <template v-if="comment.beCommentId">
          回复 <span class="reply-to">@{{ getReplyToUserName(comment) }}</span
          >：
        </template>
        {{ comment.content }}
      </div>

      <div class="comment-actions">
        <a-space>
          <a-button class="action-btn" type="text" @click="handleLike(comment)">
            <template #icon><icon-thumb-up /></template>
            {{ comment.likeCount || "点赞" }}
          </a-button>
          <a-button
            class="action-btn"
            type="text"
            @click="showReplyInput(comment)"
          >
            <template #icon><icon-message /></template>
            回复
          </a-button>
          <a-button
            v-if="sortedChildren.length"
            class="action-btn"
            type="text"
            @click="toggleReplies"
          >
            <template #icon>
              <icon-down v-if="!isExpanded" />
              <icon-up v-else />
            </template>
            {{
              isExpanded ? "收起回复" : `展开 ${sortedChildren.length} 条回复`
            }}
          </a-button>
          <a-button
            v-if="comment.userId === currentUser?.id"
            class="action-btn"
            type="text"
            @click="handleDelete(comment.id)"
          >
            <template #icon><icon-delete /></template>
            删除
          </a-button>
        </a-space>
      </div>

      <!-- 回复输入框 -->
      <div v-if="isReplyInputVisible" class="reply-input">
        <a-input
          v-model="replyContent"
          placeholder="回复评论..."
          @keyup.enter="handleAddReply"
        >
          <template #prefix>回复 @{{ comment.userVO?.userName }}：</template>
        </a-input>
        <div class="reply-actions">
          <a-space>
            <a-button size="small" @click="hideReplyInput">取消</a-button>
            <a-button type="primary" size="small" @click="handleAddReply"
              >发布</a-button
            >
          </a-space>
        </div>
      </div>
    </div>

    <!-- 子评论列表 - 递归调用自身 -->
    <div v-if="sortedChildren.length" class="sub-comments">
      <div v-show="isExpanded">
        <comment-item
          v-for="child in sortedChildren"
          :key="child.id"
          :comment="child"
          :parent-comment="comment"
          :root-comment="rootComment || comment"
          @reply-added="handleReplyAdded"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, defineProps, defineEmits, computed, watchEffect } from "vue";
import { Message } from "@arco-design/web-vue";
import {
  IconThumbUp,
  IconMessage,
  IconDelete,
  IconDown,
  IconUp,
} from "@arco-design/web-vue/es/icon";
import type { CommentVO } from "../../generated/models/CommentVO";
import { CommentControllerService } from "../../generated/services/CommentControllerService";
import { useStore } from "vuex";

const props = defineProps<{
  comment: CommentVO;
  parentComment?: CommentVO;
  rootComment?: CommentVO;
}>();

const emit = defineEmits<{
  (e: "replyAdded"): void;
}>();

const store = useStore();
const currentUser = store.state.user?.loginUser;
const replyContent = ref("");
const isReplyInputVisible = ref(false);
const isExpanded = ref(false);

// 获取被回复用户的名字
const getReplyToUserName = (reply: CommentVO) => {
  if (!reply.beCommentId) return "";

  if (props.parentComment) {
    if (reply.beCommentId === props.parentComment.id) {
      return props.parentComment.userVO?.userName;
    }
    // 在父评论的子评论中查找
    const replyTo = props.parentComment.children?.find(
      (r) => r.id === reply.beCommentId
    );
    if (replyTo) {
      return replyTo.userVO?.userName;
    }
  }

  // 如果在根��论中查找
  if (props.rootComment && props.rootComment.children) {
    const replyTo = props.rootComment.children.find(
      (r) => r.id === reply.beCommentId
    );
    if (replyTo) {
      return replyTo.userVO?.userName;
    }
  }

  return "";
};

const showReplyInput = () => {
  isReplyInputVisible.value = true;
  replyContent.value = "";
};

const hideReplyInput = () => {
  isReplyInputVisible.value = false;
  replyContent.value = "";
};

const handleAddReply = async () => {
  if (!replyContent.value.trim()) {
    Message.warning("请输入回复内容");
    return;
  }

  try {
    const res = await CommentControllerService.addCommentUsingPost({
      questionId: props.rootComment?.questionId || props.comment.questionId,
      content: replyContent.value.trim(),
      beCommentId: props.comment.id,
      userId: currentUser.id,
    });

    if (res.code === 0) {
      Message.success("回复成功");
      hideReplyInput();
      emit("replyAdded");
    } else {
      Message.error(res.message || "回复失败");
    }
  } catch (err) {
    Message.error("回复失败");
  }
};

const handleReplyAdded = () => {
  emit("replyAdded");
};

const handleDelete = async (commentId: string) => {
  try {
    const res = await CommentControllerService.deleteCommentUsingPost1(
      commentId
    );
    if (res.code === 0) {
      Message.success("删除成功");
      emit("replyAdded");
    } else {
      Message.error(res.message || "删除失败");
    }
  } catch (err) {
    Message.error("删除失败");
  }
};

const handleLike = async (comment: CommentVO) => {
  try {
    const res = await CommentControllerService.likeCommentUsingPost(comment.id);
    if (res.code === 0) {
      Message.success("点赞成功");
      comment.likeCount = (comment.likeCount || 0) + 1;
    } else {
      Message.error(res.message || "点赞失败");
    }
  } catch (err) {
    Message.error("点赞失败");
  }
};

// 格式化日期
const formatDate = (date: string | Date) => {
  const now = new Date();
  const commentDate = new Date(date);
  const diff = now.getTime() - commentDate.getTime();

  if (diff < 60000) return "刚刚";
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
  if (diff < 2592000000) return `${Math.floor(diff / 86400000)}天前`;

  return commentDate.toLocaleDateString();
};

// 切换展开/收起状态
const toggleReplies = () => {
  isExpanded.value = !isExpanded.value;
};

// 添加一个计算属性来处理子评论
const sortedChildren = computed(() => {
  if (!props.comment.children) return [];

  // 确保 children 是数组
  const childrenArray = Array.isArray(props.comment.children)
    ? props.comment.children
    : Object.values(props.comment.children);

  // 按时间排序
  return childrenArray.sort(
    (a, b) =>
      new Date(a.createTime).getTime() - new Date(b.createTime).getTime()
  );
});

// 添加调试日志
watchEffect(() => {
  console.log("评论ID:", props.comment.id);
  console.log("原始子评论数据:", props.comment.children);
  console.log("处理后的子评论:", sortedChildren.value);
});
</script>

<style scoped>
.comment-item {
  padding: 16px 0;
  border-bottom: 1px solid #e5e9ef;
}

.sub-comments {
  margin-left: 48px;
  padding-left: 16px;
  border-left: 2px solid #f0f1f2;
  transition: all 0.3s ease-in-out;
}

.comment-user {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 8px;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #61666d;
}

.time {
  font-size: 12px;
  color: #99a2aa;
}

.comment-content {
  margin: 8px 0;
  font-size: 14px;
  line-height: 1.6;
  color: #222;
}

.reply-to {
  color: #00aeec;
  font-weight: 500;
}

.comment-actions {
  margin-top: 8px;
}

.action-btn {
  color: #99a2aa;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-btn:hover {
  color: #00aeec;
}

.reply-input {
  margin: 12px 0;
  padding: 12px;
  background: #f4f5f7;
  border-radius: 4px;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

:deep(.arco-btn-text) {
  padding: 0 8px;
}
</style>
