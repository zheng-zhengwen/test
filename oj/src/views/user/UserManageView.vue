<template>
  <div id="userManageView">
    <div class="header-section">
      <h2>用户管理</h2>
      <a-button type="primary" @click="openUserModal()">
        <template #icon>
          <icon-plus />
        </template>
        新增用户
      </a-button>
    </div>
    <a-form :model="searchParams" layout="inline">
      <a-form-item field="userName" label="用户名" style="min-width: 240px">
        <a-input v-model="searchParams.userName" placeholder="请输入用户名" />
      </a-form-item>
      <a-form-item field="userAccount" label="账号" style="min-width: 240px">
        <a-input v-model="searchParams.userAccount" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item field="userRole" label="角色" style="min-width: 240px">
        <a-input v-model="searchParams.userRole" placeholder="请输入角色" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="doSubmit">提交</a-button>
      </a-form-item>
    </a-form>
    <a-divider size="0" />
    <a-card class="table-card">
      <a-table
        v-if="dataList && dataList.length > 0"
        :ref="tableRef"
        :columns="columns"
        :data="dataList"
        :pagination="{
          showTotal: false,
          pageSize: searchParams.pageSize,
          current: searchParams.current,
          total,
        }"
        @page-change="onPageChange"
      >
        <template #avatar="{ record }">
          <a-avatar :size="32" :image-url="record.userAvatar">
            <template #trigger-icon>
              <icon-user />
            </template>
          </a-avatar>
        </template>
        <template #userRole="{ record }">
          <a-tag :color="getRoleTagColor(record.userRole)">
            {{ record.userRole }}
          </a-tag>
        </template>
        <template #createTime="{ record }">
          {{ formatTime(record.createTime) }}
        </template>
        <template #updateTime="{ record }">
          {{ formatTime(record.updateTime) }}
        </template>
        <template #optional="{ record }">
          <a-space>
            <a-button type="text" size="small" @click="openUserModal(record)">
              <icon-edit />
              修改
            </a-button>
            <a-button
              type="text"
              status="danger"
              size="small"
              @click="confirmDelete(record)"
            >
              <icon-delete />
              删除
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 用户信息弹窗 -->
    <a-modal
      v-model:visible="userModalVisible"
      :title="currentUser.id ? '修改用户' : '新增用户'"
      @cancel="closeUserModal"
      :mask-closable="false"
      ok-text="确认"
      cancel-text="取消"
    >
      <a-form :model="currentUser" ref="userFormRef" :style="{ width: '100%' }">
        <a-form-item
          field="userAccount"
          label="账号"
          v-if="!currentUser.id"
          :rules="[
            { required: true, message: '请输入账号' },
            { minLength: 4, message: '账号长度不能小于 4' },
          ]"
        >
          <a-input v-model="currentUser.userAccount" placeholder="请输入账号" />
        </a-form-item>
        <a-form-item
          field="userName"
          label="昵称"
          :rules="[{ required: true, message: '请输入昵称' }]"
        >
          <a-input v-model="currentUser.userName" placeholder="请输入昵称" />
        </a-form-item>
        <a-form-item
          field="userPassword"
          label="密码"
          :rules="[
            { required: !currentUser.id, message: '请输入密码' },
            { minLength: 8, message: '密码长度不能小于 8' },
          ]"
        >
          <a-input-password
            v-model="currentUser.userPassword"
            placeholder="请输入密码"
            allow-clear
          />
        </a-form-item>
        <a-form-item
          v-if="!currentUser.id"
          field="checkPassword"
          label="确认密码"
          :rules="[
            { required: true, message: '请确认密码' },
            {
              validator: (value) => value === currentUser.value?.userPassword,
              message: '两次输入的密码不一致',
            },
          ]"
        >
          <a-input-password
            v-model="currentUser.checkPassword"
            placeholder="请再次输入密码"
            allow-clear
          />
        </a-form-item>
        <a-form-item
          field="userRole"
          label="角色"
          :rules="[{ required: true, message: '请选择角色' }]"
        >
          <a-select v-model="currentUser.userRole">
            <a-option value="user">普通用户</a-option>
            <a-option value="admin">管理员</a-option>
          </a-select>
        </a-form-item>
        <a-form-item field="userProfile" label="个人简介">
          <a-textarea
            v-model="currentUser.userProfile"
            placeholder="请输入个人简介"
          />
        </a-form-item>
      </a-form>

      <!--  自定义弹窗底部按钮 -->
      <template #footer>
        <a-button @click="closeUserModal">取消</a-button>
        <a-button type="primary" @click="handleUserSubmit">确认</a-button>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch, watchEffect } from "vue";
import {
  UserAddRequest,
  UserControllerService,
  User,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { Modal } from "@arco-design/web-vue";
import moment from "moment";
import {
  IconEdit,
  IconDelete,
  IconPlus,
  IconUser,
} from "@arco-design/web-vue/es/icon";

const tableRef = ref();
const dataList = ref([]);
const total = ref(0);
const searchParams = ref({
  current: 1,
  pageSize: 2,
  userAccount: "",
  userName: "",
  userRole: "",
});

// 用户表单相关
const userModalVisible = ref(false);
const userFormRef = ref();
const currentUser = ref({} as Partial<UserAddRequest>);

const columns = [
  {
    title: "头像",
    dataIndex: "userAvatar",
    slotName: "avatar",
    width: 70,
  },
  {
    title: "昵称",
    dataIndex: "userName",
    width: 100,
  },
  {
    title: "账号",
    dataIndex: "userAccount",
    width: 120,
  },
  {
    title: "用户角色",
    dataIndex: "userRole",
    slotName: "userRole",
    width: 100,
  },
  {
    title: "个人简介",
    dataIndex: "userProfile",
    ellipsis: true,
    tooltip: true,
    width: 150,
  },
  {
    title: "创建时间",
    dataIndex: "createTime",
    slotName: "createTime",
    width: 100,
  },
  {
    title: "更新时间",
    dataIndex: "updateTime",
    slotName: "updateTime",
    width: 100,
  },
  {
    title: "操作",
    slotName: "optional",
    width: 100,
    fixed: "right",
  },
];

// 打开用户弹窗
const openUserModal = (user?: User) => {
  if (user) {
    currentUser.value = { ...user };
    currentUser.value.userPassword = ""; // 清空密码字段
  } else {
    currentUser.value = {};
  }
  userModalVisible.value = true;
};

// 关闭用户弹窗
const closeUserModal = () => {
  userModalVisible.value = false;
  currentUser.value = {};
  userFormRef.value?.resetFields();
};

// 提交用户表单
const handleUserSubmit = async () => {
  if (!userFormRef.value) {
    return;
  }

  try {
    // 等待表单验证完成
    const errors = await userFormRef.value.validate();
    console.log("表单验证结果：", errors);

    // 如果有错误，停止提交
    if (errors) {
      return;
    }

    // 验证通过后的处理
    const submitData = {
      userAccount: currentUser.value.userAccount,
      userName: currentUser.value.userName,
      userPassword: currentUser.value.userPassword,
      checkPassword: currentUser.value.checkPassword,
      userRole: currentUser.value.userRole,
      userProfile: currentUser.value.userProfile,
    };

    let res;
    if (currentUser.value.id) {
      submitData.id = currentUser.value.id;
      if (!submitData.userPassword) {
        delete submitData.userPassword;
      }
      res = await UserControllerService.updateUserUsingPost(submitData);
    } else {
      // 新增用户时验证确认密码
      if (submitData.userPassword !== currentUser.value.checkPassword) {
        message.error("两次输入的密码不一致");
        return;
      }
      res = await UserControllerService.addUserUsingPost(submitData);
    }

    if (res.code === 0) {
      message.success(currentUser.value.id ? "修改成功" : "添加成功");
      closeUserModal();
      loadData();
    } else {
      message.error(res.message || "操作失败");
    }
  } catch (error) {
    console.error("表单提交错误：", error);
    message.error("操作失败：" + (error.message || "未知错误"));
  }
};

// 加载数据
const loadData = async () => {
  const res = await UserControllerService.listUserByPageUsingPost(
    searchParams.value,
    console.log("请求参数：", searchParams.value)
  );
  if (res.code === 0) {
    dataList.value = res.data.records;
    total.value = res.data.total;
  } else {
    message.error("加载失败", res.message);
  }
};

/**
 * 监听 searchParams 变量，改变时触发页面的重新加载
 */
watch(
  searchParams,
  () => {
    loadData();
  },
  { deep: true }
);

/**
 * 页面加载时请求数据
 */
onMounted(() => {
  loadData();
});

const onPageChange = (page: number) => {
  searchParams.value = {
    ...searchParams.value,
    current: page,
  };
};
// 删除操作的确认弹框
const confirmDelete = (user: User) => {
  Modal.confirm({
    title: "确认删除",
    content: `确定删除用户-> ${user.userName} 吗？`,
    onOk: async () => {
      const res = await UserControllerService.deleteUserUsingPost({
        id: user.id,
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
// 获取用户角色对应的标签颜色
const getRoleTagColor = (role: string) => {
  const roleColorMap = {
    admin: "red",
    user: "blue",
    // 可以添加更多角色的颜色
  };
  return roleColorMap[role.toLowerCase()] || "gray";
};
const doSubmit = () => {
  // 这里需要重置搜索页号
  searchParams.value = {
    ...searchParams.value,
    current: 1,
  };
};

// 设置 moment 使用中国时区
moment.locale("zh-cn");

// 时间格式化函数
const formatTime = (time: string) => {
  // 使用 parseZone() 保持原始时区
  const formattedTime = moment(time).parseZone().format("YYYY-MM-DD HH:mm:ss");
  return formattedTime;
};
</script>

<style scoped>
#userManageView {
  padding: 20px;
  background-color: #f2f3f5;
  min-height: calc(100vh - 60px);
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-section h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 500;
  color: #1d2129;
}

.table-card {
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

:deep(.arco-table-th) {
  background-color: #f7f8fa !important;
  font-weight: 600;
}

:deep(.arco-table-tr:hover) {
  background-color: #f2f3f5;
}

:deep(.arco-table-cell) {
  padding: 12px 16px !important;
}

:deep(.arco-btn-text) {
  padding: 0 4px;
}

:deep(.arco-space-horizontal .arco-space-item) {
  margin-right: 8px !important;
}

:deep(.arco-form-item-label-col) {
  font-weight: 500;
}

:deep(.arco-input-wrapper) {
  width: 100%;
}

:deep(.arco-select-view) {
  width: 100%;
}

:deep(.arco-form) {
  margin-bottom: 16px;
}

:deep(.arco-form-item-layout-inline) {
  margin-right: 16px;
}

:deep(.arco-divider) {
  margin: 16px 0;
}
</style>
