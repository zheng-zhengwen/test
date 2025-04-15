# 数据库初始化
# @author <a href="">程序员阿文</a>
# @from <a href="">在线编程系统</a>


-- 切换库
-- 切换库
USE my_db;

-- 插入回文数题目
INSERT INTO question (id, title, content, tags, answer, submitNum, acceptedNum, judgeCase, judgeConfig, thumbNum, favourNum, userId, createTime, updateTime, isDelete)
VALUES (
           1908389577153937409,
           '回文数',
           '# 1. 回文数\n\n## 题目描述\n\n给你一个整数 x ，如果 x 是一个回文整数，返回 true ；否则，返回 false 。\n\n回文数是指正序（从左向右）和倒序（从右向左）读都是一样的整数。\n\n## 示例\n\n**示例 1：**\n```python\n输入：x = 121\n输出：true\n```\n\n**示例 2：**\n```python\n输入：x = -121\n输出：false\n解释：从左向右读, 为 -121 。 从右向左读, 为 121- 。因此它不是一个回文数。\n```\n\n**示例 3：**\n```python\n输入：x = 10\n输出：false\n解释：从右向左读, 为 01 。因此它不是一个回文数。\n```\n\n## 提示\n\n- `-2^31 <= x <= 2^31 - 1`\n',
           '["数组","哈希表"]',
           'public class Main {\n   \n        public static void main(String[] args) {\n         \n            try {\n                int x = Integer.parseInt(args[0]);\n\n                // 特殊情况处理\n                if (x < 0 || (x % 10 == 0 && x != 0)) {\n                    System.out.println("false");\n                    return;\n                }\n\n                int revertedNumber = 0;\n                int original = x; // 保存原始值\n\n                while (x > revertedNumber) {\n                    revertedNumber = revertedNumber * 10 + x % 10;\n                    x /= 10;\n                }\n\n                // 判断是否为回文数\n                if (x == revertedNumber || x == revertedNumber / 10) {\n                    System.out.println("true");\n                } else {\n                    System.out.println("false");\n                }\n\n            } catch (NumberFormatException e) {\n                System.out.println("请输入有效的整数");\n            }\n        }\n}',
           32,
           11,
           '[{"input":"121","output":"true"}]',
           '{"timeLimit":1000,"memoryLimit":1000,"stackLimit":999}',
           0,
           0,
           1911398561732931586,
           '2025-04-05 13:21:46',
           '2025-04-11 20:26:22',
           0
       );

-- 插入整数反转题目
INSERT INTO question (id, title, content, tags, answer, submitNum, acceptedNum, judgeCase, judgeConfig, thumbNum, favourNum, userId, createTime, updateTime, isDelete)
VALUES (
           1908506063495335937,
           '整数反转',
           '# 7. 整数反转\n\n## 题目描述\n\n给你一个 32 位的有符号整数 `x`，返回将 `x` 中的数字部分反转后的结果。\n\n### 约束条件\n- 如果反转后整数超过 32 位有符号整数的范围 `[-2³¹, 2³¹ - 1]`，则返回 0\n- 假设环境不允许存储 64 位整数（有符号或无符号）\n\n## 示例\n\n| 示例 | 输入 | 输出 | 说明 |\n|------|------|------|------|\n| 1 | `x = 123` | `321` | 正数反转 |\n| 2 | `x = -123` | `-321` | 负数反转 |\n| 3 | `x = 120` | `21` | 末尾有0的情况 |\n| 4 | `x = 0` | `0` | 输入为0 |\n\n```',
           '["数学"]',
           'class Solution {\n\n        public int reverse(int x) {\n            int rev = 0;\n            while (x != 0) {\n                int pop = x % 10;\n                x /= 10;\n                // 正数溢出检查\n                if (rev > Integer.MAX_VALUE/10 || \n                    (rev == Integer.MAX_VALUE/10 && pop > 7)) {\n                    return 0;\n                }\n                // 负数溢出检查\n                if (rev < Integer.MIN_VALUE/10 || \n                    (rev == Integer.MIN_VALUE/10 && pop < -8)) {\n                    return 0;\n                }\n                rev = rev * 10 + pop;\n            }\n            return rev;\n        }\n}',
           3,
           2,
           '[{"input":"123","output":"321"}]',
           '{"timeLimit":1000,"memoryLimit":1000,"stackLimit":1000}',
           0,
           0,
           1911398561732931586,
           '2025-04-05 21:04:38',
           '2025-04-05 21:04:38',
           0
       );

-- 插入 A+B 两数之和题目
INSERT INTO question (id, title, content, tags, answer, submitNum, acceptedNum, judgeCase, judgeConfig, thumbNum, favourNum, userId, createTime, updateTime, isDelete)
VALUES (
           1910700016080044033,
           'A+B两数之和',
           '# 判题条件\n\n给定一个整数数组 nums 和一个整数目标值 target，请你在该数组中找出 和为目标值 target 的那 两个 整数，并返回它们的数组下标。\n\n你可以假设每种输入只会对应一个答案，并且你不能使用两次相同的元素。\n\n你可以按任意顺序返回答案。',
           '["数组"]',
           '# 哈希表写法解析\n\n## 问题描述\n\n**问**：为什么下面的代码，要先查询 `idx` 是否有 `target − nums[j]`，再把 `nums[j]` 和 `j` 加到 `idx` 中？能不能反过来？\n\n**答**：反过来写是错误的。例如 `nums = [2,3,1]`，`target = 4`，  \n如果先把 `nums[j]` 和 `j` 加到 `idx` 中，我们会认为 `2 + 2 = 4`，返回 `[0, 0]`，  \n但正确答案应该是 `3 + 1 = 4`，即返回 `[1, 2]`。\n\n## 原因分析\n\n题目要求「**不能使用两次相同的元素**」，也就是两个数的下标必须不同。\n\n我们的做法是枚举右边的数的下标 `j`，去找左边的数的下标 `i`。  \n由于找的是**左边的数**，如果**先把右边的数加到 `idx` 中**，  \n那么找到的数就可能包含右边的数本身，这就**不符合题意**了。\n\n---\n\n## Java 代码实现\n\n```java\nimport java.util.*;\n\npublic class Main {\n    public static void main(String[] args) {\n        int[] nums = Arrays.stream(args[0].split(",")).mapToInt(Integer::parseInt).toArray();\n        int target = Integer.parseInt(args[1]);\n\n        Map<Integer, Integer> idx = new HashMap<>();\n        for (int j = 0; j < nums.length; j++) {\n            int x = nums[j];\n            if (idx.containsKey(target - x)) {\n                System.out.printf("%d %d\n", idx.get(target - x), j);\n                return;\n            }\n            idx.put(x, j);\n        }\n        System.out.println("No solution");\n    }\n}\n```',
           29,
           7,
           '[{"input":"2 3 4","output":"9"}]',
           '{"timeLimit":1000,"memoryLimit":1000,"stackLimit":1000}',
           0,
           0,
           1911398561732931586,
           '2025-04-11 22:22:37',
           '2025-04-12 00:28:37',
           0
       );

-- 插入两数相加题目
INSERT INTO question (id, title, content, tags, answer, submitNum, acceptedNum, judgeCase, judgeConfig, thumbNum, favourNum, userId, createTime, updateTime, isDelete)
VALUES (
           1910736175523024897,
           '两数相加',
           '### 题目描述\n\n给你两个 **非空** 的链表，表示两个非负的整数。它们每位数字都是按照 **逆序** 的方式存储的，并且每个节点只能存储 **一位** 数字。\n\n请你将两个数相加，并以相同形式返回一个表示和的链表。\n\n你可以假设，除了数字 0 之外，这两个数都不会以 0 开头。\n\n---\n\n### 示例\n\n#### 示例 1：\n\n```\n输入：l1 = [2,4,3], l2 = [5,6,4]\n输出：[7,0,8]\n解释：342 + 465 = 807.\n```\n\n#### 示例 2：\n\n```\n输入：l1 = [0], l2 = [0]\n输出：[0]\n```\n\n#### 示例 3：\n\n```\n输入：l1 = [9,9,9,9,9,9,9], l2 = [9,9,9,9]\n输出：[8,9,9,9,0,0,0,1]\n```\n\n',
           '["链表","数学"]',
           'import java.util.*;\n\npublic class Main {\n\n    static class ListNode {\n        int val;\n        ListNode next;\n        ListNode(int val) {\n            this.val = val;\n        }\n    }\n\n    public static void main(String[] args) {\n        // 示例输入：l1=[2,4,3], l2=[5,6,4]\n        if (args.length < 2) {\n            System.out.println("请提供两个链表参数，例如：l1=[2,4,3] l2=[5,6,4]");\n            return;\n        }\n\n        ListNode l1 = parseInput(args[0]);\n        ListNode l2 = parseInput(args[1]);\n\n        ListNode result = addTwoNumbers(l1, l2);\n        printList(result);  // 输出结果\n    }\n\n    public static ListNode addTwoNumbers(ListNode l1, ListNode l2) {\n        ListNode head = null, tail = null;\n        int carry = 0;\n        while (l1 != null || l2 != null) {\n            int n1 = l1 != null ? l1.val : 0;\n            int n2 = l2 != null ? l2.val : 0;\n            int sum = n1 + n2 + carry;\n            if (head == null) {\n                head = tail = new ListNode(sum % 10);\n            } else {\n                tail.next = new ListNode(sum % 10);\n                tail = tail.next;\n            }\n            carry = sum / 10;\n            if (l1 != null) l1 = l1.next;\n            if (l2 != null) l2 = l2.next;\n        }\n        if (carry > 0) {\n            tail.next = new ListNode(carry);\n        }\n        return head;\n    }\n\n    // 工具方法：从字符串解析链表，例如 "l1=[2,4,3]"\n    private static ListNode parseInput(String input) {\n        // 提取中括号内的部分\n        int start = input.indexOf('[');\n        int end = input.indexOf(']');\n        if (start == -1 || end == -1) return null;\n        String[] parts = input.substring(start + 1, end).split(",");\n        ListNode dummy = new ListNode(0);\n        ListNode curr = dummy;\n        for (String part : parts) {\n            curr.next = new ListNode(Integer.parseInt(part.trim()));\n            curr = curr.next;\n        }\n        return dummy.next;\n    }\n\n    // 工具方法：打印链表\n    private static void printList(ListNode node) {\n        List<Integer> list = new ArrayList<>();\n        while (node != null) {\n            list.add(node.val);\n            node = node.next;\n        }\n        System.out.println(list);\n    }\n}\n',
           11,
           2,
           '[{"input":"[2,4,3] [5,6,4]","output":"[7, 0, 8]"},{"input":"[9,9,9,9,9,9,9] [9,9,9,9]","output":"[8,9,9,9,0, 0, 0, 1]"}]',
           '{"timeLimit":1000,"memoryLimit":1000,"stackLimit":1000}',
           0,
           0,
           1911398561732931586,
           '2025-04-12 00:46:18',
           '2025-04-13 07:48:50',
           0
       );

-- 插入二进制求和题目
INSERT INTO question (id, title, content, tags, answer, submitNum, acceptedNum, judgeCase, judgeConfig, thumbNum, favourNum, userId, createTime, updateTime, isDelete)
VALUES (
           1909249099711463426,
           '二进制求和',
           '# 67. 二进制求和\n\n## 题目描述\n\n给你两个二进制字符串 `a` 和 `b`，以二进制字符串的形式返回它们的和。\n\n## 示例\n\n**示例 1：**\n\n```\n输入: a = "11", b = "1"\n输出："100"\n```\n\n**示例 2：**\n\n```\n输入：a = "1010", b = "1011"\n输出："10101"\n```\n\n## 提示\n\n- `1 <= a.length, b.length <= 10^4`\n- `a` 和 `b` 仅由字符 `'0'` 或 `'1'` 组成\n- 字符串如果不是 `"0"`, 就不含前导零\n',
           '["位运算","数字"]',
           'public class Main {\n    \n      public static void main(String[] args) {\n        System.out.println(addBinary(args[0], args[1]));\n    }\n    \n      public static String addBinary(String a, String b) {\n        StringBuilder sb = new StringBuilder();\n        int i = a.length()-1, j = b.length()-1, carry = 0;\n        while (i >= 0 || j >= 0 || carry > 0) {\n            int sum = carry;\n            if (i >= 0) sum += a.charAt(i--) - '0';\n            if (j >= 0)