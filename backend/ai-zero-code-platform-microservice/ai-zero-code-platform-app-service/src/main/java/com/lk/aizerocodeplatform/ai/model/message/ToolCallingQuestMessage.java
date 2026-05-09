package com.lk.aizerocodeplatform.ai.model.message;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/30 11:31
 * 工具调用请求信息包装类
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class ToolCallingQuestMessage extends StreamMessage {
    /**
     * 工具id
     */
    private String id;
    /**
     * 工具名称
     */
    private String name;
    /**
     * 工具参数
     */
    private String arguments;

    public ToolCallingQuestMessage(ToolExecutionRequest toolExecutionRequest) {
        super(StreamMessageTypeEnum.TOOL_REQUEST.getValue());
        this.id = toolExecutionRequest.id();
        this.name = toolExecutionRequest.name();
        this.arguments = toolExecutionRequest.arguments();
    }
}
