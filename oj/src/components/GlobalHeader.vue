<template>
  <a-row
    align="center"
    id="globalHeader"
    :wrap="false"
    :style="{ backgroundColor: headerBgColor }"
  >
    <a-col flex="auto">
      <a-menu
        mode="horizontal"
        :selected-keys="selectedKeys"
        @menu-item-click="doMenuClick"
        :style="{ backgroundColor: headerBgColor }"
        :default-selected-keys="['1']"
      >
        <a-menu-item
          key="0"
          :style="{ padding: 0, marginRight: '20px' }"
          disabled
        >
          <div class="title-bar">
            <div class="title">在线编程系统</div>
            <img src="@/assets/index_ico_03.png" alt="" class="logo" />
          </div>
        </a-menu-item>
        <a-menu-item
          v-for="item in visibleRoutes"
          :key="item.path"
          :style="{
            backgroundColor: selectedKeys.includes(item.path)
              ? '#ffffff'
              : headerBgColor,
            color: selectedKeys.includes(item.path) ? headerBgColor : 'white',
          }"
          @click="doMenuClick(item.path)"
        >
          {{ item.name }}
        </a-menu-item>
      </a-menu>
    </a-col>
    <a-col flex="auto" class="user-info-wrapper">
      <div class="user-info">
        <a-dropdown trigger="hover">
          <a-avatar shape="circle" :image-url="userAvatar">
            <IconUser />
          </a-avatar>
          <template #content>
            <template
              v-if="loginUser && loginUser.userRole as string !== ACCESS_ENUM.NOT_LOGIN"
            >
              <a-doption>
                <template #icon>
                  <icon-idcard />
                </template>
                <template #default>
                  <a-anchor-link @click="handleClick">个人信息</a-anchor-link>
                </template>
              </a-doption>
              <a-doption>
                <template #icon>
                  <icon-poweroff />
                </template>
                <template #default>
                  <a-anchor-link @click="logout">退出登录</a-anchor-link>
                </template>
              </a-doption>
            </template>
            <template v-else>
              <a-doption>
                <template #icon>
                  <icon-user />
                </template>
                <template #default>
                  <a-anchor-link href="/user/login">登录</a-anchor-link>
                </template>
              </a-doption>
            </template>
          </template>
        </a-dropdown>
        <div class="user-name">
          {{ store.state.user?.loginUser?.userName ?? "未登录" }}
        </div>
      </div>
    </a-col>
  </a-row>

  <!-- 个人信息对话框 -->
  <a-modal v-model:visible="visible" @ok="handleOk" :footer="false">
    <template #title>用户简介</template>
    <div>
      <a-descriptions
        size="mini"
        layout="inline-vertical"
        :data="data"
        bordered
      />
    </div>
    <a-button
      @click="openModalForm(loginUser.id)"
      style="margin-top: 10px"
      :visible="visible2"
    >
      修改个人信息
    </a-button>
  </a-modal>

  <!-- 修改个人信息对话框 -->
  <a-modal
    width="30%"
    :visible="visible2"
    placement="right"
    @ok="handleOk2"
    @cancel="closeModel"
    unmountOnClose
  >
    <div style="text-align: center; margin-bottom: 20px">
      <a-upload
        action="/"
        :show-file-list="false"
        :custom-request="uploadAvatar"
        accept="image/*"
      >
        <template #upload-button>
          <a-avatar shape="circle" :image-url="userInfo?.userAvatar">
            <icon-user />
          </a-avatar>
        </template>
      </a-upload>
      <div style="margin-top: 10px; color: var(--color-text-2)">
        点击头像更换
      </div>
    </div>
    <a-form title="个人信息" style="max-width: 360px; margin: 0 auto">
      <a-form-item field="名称" label="名称 :">
        <a-input v-model="userInfo.userName" placeholder="请输入用户名称" />
      </a-form-item>
      <a-form-item field="账号" label="账号 :" disabled>
        <a-input v-model="userInfo.userAccount" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item field="用户角色" label="角色 :" disabled>
        <a-select v-model="userInfo.userRole" placeholder="请输入用户角色">
          <a-option value="admin">管理员</a-option>
          <a-option value="user">普通用户</a-option>
        </a-select>
      </a-form-item>
      <a-form-item field="userProfile" label="简介 :">
        <a-textarea v-model="userInfo.userProfile" placeholder="请输入简介" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script lang="ts" setup>
import { routes } from "../router/routes";
import { useRouter } from "vue-router";
import { computed, ref, onMounted, onBeforeUnmount } from "vue";
import { useStore } from "vuex";
import checkAccess from "@/access/checkAccess";
import ACCESS_ENUM from "@/access/accessEnum";
import {
  User,
  UserControllerService,
  FileControllerService,
} from "../../generated";
import message from "@arco-design/web-vue/es/message";
import moment from "moment";

const router = useRouter();
const store = useStore();

// 展示在菜单的路由数组
const visibleRoutes = computed(() => {
  return routes.filter((item, index) => {
    if (item.meta?.hideInMenu) {
      return false;
    }
    if (!checkAccess(store.state.user.loginUser, item.meta?.access as string)) {
      return false;
    }
    return true;
  });
});

// 退出登录
const logout = async () => {
  try {
    await UserControllerService.userLogoutUsingPost();
    store.commit("user/setLoginUser", {
      userName: "未登录",
      userRole: ACCESS_ENUM.NOT_LOGIN,
      userAvatar: "",
    });
    localStorage.removeItem("token");
    sessionStorage.clear();
    router.push("/user/login").then(() => {
      location.reload();
    });
  } catch (error) {
    message.error("退出登录失败");
  }
};

// 默认主页
// const selectedKeys = ref(["/"]);
const selectedKeys = ref([router.currentRoute.value.path]);

// 窗口响应式处理
const windowWidth = ref(window.innerWidth);
const collapseThreshold = 1000;
const isCollapsed = ref(false);
const loginUser = computed(() => store.state.user.loginUser);
const userAvatar = computed(() => loginUser.value.userAvatar);

const handleResize = () => {
  windowWidth.value = window.innerWidth;
  isCollapsed.value = windowWidth.value < collapseThreshold;
};

onMounted(() => {
  window.addEventListener("resize", handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", handleResize);
});

router.afterEach((to) => {
  if (!isCollapsed.value) {
    selectedKeys.value = [to.path];
  }
  //强制更新
  const newLoginUser = store.state.user.loginUser;
  loginUser.value = newLoginUser;
  userAvatar.value = newLoginUser.userAvatar;
});

const doMenuClick = (key: string) => {
  router.push({
    path: key,
  });
  selectedKeys.value = [key];
};

// 定义背景色变量
const headerBgColor = "#2c6545";

// 个人信息弹窗相关
const visible = ref(false);
const handleClick = () => {
  visible.value = true;
};
const handleOk = () => {
  visible.value = false;
};

const data = computed(() => {
  return [
    {
      label: "用户名称",
      value: loginUser.value.userName,
    },
    {
      label: "用户id",
      value: loginUser.value.id,
    },
    {
      label: "简介",
      value: loginUser.value.userProfile,
    },
    {
      label: "注册时间",
      value: moment(loginUser.value.createTime).format("YYYY-MM-DD hh:mm"),
    },
  ];
});

// 修改个人信息弹窗
const visible2 = ref(false);
const userInfo = ref<User>({
  userName: "",
  userAccount: "",
  userRole: "",
  userProfile: "",
  userAvatar: "",
});
const userAvatarImg = ref("");

const openModalForm = async (userId: any) => {
  const res = await UserControllerService.getUserByIdUsingGet(userId);
  userInfo.value = {
    ...res.data,
    userPassword: "", // 清空密码字段
  };
  userAvatarImg.value = res.data?.userAvatar || "";
  visible2.value = true;
};

/**
 * 上传头像
 */
const uploadAvatar = async (options: any) => {
  const file = options.fileItem.file;
  const userId = loginUser.value?.id;

  if (!userId) {
    message.error("用户未登录");
    return;
  }

  try {
    if (!file) {
      message.error("请选择要上传的文件");
      return;
    }

    if (!file.type.startsWith("image/")) {
      message.error("请上传图片文件");
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      message.error("图片大小不能超过5MB");
      return;
    }

    const res = await UserControllerService.uploadAvatarUsingPost(file, userId);

    if (res.code === 0 && res.data) {
      userAvatarImg.value = res.data;
      if (userInfo.value) {
        userInfo.value.userAvatar = res.data;
      }
      message.success("头像上传成功");

      // 🔄 强制刷新登录用户信息
      await store.dispatch("user/getLoginUser");
    } else {
      message.error("头像上传失败：" + (res.message || "未知错误"));
    }
  } catch (error: any) {
    message.error("头像上传失败：" + (error.message || "未知错误"));
  }
};

/**
 * 确定修改按钮
 */
const handleOk2 = async () => {
  try {
    // 创建更新对象，排除空密码
    const updateData = {
      userName: userInfo.value.userName,
      userAvatar: userAvatarImg.value,
      userProfile: userInfo.value.userProfile,
    };

    // 调用后端更新个人信息接口
    const res = await UserControllerService.updateMyUserUsingPost(updateData);

    if (res.code === 0) {
      message.success("更新成功！");
      // 更新全局用户信息
      await store.dispatch("user/getLoginUser");
      visible2.value = false;
    } else {
      message.error("更新失败！" + (res.message || "未知错误"));
    }
  } catch (error: any) {
    message.error("更新失败：" + (error.message || "未知错误"));
  }
};

const closeModel = () => {
  visible2.value = false;
};
</script>

<style scoped>
.title-bar {
  display: flex;
  flex-direction: column;
  background-color: #2c6545;
}

.title {
  font-size: 20px;
  margin-left: 100px;
  font-weight: bold;
  /**用多个颜色拼接长链条*/
  background: linear-gradient(
    270deg,
    #ffe4e1,
    #fffaf0,
    #ffffe0,
    #f0fff0,
    #e0ffff,
    #f0f8ff,
    #ffe4e1
  );
  background-size: 1400% 1400%; /**让渐变图放大（形成流动空间*/
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: rainbowFlow 8s ease infinite;
}

@keyframes rainbowFlow {
  /**动画位移渐变背景*/
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

.logo {
  height: 48px;
}

.user-info-wrapper {
  display: flex;
  justify-content: flex-end;
}

.user-info {
  display: flex;
  align-items: center;
  padding: 4px 17px;
}

.user-name {
  font-size: 14px;
  color: #fff;
  margin-left: 5px;
}
</style>
