// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 删除用户 POST /user/delete */
export async function deleteUser(body: API.DeleteUserDTO, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/user/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 根据id查询用户 GET /user/get */
export async function getUserById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getUserByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseUser>('/user/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 根据id查询脱敏后的用户 GET /user/get/vo */
export async function getUserVoById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getUserVoByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseUserVO>('/user/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 获取当前用户登录信息 GET /user/getCurrentUser */
export async function getCurrentUserInfo(options?: { [key: string]: any }) {
  return request<API.BaseResponseUserLoginVO>('/user/getCurrentUser', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 用户登录 POST /user/login */
export async function userLogin(body: API.UserLoginDTO, options?: { [key: string]: any }) {
  return request<API.BaseResponseUserLoginVO>('/user/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 用户注销 GET /user/logout */
export async function userLogout(options?: { [key: string]: any }) {
  return request<API.BaseResponseObject>('/user/logout', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 条件分页查询 POST /user/page */
export async function getUserPage(body: API.QueryUserDTO, options?: { [key: string]: any }) {
  return request<API.BaseResponsePageUserVO>('/user/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 用户注册 POST /user/register */
export async function userRegister(body: API.UserRegisterDTO, options?: { [key: string]: any }) {
  return request<API.BaseResponseLong>('/user/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 增加用户 POST /user/save */
export async function saveUser(body: API.AddUserDTO, options?: { [key: string]: any }) {
  return request<API.BaseResponseLong>('/user/save', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 更新用户 POST /user/update */
export async function updateUser(body: API.UpdateUserDTO, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/user/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
