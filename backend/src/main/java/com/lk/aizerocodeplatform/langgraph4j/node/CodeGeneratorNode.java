package com.lk.aizerocodeplatform.langgraph4j.node;

import cn.hutool.core.util.RandomUtil;
import com.lk.aizerocodeplatform.constant.AppConstant;
import com.lk.aizerocodeplatform.constant.CodeFileSaveConstant;
import com.lk.aizerocodeplatform.core.AiCodeGenFacade;
import com.lk.aizerocodeplatform.enums.CodeGenTypeEnum;
import com.lk.aizerocodeplatform.langgraph4j.state.WorkflowContext;
import com.lk.aizerocodeplatform.tools.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.Duration;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/5/5 17:48
 * 代码生成节点
 */
@Slf4j
public class CodeGeneratorNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 代码生成");

            // 使用增强提示词作为发给 AI 的用户消息
            String userMessage = context.getEnhancedPrompt();
            CodeGenTypeEnum generationType = context.getGenerationType();
            // 获取 AI 代码生成外观服务
            AiCodeGenFacade codeGeneratorFacade = SpringContextUtil.getBean(AiCodeGenFacade.class);
            log.info("开始生成代码，类型: {} ({})", generationType.getValue(), generationType.getText());
            // 先使用随机数的 appId (后续再整合到业务中)
            Long appId = Long.valueOf(RandomUtil.randomNumbers(6));
            // 调用流式代码生成
            Flux<String> codeStream = codeGeneratorFacade.generateCodeAndSaveStream(userMessage, generationType, appId);
            // 同步等待流式输出完成
            codeStream.blockLast(Duration.ofMinutes(10)); // 最多等待 10 分钟
            // 根据类型设置生成目录
            String generatedCodeDir;
            if (generationType == CodeGenTypeEnum.VUE_PROJECT) {
                generatedCodeDir = CodeFileSaveConstant.ROOT_PATH
                        + File.separator
                        + generationType.getValue()
                        + "_"
                        + appId;
            } else {
                generatedCodeDir = CodeFileSaveConstant.ROOT_PATH
                        + File.separator
                        + appId
                        + "_"
                        + generationType.getValue();
            }
            log.info("AI 代码生成完成，生成目录: {}", generatedCodeDir);

            // 更新状态
            context.setCurrentStep("代码生成");
            context.setGeneratedCodeDir(generatedCodeDir);
            return WorkflowContext.saveContext(context);
        });
    }
}

