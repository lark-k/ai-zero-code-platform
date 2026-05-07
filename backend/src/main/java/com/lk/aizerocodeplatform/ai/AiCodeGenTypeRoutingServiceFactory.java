package com.lk.aizerocodeplatform.ai;

import com.lk.aizerocodeplatform.tools.SpringContextUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/5/3 17:49
 * ai代码生成类型路由服务工厂
 */
@Configuration
public class AiCodeGenTypeRoutingServiceFactory {
//    @Resource
//    private ChatModel chatModel;

    @Scope("prototype")
    @Bean
    public AiCodeGenTypeRoutingService createAiCodeGenTypeRoutingService() {
        // 动态获取多例的路由 ChatModel，支持并发
        ChatModel chatModel = SpringContextUtil.getBean("chatModel", ChatModel.class);
        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                .chatModel(chatModel)
                .build();
    }

    @Bean
    public AiCodeGenTypeRoutingService genTypeRoutingService() {
        return createAiCodeGenTypeRoutingService();
    }
}
