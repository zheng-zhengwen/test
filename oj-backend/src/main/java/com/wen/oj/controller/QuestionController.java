package com.wen.oj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.wen.oj.AI.AiManager;
import com.wen.oj.AI.AiQuestionVO;
import com.wen.oj.model.dto.questionsubmit.QuestionSubmitQueryDTO;
import com.wen.oj.annotation.AuthCheck;
import com.wen.oj.common.BaseResponse;
import com.wen.oj.common.DeleteRequest;
import com.wen.oj.common.ErrorCode;
import com.wen.oj.common.ResultUtils;
import com.wen.oj.constant.UserConstant;
import com.wen.oj.exception.BusinessException;
import com.wen.oj.exception.ThrowUtils;
import com.wen.oj.model.dto.question.*;
import com.wen.oj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.wen.oj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.wen.oj.model.entity.Question;
import com.wen.oj.model.entity.QuestionSubmit;
import com.wen.oj.model.entity.User;
import com.wen.oj.model.enums.QuestionsSubmitLanguageEnum;
import com.wen.oj.model.vo.*;
import com.wen.oj.service.QuestionService;
import com.wen.oj.service.QuestionSubmitService;
import com.wen.oj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 题目接口
 *
 * @author <a href="https://github.com/zheng-zhengwen">程序员阿文</a>
 * @from <a href="https://wen.icu">在线编程系统</a>
 */
@RestController
@RequestMapping("/question")
@Slf4j
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    private final static Gson GSON = new Gson();

    @Resource
    private QuestionSubmitService questionSubitService;

    @Resource
    private AiManager aiManager;


    @Resource
    private QuestionSubmitService questionSubmitService;

    private static final String CACHE_KEY_PREFIX = "question:ai:";

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // region 增删改查

    /**
     * 创建
     *
     * @param questionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest, HttpServletRequest request) {
        if (questionAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionAddRequest, question);
        List<String> tags = questionAddRequest.getTags();
        if (tags != null) {
            question.setTags(GSON.toJson(tags));
        }
        List<JudgeCase> judgeCases = questionAddRequest.getJudgeCase();
        if (judgeCases != null) {
            question.setJudgeCase(GSON.toJson(judgeCases));
        }
        JudgeConfig judgeConfig = questionAddRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        questionService.validQuestion(question, true);
        User loginUser = userService.getLoginUser(request);
        question.setUserId(loginUser.getId());
        question.setFavourNum(0);
        question.setThumbNum(0);
        boolean result = questionService.save(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newQuestionId = question.getId();
        return ResultUtils.success(newQuestionId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestion.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = questionService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param questionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {
        Logger logger = LoggerFactory.getLogger(QuestionController.class);
        System.out.println("更新题目1111="+logger);
        if (questionUpdateRequest == null || questionUpdateRequest.getId() <= 0) {
            logger.error("更新题目请求参数错误，请求参数: {}", questionUpdateRequest);
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionUpdateRequest, question);
        List<String> tags = questionUpdateRequest.getTags();
        if (tags != null) {
            question.setTags(GSON.toJson(tags));
        }
        List<JudgeCase> judgeCases = questionUpdateRequest.getJudgeCase();
        if (judgeCases != null) {
            question.setJudgeCase(GSON.toJson(judgeCases));
        }
        JudgeConfig judgeConfig = questionUpdateRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        // 参数校验
        questionService.validQuestion(question, false);
        long id = questionUpdateRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        if (oldQuestion == null) {
            logger.error("要更新的题目不存在，题目ID: {}", id);
            ThrowUtils.throwIf(true, ErrorCode.NOT_FOUND_ERROR);
        }
        boolean result = questionService.updateById(question);
        if (result) {
            logger.info("题目更新成功，题目ID: {}", id);
        } else {
            logger.error("题目更新失败，题目ID: {}", id);
        }
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Question> getQuestionById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = questionService.getById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        //不是本人/管理员，则脱敏
        if (!question.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return ResultUtils.success(question);
    }

    /**
     * 根据 id 获取（封装类 脱敏）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = questionService.getById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(questionService.getQuestionVO(question, request));
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                               HttpServletRequest request) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listMyQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                                 HttpServletRequest request) {
        if (questionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        questionQueryRequest.setUserId(loginUser.getId());
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    /**
     * 分页获取题目列表（仅管理员）【不脱敏】
     *
     * @param questionQueryRequest
     * @param request
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                           HttpServletRequest request) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionPage);
    }


    // endregion

    /**
     * 编辑题目（用户）
     *
     * @param questionEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editQuestion(@RequestBody QuestionEditRequest questionEditRequest, HttpServletRequest request) {
        log.debug("净来了");
        if (questionEditRequest == null || questionEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionEditRequest, question);
        List<String> tags = questionEditRequest.getTags();
        if (tags != null) {
            question.setTags(GSON.toJson(tags));
        }
        List<JudgeCase> judgeCases = questionEditRequest.getJudgeCase();
        if (judgeCases != null) {
            question.setJudgeCase(GSON.toJson(judgeCases));
        }
        JudgeConfig judgeConfig = questionEditRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        // 参数校验
        questionService.validQuestion(question, false);
        User loginUser = userService.getLoginUser(request);
        long id = questionEditRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldQuestion.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = questionService.updateById(question);
        return ResultUtils.success(result);
    }


    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param request
     * @return resultNum 提交记录id
     */
    @PostMapping("/question_submit/do")
    public BaseResponse<Long> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
                                               HttpServletRequest request) {
        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能点赞
        final User loginUser = userService.getLoginUser(request);
        //long questionId = questionSubmitAddRequest.getQuestionId();
        long result = questionSubitService.doQuestionSubmit(questionSubmitAddRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 分页获取题目提交列表（仅管理员，普通用户只能看到给答案，提交代码等公开信息）【不脱敏】
     *
     * @param questionSubmitQueryRequest
     * @param request                    return resultNum 提交记录id
     */
    @PostMapping("/question_submit/list/page")
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
                                                                         HttpServletRequest request) {
        System.out.println("接收到的请求参数：" + questionSubmitQueryRequest);
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        // 直接调用本地 UserService 的方法获取登录用户
        User loginUser = userService.getLoginUser(request);
        Page<QuestionSubmit> questionSubmitPage = questionSubitService.page(new Page<>(current, size),
                questionSubitService.getQueryWrapper(questionSubmitQueryRequest));
        return ResultUtils.success(questionSubitService.getQuestionSubmitVOPage(questionSubmitPage, loginUser));
//        long current =  questionSubmitQueryRequest.getCurrent();
//        long size = questionSubmitQueryRequest.getPageSize();
//        //从数据库中查询原始的题目提交给分页信息
//        Page<QuestionSubmit> questionSubmitPage = questionSumitService.page(new Page<>(current, size),
//                questionSumitService.getQueryWrapper(questionSubmitQueryRequest));
//        final User loginUser = userService.getLoginUser(request);
//        //返回脱敏信息
//        return ResultUtils.success(questionSumitService.getQuestionSubmitVOPage(questionSubmitPage,loginUser));
    }

    /**
     * ai答题
     */
    @PostMapping("/question/ai")
    public BaseResponse<AiQuestionVO> aiQuestion(@RequestBody QuestionSubmitQueryDTO questionSubmitQueryDTO, HttpServletRequest request) {
        if (questionSubmitQueryDTO == null || questionSubmitQueryDTO.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //如果没设编程语言，则默认设为java
        if (questionSubmitQueryDTO.getLanguage() == null) {
            questionSubmitQueryDTO.setLanguage(QuestionsSubmitLanguageEnum.JAVA.getValue());
        }
//        User loginUser = userFeignClient.getLoginUser(request);
//        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        Question question = questionService.getById(questionSubmitQueryDTO.getQuestionId());
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //定义缓存键
        String cacheKey = CACHE_KEY_PREFIX + question.getId()+questionSubmitQueryDTO.getLanguage();
        // 尝试从Redis中获取缓存
        AiQuestionVO cachedAiQuestionVO = (AiQuestionVO) redisTemplate.opsForValue().get(cacheKey);
        if (cachedAiQuestionVO != null) {
            // 如果缓存存在，直接返回
            return ResultUtils.success(cachedAiQuestionVO);
        } else {
            // 如果缓存不存在，调用AI方法获取结果
            AiQuestionVO aiQuestionVO = aiManager.getGenResultByDeepSeek(question.getTitle(), question.getContent(), questionSubmitQueryDTO.getLanguage(), question.getId());

            // 将结果存入Redis并设置过期时间为一小时//一天
//            redisTemplate.opsForValue().set(cacheKey, aiQuestionVO, 1, TimeUnit.HOURS);
            redisTemplate.opsForValue().set(cacheKey, aiQuestionVO, 1, TimeUnit.DAYS);

            return ResultUtils.success(aiQuestionVO);
        }
//        AiQuestionVO aiQuestionVO = aiManager.getGenResultByDeepSeek(question.getTitle(), question.getContent(), questionSubmitQueryDTO.getLanguage(), question.getId());
//        return ResultUtils.success(aiQuestionVO);
    }

    /**
     * 排行
     * */
    @GetMapping("/leaderboard")
    public BaseResponse<List<UserLeaderboardVO>> getLeaderboard() {
        List<UserLeaderboardVO> leaderboardVOS=  questionSubmitService.getLeaderboard();
        return ResultUtils.success(leaderboardVOS);
    }

    /**
     * 分页获取题目提交列表（用户自己）
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    @PostMapping("/question_submit/list/page/my")
    public BaseResponse<Page<MyQuestionSubmitVO>> listQuestionMySubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest, HttpServletRequest request) {
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        User loginUser = userService.getLoginUser(request);
        questionSubmitQueryRequest.setUserId(loginUser.getId());
        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, size),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
        return ResultUtils.success(questionSubmitService.getMyQuestionSubmitVOPage(questionSubmitPage, loginUser));
    }

    /**
     * 返回近日热题列表
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/hot/list")
    public BaseResponse<Page<HotQuestionVO>> getHotQuestionSubmitList(@RequestBody QuestionQueryRequest questionQueryRequest,HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        questionQueryRequest.setUserId(loginUser.getId());
        Page<HotQuestionVO> questionVOPage=  questionService.listHotQuestions(questionQueryRequest);
        return ResultUtils.success(questionVOPage);
    }
    /**
     * 统计个人数据
     * @param request
     * @return
     */
    @GetMapping("/status")
    public BaseResponse<UserStatsVO> getUserStats(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        UserStatsVO userStatsVO=  questionSubmitService.getUserStats(loginUser.getId());
        return ResultUtils.success(userStatsVO);
    }

}
