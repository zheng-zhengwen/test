package com.wen.oj.model.dto.question;


import lombok.Data;

/**
 * 题目用例
 * */
@Data
public class JudgeCase {
    /**
     * 题目用例
     * */
    private String input;
    /**
     * 输出用例
     * */
    private String output;
}
