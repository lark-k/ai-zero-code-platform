package com.lk.aizerocodeplatform.service;

import com.lk.aizerocodeplatform.model.dto.chat_history.ChatHistoryQueryDTO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.lk.aizerocodeplatform.model.entity.ChatHistory;
import jakarta.servlet.http.HttpServletRequest;

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
     * @param appId       应用id
     * @param userId      用户id
     * @param message     对话内容
     * @param messageType 消息类型
     * @return 是否增加成功
     */
    Boolean addChatHistory(Long appId, Long userId, String message, String messageType);

    /**
     * 根据应用id删除对话历史
     *
     * @param appId 应用id
     * @return 是否删除成功
     */
    Boolean deleteChatHistory(Long appId);

    /**
     * 游标查询对话历史
     *
     * @param chatHistoryQueryDTO 对话历史请求信息
     * @return 游标查询对话历史后的结果
     */
    Page<ChatHistory> getChatHistoryPage(ChatHistoryQueryDTO chatHistoryQueryDTO, HttpServletRequest request);

    /**
     * 构造游标分页查询条件
     *
     * @param chatHistoryQueryDTO 查询请求
     * @return 游标分页查询条件
     */
    QueryWrapper getChatHistoryQueryWrapper(ChatHistoryQueryDTO chatHistoryQueryDTO);
}
