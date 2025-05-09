package com.wen.oj.controller;

import cn.hutool.core.io.FileUtil;
import com.wen.oj.common.BaseResponse;
import com.wen.oj.common.ErrorCode;
import com.wen.oj.common.ResultUtils;
import com.wen.oj.constant.FileConstant;
import com.wen.oj.exception.BusinessException;
import com.wen.oj.manager.CosManager;
import com.wen.oj.model.dto.file.UploadFileRequest;
import com.wen.oj.model.entity.User;
import com.wen.oj.model.enums.FileUploadBizEnum;
import com.wen.oj.service.UserService;
import java.io.File;
import java.util.Arrays;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件接口
 *
 * @author <a href="https://github.com/zheng-zhengwen">程序员阿文</a>
 * @from <a href="https://wen.icu">在线编程系统</a>
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    /**
     * 文件上传
     *
     * @param multipartFile
     * @param uploadFileRequest
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
                                           UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        try {
            // 接收请求
            String biz = uploadFileRequest.getBiz();
            System.out.println("接收到文件上传请求，业务类型为: " + biz);

            // 验证业务类型
            FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
            if (fileUploadBizEnum == null) {
                System.out.println("业务类型参数错误，biz = " + biz);
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            System.out.println("业务类型验证通过，业务类型为: " + fileUploadBizEnum.getText());

            // 验证文件
            validFile(multipartFile, fileUploadBizEnum);
            System.out.println("文件验证通过");

            // 获取登录用户信息
            User loginUser = userService.getLoginUser(request);
            System.out.println("获取到登录用户信息，用户ID为: " + loginUser.getId());

            // 生成文件存储路径
            String uuid = RandomStringUtils.randomAlphanumeric(8);
            String filename = uuid + "-" + multipartFile.getOriginalFilename();
            String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId(), filename);
            System.out.println("生成文件存储路径: " + filepath);

            File file = null;
            try {
                // 创建临时文件
                file = File.createTempFile(filepath, null);
                System.out.println("创建临时文件: " + file.getAbsolutePath());

                // 转移文件内容
                multipartFile.transferTo(file);
                System.out.println("文件内容已转移到临时文件");

                // 上传文件到对象存储
                cosManager.putObject(filepath, file);
                System.out.println("文件已成功上传到对象存储，路径为: " + filepath);

                // 返回可访问地址
                String accessUrl = FileConstant.COS_HOST + filepath;
                System.out.println("文件可访问地址为: " + accessUrl);
                return ResultUtils.success(accessUrl);
            } catch (Exception e) {
                System.out.println("文件上传过程中出现错误，文件路径 = " + filepath + ", 错误信息: " + e.getMessage());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
            } finally {
                if (file != null) {
                    // 删除临时文件
                    boolean delete = file.delete();
                    if (delete) {
                        System.out.println("临时文件已成功删除，文件路径 = " + file.getAbsolutePath());
                    } else {
                        System.out.println("临时文件删除失败，文件路径 = " + file.getAbsolutePath());
                    }
                }
            }
        } catch (BusinessException e) {
            System.out.println("业务异常: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("未知异常: " + e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }
    }
//    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
//            UploadFileRequest uploadFileRequest, HttpServletRequest request) {
//        String biz = uploadFileRequest.getBiz();
//        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
//        if (fileUploadBizEnum == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        validFile(multipartFile, fileUploadBizEnum);
//        User loginUser = userService.getLoginUser(request);
//        // 文件目录：根据业务、用户来划分
//        String uuid = RandomStringUtils.randomAlphanumeric(8);
//        String filename = uuid + "-" + multipartFile.getOriginalFilename();
//        String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId(), filename);
//        System.out.println("上传头像："+filepath);
//        File file = null;
//        try {
//            // 上传文件
//            file = File.createTempFile(filepath, null);
//            multipartFile.transferTo(file);
//            cosManager.putObject(filepath, file);
//            // 返回可访问地址
//            return ResultUtils.success(FileConstant.COS_HOST + filepath);
//        } catch (Exception e) {
//            log.error("file upload error, filepath = " + filepath, e);
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
//        } finally {
//            if (file != null) {
//                // 删除临时文件
//                boolean delete = file.delete();
//                if (!delete) {
//                    log.error("file delete error, filepath = {}", filepath);
//                }
//            }
//        }
//    }

    /**
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024L;
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }
}
