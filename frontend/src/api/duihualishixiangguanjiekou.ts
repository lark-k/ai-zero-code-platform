// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 游标分页查询对话历史 POST /chatHistory/pageQuery */
export async function pageQuery(body: API.ChatHistoryQueryDTO, options?: { [key: string]: any }) {
  return request<API.BaseResponsePageChatHistory>('/chatHistory/pageQuery', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
