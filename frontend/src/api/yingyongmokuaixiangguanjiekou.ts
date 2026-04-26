// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 增加应用 POST /app/addApp */
export async function addApp(body: API.AddAppDTO, options?: { [key: string]: any }) {
  return request<API.BaseResponseLong>('/app/addApp', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 管理员删除应用 POST /app/admin/delete */
export async function deleteAppByAdmin(body: API.DeleteAppDTO, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/app/admin/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 管理员查询单个应用信息 POST /app/admin/getApp */
export async function getAppVoByAdmin(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getAppVoByAdminParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseAppVO>('/app/admin/getApp', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 管理员分页查询应用 POST /app/admin/pageQuery */
export async function queryPageByAdmin(body: API.QueryAppDTO, options?: { [key: string]: any }) {
  return request<API.BaseResponsePageAppVO>('/app/admin/pageQuery', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 管理员更新应用 POST /app/admin/update */
export async function updateAppByAdmin(
  body: API.AppAdminUpdateDTO,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/app/admin/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 取消置顶精选应用 GET /app/cancelTop */
export async function cancelTop(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.cancelTopParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/app/cancelTop', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 用户对话生成应用 GET /app/chat */
export async function chatToGenCode(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.chatToGenCodeParams,
  options?: { [key: string]: any }
) {
  return request<API.ServerSentEventString[]>('/app/chat', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 删除应用 POST /app/deleteApp */
export async function deleteApp(body: API.DeleteAppDTO, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/app/deleteApp', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 用户部署应用 POST /app/deploy */
export async function appDeploy(body: API.AppDeployDTO, options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/app/deploy', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 根据id查询应用信息 GET /app/getAppById */
export async function getAppVoById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getAppVoByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseAppVO>('/app/getAppById', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 分页查询应用信息(包括作者的脱敏信息) POST /app/getAppVoListByPage */
export async function getAppVoByPage(body: API.QueryAppDTO, options?: { [key: string]: any }) {
  return request<API.BaseResponsePageAppVO>('/app/getAppVoListByPage', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 分页查询精选应用信息(包括作者的脱敏信息) POST /app/getAppVoListByPageForGood */
export async function getAppVoPageForGood(body: API.QueryAppDTO, options?: { [key: string]: any }) {
  return request<API.BaseResponsePageAppVO>('/app/getAppVoListByPageForGood', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 置顶精选应用 GET /app/toTop */
export async function stickToTop(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.stickToTopParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/app/toTop', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 修改应用 POST /app/updateApp */
export async function updateApp(body: API.UpdateAppDTO, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/app/updateApp', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
