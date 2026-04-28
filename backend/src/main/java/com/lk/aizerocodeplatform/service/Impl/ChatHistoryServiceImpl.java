package com.lk.aizerocodeplatform.service.Impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.lk.aizerocodeplatform.model.entity.ChatHistory;
import com.lk.aizerocodeplatform.mapper.ChatHistoryMapper;
import com.lk.aizerocodeplatform.service.ChatHistoryService;
import org.springframework.stereotype.Service;

/**
 *  服务层实现。
 *
 * @author LK
 * @since 2026-04-28
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

}
