package com.wen.oj.judge.codesandbox;

import com.wen.oj.judge.codesandbox.model.ExecuteCodeRequest;
import com.wen.oj.judge.codesandbox.model.ExecuteCodeResponse;
import com.wen.oj.model.enums.QuestionsSubmitLanguageEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


@SpringBootTest
class CodeSandboxTest {
    @Value("${codesandbox.type:example}")
    private String type;

//    @Test
    void executeCode() {
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        String code = "int main(){}";
        String language = QuestionsSubmitLanguageEnum.JAVA.getValue();
        List<String> inputList = Arrays.asList("1 2", "3");
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        Assertions.assertNotNull(executeCodeResponse);
    }

//    @Test
    void executeCodeByValue() {
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        String code = "int main() {}";
        String language = QuestionsSubmitLanguageEnum.JAVA.getValue();
        List<String> inputList = Arrays.asList("1 2", "3 4");
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        Assertions.assertNotNull(executeCodeResponse);
    }

    @Test
    void executeCodeByProxy() {
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        codeSandbox=new CodeSandboxProxy(codeSandbox);
        String code = "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        int a = Integer.parseInt(args[0]);\n" +
                "        int b = Integer.parseInt(args[1]);\n" +
                "        System.out.println(\"结果:\" + (a + b));\n" +
                "    }\n" +
                "}\n";
        String language = QuestionsSubmitLanguageEnum.JAVA.getValue();
        List<String> inputList = Arrays.asList("1 2", "3");
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        Assertions.assertNotNull(executeCodeResponse);
    }

    @Test
    void executeCodeByProxy2() {
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        codeSandbox = new CodeSandboxProxy(codeSandbox);
        String code = "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        if (args.length < 2) {\n" +
                "            System.out.println(\"输入参数不足，请提供至少两个整数。\");\n" +
                "            return;\n" +
                "        }\n" +
                "        try {\n" +
                "            int a = Integer.parseInt(args[0]);\n" +
                "            int b = Integer.parseInt(args[1]);\n" +
                "            System.out.println(\"结果:\" + (a + b));\n" +
                "        } catch (NumberFormatException e) {\n" +
                "            System.out.println(\"输入不是有效的整数，请检查输入。\");\n" +
                "        }\n" +
                "    }\n" +
                "}\n";
        String language = QuestionsSubmitLanguageEnum.JAVA.getValue();
        List<String> inputList = Arrays.asList("1 2", "3");
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        Assertions.assertNotNull(executeCodeResponse);
    }

    @Test
    void executeCodeByProxy1() {
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        codeSandbox=new CodeSandboxProxy(codeSandbox);
        String code = "#include <iostream>\n" +
                "#include <cstdlib> // 用于 atoi\n" +
                "#include <vector>\n" +
                "#include <algorithm>\n" +
                "\n" +
                "using namespace std;\n" +
                "\n" +
                "int trap(vector<int>& height) {\n" +
                "    if (height.empty()) return 0;\n" +
                "    \n" +
                "    int left = 0, right = height.size() - 1;\n" +
                "    int left_max = height[left], right_max = height[right];\n" +
                "    int result = 0;\n" +
                "    \n" +
                "    while (left < right) {\n" +
                "        if (left_max < right_max) {\n" +
                "            left++;\n" +
                "            if (height[left] < left_max) {\n" +
                "                result += left_max - height[left];\n" +
                "            } else {\n" +
                "                left_max = height[left];\n" +
                "            }\n" +
                "        } else {\n" +
                "            right--;\n" +
                "            if (height[right] < right_max) {\n" +
                "                result += right_max - height[right];\n" +
                "            } else {\n" +
                "                right_max = height[right];\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "    \n" +
                "    return result;\n" +
                "}\n" +
                "\n" +
                "int main(int argc, char* argv[]) {\n" +
                "    vector<int> height;\n" +
                "    for (int i = 1; i < argc; i++) {\n" +
                "        height.push_back(atoi(argv[i]));\n" +
                "    }\n" +
                "    cout << trap(height) << endl;\n" +
                "    return 0;\n" +
                "}";
        String language = QuestionsSubmitLanguageEnum.CPLUSPLUS.getValue();
        List<String> inputList = Arrays.asList("4 2 0 3 2 5", "9");
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        Assertions.assertNotNull(executeCodeResponse);
    }
    @Test
    void executeCodeByProxy4() {
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        codeSandbox=new CodeSandboxProxy(codeSandbox);
        String code = "#include <iostream>\n" +
                "#include <cstdlib> // 用于 atoi\n" +
                "\n" +
                "int main(int argc, char* argv[]) {\n" +
                "    // 解析参数\n" +
                "    int a = std::atoi(argv[1]);\n" +
                "    int b = std::atoi(argv[2]);\n" +
                "    std::cout << a + b << std::endl;\n" +
                "    return 0;\n" +
                "}";
        String language = QuestionsSubmitLanguageEnum.CPLUSPLUS.getValue();
        List<String> inputList = Arrays.asList("4 2", "6");
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        Assertions.assertNotNull(executeCodeResponse);
    }
    @Test
    void executeCodeByProxy3() {
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        codeSandbox=new CodeSandboxProxy(codeSandbox);
        String code = "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        int[] height = new int[args.length];\n" +
                "        for (int i = 0; i < args.length; i++) {\n" +
                "            height[i] = Integer.parseInt(args[i]);\n" +
                "        }\n" +
                "        System.out.println(trap(height));\n" +
                "    }\n" +
                "\n" +
                "    public static int trap(int[] height) {\n" +
                "        if (height == null || height.length == 0) {\n" +
                "            return 0;\n" +
                "        }\n" +
                "        int left = 0, right = height.length - 1;\n" +
                "        int left_max = 0, right_max = 0;\n" +
                "        int result = 0;\n" +
                "        while (left < right) {\n" +
                "            if (height[left] < height[right]) {\n" +
                "                if (height[left] >= left_max) {\n" +
                "                    left_max = height[left];\n" +
                "                } else {\n" +
                "                    result += left_max - height[left];\n" +
                "                }\n" +
                "                left++;\n" +
                "            } else {\n" +
                "                if (height[right] >= right_max) {\n" +
                "                    right_max = height[right];\n" +
                "                } else {\n" +
                "                    result += right_max - height[right];\n" +
                "                }\n" +
                "                right--;\n" +
                "            }\n" +
                "        }\n" +
                "        return result;\n" +
                "    }\n" +
                "}";
        String language = QuestionsSubmitLanguageEnum.JAVA.getValue();
        List<String> inputList = Arrays.asList("4 2 0 3 2 5", "9");
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        Assertions.assertNotNull(executeCodeResponse);
    }


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String type = scanner.next();
            CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
            String code = "int main(){}";
            String language = QuestionsSubmitLanguageEnum.JAVA.getValue();
            List<String> inputList = Arrays.asList("1 2", "3 4");
            ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                    .code(code)
                    .language(language)
                    .inputList(inputList)
                    .build();
            ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
            Assertions.assertNotNull(executeCodeResponse);
        }
    }
}