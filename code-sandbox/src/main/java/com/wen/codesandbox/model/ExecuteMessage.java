package com.wen.codesandbox.model;

import lombok.Data;

/**
 * 进程执行信息
 */
@Data
public class ExecuteMessage {
    //错误码
    private Integer exitValue;

    //控制台正确信息
    private String message;

    //控制台错误信息
    private String errorMessage;

    //程序消耗时间
    private Long time;

    //程序消耗内存
    private Long memory;
}