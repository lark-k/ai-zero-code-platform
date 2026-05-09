package com.lk.aizerocodeplatform.config;

import com.lk.aizerocodeplatform.monitor.AiModelMonitorListener;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.List;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/29 13:28
 * 配置推理流式聊天模型，可以支持更复杂的任务推理（多例）
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.reasoning-stream-chat-model")
@Data
public class ReasoningStreamChatModelConfig {
    private String baseUrl;

    private String apiKey;

    private String modelName;

    private Integer maxTokens;

    private boolean logRequests;

    private boolean logResponses;

    @Resource
    private AiModelMonitorListener aiModelMonitorListener;


    @Scope("prototype")
    @Bean
    public StreamingChatModel reasoningStreamChatModel() {
        return OpenAiStreamingChatModel
                .builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .listeners(List.of(aiModelMonitorListener))
                .build();
    }
}
