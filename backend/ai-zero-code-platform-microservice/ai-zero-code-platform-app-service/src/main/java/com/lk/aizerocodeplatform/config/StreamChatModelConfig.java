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
 * @ Date 2026/5/7 13:35
 * 配置普通的流式对话模型（多例）
 */
@ConfigurationProperties(prefix = "langchain4j.open-ai.streaming-chat-model")
@Configuration
@Data
public class StreamChatModelConfig {
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
    public StreamingChatModel streamingChatModel() {
        return OpenAiStreamingChatModel.builder()
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
