package com.wen.oj.AI;

import com.volcengine.ark.runtime.service.ArkService;

public class AiManagerLocalTest {

    // 配置参数（请修改以下值）
    private static final String API_KEY = "41ac7a76-ce1e-4fb5-a372-23b9cbef5a73"; // 替换为真实API密钥
    private static final String MODEL_ID = "doubao-1-5-lite-32k-250115";

    // 测试用题目信息
    private static final String TEST_TITLE = "两数之和";
    private static final String TEST_CONTENT = "给定一个整数数组 nums 和一个整数目标值 target，请你在该数组中找出 和为目标值 target 的那两个整数，并返回它们的数组下标。\n" +
            "        你可以假设每种输入只会对应一个答案。但是，数组中同一个元素在答案里不能重复出现。\n" +
            "        你可以按任意顺序返回答案。";

    private static final String TEST_LANGUAGE = "Java";
    private static final Long TEST_QUESTION_ID = 1L;

    public static void main(String[] args) {
        // 1. 初始化ARK服务
        ArkService arkService = ArkService.builder()
                .apiKey(API_KEY)
                .build();

        // 2. 创建AI管理器实例
        AiManager aiManager = new AiManager();
        aiManager.setAiService(arkService); // 需要给AiManager添加set方法

        // 3. 执行测试
        testNormalCase(aiManager);
        testEdgeCase(aiManager);
    }

    private static void testNormalCase(AiManager aiManager) {
        System.out.println("============ 正常情况测试 ============");
        try {
            AiQuestionVO result = aiManager.getGenResultByDeepSeek(
                    TEST_TITLE,
                    TEST_CONTENT,
                    TEST_LANGUAGE,
                    TEST_QUESTION_ID
            );

            System.out.println("✅ 测试通过 - 成功获取响应");
            System.out.println("\n生成的代码分析：");
            System.out.println(result.getResult());
            System.out.println("\n生成的代码：");
            System.out.println(result.getCode());
        } catch (Exception e) {
            System.err.println("❌ 测试失败：" + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("==================================\n");
    }

    private static void testEdgeCase(AiManager aiManager) {
        System.out.println("============ 边界情况测试 ============");
        try {
            // 测试空内容
            AiQuestionVO emptyResult = aiManager.getGenResultByDeepSeek(
                    "",
                    "",
                    "Java",
                    2L
            );
            System.out.println("空输入测试结果：\n" + emptyResult);
        } catch (Exception e) {
            System.out.println("✅ 预期内的异常处理成功：" + e.getMessage());
        }
        System.out.println("==================================\n");
    }
}