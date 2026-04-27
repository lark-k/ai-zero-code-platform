export interface HeaderMenuItem {
  key: string
  label: string
  path: string
  adminOnly?: boolean
  loginOnly?: boolean
}

export const headerMenuItems: HeaderMenuItem[] = [
  {
    key: 'home',
    label: '首页',
    path: '/',
  },
  {
    key: 'featured-apply',
    label: '精选申请',
    path: '/app/featured/apply',
    loginOnly: true,
  },
  {
    key: 'app-manage',
    label: '应用管理',
    path: '/admin/appManage',
    adminOnly: true,
  },
  {
    key: 'featured-apply-manage',
    label: '审核申请',
    path: '/admin/appFeaturedApplyManage',
    adminOnly: true,
  },
  {
    key: 'user-manage',
    label: '用户管理',
    path: '/admin/userManage',
    adminOnly: true,
  },
]
