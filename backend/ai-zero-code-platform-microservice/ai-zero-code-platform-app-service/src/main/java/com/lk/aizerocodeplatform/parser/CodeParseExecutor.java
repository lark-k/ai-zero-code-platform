package com.lk.aizerocodeplatform.parser;

import com.lk.aizerocodeplatform.enums.CodeGenTypeEnum;
import com.lk.aizerocodeplatform.exception.BusinessException;
import com.lk.aizerocodeplatform.exception.ErrorCode;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/23 21:18
 * 解析器的统一执行器
 */
public class CodeParseExecutor {
    // 单html模式解析器对象
    private static final HtmlCodeParser HTML_CODE_PARSER = new HtmlCodeParser();
    // 多文件模式解析器对象
    private static final MultiFileCodeParser MULTI_FILE_CODE_PARSER = new MultiFileCodeParser();

    /**
     * 执行解析器
     *
     * @param codeContent     AI生成的代码内容
     * @param codeGenTypeEnum 代码生成类型
     */
    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenTypeEnum) {
        // 调用统一的解析器执行器，根据CodeGenTypeEnum决定调用哪一个解析器
        return switch (codeGenTypeEnum) {
            case HTML -> HTML_CODE_PARSER.parseCode(codeContent);
            case MULTI_FILE -> MULTI_FILE_CODE_PARSER.parseCode(codeContent);
            default ->
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的生成类型:" + codeGenTypeEnum.getValue());
        };
    }
}
