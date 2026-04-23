package com.lk.aizerocodeplatform.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/23 13:50
 * 创建ai代码生成服务工厂，用于初始化服务
 */
@Configuration
public class AiCodeGenServiceFactory {
    @Resource
    private ChatModel chatModel;

    @Bean
    public AiCodeGenService aiCodeGenService() {
        return AiServices.create(AiCodeGenService.class, chatModel);
    }
}
