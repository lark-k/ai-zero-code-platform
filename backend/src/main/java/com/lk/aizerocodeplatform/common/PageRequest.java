package com.lk.aizerocodeplatform.common;

import lombok.Data;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/20 18:06
 * 分页请求公用字段
 */
@Data
public class PageRequest {

    /**
     * 当前页号
     */
    private int pageNum = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认降序）
     */
    private String sortOrder = "descend";
}

