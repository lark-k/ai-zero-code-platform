package com.lk.aizerocodeplatform.enums;

import lombok.Getter;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/23 15:26
 * 代码生成类型枚举
 */
@Getter
public enum CodeGenTypeEnum {
    HTML("原生 HTML 模式", "html"),
    MULTI_FILE("原生多文件模式", "multi_file"),
    ;
    private final String text;
    private final String value;

    CodeGenTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value值获取对应的枚举
     */
    public static CodeGenTypeEnum getByValue(String value) {
        for (CodeGenTypeEnum codeGenTypeEnum : CodeGenTypeEnum.values()) {
            if (codeGenTypeEnum.getValue().equals(value)) {
                return codeGenTypeEnum;
            }
        }
        return null;
    }
}
