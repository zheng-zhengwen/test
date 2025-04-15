package com.wen.oj.judge.strategy;

import com.wen.oj.model.dto.question.JudgeCase;
import com.wen.oj.judge.codesandbox.model.JudgeInfo;
import com.wen.oj.model.entity.Question;
import com.wen.oj.model.entity.QuestionSubmit;
import lombok.Data;

import java.util.List;

/**
 * 上下文对象（定义在策略中传递的参数）相当于DTO
 * */
@Data
public class JudgeContext {
    private JudgeInfo judgeInfo;

    private List<String> inputList;

    private List<String> outputList;

    private List<JudgeCase> judgeCaseList;

    private Question question;

    private QuestionSubmit questionSubmit;
}
