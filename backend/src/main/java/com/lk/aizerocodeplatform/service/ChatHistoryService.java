package com.lk.aizerocodeplatform.service;

import com.mybatisflex.core.service.IService;
import com.lk.aizerocodeplatform.model.entity.ChatHistory;

/**
 * 服务层。
 *
 * @author LK
 * @since 2026-04-28
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 增加对话历史
     *
     * @param appId   应用id
     * @param userId  用户id
     * @param message 对话内容
     * @param messageType 消息类型
     * @return 是否增加成功
     */
    Boolean addChatHistory(Long appId, Long userId, String message, String messageType);
}
