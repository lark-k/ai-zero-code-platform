package com.lk.aizerocodeplatform.controller;

import com.lk.aizerocodeplatform.common.BaseResponse;
import com.lk.aizerocodeplatform.common.ResultUtils;
import com.lk.aizerocodeplatform.model.dto.chat_history.ChatHistoryQueryDTO;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.lk.aizerocodeplatform.model.entity.ChatHistory;
import com.lk.aizerocodeplatform.service.ChatHistoryService;
import org.springframework.web.bind.annotation.RestController;


/**
 * 控制层。
 *
 * @author LK
 * @since 2026-04-28
 */
@Tag(name = "对话历史相关接口")
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Operation(summary = "游标分页查询对话历史")
    @PostMapping(value = "/pageQuery")
    public BaseResponse<Page<ChatHistory>> pageQuery(@RequestBody ChatHistoryQueryDTO chatHistoryQueryDTO, HttpServletRequest request) {
        return ResultUtils.success(chatHistoryService.getChatHistoryPage(chatHistoryQueryDTO, request));
    }
}
