package com.lk.aizerocodeplatform.service.Impl;

import java.time.LocalDateTime;

import cn.hutool.core.util.StrUtil;
import com.lk.aizerocodeplatform.constant.UserConstant;
import com.lk.aizerocodeplatform.exception.BusinessException;
import com.lk.aizerocodeplatform.exception.ErrorCode;
import com.lk.aizerocodeplatform.exception.ThrowUtils;
import com.lk.aizerocodeplatform.model.dto.chat_history.ChatHistoryQueryDTO;
import com.lk.aizerocodeplatform.model.vo.user.UserLoginVO;
import com.lk.aizerocodeplatform.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.lk.aizerocodeplatform.model.entity.ChatHistory;
import com.lk.aizerocodeplatform.mapper.ChatHistoryMapper;
import com.lk.aizerocodeplatform.service.ChatHistoryService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/**
 * 服务层实现。
 *
 * @author LK
 * @since 2026-04-28
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    private UserService userService;

    @Override
    public Boolean addChatHistory(Long appId, Long userId, String message, String messageType) {
        // 判断参数是否合法
        ThrowUtils.throwIf(appId == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        ThrowUtils.throwIf(userId == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(message == null, ErrorCode.PARAMS_ERROR, "对话内容为空");
        ThrowUtils.throwIf(messageType == null, ErrorCode.PARAMS_ERROR, "不支持的消息类型");
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

    @Override
    public Page<ChatHistory> getChatHistoryPage(ChatHistoryQueryDTO chatHistoryQueryDTO, HttpServletRequest request) {
        // 判断参数是否合法
        ThrowUtils.throwIf(chatHistoryQueryDTO == null, ErrorCode.PARAMS_ERROR);
        // 判断查询资格
        UserLoginVO currentUserLoginVo = userService.getCurrentUserLoginVo(request);
        ThrowUtils.throwIf(currentUserLoginVo == null, ErrorCode.NOT_LOGIN_ERROR);
        // 拿到当前登录用户的id和权限
        Long userId = currentUserLoginVo.getId();
        String userRole = currentUserLoginVo.getUserRole();
        if (!chatHistoryQueryDTO.getUserId().equals(userId) && !userRole.equals(UserConstant.ADMIN_ROLE)) {
            // 普通用户只能查看自己的对话历史
            // 管理员可以查看所有的对话历史
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        int pageSize = chatHistoryQueryDTO.getPageSize();
        // 构造游标查询条件
        QueryWrapper queryWrapper = getChatHistoryQueryWrapper(chatHistoryQueryDTO);
        // 游标分页查询
        return this.page(new Page<>(1, pageSize), queryWrapper);
    }

    @Override
    public QueryWrapper getChatHistoryQueryWrapper(ChatHistoryQueryDTO chatHistoryQueryDTO) {
        ThrowUtils.throwIf(chatHistoryQueryDTO == null, ErrorCode.PARAMS_ERROR);
        Long id = chatHistoryQueryDTO.getId();
        String message = chatHistoryQueryDTO.getMessage();
        String messageType = chatHistoryQueryDTO.getMessageType();
        Long appId = chatHistoryQueryDTO.getAppId();
        Long userId = chatHistoryQueryDTO.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryDTO.getLastCreateTime();
        String sortField = chatHistoryQueryDTO.getSortField();
        String sortOrder = chatHistoryQueryDTO.getSortOrder();
        QueryWrapper wrapper = QueryWrapper.create()
                .eq("id", id)
                .like("message", message)
                .eq("messageType", messageType)
                .eq("appId", appId)
                .eq("userId", userId);
        if (lastCreateTime != null) {
            // 构造游标查询逻辑
            wrapper.lt("createTime", lastCreateTime);
        }
        // 构造排序条件
        if (StrUtil.isBlank(sortField)) {
            // 排序字段为空时，默认按照创建时间排序
            wrapper.orderBy("createTime", false);
        } else {
            // 排序字段不为空时，就按照排序字段排序
            wrapper.orderBy(sortField, "ascend".equals(sortOrder));
        }
        return wrapper;
    }
}
