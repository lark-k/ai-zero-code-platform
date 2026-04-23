package com.lk.aizerocodeplatform.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.lk.aizerocodeplatform.constant.CodeFileSaveConstant;
import com.lk.aizerocodeplatform.enums.CodeGenTypeEnum;
import com.lk.aizerocodeplatform.exception.BusinessException;
import com.lk.aizerocodeplatform.exception.ErrorCode;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/23 22:33
 * 代码文件保存（父类模板）
 * 需要保存的文件类型不确定，所以使用泛型
 */
public abstract class CodeFileSaveTemplate<T> {
    /**
     * 保存代码文件的核心方法，定义为final不允许子类重写,子类必须遵循该实现流程
     */
    public final File saveCode(T result) {
        // 1、验证参数
        validateInput(result);
        // 2、得到唯一目录
        String uniqueDir = getUniqueDir();
        // 3、保存文件
        saveFiles(uniqueDir, result);
        // 4、返回路径
        return new File(uniqueDir);
    }

    /**
     * 校验参数是否合法（子类可以重写该方法）
     *
     * @param result 输入参数
     */
    protected void validateInput(T result) {
        if (result == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    }

    /**
     * 根据雪花算法+codeGenType获取到唯一的保存路径
     *
     * @return 唯一的保存路径
     */
    protected String getUniqueDir() {
        // 获取代码生成类型
        CodeGenTypeEnum codeGenType = getCodeGenType();
        // 保存路径：/temp/code_output/雪花算法_codeGenType
        return CodeFileSaveConstant.ROOT_PATH + File.separator + IdUtil.getSnowflakeNextIdStr() + "_" + codeGenType.getValue();
    }

    /**
     * 保存单个文件(不允许子类重写)
     *
     * @param fileDir  文件路径
     * @param filename 文件名
     * @param content  文件内容
     */
    protected final void writeFile(String fileDir, String filename, String content) {
        String filePath = fileDir + File.separator + filename;
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }

    /**
     * 获取代码生成方式（由子类重写）
     */
    protected abstract CodeGenTypeEnum getCodeGenType();

    /**
     * 保存文件（由子类重写）
     *
     * @param uniqueDir 唯一目录
     * @param result    需要保存的代码内容
     */
    protected abstract void saveFiles(String uniqueDir, T result);
}
