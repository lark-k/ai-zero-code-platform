package com.lk.aizerocodeplatform.langgraph4j.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;


/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/5/5 23:29
 * AI图片收集服务
 */
public interface ImageCollectionService {
    /**
     * 根据用户提示词收集所需的图片资源
     * AI 会根据需求自主选择调用相应的工具
     */
    @SystemMessage(fromResource = "prompts/image-collection-system-prompt.txt")
    String collectImages(@UserMessage String userPrompt);
}
