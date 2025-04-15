package com.wen.oj.constant;

/**
 * 用户常量
 *
 * @author <a href="https://github.com/zheng-zhengwen">程序员阿文</a>
 * @from <a href="https://wen.icu">在线编程系统</a>
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";

    String DEFAULT_AVATAR = "https://q1.itc.cn/q_70/images03/20240414/d477378709494a9e8adf154fb5200feb.jpeg";
//    String DEFAULT_AVATAR="https://pic.cnblogs.com/avatar/3210868/20230531015907.png";

    String DEFAULT_INTRODUCE = "这个人很懒，什么都没有留下";

    // endregion
}
