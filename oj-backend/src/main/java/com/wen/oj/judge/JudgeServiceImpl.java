package com.wen.oj.judge;

import cn.hutool.json.JSONUtil;
import com.wen.oj.common.ErrorCode;
import com.wen.oj.exception.BusinessException;
import com.wen.oj.judge.codesandbox.CodeSandbox;
import com.wen.oj.judge.codesandbox.CodeSandboxFactory;
import com.wen.oj.judge.codesandbox.CodeSandboxProxy;
import com.wen.oj.judge.codesandbox.model.ExecuteCodeRequest;
import com.wen.oj.judge.codesandbox.model.ExecuteCodeResponse;
import com.wen.oj.judge.strategy.JudgeContext;
import com.wen.oj.model.dto.question.JudgeCase;
import com.wen.oj.judge.codesandbox.model.JudgeInfo;
import com.wen.oj.model.entity.Question;
import com.wen.oj.model.entity.QuestionSubmit;
import com.wen.oj.model.enums.JudgeInfoMessageEnum;
import com.wen.oj.model.enums.QuestionSubmitStatusEnum;
import com.wen.oj.service.QuestionService;
import com.wen.oj.service.QuestionSubmitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service //和@Component功能一样，都是将类交给spring管理，但是@Service是专门用来管理service层的
class JudgeServiceImpl implements JudgeService {
    @Resource
    private QuestionService questionService;
    @Resource
    private QuestionSubmitService questionSubmitService;
    @Resource
    private JudgeManager judgeManager;
    @Value("${codesandbox.type}")
    private String type;

    @Override
    public QuestionSubmit dojudge(long questionSubmitId) {
        //1）传入题目的提交 id，获取到对应的题目，提交信息（包含代码，编程语言等）
        //2）如果题目提交状态不为等待中，就不用重复执行了，只执行等待中的题目
        //3）更改题目提交状态为“判题中”，防止重复执行，也能让用户即时看到状态
        //传入提交的id，获取对应的题目信息
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        //如果不为待判题状态，抛出异常
        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题中");
        }
        //更改题目状态为"判题中"，防止重复执行，也能让用户即时看到状态
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }

        //调用指定代码沙箱，获取执行结果
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        codeSandbox = new CodeSandboxProxy(codeSandbox);
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();

        //获取输入用例
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        System.out.println("获得executeCodeResponse："+executeCodeResponse);
        List<String> outputList = executeCodeResponse.getOutputList();
        System.out.println("获得outputList："+outputList);
        //根据沙箱的执行结果，设置题目的判题状态和信息
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        System.out.println("获得judgeContext："+judgeContext);
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(outputList);
        judgeContext.setJudgeCaseList(judgeCaseList);
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);

        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);
        System.out.println("获得judgeInfo："+judgeInfo);
        // 修改数据库中的判题结果
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        // 使用枚举判断判题结果
        if (JudgeInfoMessageEnum.ACCEPTED.getValue().equals(judgeInfo.getMessage())) {
            questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
            // 通过数加1
            question.setAcceptedNum(question.getAcceptedNum() + 1);
        } else {
            questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.FAILED.getValue());
        }
        // 提交数加1
        question.setSubmitNum(question.getSubmitNum() + 1);
        // 更新题目信息
        boolean questionUpdate = questionService.updateById(question);
        if (!questionUpdate) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目信息更新错误");
        }
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        QuestionSubmit questionSubmitResult = questionSubmitService.getById(questionSubmitId);
        return questionSubmitResult;
    }
}