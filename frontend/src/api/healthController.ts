// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /healthTest */
export async function healthCheck(options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/healthTest', {
    method: 'GET',
    ...(options || {}),
  })
}
