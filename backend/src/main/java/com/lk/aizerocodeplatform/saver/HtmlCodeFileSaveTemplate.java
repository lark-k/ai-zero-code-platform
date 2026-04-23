package com.lk.aizerocodeplatform.saver;

import com.lk.aizerocodeplatform.ai.model.HtmlCodeResult;
import com.lk.aizerocodeplatform.enums.CodeGenTypeEnum;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/23 23:03
 * Html模式代码文件保存模板
 */
public class HtmlCodeFileSaveTemplate extends CodeFileSaveTemplate<HtmlCodeResult> {
    @Override
    protected CodeGenTypeEnum getCodeGenType() {
        return CodeGenTypeEnum.HTML;
    }

    @Override
    protected void saveFiles(String uniqueDir, HtmlCodeResult result) {
        writeFile(uniqueDir,"index.html", result.getHtmlCode());
    }
}
