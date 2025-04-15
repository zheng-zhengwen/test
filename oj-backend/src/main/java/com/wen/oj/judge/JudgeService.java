package com.wen.oj.judge;


import com.wen.oj.model.entity.QuestionSubmit;

/**
 * 判题服务
 * */
public interface JudgeService {
    /**
     * 执行判题
     * @param questionSubmitId
     * @return
     */
    QuestionSubmit dojudge(long questionSubmitId);
}
