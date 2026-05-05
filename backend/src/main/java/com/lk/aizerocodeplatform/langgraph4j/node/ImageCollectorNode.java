package com.lk.aizerocodeplatform.langgraph4j.node;

import com.lk.aizerocodeplatform.langgraph4j.ai.ImageCollectionService;
import com.lk.aizerocodeplatform.langgraph4j.model.ImageCategoryEnum;
import com.lk.aizerocodeplatform.langgraph4j.model.ImageResource;
import com.lk.aizerocodeplatform.langgraph4j.state.WorkflowContext;
import com.lk.aizerocodeplatform.tools.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.Arrays;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/5/5 17:42
 * 图片收集节点
 */
@Slf4j
public class ImageCollectorNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            String originalPrompt = context.getOriginalPrompt();
            String imageListStr = "";
            try {
                // 获取AI图片收集服务
                ImageCollectionService imageCollectionService = SpringContextUtil.getBean(ImageCollectionService.class);
                // 使用 AI 服务进行智能图片收集
                imageListStr = imageCollectionService.collectImages(originalPrompt);
            } catch (Exception e) {
                log.error("图片收集失败: {}", e.getMessage(), e);
            }
            // 更新状态
            context.setCurrentStep("图片收集");
            context.setImageListStr(imageListStr);
            return WorkflowContext.saveContext(context);
        });
    }
}

