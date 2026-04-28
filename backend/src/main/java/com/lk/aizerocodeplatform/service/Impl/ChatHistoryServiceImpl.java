package com.lk.aizerocodeplatform.service.Impl;

import com.lk.aizerocodeplatform.exception.ErrorCode;
import com.lk.aizerocodeplatform.exception.ThrowUtils;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.lk.aizerocodeplatform.model.entity.ChatHistory;
import com.lk.aizerocodeplatform.mapper.ChatHistoryMapper;
import com.lk.aizerocodeplatform.service.ChatHistoryService;
import org.springframework.stereotype.Service;

/**
 * 服务层实现。
 *
 * @author LK
 * @since 2026-04-28
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Override
    public Boolean addChatHistory(Long appId, Long userId, String message, String messageType) {
        // 判断参数是否合法
        ThrowUtils.throwIf(appId == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        ThrowUtils.throwIf(userId == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(message == null, ErrorCode.PARAMS_ERROR, "对话内容为空");
        ThrowUtils.throwIf(messageType == null, ErrorCode.PARAMS_ERROR,"不支持的消息类型");
        // 信息入库
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .userId(userId)
                .message(message)
                .messageType(messageType)
                .build();
        return this.save(chatHistory);
    }

    @Override
    public Boolean deleteChatHistory(Long appId) {
        // 判断参数
        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR);
        // 删除该应用对应的对话历史数据
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("appId", appId);
        return remove(queryWrapper);
    }
}
