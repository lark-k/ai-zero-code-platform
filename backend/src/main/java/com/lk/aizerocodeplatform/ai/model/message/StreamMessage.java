package com.lk.aizerocodeplatform.ai.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/30 11:22
 * 工具调用流式输出信息的基类
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class StreamMessage {
    /**
     * 消息类型
     */
    private String type;
}
