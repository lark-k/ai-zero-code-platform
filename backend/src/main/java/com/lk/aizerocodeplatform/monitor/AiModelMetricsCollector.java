package com.lk.aizerocodeplatform.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/5/9 12:37
 * 监控指标收集器，用于收集指定的内容到普罗米修斯中  Prometheus
 */
@Component
@Slf4j
public class AiModelMetricsCollector {

    @Resource
    private MeterRegistry meterRegistry;

    // 缓存已创建的指标，避免重复创建（按指标类型分离缓存）   使用支持并发的ConcurrentHashMap
    private final ConcurrentMap<String, Counter> requestCountersCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> errorCountersCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> tokenCountersCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Timer> responseTimersCache = new ConcurrentHashMap<>();

    /**
     * 记录请求次数
     */
    public void recordRequest(String userId, String appId, String modelName, String status) {
        String key = String.format("%s_%s_%s_%s", userId, appId, modelName, status);
        Counter counter = requestCountersCache.computeIfAbsent(key, k ->
                Counter.builder("ai_model_requests_total")
                        .description("AI模型总请求次数")
                        .tag("user_id", userId)
                        .tag("app_id", appId)
                        .tag("model_name", modelName)
                        .tag("status", status)
                        .register(meterRegistry)
        );
        counter.increment();
    }

    /**
     * 记录错误
     */
    public void recordError(String userId, String appId, String modelName, String errorMessage) {
        String key = String.format("%s_%s_%s_%s", userId, appId, modelName, errorMessage);
        Counter counter = errorCountersCache.computeIfAbsent(key, k ->
                Counter.builder("ai_model_errors_total")
                        .description("AI模型错误次数")
                        .tag("user_id", userId)
                        .tag("app_id", appId)
                        .tag("model_name", modelName)
                        .tag("error_message", errorMessage)
                        .register(meterRegistry)
        );
        counter.increment();
    }

    /**
     * 记录Token消耗
     */
    public void recordTokenUsage(String userId, String appId, String modelName,
                                 String tokenType, long tokenCount) {
        String key = String.format("%s_%s_%s_%s", userId, appId, modelName, tokenType);
        Counter counter = tokenCountersCache.computeIfAbsent(key, k ->
                Counter.builder("ai_model_tokens_total")
                        .description("AI模型Token消耗总数")
                        .tag("user_id", userId)
                        .tag("app_id", appId)
                        .tag("model_name", modelName)
                        .tag("token_type", tokenType)
                        .register(meterRegistry)
        );
        counter.increment(tokenCount);
    }

    /**
     * 记录响应时间
     */
    public void recordResponseTime(String userId, String appId, String modelName, Duration duration) {
        String key = String.format("%s_%s_%s", userId, appId, modelName);
        Timer timer = responseTimersCache.computeIfAbsent(key, k ->
                Timer.builder("ai_model_response_duration_seconds")
                        .description("AI模型响应时间")
                        .tag("user_id", userId)
                        .tag("app_id", appId)
                        .tag("model_name", modelName)
                        .register(meterRegistry)
        );
        timer.record(duration);
    }
}

