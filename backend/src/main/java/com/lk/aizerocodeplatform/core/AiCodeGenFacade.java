package com.lk.aizerocodeplatform.core;

import com.lk.aizerocodeplatform.ai.AiCodeGenService;
import com.lk.aizerocodeplatform.ai.model.HtmlCodeResult;
import com.lk.aizerocodeplatform.ai.model.MultiFileCodeResult;
import com.lk.aizerocodeplatform.enums.CodeGenTypeEnum;
import com.lk.aizerocodeplatform.exception.BusinessException;
import com.lk.aizerocodeplatform.exception.ErrorCode;
import com.lk.aizerocodeplatform.exception.ThrowUtils;
import com.lk.aizerocodeplatform.parser.CodeParseExecutor;
import com.lk.aizerocodeplatform.saver.CodeFileSaveExecutor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/23 17:08
 * AI代码生成门面类（采用门面设计模式）
 */
@Service
@Slf4j
public class AiCodeGenFacade {
    @Resource
    private AiCodeGenService aiCodeGenService;

    /**
     * 生成代码并保存
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 代码生成类型
     * @return 文件
     */
    public File generateCodeAndSave(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        ThrowUtils.throwIf(codeGenTypeEnum == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(userMessage == null, ErrorCode.PARAMS_ERROR);
        return switch (codeGenTypeEnum) {
            case HTML -> generateCodeHtmlAndSave(userMessage);
            case MULTI_FILE -> generateCodeMultiFileAndSave(userMessage);
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 保存HTML模式的代码文件
     *
     * @param userMessage 用户提示词
     * @return 文件
     */
    private File generateCodeHtmlAndSave(String userMessage) {
        HtmlCodeResult htmlCodeResult = aiCodeGenService.generateHtmlCode(userMessage);
        return CodeFileSaveExecutor.executeCodeFileSave(htmlCodeResult,CodeGenTypeEnum.HTML);
    }

    /**
     * 保存多文件模式的代码文件
     *
     * @param userMessage 用户提示词
     * @return 文件
     */
    private File generateCodeMultiFileAndSave(String userMessage) {
        MultiFileCodeResult multiFileCodeResult = aiCodeGenService.generateMultiFileCode(userMessage);
        return CodeFileSaveExecutor.executeCodeFileSave(multiFileCodeResult,CodeGenTypeEnum.MULTI_FILE);
    }

    /**
     * 生成代码并保存（流式）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 代码生成类型
     * @return 文件
     */
    public Flux<String> generateCodeAndSaveStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        ThrowUtils.throwIf(codeGenTypeEnum == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(userMessage == null, ErrorCode.PARAMS_ERROR);
        return switch (codeGenTypeEnum) {
            case HTML -> generateCodeHtmlAndSaveStream(userMessage);
            case MULTI_FILE -> generateCodeMultiFileAndSaveStream(userMessage);
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 保存HTML模式的代码文件（流式）
     *
     * @param userMessage 用户提示词
     * @return 文件
     */
    private Flux<String> generateCodeHtmlAndSaveStream(String userMessage) {
        StringBuilder stringBuilder = new StringBuilder();
        return aiCodeGenService.generateHtmlCodeStream(userMessage)
                .doOnNext(stringBuilder::append)
                .doOnComplete(() -> {
                    try {
                        String completeHtmlCode = stringBuilder.toString();
                        // 调用统一的解析器执行器，根据生成代码的方式决定调用哪一个解析器
                        HtmlCodeResult htmlCodeResult = (HtmlCodeResult) CodeParseExecutor.executeParser(completeHtmlCode, CodeGenTypeEnum.HTML);
                        // 调用统一的代码文件保存执行器，根据生成的代码方式决定调用哪一个文件保存模板
                        File dir = CodeFileSaveExecutor.executeCodeFileSave(htmlCodeResult, CodeGenTypeEnum.HTML);
                        log.info("文件保存成功，路径为：{}", dir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存失败：{}", e.getMessage());
                    }
                });
    }

    /**
     * 保存多文件模式的代码文件（流式）
     *
     * @param userMessage 用户提示词
     * @return 文件
     */
    private Flux<String> generateCodeMultiFileAndSaveStream(String userMessage) {
        StringBuilder stringBuilder = new StringBuilder();
        return aiCodeGenService.generateMultiFileCodeStream(userMessage)
                .doOnNext(stringBuilder::append)
                .doOnComplete(() -> {
                    try {
                        String completeMultiFileCode = stringBuilder.toString();
                        // 调用统一的解析器执行器，根据生成代码的方式决定调用哪一个解析器
                        MultiFileCodeResult multiFileCodeResult = (MultiFileCodeResult) CodeParseExecutor.executeParser(completeMultiFileCode, CodeGenTypeEnum.MULTI_FILE);
                        // 调用统一的代码文件保存执行器，根据生成的代码方式决定调用哪一个文件保存模板
                        File dir = CodeFileSaveExecutor.executeCodeFileSave(multiFileCodeResult, CodeGenTypeEnum.MULTI_FILE);
                        log.info("文件保存成功，路径为：{}", dir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存失败：{}", e.getMessage());
                    }
                });
    }
}
