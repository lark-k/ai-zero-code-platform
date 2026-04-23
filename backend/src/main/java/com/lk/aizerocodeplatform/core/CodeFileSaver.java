package com.lk.aizerocodeplatform.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.lk.aizerocodeplatform.ai.model.HtmlCodeResult;
import com.lk.aizerocodeplatform.ai.model.MultiFileCodeResult;
import com.lk.aizerocodeplatform.constant.CodeFileSaveConstant;
import com.lk.aizerocodeplatform.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/23 15:31
 * 保存代码文件
 */
public class CodeFileSaver {

    /**
     * 根据雪花算法+codeGenType获取到唯一的保存路径
     *
     * @param codeGenType 生成代码的方式
     * @return 唯一的保存路径
     */
    private static String getUniqueDir(String codeGenType) {
        // 保存路径：/temp/code_output/雪花算法_codeGenType
        return CodeFileSaveConstant.ROOT_PATH + File.separator + IdUtil.getSnowflakeNextIdStr() + "_" + codeGenType;
    }

    /**
     * 保存单个文件
     *
     * @param fileDir  文件路径
     * @param filename 文件名
     * @param content  文件内容
     */
    private static void writeFile(String fileDir, String filename, String content) {
        String filePath = fileDir + File.separator + filename;
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }

    /**
     * 保存单HTML模式
     *
     * @param htmlCodeResult 单HTML模式下ai生成的结果对象
     * @return 文件
     */
    public static File saveHtml(HtmlCodeResult htmlCodeResult) {
        String uniqueDir = getUniqueDir(CodeGenTypeEnum.HTML.getValue());
        writeFile(uniqueDir, "index.html", htmlCodeResult.getHtmlCode());
        return new File(uniqueDir);
    }

    public static File saveMultiFile(MultiFileCodeResult multiFileCodeResult) {
        String uniqueDir = getUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        writeFile(uniqueDir, "index.html", multiFileCodeResult.getHtmlCode());
        writeFile(uniqueDir, "style.css", multiFileCodeResult.getCssCode());
        writeFile(uniqueDir, "script.js", multiFileCodeResult.getJsCode());
        return new File(uniqueDir);
    }
}
