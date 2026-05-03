declare namespace API {
  type AddAppDTO = {
    initPrompt?: string
  }

  type AddFeaturedApplyDTO = {
    appId?: number
    applyReason?: string
  }

  type AddUserDTO = {
    userName?: string
    userAccount?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type AdminCheckDTO = {
    id?: number
    appId?: number
    reviewRemark?: string
  }

  type AdminCheckVO = {
    id?: number
    appId?: number
    userId?: number
    applyReason?: string
    status?: string
    reviewRemark?: string
    reviewUserId?: number
    reviewTime?: string
    createTime?: string
    updateTime?: string
  }

  type AdminPageQueryFeatureApplyDTO = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    appId?: number
    userId?: number
    applyReason?: string
    status?: string
    reviewRemark?: string
    reviewUserId?: number
  }

  type AppAdminUpdateDTO = {
    id?: number
    appName?: string
    cover?: string
    priority?: number
  }

  type AppDeployDTO = {
    appId?: number
  }

  type AppVO = {
    id?: number
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    deployedTime?: string
    priority?: number
    userId?: number
    createTime?: string
    updateTime?: string
    userVo?: UserVO
  }

  type BaseResponseAppVO = {
    code?: number
    data?: AppVO
    message?: string
  }

  type BaseResponseBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseLong = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponseObject = {
    code?: number
    data?: Record<string, any>
    message?: string
  }

  type BaseResponsePageAdminCheckVO = {
    code?: number
    data?: PageAdminCheckVO
    message?: string
  }

  type BaseResponsePageAppVO = {
    code?: number
    data?: PageAppVO
    message?: string
  }

  type BaseResponsePageChatHistory = {
    code?: number
    data?: PageChatHistory
    message?: string
  }

  type BaseResponsePagePageQueryFeatureApplyVO = {
    code?: number
    data?: PagePageQueryFeatureApplyVO
    message?: string
  }

  type BaseResponsePageUserVO = {
    code?: number
    data?: PageUserVO
    message?: string
  }

  type BaseResponseString = {
    code?: number
    data?: string
    message?: string
  }

  type BaseResponseUser = {
    code?: number
    data?: User
    message?: string
  }

  type BaseResponseUserLoginVO = {
    code?: number
    data?: UserLoginVO
    message?: string
  }

  type BaseResponseUserVO = {
    code?: number
    data?: UserVO
    message?: string
  }

  type cancelDeployParams = {
    appId: number
  }

  type cancelTopParams = {
    appId: number
  }

  type ChatHistory = {
    id?: number | string
    message?: string
    messageType?: string
    appId?: number | string
    userId?: number | string
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type ChatHistoryQueryDTO = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number | string
    message?: string
    messageType?: string
    appId?: number | string
    userId?: number | string
    lastCreateTime?: string
  }

  type chatToGenCodeParams = {
    message: string
    appId: number
  }

  type downloadAppCodeParams = {
    appId: number
  }

  type DeleteAppDTO = {
    id?: number
  }

  type DeleteFeaturedApplyDTO = {
    id?: number
  }

  type DeleteUserDTO = {
    id?: number
  }

  type getAppVoByAdminParams = {
    id: number
  }

  type getAppVoByIdParams = {
    id: number
  }

  type getUserByIdParams = {
    id: number
  }

  type getUserVoByIdParams = {
    id: number
  }

  type PageAdminCheckVO = {
    records?: AdminCheckVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageAppVO = {
    records?: AppVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageChatHistory = {
    records?: ChatHistory[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PagePageQueryFeatureApplyVO = {
    records?: PageQueryFeatureApplyVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageQueryFeatureApplyDTO = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    appId?: number
    applyReason?: string
    status?: string
  }

  type PageQueryFeatureApplyVO = {
    id?: number
    appId?: number
    applyReason?: string
    status?: string
    reviewRemark?: string
    reviewUserId?: number
    reviewTime?: string
    createTime?: string
    updateTime?: string
  }

  type PageUserVO = {
    records?: UserVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type QueryAppDTO = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    priority?: number
    userId?: number
  }

  type QueryUserDTO = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    userName?: string
    userAccount?: string
    userProfile?: string
    userRole?: string
  }

  type ReAddFeaturedApplyDTO = {
    id?: number
    appId?: number
    applyReason?: string
    status?: string
  }

  type ChatStreamChunk = string

  type serveStaticResourceParams = {
    appId: number
  }

  type stickToTopParams = {
    appId: number
  }

  type UpdateAppDTO = {
    id?: number
    appName?: string
  }

  type UpdateFeaturedApplyDTO = {
    id?: number
    applyReason?: string
  }

  type UpdateUserDTO = {
    id?: number
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type User = {
    id?: number
    userAccount?: string
    userPassword?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    editTime?: string
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type UserLoginDTO = {
    userAccount?: string
    userPassword?: string
  }

  type UserLoginVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserRegisterDTO = {
    userAccount?: string
    userPassword?: string
    confirmPassword?: string
  }

  type UserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
  }
}
