package com.wen.oj.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wen.oj.model.dto.question.JudgeCase;
import com.wen.oj.model.dto.question.JudgeConfig;
import com.wen.oj.judge.codesandbox.model.JudgeInfo;
import com.wen.oj.model.entity.Question;
import com.wen.oj.model.enums.JudgeInfoMessageEnum;

import java.util.List;
import java.util.Optional;

/**
 * Java程序的判题策略
 */
public class JavaLanguageJudgeStrategy implements JudgeStrategy {
    /**
     * 执行判题
     * @param judgeContext
     * @return
     */
    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        JudgeInfo judgeInfo = judgeContext.getJudgeInfo();
        Long memory = Optional.ofNullable(judgeInfo.getMemory()).orElse(0L);
        Long time = Optional.ofNullable(judgeInfo.getTime()).orElse(0L);
        List<String> inputList = judgeContext.getInputList();
        List<String> outputList = judgeContext.getOutputList();
        Question question = judgeContext.getQuestion();
        List<JudgeCase> judgeCaseList = judgeContext.getJudgeCaseList();
        JudgeInfoMessageEnum judgeInfoMessageEnum=JudgeInfoMessageEnum.ACCEPTED;
        JudgeInfo judgeInfoResponse=new JudgeInfo();
        judgeInfoResponse.setMemory(memory);
        judgeInfoResponse.setTime(time);
        //先判断沙箱执行的结果输出数量是否和预期输出数量相等
        if(outputList.size()!= inputList.size()){
            judgeInfoMessageEnum=JudgeInfoMessageEnum.WRONG_ANSWER;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }
//        //判断每一项输出和预期输出是否相等
//        for(int i=0;i<judgeCaseList.size();i++){
//            JudgeCase judgeCase = judgeCaseList.get(i);
//            if(!judgeCase.getOutput().equals(outputList.get(i))){
//                judgeInfoMessageEnum=JudgeInfoMessageEnum.WRONG_ANSWER;
//                judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
//                return judgeInfoResponse;
//            }
//        }
        //使用json解析，判断每一项输出和预期输出是否相等（增强版本）
        ObjectMapper objectMapper = new ObjectMapper();
        for (int i = 0; i < judgeCaseList.size(); i++) {
            JudgeCase judgeCase = judgeCaseList.get(i);
            try {
                // 统一分隔符为空格
                String expectedOutputStr = judgeCase.getOutput().replaceAll("[,，]", " ");
                String actualOutputStr = outputList.get(i).replaceAll("[,，]", " ");

                Object expectedOutputObj = objectMapper.readValue(expectedOutputStr, Object.class);
                Object actualOutputObj = objectMapper.readValue(actualOutputStr, Object.class);

                if (!expectedOutputObj.equals(actualOutputObj)) {
                    judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
                    judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
                    return judgeInfoResponse;
                }
            } catch (Exception e) {
                // 解析失败，按错误处理
                judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
                judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
                return judgeInfoResponse;
            }
        }
        //判断题目限制
        String judgeConfigStr = question.getJudgeConfig();
        JudgeConfig judgeConfig = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);
        Long needMemoryLimit = judgeConfig.getMemoryLimit();
        Long needTimeLimit = judgeConfig.getTimeLimit();
        if(memory > needMemoryLimit){
            judgeInfoMessageEnum=JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }
        //Java程序本身需要额外执行1秒钟
        long JAVA_PROGRAM_TIME_COST=1000L;
        if((time - JAVA_PROGRAM_TIME_COST) >needTimeLimit){
            judgeInfoMessageEnum=JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }
        judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
        return judgeInfoResponse;
    }
}
