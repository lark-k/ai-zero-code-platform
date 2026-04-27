// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 增加应用精选申请 POST /appFeaturedApply/add */
export async function addAppFeaturedApply(
  body: API.AddFeaturedApplyDTO,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseLong>('/appFeaturedApply/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 管理员同意应用精选申请 POST /appFeaturedApply/admin/agreeApply */
export async function agreeApply(body: API.AdminCheckDTO, options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/appFeaturedApply/admin/agreeApply', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 管理员撤销应用精选申请 POST /appFeaturedApply/admin/cancelApply */
export async function cancelApply(body: API.AdminCheckDTO, options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/appFeaturedApply/admin/cancelApply', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 管理员拒绝应用精选申请 POST /appFeaturedApply/admin/disagreeApply */
export async function disagreeApply(body: API.AdminCheckDTO, options?: { [key: string]: any }) {
  return request<API.BaseResponseString>('/appFeaturedApply/admin/disagreeApply', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 管理员分页查询应用精选申请 POST /appFeaturedApply/admin/pageQueryApply */
export async function adminPageQueryApply(
  body: API.AdminPageQueryFeatureApplyDTO,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageAdminCheckVO>('/appFeaturedApply/admin/pageQueryApply', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 删除应用精选申请 POST /appFeaturedApply/delete */
export async function deleteAppFeaturedApply(
  body: API.DeleteFeaturedApplyDTO,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/appFeaturedApply/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 用户分页查询应用精选申请 POST /appFeaturedApply/pageQuery */
export async function getPageQueryFeatureApplyVoList(
  body: API.PageQueryFeatureApplyDTO,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePagePageQueryFeatureApplyVO>('/appFeaturedApply/pageQuery', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 用户重新提交申请 POST /appFeaturedApply/reAdd */
export async function reAddApply(
  body: API.ReAddFeaturedApplyDTO,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseString>('/appFeaturedApply/reAdd', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 更新应用精选申请 POST /appFeaturedApply/update */
export async function updateAppFeaturedApply(
  body: API.UpdateFeaturedApplyDTO,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/appFeaturedApply/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
