package com.lk.aizerocodeplatform.core;

import cn.hutool.json.JSONUtil;
import com.lk.aizerocodeplatform.ai.AiCodeGenService;
import com.lk.aizerocodeplatform.ai.AiCodeGenServiceFactory;
import com.lk.aizerocodeplatform.ai.model.HtmlCodeResult;
import com.lk.aizerocodeplatform.ai.model.MultiFileCodeResult;
import com.lk.aizerocodeplatform.ai.model.message.AiResponseMessage;
import com.lk.aizerocodeplatform.ai.model.message.ToolCallingQuestMessage;
import com.lk.aizerocodeplatform.ai.model.message.ToolCallingResultMessage;
import com.lk.aizerocodeplatform.enums.CodeGenTypeEnum;
import com.lk.aizerocodeplatform.exception.BusinessException;
import com.lk.aizerocodeplatform.exception.ErrorCode;
import com.lk.aizerocodeplatform.exception.ThrowUtils;
import com.lk.aizerocodeplatform.parser.CodeParseExecutor;
import com.lk.aizerocodeplatform.saver.CodeFileSaveExecutor;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
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
//    @Resource
//    private AiCodeGenService aiCodeGenService;

    @Resource
    private AiCodeGenServiceFactory aiCodeGenServiceFactory;

    /**
     * 生成代码并保存
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 代码生成类型
     * @param appId           应用id
     * @return 文件
     */
    public File generateCodeAndSave(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        ThrowUtils.throwIf(codeGenTypeEnum == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(userMessage == null, ErrorCode.PARAMS_ERROR);
        return switch (codeGenTypeEnum) {
            case HTML -> generateCodeHtmlAndSave(userMessage, appId);
            case MULTI_FILE -> generateCodeMultiFileAndSave(userMessage, appId);
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
     * @param appId       应用id
     * @return 文件
     */
    private File generateCodeHtmlAndSave(String userMessage, Long appId) {
        HtmlCodeResult htmlCodeResult = aiCodeGenServiceFactory.getAiCodeGenService(appId).generateHtmlCode(userMessage);
        return CodeFileSaveExecutor.executeCodeFileSave(htmlCodeResult, CodeGenTypeEnum.HTML, appId);
    }

    /**
     * 保存多文件模式的代码文件
     *
     * @param userMessage 用户提示词
     * @param appId       应用id
     * @return 文件
     */
    private File generateCodeMultiFileAndSave(String userMessage, Long appId) {
        MultiFileCodeResult multiFileCodeResult = aiCodeGenServiceFactory.getAiCodeGenService(appId).generateMultiFileCode(userMessage);
        return CodeFileSaveExecutor.executeCodeFileSave(multiFileCodeResult, CodeGenTypeEnum.MULTI_FILE, appId);
    }

    /**
     * 生成代码并保存（流式）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 代码生成类型
     * @param appId           应用id
     * @return 文件
     */
    public Flux<String> generateCodeAndSaveStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        ThrowUtils.throwIf(codeGenTypeEnum == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(userMessage == null, ErrorCode.PARAMS_ERROR);
        return switch (codeGenTypeEnum) {
            case HTML -> generateCodeHtmlAndSaveStream(userMessage, appId);
            case MULTI_FILE -> generateCodeMultiFileAndSaveStream(userMessage, appId);
            case VUE_PROJECT -> generateCodeVueProjectAndSaveStream(userMessage, appId, codeGenTypeEnum);
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
     * @param appId       应用id
     * @return 文件
     */
    private Flux<String> generateCodeHtmlAndSaveStream(String userMessage, Long appId) {
        Flux<String> codeStream = aiCodeGenServiceFactory.getAiCodeGenService(appId).generateHtmlCodeStream(userMessage);
        return processCodeStream(codeStream, CodeGenTypeEnum.HTML, appId);
    }

    /**
     * 保存多文件模式的代码文件（流式）
     *
     * @param userMessage 用户提示词
     * @param appId       应用id
     * @return 文件
     */
    private Flux<String> generateCodeMultiFileAndSaveStream(String userMessage, Long appId) {
        Flux<String> codeStream = aiCodeGenServiceFactory.getAiCodeGenService(appId).generateMultiFileCodeStream(userMessage);
        return processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId);
    }

    /**
     * 保存Vue工程模式的代码文件（流式）
     *
     * @param userMessage     用户提示词
     * @param appId           应用id
     * @param codeGenTypeEnum 生成代码类型
     * @return Ai回复
     */
    private Flux<String> generateCodeVueProjectAndSaveStream(String userMessage, Long appId, CodeGenTypeEnum codeGenTypeEnum) {
        return processTokenStream(aiCodeGenServiceFactory.getAiCodeGenService(appId, codeGenTypeEnum).generateVueProjectCodeStream(userMessage, appId));
    }

    /**
     * 通用代码流处理方法
     *
     * @param codeStream      代码流
     * @param codeGenTypeEnum 生成代码方式
     * @param appId           应用id
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        StringBuilder stringBuilder = new StringBuilder();
        return codeStream
                .doOnNext(stringBuilder::append)
                .doOnComplete(() -> {
                    try {
                        String completeCode = stringBuilder.toString();
                        // 调用统一的解析器执行器，根据生成代码的方式决定调用哪一个解析器
                        Object commonParseResult = CodeParseExecutor.executeParser(completeCode, codeGenTypeEnum);
                        // 调用统一的代码文件保存执行器，根据生成的代码方式决定调用哪一个文件保存模板
                        File dir = CodeFileSaveExecutor.executeCodeFileSave(commonParseResult, codeGenTypeEnum, appId);
                        log.info("文件保存成功，路径为：{}", dir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("保存失败：{}", e.getMessage());
                    }
                });
    }

    /**
     * 将tokenStream转换为Flux，本质是一个适配器，通过转换后原来方法返回值为Flux的方法仍然可用
     */
    private Flux<String> processTokenStream(TokenStream tokenStream) {
        return Flux.create(sink -> {
            // 监听tokenStream
            tokenStream.onPartialResponse((String partialResponse) -> {
                        // 将AI回复的消息封装为AI回复消息包装类
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        // 将AI回复消息包装类转换为json字符串
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                        // 将工具调用请求信息封装为包装类
                        ToolCallingQuestMessage toolCallingQuestMessage = new ToolCallingQuestMessage(toolExecutionRequest);
                        // 将工具调用请求信息包装类转换为json字符串
                        sink.next(JSONUtil.toJsonStr(toolCallingQuestMessage));
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        // 将工具调用结果信息封装为包装类
                        ToolCallingResultMessage toolCallingResultMessage = new ToolCallingResultMessage(toolExecution);
                        // 将工具调用结果信息包装类转换为json字符串
                        sink.next(JSONUtil.toJsonStr(toolCallingResultMessage));
                    })
                    .onCompleteResponse((ChatResponse chatResponse) -> {
                        sink.complete();
                    })
                    .onError((Throwable error) -> {
                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start();
        });
    }
}