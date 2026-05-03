package com.lk.aizerocodeplatform.ai;

import com.lk.aizerocodeplatform.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/5/3 17:47
 * AI代码生成类型路由服务
 */
public interface AiCodeGenTypeRoutingService {
    @SystemMessage(fromResource = "prompts/code_type_routing_prompt.txt")
    CodeGenTypeEnum routeCodeGenType(String userPrompt);
}
