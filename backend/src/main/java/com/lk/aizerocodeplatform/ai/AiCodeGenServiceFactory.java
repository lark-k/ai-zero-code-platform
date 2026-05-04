package com.lk.aizerocodeplatform.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.lk.aizerocodeplatform.ai.tools.*;
import com.lk.aizerocodeplatform.enums.CodeGenTypeEnum;
import com.lk.aizerocodeplatform.exception.BusinessException;
import com.lk.aizerocodeplatform.exception.ErrorCode;
import com.lk.aizerocodeplatform.model.entity.ChatHistory;
import com.lk.aizerocodeplatform.service.ChatHistoryService;
import com.mybatisflex.core.query.QueryWrapper;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * @Author 梁科
 * @Version 1.0
 * @ Date 2026/4/23 13:50
 * 创建ai代码生成服务工厂，用于初始化服务
 */
@Slf4j
@Configuration
public class AiCodeGenServiceFactory {
    @Resource
    private ChatModel chatModel;
    @Resource
    private StreamingChatModel openAiStreamingChatModel;
    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;
    @Resource
    private ChatHistoryService chatHistoryService;
    /**
     * 注入配置的推理流式模型
     * 注意：必须给上面的StreamingChatModel对象改个名字，不然系统不知道装配哪个bean
     */
    @Resource
    private StreamingChatModel reasoningStreamChatModel;
    @Resource
    private ToolManager toolManager;

    /**
     * Caffeine本质就是一个Map，只是能够更方便的进行管理里面的信息，比如设置过期时间等。
     * 为了防止同一个appId会不断创建新的AiCodeGenService服务，这里引入Caffeine本地缓存。
     * 即，在一定的时间内，如果同一个appId不断使用，就可以直接从缓存中拿到AiCodeGenService服务，
     * 就不需要频繁的创建AiCodeGenService服务了！！！
     */
    private final Cache<String, AiCodeGenService> serviceCache = Caffeine.newBuilder()
            // 最大缓存1000条信息
            .maximumSize(1000)
            // 写入后30分钟过期
            .expireAfterWrite(Duration.ofMinutes(30))
            // 访问后10分钟过期
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.info("Ai服务实例被移除，appId:{}，原因:{}", key, cause);
            })
            .build();


    /**
     * 获取带有记忆的AiCodeGenService服务
     *
     * @param appId 应用id
     * @return 带有记忆的AiCodeGenService服务
     */
    public AiCodeGenService getAiCodeGenService(Long appId) {
        // 先从Caffeine本地缓存中取，如果没有取到就调用createAiCodeGenService方法创建
//        return serviceCache.get(appId, this::createAiCodeGenService);

        // 为了保证跟之前的代码兼容，依旧可以使用该方法获取HTML,MULTI_FILE类型的代码生成服务AiCodeGenService对象
        return getAiCodeGenService(appId, CodeGenTypeEnum.HTML);
    }

    /**
     * 获取带有记忆的AiCodeGenService服务
     *
     * @param appId 应用id
     * @return 带有记忆的AiCodeGenService服务
     */
    public AiCodeGenService getAiCodeGenService(Long appId, CodeGenTypeEnum codeGenTypeEnum) {
        String cacheKey = this.getCaffeineCacheKey(appId, codeGenTypeEnum);
        // 先从Caffeine本地缓存中取，如果没有取到就调用createAiCodeGenService方法创建
        return serviceCache.get(cacheKey, key -> createAiCodeGenService(appId, codeGenTypeEnum));
    }

    /**
     * 通过appId创建AiCodeGenService，
     * 用于隔离不同的对话记忆
     *
     * @param appId 应用id
     * @return AI代码生成服务
     */
    private AiCodeGenService createAiCodeGenService(Long appId, CodeGenTypeEnum codeGenTypeEnum) {
        // 创建对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();
        // 首次创建AiCodeGenService需要从数据库中加载历史对话到对话记忆中
        loadChatHistoryToMemory(appId, chatMemory, 20L);
        // 根据不同的代码生成类型，创建不同的AI服务
        return switch (codeGenTypeEnum) {
            // vue工程项目使用推理流式模型
            case VUE_PROJECT -> AiServices.builder(AiCodeGenService.class)
                    .streamingChatModel(reasoningStreamChatModel)
                    .chatMemoryProvider(memoryId -> chatMemory)
                    .tools(
                        toolManager.getAllTools()
                    )
                    .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(toolExecutionRequest, "Error: there is no tool called " + toolExecutionRequest.name()))
                    .build();
            // 普通代码文件使用普通模型
            case MULTI_FILE, HTML -> AiServices.builder(AiCodeGenService.class)
                    .chatModel(chatModel)
                    .streamingChatModel(openAiStreamingChatModel)
                    .chatMemory(chatMemory)
                    .build();
            default ->
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型：" + codeGenTypeEnum.getValue());
        };
    }

    /**
     * 加载对话历史到对话记忆中
     *
     * @param appId       应用id
     * @param chatMemory  对话记忆
     * @param maxMessages 最大加载对话历史的个数
     * @return 加载成功的个数
     */
    Integer loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, Long maxMessages) {
        try {
            // 记录查询到的对话历史个数
            Integer loadCount = 0;
            // 构造数据库查询条件
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)
                    .orderBy(ChatHistory::getCreateTime, false)
                    .limit(1, maxMessages);
            // 根据查询条件查询数据库的对话历史信息
            List<ChatHistory> chatHistories = chatHistoryService.list(queryWrapper);
            // 反转列表，保证旧对话记录在前面，新对话记录在后面
            chatHistories = chatHistories.reversed();
            // 先清理对话记忆，防止信息重复
            chatMemory.clear();
            // 遍历查询到的对话历史信息，根据不同的消息类型存放在对话记忆中
            for (ChatHistory chatHistory : chatHistories) {
                if ("user".equals(chatHistory.getMessageType())) {
                    chatMemory.add(new UserMessage(chatHistory.getMessage()));
                }
                if ("ai".equals(chatHistory.getMessageType())) {
                    chatMemory.add(new AiMessage(chatHistory.getMessage()));
                }
                loadCount++;
            }
            log.info("成功为 appId: {} 加载了 {} 条历史对话", appId, loadCount);
            return loadCount;
        } catch (Exception e) {
            log.error("加载历史对话失败，appId: {}, error: {}", appId, e.getMessage());
            // 由于加载对话记忆不是特别重要，此处不抛出异常，保证系统主要功能可用
            return 0;
        }
    }


    /**
     * 获取Caffeine本地缓存的key
     *
     * @param appId           应用id
     * @param codeGenTypeEnum 代码生成类型
     * @return Caffeine本地缓存的key
     */
    public String getCaffeineCacheKey(Long appId, CodeGenTypeEnum codeGenTypeEnum) {
        return appId + "_" + codeGenTypeEnum.getValue();
    }


    @Bean
    public AiCodeGenService aiCodeGenService() {
//        return AiServices.builder(AiCodeGenService.class)
//                .chatModel(chatModel)
//                .openAiStreamingChatModel(openAiStreamingChatModel)
//                .build();
        // 为了保证跟之前的代码兼容，依旧提供一个默认的AI Service的bean
        return getAiCodeGenService(0L);
    }
}
