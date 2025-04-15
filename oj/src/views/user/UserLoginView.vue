<template>
  <div id="userLoginView">
    <div class="login-container">
      <h2 class="login-title">用户登录</h2>
      <a-form
        :model="form"
        label-align="left"
        auto-label-width
        @submit="handleSubmit"
        class="login-form"
      >
        <a-form-item field="userAccount" label="账号">
          <a-input v-model="form.userAccount" placeholder="请输入账号" />
        </a-form-item>
        <a-form-item
          field="userPassword"
          tooltip="密码不少于 8 位"
          label="密码"
        >
          <a-input-password
            v-model="form.userPassword"
            placeholder="请输入密码"
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" class="submit-btn">
            登录
          </a-button>
          <div class="register-link">
            <router-link to="/user/register">没有账号？点击注册</router-link>
          </div>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from "vue";
import { UserControllerService, UserLoginRequest } from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import { useStore } from "vuex";

const form = reactive({
  userAccount: "",
  userPassword: "",
} as UserLoginRequest);

const router = useRouter();
const store = useStore();

const handleSubmit = async () => {
  const res = await UserControllerService.userLoginUsingPost(form);
  if (res.code === 0) {
    message.success("登录成功");
    await store.dispatch("user/getLoginUser");
    router.push({
      path: "/",
      replace: true,
    });
  } else {
    message.error("登录失败，" + res.message);
  }
};
</script>

<style scoped>
#userLoginView {
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-container {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(12px);
  border-radius: 12px;
  padding: 24px 32px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 480px;
}

.login-title {
  font-size: 24px;
  color: #2c6545;
  margin-bottom: 24px;
  text-align: center;
  font-weight: bold;
}

.login-form {
  width: 100%;
}

.submit-btn {
  width: 50% !important;
  margin-right: 8px;
  background-color: #2c6545 !important;
  border-color: #2c6545 !important;
}

.submit-btn:hover {
  transform: scale(1.05);
}

.register-link {
  margin-top: 16px;
  text-align: center;
}

.register-link a {
  color: #2c6545;
  transition: color 0.3s;
}

.register-link a:hover {
  color: #116466;
}
</style>
