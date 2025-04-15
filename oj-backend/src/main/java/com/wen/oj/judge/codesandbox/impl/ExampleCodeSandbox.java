package com.wen.oj.judge.codesandbox.impl;

import com.wen.oj.judge.codesandbox.CodeSandbox;
import com.wen.oj.judge.codesandbox.model.ExecuteCodeRequest;
import com.wen.oj.judge.codesandbox.model.ExecuteCodeResponse;
import com.wen.oj.judge.codesandbox.model.JudgeInfo;
import com.wen.oj.model.enums.JudgeInfoMessageEnum;
import com.wen.oj.model.enums.QuestionSubmitStatusEnum;

import java.util.List;

/**
 * 示例代码沙箱(演示业务流程)
 */
public class ExampleCodeSandbox implements CodeSandbox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        // 获取输入
        List<String> inputList = executeCodeRequest.getInputList();
        //创建并设置执行代码响应对象
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(inputList);
        executeCodeResponse.setMessage("执行成功");
        // 设置状态，表示代码提交执行成功
        executeCodeResponse.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());

        //创建并设置判题信息对象
        JudgeInfo judgeInfo = new JudgeInfo();
        // 设置判题信息,表示代码执行成功
        judgeInfo.setMessage(JudgeInfoMessageEnum.ACCEPTED.getText());
        judgeInfo.setMemory(100L);
        judgeInfo.setTime(100L);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        //返回执行代码响应对象
        return executeCodeResponse;
    }

}