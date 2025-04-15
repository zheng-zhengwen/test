package com.wen.oj.AI;

import cn.hutool.core.collection.CollUtil;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionChoice;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import com.wen.oj.common.ErrorCode;
import com.wen.oj.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于对接 AI 平台
 */
@Service
@Slf4j
public class AiManager {

    @Resource
    private ArkService aiService;

    private final String DEFAULT_MODEL = "doubao-1-5-lite-32k-250115";

    public static final String PRECONDITION = "现在你是一位精通OJ竞赛题目的算法专家，接下来我会按照以下固定格式给你发送内容：\n" +
            "题目标题：\n" +
            "{该算法题的标题}\n" +
            "题目内容:\n" +
            "{该算法题的具体内容}\n" +
            "题目使用语言:\n" +
            "{解决该题目所使用的编程语言}\n" +
            "请认真根据这两部分内容，必须严格按照以下指定格式生成markdown内容（此外不要输出任何多余的开头、结尾、注释）\n" +
            "【【【【【\n" +
            "明确的代码分析，越详细越好，不要生成多余的注释\n" +
            "【【【【【\n" +
            "解答该题目对应的代码，代码相关参数是通过命令行传来的且写在main方法中，只需生成要求编程语言的代码\n";

    /**
     * 调用 AI 接口，获取响应字符串
     *
     * @param userPrompt
     * @return
     */
    public String doChat(String userPrompt) {
        return doChat("", userPrompt, DEFAULT_MODEL);
    }

    /**
     * 调用 AI 接口，获取响应字符串
     *
     * @param systemPrompt
     * @param userPrompt
     * @return
     */
    public String doChat(String systemPrompt, String userPrompt) {
        return doChat(systemPrompt, userPrompt, DEFAULT_MODEL);
    }

    /**
     * 调用 AI 接口，获取响应字符串
     *
     * @param systemPrompt
     * @param userPrompt
     * @param model
     * @return
     */
    public String doChat(String systemPrompt, String userPrompt, String model) {
        // 构造消息列表
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = ChatMessage.builder().role(ChatMessageRole.SYSTEM).content(systemPrompt).build();
        final ChatMessage userMessage = ChatMessage.builder().role(ChatMessageRole.USER).content(userPrompt).build();
        messages.add(systemMessage);
        messages.add(userMessage);
        // 构造请求
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(model)
                .messages(messages)
                .build();
        // 调用接口发送请求
        List<ChatCompletionChoice> choices = aiService.createChatCompletion(chatCompletionRequest).getChoices();
        if (CollUtil.isNotEmpty(choices)) {
            return (String) choices.get(0).getMessage().getContent();
        }
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 调用失败，没有返回结果");
    }

    /**
     * 获取 AI 生成结果
     *
     * @param title
     * @param content
     * @param language
     * @param questionId
     * @return
     */
    public AiQuestionVO getGenResultByDeepSeek(final String title, final String content, final String language, final Long questionId) {
        String promote = AiManager.PRECONDITION + "标题 " + title + " \n内容: " + content + "\n编程语言: " + language;
        String resultData = doChat(promote);
        log.info("AI 生成的信息: {}", resultData);
        String genResult = null;
        String genCode = resultData;
        if (resultData.split("【【【【【").length >= 3) {
            genResult = resultData.split("【【【【【")[1].trim();
            genCode = resultData.split("【【【【【")[2].trim();
        }
        return new AiQuestionVO(genResult, genCode, questionId);
    }

    // 在 AiManager 类中添加
    public void setAiService(ArkService aiService) {
        this.aiService = aiService;
    }
}