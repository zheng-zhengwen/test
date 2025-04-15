package com.wen.oj.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author wen
 * @version 1.0
 **/
@Data
public class UserStatsVO implements Serializable {
    /**
     * 已解决题目数
     */
    private Integer solvedCount;

    /**
     * 提交次数
     */
    private Integer submitCount;

    /**
     * 通过率
     */
    private Double passRate;

    private static final long serialVersionUID = 1L;
}
