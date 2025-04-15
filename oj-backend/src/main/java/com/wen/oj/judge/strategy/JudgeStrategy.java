package com.wen.oj.judge.strategy;


import com.wen.oj.judge.codesandbox.model.JudgeInfo;

/**
 * 判题策略接口
 */
public interface JudgeStrategy {
    /**
     * 执行判题
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext);
}
