package com.lk.aizerocodeplatform.saver;

import com.lk.aizerocodeplatform.ai.model.MultiFileCodeResult;
import com.lk.aizerocodeplatform.enums.CodeGenTypeEnum;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/23 23:05
 * 多文件模式代码文件保存模板
 */
public class MultiCodeFileSaveTemplate extends CodeFileSaveTemplate<MultiFileCodeResult> {
    @Override
    protected CodeGenTypeEnum getCodeGenType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }

    @Override
    protected void saveFiles(String uniqueDir, MultiFileCodeResult result) {
        writeFile(uniqueDir,"index.html", result.getHtmlCode());
        writeFile(uniqueDir,"style.css",result.getCssCode());
        writeFile(uniqueDir,"script.js",result.getJsCode());
    }
}
