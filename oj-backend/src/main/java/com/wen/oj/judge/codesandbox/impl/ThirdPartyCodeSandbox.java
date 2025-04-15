package com.wen.oj.judge.codesandbox.impl;

import com.wen.oj.judge.codesandbox.CodeSandbox;
import com.wen.oj.judge.codesandbox.model.ExecuteCodeRequest;
import com.wen.oj.judge.codesandbox.model.ExecuteCodeResponse;

/**
 * 示例代码沙箱
 */
public class ThirdPartyCodeSandbox implements CodeSandbox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("第三方代码沙箱");
        return null;
    }

}