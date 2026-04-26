// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 预览代码生成后的网站 GET /static/${param0}/&#42;&#42; */
export async function serveStaticResource(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.serveStaticResourceParams,
  options?: { [key: string]: any }
) {
  const { appId: param0, ...queryParams } = params
  return request<string>(`/static/${param0}/**`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}
