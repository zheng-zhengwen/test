package com.wen.oj.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wen.oj.common.ErrorCode;
import com.wen.oj.constant.CommonConstant;
import com.wen.oj.constant.UserConstant;
import com.wen.oj.exception.BusinessException;
import com.wen.oj.judge.JudgeService;
import com.wen.oj.mapper.QuestionMapper;
import com.wen.oj.mapper.UserMapper;
import com.wen.oj.model.dto.question.QuestionQueryRequest;
import com.wen.oj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.wen.oj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.wen.oj.model.entity.Question;
import com.wen.oj.model.entity.QuestionSubmit;
import com.wen.oj.model.entity.User;
import com.wen.oj.model.enums.QuestionsSubmitLanguageEnum;
import com.wen.oj.model.enums.QuestionSubmitStatusEnum;
import com.wen.oj.model.vo.*;
import com.wen.oj.service.QuestionService;
import com.wen.oj.service.QuestionSubmitService;
import com.wen.oj.mapper.QuestionSubmitMapper;
import com.wen.oj.service.UserService;
import com.wen.oj.utils.SqlUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author W
 * @description 针对表【question_submit(题目提交)】的数据库操作Service实现
 * @createDate 2025-03-19 19:20:21
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
        implements QuestionSubmitService {


    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private UserMapper userMapper; // 新增 UserMapper 依赖

    @Resource
    @Lazy
    private JudgeService judgeService;

    @Resource
    private QuestionSubmitMapper questionSubmitMapper;

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        //todo 校验编程语言是否合法
        String language = questionSubmitAddRequest.getLanguage();
        QuestionsSubmitLanguageEnum languageEnum = QuestionsSubmitLanguageEnum.getEnumByValue(language);
        if (languageEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言不合法");
        }
        //获取题目id
        long questionId = questionSubmitAddRequest.getQuestionId();
        // 判断实体是否存在，根据类别获取实体
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 是否已提交题目
        long userId = loginUser.getId();
        // 每个用户串行提交题目
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setCode(questionSubmitAddRequest.getCode());
        questionSubmit.setLanguage(language);
        //todo 设置初始状态
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        questionSubmit.setJudgeInfo("{}");
        boolean save = this.save(questionSubmit);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据插入失败");
        }
        Long questionSubmitId = questionSubmit.getId();
        //执行判题服务
        CompletableFuture.runAsync(() -> {
            judgeService.dojudge(questionSubmitId);
        });
        return questionSubmitId;
    }


    /**
     * 获取查询包装类
     * （用户根据哪些字段查询，根据前端传来的请求对象，得到 mybatis 框架支持的查询 QueryWraper 类）
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }
        String language = questionSubmitQueryRequest.getLanguage();
        Integer status = questionSubmitQueryRequest.getStatus();
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        Long userId = questionSubmitQueryRequest.getUserId();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq(StringUtils.isNotBlank(language), "language", language);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(QuestionSubmitStatusEnum.getEnumByValue(status) != null, "status", status);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取题目封转
     *
     * @param questionSubmit
     * @param loginUser
     * @return
     */
    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        /**
        //脱敏：仅管理员和本用户能看到自己（提交userId 和 登录用户 id 不同）提交代码）
//        long userId = loginUser.getId();
//        //处理脱敏
//        if (userId != questionSubmit.getUserId() && !userService.isAdmin(loginUser)) {
//            questionSubmitVO.setCode(null);
//        }
//        return questionSubmitVO;
         */
        //只有本人和管理员才能看见提交的代码
        Long userId1 = loginUser.getId();
        // 1. 关联查询用户信息
        Long userId = questionSubmitVO.getUserId();
        if (!isAdmin(loginUser) && !userId1.equals(userId)) {
            questionSubmitVO.setCode(null);
        }
        if (userId != null && userId > 0) {
            User user = userMapper.selectById(userId); // 直接从数据库查询用户信息
            UserVO userVO = getUserVO(user);
            questionSubmitVO.setUserVO(userVO);
        }
        // 2. 关联查询题目信息
        Long questionId = questionSubmit.getQuestionId();
        if (questionId != null && questionId > 0) {
            Question question = questionService.getById(questionId);
            QuestionVO questionVO = questionService.getQuestionVO(question, loginUser);
            questionSubmitVO.setQuestionVO(questionVO);
        }
        return questionSubmitVO;
    }

    // 判断是否为管理员
    private boolean isAdmin(User user) {
//        return user != null && "ADMIN".equals(user.getUserRole()); // 管理员角色为 "ADMIN"
        return user != null && UserConstant.ADMIN_ROLE.equals(user.getUserRole());
    }

    // 获取脱敏的用户信息
    private UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }


    /**
     * 分页获取题目封转
     *
     * @param questionSubmitPage
     * @param loginUser
     * @return
     */
    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser) {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollectionUtils.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionSubmitList.stream().map(QuestionSubmit::getUserId).collect(Collectors.toSet());
        // 直接调用本地用户服务的方法获取用户列表
        List<User> userList = userService.listByIds(new ArrayList<>(userIdSet));
        Map<Long, List<User>> userIdUserListMap = userList.stream()
                .collect(Collectors.groupingBy(User::getId));

        // 2. 关联查询题目信息
        Set<Long> questionIdSet = questionSubmitList.stream().map(QuestionSubmit::getQuestionId).collect(Collectors.toSet());
        Map<Long, List<Question>> questionIdQuestionListMap = questionService.listByIds(questionIdSet).stream()
                .collect(Collectors.groupingBy(Question::getId));

        // 填充信息
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream().map(questionSubmit -> {
            QuestionSubmitVO questionSubmitVO = getQuestionSubmitVO(questionSubmit, loginUser);
            Long userId = questionSubmit.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            // 直接使用本地用户服务的方法将用户信息转换为VO
            UserVO userVO = userService.getUserVO(user);
            questionSubmitVO.setUserVO(userVO);

            Long questionId = questionSubmit.getQuestionId();
            Question question = null;
            if (questionIdQuestionListMap.containsKey(questionId)) {
                question = questionIdQuestionListMap.get(questionId).get(0);
            }
            questionSubmitVO.setQuestionVO(questionService.getQuestionVO(question, loginUser));
            return questionSubmitVO;
        }).collect(Collectors.toList());
        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;

//        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream()
//                .map(questionSubmit -> getQuestionSubmitVO(questionSubmit, loginUser))
//                .collect(Collectors.toList());
//        questionSubmitVOPage.setRecords(questionSubmitVOList);
//        return questionSubmitVOPage;
    }

    // 简单的 User 转 UserVO 方法，可根据实际情况修改
    private UserVO convertToUserVO(User user) {
        UserVO userVO = new UserVO();
        // 假设 UserVO 和 User 有相同的属性名
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }


//
//    /**
//     * 封装了事务的方法
//     *
//     * @param userId
//     * @param questionId
//     * @return
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public int doQuestionSubmitInner(long userId, long questionId) {
//        QuestionSubmit questionSubmit = new QuestionSubmit();
//        questionSubmit.setUserId(userId);
//        questionSubmit.setQuestionId(questionId);
//        QueryWrapper<QuestionSubmit> thumbQueryWrapper = new QueryWrapper<>(questionSubmit);
//        QuestionSubmit oldQuestionSubmit = this.getOne(thumbQueryWrapper);
//        boolean result;
//        // 已提交题目
//        if (oldQuestionSubmit != null) {
//            result = this.remove(thumbQueryWrapper);
//            if (result) {
//                // 提交题目数 - 1
//                result = questionService.update()
//                        .eq("id", questionId)
//                        .gt("thumbNum", 0)
//                        .setSql("thumbNum = thumbNum - 1")
//                        .update();
//                return result ? -1 : 0;
//            } else {
//                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
//            }
//        } else {
//            // 未提交题目
//            result = this.save(questionSubmit);
//            if (result) {
//                // 提交题目数 + 1
//                result = questionService.update()
//                        .eq("id", questionId)
//                        .setSql("thumbNum = thumbNum + 1")
//                        .update();
//                return result ? 1 : 0;
//            } else {
//                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
//            }
//        }
//    }

    //获取用户提交数和通过数
    @Override
    public UserStatsVO getUserStats(Long id) {
        UserStatsVO userStatsVO = new UserStatsVO();
        //查询用户提交数
        QueryWrapper<QuestionSubmit> questionSubmitLambdaQueryWrapper = new QueryWrapper<>();
        questionSubmitLambdaQueryWrapper.eq("userId", id);
        int submitCount = (int) this.count(questionSubmitLambdaQueryWrapper);
        //查询用户成功提交数
        int solvedCount = questionSubmitMapper.getSolvedCount(id);
        //计算通过率,保留两位小数
        double passRate = submitCount > 0 ? (double) solvedCount / submitCount * 100 : 0;
        System.out.println("通过率：" + passRate);
        userStatsVO.setSubmitCount(submitCount);
        userStatsVO.setSolvedCount(solvedCount);
        userStatsVO.setPassRate(passRate);
        return userStatsVO;
    }

    //获取排行
    @Override
    public List<UserLeaderboardVO> getLeaderboard() {
        return questionSubmitMapper.getLeaderBoard(5);
    }

    //获取用户提交记录
    @Override
    public Page<MyQuestionSubmitVO> getMyQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser) {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<MyQuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollUtil.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }
        // 1. 关联查询用户信息
        User loginUserNew = userService.getById(loginUser.getId()); // 修改此处
        // 2. 关联查询题目信息
        Set<Long> questionIdSet = questionSubmitList.stream().map(QuestionSubmit::getQuestionId).collect(Collectors.toSet());
        Map<Long, List<Question>> questionIdQuestionListMap = questionService.listByIds(questionIdSet).stream()
                .collect(Collectors.groupingBy(Question::getId));
        // 填充信息
        List<MyQuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream()
                .limit(5)
                .map(questionSubmit -> {
                    MyQuestionSubmitVO myQuestionSubmitVO = new MyQuestionSubmitVO();
                    Long questionId = questionSubmit.getQuestionId();
                    Question question = null;
                    if (questionIdQuestionListMap.containsKey(questionId)) {
                        question = questionIdQuestionListMap.get(questionId).get(0);
                    }
                    myQuestionSubmitVO.setTitle(question.getTitle());
                    myQuestionSubmitVO.setCreateTime(questionSubmit.getCreateTime());
                    myQuestionSubmitVO.setStatus(questionSubmit.getStatus());
                    myQuestionSubmitVO.setLanguage(questionSubmit.getLanguage());
                    myQuestionSubmitVO.setId(questionSubmit.getQuestionId());
                    return myQuestionSubmitVO;
                }).collect(Collectors.toList());
        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;
    }

}




