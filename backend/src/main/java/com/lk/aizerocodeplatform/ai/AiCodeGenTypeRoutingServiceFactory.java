package com.lk.aizerocodeplatform.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/5/3 17:49
 * ai代码生成类型路由服务工厂
 */
@Configuration
public class AiCodeGenTypeRoutingServiceFactory {
    @Resource
    private ChatModel chatModel;

    @Bean
    public AiCodeGenTypeRoutingService genTypeRoutingService() {
        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                .chatModel(chatModel)
                .build();
    }
}
