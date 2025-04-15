package com.wen.oj.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户更新请求
 *
 * @author <a href="https://github.com/zheng-zhengwen">程序员阿文</a>
 * @from <a href="https://wen.icu">在线编程系统</a>
 */
@Data
public class UserUpdateRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;
    /**
     * 用户密码
     */
    private String userPassword;

    private static final long serialVersionUID = 1L;
}