package com.lk.aizerocodeplatform.ai;

import com.lk.aizerocodeplatform.ai.model.HtmlCodeResult;
import com.lk.aizerocodeplatform.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/23 13:45
 * AI代码生成服务
 */
public interface AiCodeGenService {
    /**
     * 生成HTML代码
     *
     * @param userMessage 用户提示词
     * @return ai回复内容
     */
    @SystemMessage(fromResource = "prompts/single_file_system_prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 生成多文件代码
     *
     * @param userMessage 用户提示词
     * @return ai回复内容
     */
    @SystemMessage(fromResource = "prompts/multi_file_system_prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);

    /**
     * 生成HTML代码（流式）
     *
     * @param userMessage 用户提示词
     * @return ai回复内容
     */
    @SystemMessage(fromResource = "prompts/single_file_system_prompt.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);

    /**
     * 生成多文件代码（流式）
     *
     * @param userMessage 用户提示词
     * @return ai回复内容
     */
    @SystemMessage(fromResource = "prompts/multi_file_system_prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);

    /**
     * 生成Vue工程项目代码（流式）
     *
     * @param userMessage 用户提示词
     * @param appId 应用id
     * @return ai回复内容
     * MemoryId注解可以把参数当做工具上下文保存下来，在调用工具时可以通过ToolMemoryId获取；
     * 当使用了MemoryId注解时，用户发送消息的参数必须使用UserMessage注解。
     */
    @SystemMessage(fromResource = "prompts/vue_project_file_system_prompt.txt")
    TokenStream generateVueProjectCodeStream(@UserMessage String userMessage, @MemoryId Long appId);
}
