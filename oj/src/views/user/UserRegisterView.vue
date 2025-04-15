<template>
  <div id="userRegisterView">
    <div class="register-container">
      <h2 class="register-title">用户注册</h2>
      <a-form
        :model="form"
        label-align="left"
        auto-label-width
        @submit="handleSubmit"
        class="register-form"
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
        <a-form-item
          field="checkPassword"
          tooltip="密码不少于 8 位"
          label="确认密码"
        >
          <a-input-password
            v-model="form.checkPassword"
            placeholder="请再次输入密码"
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" class="submit-btn">
            注册
          </a-button>
          <div class="login-link">
            <router-link to="/user/login">已有账号？点击登录</router-link>
          </div>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from "vue";
import { UserControllerService, UserRegisterRequest } from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import { useStore } from "vuex";

const form = reactive({
  userAccount: "",
  userPassword: "",
  checkPassword: "",
} as UserRegisterRequest);

const router = useRouter();
const store = useStore();

const handleSubmit = async () => {
  const res = await UserControllerService.userRegisterUsingPost(form);
  if (res.code === 0) {
    message.success("注册成功");
    router.push({
      path: "/user/login",
      replace: true,
    });
  } else {
    message.error("注册失败，" + res.message);
  }
};
</script>

<style scoped>
#userRegisterView {
  display: flex;
  justify-content: center;
  align-items: center;
}

.register-container {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(12px);
  border-radius: 12px;
  padding: 24px 32px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 480px;
}

.register-title {
  font-size: 24px;
  color: #2c6545;
  margin-bottom: 24px;
  text-align: center;
  font-weight: bold;
}

.register-form {
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

.login-link {
  margin-top: 16px;
  text-align: center;
}

.login-link a {
  color: #2c6545;
  transition: color 0.3s;
}

.login-link a:hover {
  color: #116466;
}
</style>
