package com.wen.oj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wen.oj.annotation.AuthCheck;
import com.wen.oj.common.BaseResponse;
import com.wen.oj.common.ErrorCode;
import com.wen.oj.common.ResultUtils;
import com.wen.oj.constant.UserConstant;
import com.wen.oj.exception.BusinessException;
import com.wen.oj.model.dto.question.QuestionQueryRequest;
import com.wen.oj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.wen.oj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.wen.oj.model.entity.Question;
import com.wen.oj.model.entity.QuestionSubmit;
import com.wen.oj.model.entity.User;
import com.wen.oj.model.vo.QuestionSubmitVO;
import com.wen.oj.service.QuestionSubmitService;
import com.wen.oj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 题目提交接口
 *
 * @author <a href="https://github.com/zheng-zhengwen">程序员阿文</a>
 * @from <a href="https://wen.icu">在线编程系统</a>
 */
@RestController
//@RequestMapping("/question_submit")
@Slf4j
@Deprecated
public class QuestionSubmitController {

    @Resource
    private QuestionSubmitService questionSumitService;

    @Resource
    private UserService userService;

//    /**
//     * 提交题目
//     *
//     * @param questionSubmitAddRequest
//     * @param request
//     * @return resultNum 提交记录id
//     */
//    @PostMapping("/")
//    public BaseResponse<Long> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
//                                               HttpServletRequest request) {
//        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        // 登录才能点赞
//        final User loginUser = userService.getLoginUser(request);
//        //long questionId = questionSubmitAddRequest.getQuestionId();
//        long result = questionSumitService.doQuestionSubmit(questionSubmitAddRequest, loginUser);
//        return ResultUtils.success(result);
//    }
//
//    /**
//     * 分页获取题目提交列表（仅管理员，普通用户只能看到给答案，提交代码等公开信息）【不脱敏】
//     *
//     * @param questionSubmitQueryRequest
//     * @param request
//     */
//    @PostMapping("/list/page")
//    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
//                                                                         HttpServletRequest request)  {
//        long current =  questionSubmitQueryRequest.getCurrent();
//        long size = questionSubmitQueryRequest.getPageSize();
//        //从数据库中查询原始的题目提交给分页信息
//        Page<QuestionSubmit> questionSubmitPage = questionSumitService.page(new Page<>(current, size),
//                questionSumitService.getQueryWrapper(questionSubmitQueryRequest));
//        final User loginUser = userService.getLoginUser(request);
//        //返回脱敏信息
//        return ResultUtils.success(questionSumitService.getQuestionSubmitVOPage(questionSubmitPage,loginUser));
//    }


}
