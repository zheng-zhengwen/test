import router from "@/router";
import store from "@/store";
import ACCESS_ENUM from "@/access/accessEnum";
import checkAccess from "@/access/checkAccess";

router.beforeEach(async (to, from, next) => {
  console.log("登录用户信息", store.state.user.loginUser);

  let loginUser = store.state.user.loginUser;
  //未登录就自动登录
  if (!loginUser || !loginUser.userRole) {
    //加await是为了等用户登录成功之后，在执行后续的代码
    await store.dispatch("user/getLoginUser");
    loginUser = store.state.user.loginUser;
  }
  const needAccess = (to.meta?.access as string) ?? ACCESS_ENUM.NOT_LOGIN;
  //如果跳转页面需要登录
  if (needAccess !== ACCESS_ENUM.NOT_LOGIN) {
    //没登录跳转到登录页面
    if (
      !loginUser ||
      !loginUser.userRole ||
      loginUser.userRole === ACCESS_ENUM.NOT_LOGIN
    ) {
      next(`/user/login?redirect=${to.fullPath}`);
      return;
    }
    if (!checkAccess(loginUser, needAccess as string)) {
      next("/noAuth");
      return;
    }
  }

  next();
});
