export interface HeaderMenuItem {
  key: string
  label: string
  path: string
}

export const headerMenuItems: HeaderMenuItem[] = [
  {
    key: 'home',
    label: '首页',
    path: '/',
  },
  {
    key: 'about',
    label: '关于平台',
    path: '/about',
  },
]
