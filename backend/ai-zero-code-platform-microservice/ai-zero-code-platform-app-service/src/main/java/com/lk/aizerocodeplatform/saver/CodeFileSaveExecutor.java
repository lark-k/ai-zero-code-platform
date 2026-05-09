package com.lk.aizerocodeplatform.saver;

import com.lk.aizerocodeplatform.ai.model.HtmlCodeResult;
import com.lk.aizerocodeplatform.ai.model.MultiFileCodeResult;
import com.lk.aizerocodeplatform.enums.CodeGenTypeEnum;
import com.lk.aizerocodeplatform.exception.BusinessException;
import com.lk.aizerocodeplatform.exception.ErrorCode;

import java.io.File;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/23 23:07
 * 代码文件保存执行器
 */
public class CodeFileSaveExecutor {
    private static final HtmlCodeFileSaveTemplate HTML_CODE_FILE_SAVE_TEMPLATE = new HtmlCodeFileSaveTemplate();
    private static final MultiCodeFileSaveTemplate MULTI_CODE_FILE_SAVE_TEMPLATE = new MultiCodeFileSaveTemplate();

    /**
     * 根据CodeGenTypeEnum类型执行不同的文件保存
     *
     * @param result          需要保存文件的类型
     * @param codeGenTypeEnum 生成的代码类型
     * @param appId 应用id
     * @return 文件路径
     */
    public static File executeCodeFileSave(Object result, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        return switch (codeGenTypeEnum) {
            case HTML -> HTML_CODE_FILE_SAVE_TEMPLATE.saveCode((HtmlCodeResult) result, appId);
            case MULTI_FILE -> MULTI_CODE_FILE_SAVE_TEMPLATE.saveCode((MultiFileCodeResult) result, appId);
            default ->
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "该文件类型不能被保存 + " + codeGenTypeEnum.getValue());
        };
    }
}
