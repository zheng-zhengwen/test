package com.wen.oj.common;

import java.io.Serializable;
import lombok.Data;

/**
 * 删除请求
 *
 * @author <a href="https://github.com/zheng-zhengwen">程序员阿文</a>
 * @from <a href="https://wen.icu">在线编程系统</a>
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}