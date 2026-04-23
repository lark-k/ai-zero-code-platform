package com.lk.aizerocodeplatform.parser;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/23 21:04
 * 代码解析器策略接口
 */
public interface CodeParser <T> {
    /**
     * 不同的解析器返回不同的类型，返回值使用泛型
     * @param codeContent AI生成的代码内容
     */
    T parseCode(String codeContent);
}
