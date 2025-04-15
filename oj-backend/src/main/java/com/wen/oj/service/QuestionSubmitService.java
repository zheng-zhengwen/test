package com.wen.oj.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wen.oj.model.dto.question.QuestionQueryRequest;
import com.wen.oj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.wen.oj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.wen.oj.model.entity.Question;
import com.wen.oj.model.entity.QuestionSubmit;
import com.wen.oj.model.entity.User;
import com.wen.oj.model.vo.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author W
 * @description 针对表【question_submit(题目提交)】的数据库操作Service
 * @createDate 2025-03-19 19:20:21
 */
public interface QuestionSubmitService extends IService<QuestionSubmit> {
    /**
     * 题目提交
     *
     * @param questionSubmitAddRequest 题目提交信息
     * @param loginUser
     * @return
     */
    long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser);

    /**
     * 获取查询条件
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest);


    /**
     * 获取题目封装
     *
     * @param questionSubmit
     * @param loginUser
     * @return
     */
    QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser);

    /**
     * 分页获取题目封装
     *
     * @param questionSubmitPage
     * @param loginUser
     * @return
     */
    Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser);

    UserStatsVO getUserStats(Long id);

    List<UserLeaderboardVO> getLeaderboard();

    Page<MyQuestionSubmitVO> getMyQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser);


}
