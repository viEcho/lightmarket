import { createRouter, createWebHistory } from 'vue-router'

// 公共页面组件
import Markets from '../views/Markets.vue'
import Leaderboard from '../components/Leaderboard.vue'
import Activity from '../components/Activity.vue'
import CreateMarket from '../components/CreateMarket.vue'
import MarketDetail from '../components/MarketDetail.vue'

// 管理系统组件
import AdminLogin from '../components/AdminLogin.vue'
import AdminLayout from '../views/AdminLayout.vue'
import ReviewMarket from '../components/ReviewMarket.vue'
import AdminDashboard from '../components/AdminDashboard.vue'
import MarketTransactions from '../components/MarketTransactions.vue'

const routes = [
  // 公共页面路由
  {
    path: '/',
    name: 'home',
    component: Markets
  },
  {
    path: '/markets',
    name: 'markets',
    component: Markets
  },
  {
    path: '/leaderboard',
    name: 'leaderboard',
    component: Leaderboard
  },
  {
    path: '/activity',
    name: 'activity',
    component: Activity
  },
  {
    path: '/create-market',
    name: 'create-market',
    component: CreateMarket
  },
  {
    path: '/market/:marketId',
    name: 'market-detail',
    component: MarketDetail,
    props: true
  },

  // 管理系统路由 - 需要 /admin 前缀
  {
    path: '/admin-login',
    name: 'admin-login',
    component: AdminLogin,
    meta: { requiresAuth: false }
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/admin/review'
      },
      {
        path: 'review',
        name: 'admin-review',
        component: ReviewMarket
      },
      {
        path: 'dashboard',
        name: 'admin-dashboard',
        component: AdminDashboard
      },
      {
        path: 'market/:id/transactions',
        name: 'admin-market-transactions',
        component: MarketTransactions
      }
    ]
  },

  // 404页面
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 检查管理员权限
router.beforeEach((to, from, next) => {
  const isLoggedIn = localStorage.getItem('adminLoggedIn') === 'true'

  // 如果访问需要权限的admin页面
  if (to.meta.requiresAuth && !isLoggedIn) {
    next({ name: 'admin-login', query: { redirect: to.fullPath } })
  }
  // 如果已登录，访问登录页则重定向到管理后台
  else if (to.name === 'admin-login' && isLoggedIn) {
    next({ name: 'admin-review' })
  }
  else {
    next()
  }
})

export default router
