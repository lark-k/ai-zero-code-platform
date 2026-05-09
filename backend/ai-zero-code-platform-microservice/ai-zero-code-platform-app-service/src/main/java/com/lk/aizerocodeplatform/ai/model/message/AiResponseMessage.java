package com.lk.aizerocodeplatform.ai.model.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/30 11:23
 * ai回复消息包装类
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
public class AiResponseMessage extends StreamMessage {
    /**
     * ai回复的内容
     */
    private String data;

    public AiResponseMessage(String data) {
        super(StreamMessageTypeEnum.AI_RESPONSE.getValue());
        this.data = data;
    }
}
