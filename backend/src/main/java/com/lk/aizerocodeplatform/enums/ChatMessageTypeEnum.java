package com.lk.aizerocodeplatform.enums;

import lombok.Getter;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/28 13:36
 * 对话消息类型枚举
 */
@Getter
public enum ChatMessageTypeEnum {
    AI("AI", "ai"),
    User("用户", "user");
    private final String text;
    private final String value;

    ChatMessageTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value得到枚举类型
     *
     * @param value
     * @return
     */
    public static ChatMessageTypeEnum getEnumByValue(String value) {
        for (ChatMessageTypeEnum chatMessageTypeEnum : ChatMessageTypeEnum.values()) {
            if (chatMessageTypeEnum.getValue().equals(value)) {
                return chatMessageTypeEnum;
            }
        }
        return null;
    }
}
