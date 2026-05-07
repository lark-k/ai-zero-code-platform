package com.lk.aizerocodeplatform.langgraph4j.ai;

import com.lk.aizerocodeplatform.tools.SpringContextUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/5/6 16:37
 * 代码质量检查服务工厂
 */
@Slf4j
@Configuration
public class CodeQualityCheckServiceFactory {

//    @Resource
//    private ChatModel chatModel;

    /**
     * 创建代码质量检查 AI 服务
     */
    @Scope("prototype")
    @Bean
    public CodeQualityCheckService createCodeQualityCheckService() {
        ChatModel chatModel = SpringContextUtil.getBean("chatModel", ChatModel.class);
        return AiServices.builder(CodeQualityCheckService.class)
                .chatModel(chatModel)
                .build();
    }
}

