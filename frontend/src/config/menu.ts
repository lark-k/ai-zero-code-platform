export interface HeaderMenuItem {
  key: string
  label: string
  path: string
  adminOnly?: boolean
}

export const headerMenuItems: HeaderMenuItem[] = [
  {
    key: 'home',
    label: '主页',
    path: '/',
  },
  {
    key: 'app-manage',
    label: '应用管理',
    path: '/admin/appManage',
    adminOnly: true,
  },
  {
    key: 'user-manage',
    label: '用户管理',
    path: '/admin/userManage',
    adminOnly: true,
  },
]
