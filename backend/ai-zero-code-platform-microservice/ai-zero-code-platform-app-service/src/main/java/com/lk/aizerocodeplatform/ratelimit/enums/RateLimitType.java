package com.lk.aizerocodeplatform.ratelimit.enums;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/5/7 21:13
 * 限流类型
 */
public enum RateLimitType {

    /**
     * 接口级别限流
     */
    API,

    /**
     * 用户级别限流
     */
    USER,

    /**
     * IP级别限流
     */
    IP
}

