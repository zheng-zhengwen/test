package com.wen.oj.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qcloud.cos.model.PutObjectResult;
import com.wen.oj.model.dto.questionsubmit.QuestionSubmitQueryDTO;
import com.wen.oj.annotation.AuthCheck;
import com.wen.oj.common.BaseResponse;
import com.wen.oj.common.DeleteRequest;
import com.wen.oj.common.ErrorCode;
import com.wen.oj.common.ResultUtils;
import com.wen.oj.config.CosClientConfig;
import com.wen.oj.config.MinioConfiguration;
import com.wen.oj.config.WxOpenConfig;
import com.wen.oj.constant.UserConstant;
import com.wen.oj.exception.BusinessException;
import com.wen.oj.exception.ThrowUtils;
import com.wen.oj.manager.CosManager;
import com.wen.oj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.wen.oj.model.dto.user.UserAddRequest;
import com.wen.oj.model.dto.user.UserLoginRequest;
import com.wen.oj.model.dto.user.UserQueryRequest;
import com.wen.oj.model.dto.user.UserRegisterRequest;
import com.wen.oj.model.dto.user.UserUpdateMyRequest;
import com.wen.oj.model.dto.user.UserUpdateRequest;
import com.wen.oj.model.entity.Question;
import com.wen.oj.model.entity.QuestionSubmit;
import com.wen.oj.model.entity.User;
import com.wen.oj.model.vo.*;
import com.wen.oj.service.QuestionService;
import com.wen.oj.service.QuestionSubmitService;
import com.wen.oj.service.UserService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.mp.api.WxMpService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户接口
 *
 * @author <a href="https://github.com/zheng-zhengwen">程序员阿文</a>
 * @from <a href="https://wen.icu">在线编程系统</a>
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private WxOpenConfig wxOpenConfig;

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private MinioClient minioClient;
    @Resource
    private MinioConfiguration minioConfiguration;

    @Resource
    private CosManager cosManager;

    @Resource
    private CosClientConfig cosClientConfig;

    /**
     *腾讯云 COS 客户端(上传头像)
     * */
    @PostMapping("/upload/avatar")
    public BaseResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file, @RequestParam("userId") Long userId) {
        try {
            // 校验文件类型
            String contentType = file.getContentType();
            if (contentType != null && !contentType.startsWith("image/")) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "只能上传图片文件");
            }

            // 校验文件大小（例如最大 2MB）
            long maxSize = 2 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 2MB");
            }

            // 生成文件名：userId_timestamp.extension
            String extension = getFileExtension(file.getOriginalFilename());
            String fileName = String.format("avatar/%s_%s%s", userId, System.currentTimeMillis(), extension);

            // 创建临时文件
            File tempFile = File.createTempFile("temp-avatar", extension);
            Files.copy(file.getInputStream(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // 上传到COS
            PutObjectResult result = cosManager.putObject(fileName, tempFile);

            // 删除临时文件
            tempFile.delete();

            // 获取文件访问URL
            String avatarUrl = "https://" + cosClientConfig.getBucket() + ".cos." + cosClientConfig.getRegion() + ".myqcloud.com/" + fileName;

            // 更新用户头像URL
            User user = userService.getById(userId);
            if (user != null) {
                user.setUserAvatar(avatarUrl);
                userService.updateById(user);
            }
            return ResultUtils.success(avatarUrl);
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件上传失败");
        }
    }

    private String getFileExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf(".")))
                .orElse("");
    }

//    //头像文件
//    @PostMapping("/upload/avatar")
//    public BaseResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file, @RequestParam("userId") Long userId) {
//        try {
//            // 校验文件类型
//            String contentType = file.getContentType();
//            if (contentType != null && !contentType.startsWith("image/")) {
//                throw new BusinessException(ErrorCode.PARAMS_ERROR, "只能上传图片文件");
//            }
//
//            // 校验文件大小（例如最大 2MB）
//            long maxSize = 2 * 1024 * 1024;
//            if (file.getSize() > maxSize) {
//                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 2MB");
//            }
//
//            // 生成文件名：userId_timestamp.extension
//            String extension = getFileExtension(file.getOriginalFilename());
//            String fileName = String.format("avatar/%s_%s%s", userId, System.currentTimeMillis(), ".jpg");
//
//            // 上传到 MinIO
//            minioClient.putObject(
//                    PutObjectArgs.builder()
//                            .bucket(minioConfiguration.getBucket())  // MinIO bucket 名称
//                            .object(fileName)       // 文件名
//                            .stream(file.getInputStream(), file.getSize(), -1)
//                            .contentType(contentType)
//                            .build()
//            );
//            // 获取文件访问URL
//            String avatarUrl = String.format("%s/%s/%s",minioConfiguration.getEndpoint(),minioConfiguration.getBucket(),fileName);
//            // 更新用户头像URL
//            User user = userService.getById(userId);
//            if (user != null) {
//                // 删除旧头像文件（如果存在）
//                String oldAvatarUrl = user.getUserAvatar();
//                if (StringUtils.isNotBlank(oldAvatarUrl)) {
//                    try {
//                        String oldFileName = extractFilePathFromUrl(oldAvatarUrl);
//                        minioClient.removeObject(
//                                RemoveObjectArgs.builder()
//                                        .bucket(minioConfiguration.getBucket())
//                                        .object(oldFileName)
//                                        .build()
//                        );
//                    } catch (Exception e) {
//                        // 记录日志但不影响新文件上传
//                        log.error("删除旧头像文件失败", e);
//                    }
//                }
//                user.setUserAvatar(avatarUrl);
//                userService.updateById(user);
//            }
//            return ResultUtils.success(avatarUrl);
//        } catch (BusinessException e) {
//            throw e;
//        } catch (Exception e) {
//            log.error("文件上传失败", e);
//            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件上传失败");
//        }
//    }
//
//
//    private String getFileExtension(String filename) {
//        return Optional.ofNullable(filename)
//                .filter(f -> f.contains("."))
//                .map(f -> f.substring(f.lastIndexOf(".")))
//                .orElse("");
//    }
//
//    /**
//     * 从URL中提取文件路径
//     * 例如：从 http://192.168.0.116:9000/wen/avatar/xxx.jpg 提取出 avatar/xxx.jpg
//     */
//    private String extractFilePathFromUrl(String url) {
//        try {
//            if (StringUtils.isBlank(url)) {
//                return null;
//            }
//            // 查找最后一个斜杠之前的 "wen/" 位置
//            int bucketIndex = url.lastIndexOf(minioConfiguration.getBucket() + "/");
//            if (bucketIndex != -1) {
//                // 返回 bucket 名称后的路径部分
//                return url.substring(bucketIndex + minioConfiguration.getBucket().length() + 1);
//            }
//            return null;
//        } catch (Exception e) {
//            log.error("提取文件路径失败, url: {}", url, e);
//            return null;
//        }
//    }

    // region 登录相关

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);


        return ResultUtils.success(loginUserVO);
    }

    /**
     * 用户登录（微信开放平台）
     */
    @GetMapping("/login/wx_open")
    public BaseResponse<LoginUserVO> userLoginByWxOpen(HttpServletRequest request, HttpServletResponse response,
                                                       @RequestParam("code") String code) {
        WxOAuth2AccessToken accessToken;
        try {
            WxMpService wxService = wxOpenConfig.getWxMpService();
            accessToken = wxService.getOAuth2Service().getAccessToken(code);
            WxOAuth2UserInfo userInfo = wxService.getOAuth2Service().getUserInfo(accessToken, code);
            String unionId = userInfo.getUnionId();
            String mpOpenId = userInfo.getOpenid();
            if (StringUtils.isAnyBlank(unionId, mpOpenId)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败，系统错误");
            }
            return ResultUtils.success(userService.userLoginByMpOpen(userInfo, request));
        } catch (Exception e) {
            log.error("userLoginByWxOpen error", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败，系统错误");
        }
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    // endregion

    // region 增删改查

    /**
     * 创建用户
     *
     * @param userAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 默认密码 12345678
//        String defaultPassword = "12345678";
//        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + defaultPassword).getBytes());
//        user.setUserPassword(encryptPassword);
//        boolean result = userService.save(user);
//        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
//        return ResultUtils.success(user.getId());
        // 校验参数
        Long result = userService.userAdd(userAddRequest);
        return ResultUtils.success(result);
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.USER_LOGIN_STATE)
//    @AuthCheck(anyRole = {UserConstant.ADMIN_ROLE, UserConstant.USER_LOGIN_STATE})
    public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request) {
        BaseResponse<User> response = getUserById(id, request);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                   HttpServletRequest request) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }

    /**
     * 分页获取用户封装列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                       HttpServletRequest request) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return ResultUtils.success(userVOPage);
    }

    // endregion

    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest
     * @param request
     * @return
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
                                              HttpServletRequest request) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
                                            HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userUpdate(userUpdateRequest);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取当前登录用户的提交记录
     *
     * @param request
     * @return
     */
    @GetMapping("/get/userquestion")
    public BaseResponse<UserQuestionVO> getLoginUserQuestion(HttpServletRequest request) {
        UserQuestionVO userQuestionVO = getUserQuestionVO(request, null);
        return ResultUtils.success(userQuestionVO);
    }

    /**
     * 获取当前登录用户的指定题目的提交记录
     *
     * @param questionId
     * @param request
     * @return
     */
    @GetMapping("/get/userquestion/{questionId}")
    public BaseResponse<UserQuestionVO> getLoginUserQuestionByQuestionId(@PathVariable("questionId") Long questionId, HttpServletRequest request) {
        UserQuestionVO userQuestionVO = getUserQuestionVO(request, questionId);
        return ResultUtils.success(userQuestionVO);
    }

    // region 私有方法
    private UserQuestionVO getUserQuestionVO(HttpServletRequest request, Long questionId) {
        User user = userService.getLoginUser(request);
        LoginUserVO loginUserVO = userService.getLoginUserVO(user);
        UserQuestionVO userQuestionVO = new UserQuestionVO();
        BeanUtils.copyProperties(loginUserVO, userQuestionVO);

        QuestionSubmitQueryDTO questionSubmitQueryDTO = new QuestionSubmitQueryDTO();
        questionSubmitQueryDTO.setUserId(user.getId());
        if (questionId != null) {
            questionSubmitQueryDTO.setQuestionId(questionId);
        }


        // 将 QuestionSubmitQueryDTO 转换为 QuestionSubmitQueryRequest
        QuestionSubmitQueryRequest questionSubmitQueryRequest = convertToQueryRequest(questionSubmitQueryDTO);

        QueryWrapper<QuestionSubmit> queryWrapper = questionSubmitService.getQueryWrapper(questionSubmitQueryRequest);
        System.out.println("构建的查询条件：" + queryWrapper.getSqlSegment());

        // 直接调用本地服务方法获取题目提交记录
        List<QuestionSubmit> questionSubmitList = questionSubmitService.list(questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));

        List<QuestionSubmitVO> userQuestionsList = questionSubmitList.stream()
                .map(QuestionSubmitVO::objToVo)
                .peek(questionSubmitVO -> {
                    Long questionSubmitId = questionSubmitVO.getQuestionId();
                    // 直接调用本地服务方法获取题目信息
//                    Question question = questionService.getById(questionSubmitId);
//                    QuestionVO questionVO = questionService.getQuestionVO(question, user);
//                    questionSubmitVO.setQuestionVO(questionVO);

                    try {
                        Question question = questionService.getById(questionSubmitId);
                        QuestionVO questionVO = questionService.getQuestionVO(question, user);
                        questionSubmitVO.setQuestionVO(questionVO);

                    } catch (Exception e) {
                        log.error("获取题目信息失败，题目 ID：" + questionSubmitId, e);
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取题目信息失败");
                    }
                })
                .collect(Collectors.toList());

        userQuestionVO.setQuestionSubmitList(userQuestionsList);
        return userQuestionVO;
    }

    private QuestionSubmitQueryRequest convertToQueryRequest(QuestionSubmitQueryDTO queryDTO) {
        QuestionSubmitQueryRequest queryRequest = new QuestionSubmitQueryRequest();
        BeanUtils.copyProperties(queryDTO, queryRequest);
        return queryRequest;
    }


}
