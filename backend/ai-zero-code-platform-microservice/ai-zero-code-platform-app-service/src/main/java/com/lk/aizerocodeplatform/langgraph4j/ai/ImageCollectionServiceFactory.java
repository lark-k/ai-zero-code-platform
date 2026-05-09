package com.lk.aizerocodeplatform.langgraph4j.ai;

import com.lk.aizerocodeplatform.langgraph4j.tools.ImageSearchTool;
import com.lk.aizerocodeplatform.langgraph4j.tools.LogoGeneratorTool;
import com.lk.aizerocodeplatform.langgraph4j.tools.MermaidDiagramTool;
import com.lk.aizerocodeplatform.langgraph4j.tools.UndrawIllustrationTool;
import com.lk.aizerocodeplatform.tools.SpringContextUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/5/5 23:32
 * AI图片收集服务工厂
 */
@Slf4j
@Configuration
public class ImageCollectionServiceFactory {

//    @Resource
//    private ChatModel chatModel;

    @Resource
    private ImageSearchTool imageSearchTool;

    @Resource
    private UndrawIllustrationTool undrawIllustrationTool;

    @Resource
    private MermaidDiagramTool mermaidDiagramTool;

    @Resource
    private LogoGeneratorTool logoGeneratorTool;

    /**
     * 创建图片收集 AI 服务
     */
    @Scope("prototype")
    @Bean
    public ImageCollectionService createImageCollectionService() {
        ChatModel chatModel = SpringContextUtil.getBean("chatModel", ChatModel.class);
        return AiServices.builder(ImageCollectionService.class)
                .chatModel(chatModel)
                .tools(
                        imageSearchTool,
                        undrawIllustrationTool,
                        mermaidDiagramTool,
                        logoGeneratorTool
                )
                .build();
    }
}

