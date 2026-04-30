package com.lk.aizerocodeplatform.ai.model.message;

import dev.langchain4j.service.tool.ToolExecution;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/30 11:40
 * 工具调用结果消息包装类
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
public class ToolCallingResultMessage extends StreamMessage {
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
    /**
     * 工具执行结果
     */
    private String result;

    public ToolCallingResultMessage(ToolExecution toolExecution) {
        super(StreamMessageTypeEnum.TOOL_EXECUTED.getValue());
        this.id = toolExecution.request().id();
        this.name = toolExecution.request().name();
        this.arguments = toolExecution.request().arguments();
        this.result = toolExecution.result();
    }
}
