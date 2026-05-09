package com.lk.aizerocodeplatform.langgraph4j.ai;

import com.lk.aizerocodeplatform.tools.SpringContextUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/5/6 22:08
 */
@Configuration
public class ImageCollectionPlanServiceFactory {

//    @Resource
//    private ChatModel chatModel;

    @Scope("prototype")
    @Bean
    public ImageCollectionPlanService createImageCollectionPlanService() {
        ChatModel chatModel = SpringContextUtil.getBean("chatModel", ChatModel.class);
        return AiServices.builder(ImageCollectionPlanService.class)
                .chatModel(chatModel)
                .build();
    }
}

