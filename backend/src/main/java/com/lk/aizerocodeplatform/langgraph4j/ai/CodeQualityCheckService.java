package com.lk.aizerocodeplatform.langgraph4j.ai;

import com.lk.aizerocodeplatform.langgraph4j.model.QualityResult;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/5/6 16:36
 * 代码质量检查AI服务接口
 */
public interface CodeQualityCheckService {

    /**
     * 检查代码质量
     * AI 会分析代码并返回质量检查结果
     */
    @SystemMessage(fromResource = "prompts/code-quality-check-system-prompt.txt")
    QualityResult checkCodeQuality(@UserMessage String codeContent);
}

