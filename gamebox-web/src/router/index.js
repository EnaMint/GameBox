import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      children: [
        { path: '', name: 'home', component: () => import('@/views/home/HomeView.vue') },
        { path: 'strategy', name: 'strategy-list', component: () => import('@/views/strategy/StrategyListView.vue') },
        { path: 'strategy/create', name: 'strategy-create', component: () => import('@/views/strategy/StrategyEditView.vue'), meta: { requiresAuth: true } },
        { path: 'strategy/edit/:id', name: 'strategy-edit', component: () => import('@/views/strategy/StrategyEditView.vue'), meta: { requiresAuth: true } },
        { path: 'strategy/:id', name: 'strategy-detail', component: () => import('@/views/strategy/StrategyDetailView.vue') },
        { path: 'games', name: 'game-library', component: () => import('@/views/game/GameLibraryView.vue') },
        { path: 'my/games', name: 'my-games', component: () => import('@/views/game/MyGameView.vue'), meta: { requiresAuth: true } },
        { path: 'team', name: 'team-hall', component: () => import('@/views/team/TeamHallView.vue') },
        { path: 'team/create', name: 'team-create', component: () => import('@/views/team/TeamEditView.vue'), meta: { requiresAuth: true } },
        { path: 'team/:id', name: 'team-detail', component: () => import('@/views/team/TeamDetailView.vue') },
        { path: 'record', name: 'record-feed', component: () => import('@/views/record/RecordFeedView.vue') },
        { path: 'record/create', name: 'record-create', component: () => import('@/views/record/RecordEditView.vue'), meta: { requiresAuth: true } },
        { path: 'profile', name: 'profile', component: () => import('@/views/user/ProfileView.vue'), meta: { requiresAuth: true } },
        { path: 'user/:id', name: 'user-home', component: () => import('@/views/user/UserHomeView.vue') },
        { path: 'user/:id/follows', name: 'user-follows', component: () => import('@/views/user/FollowListView.vue'), meta: { followType: 'following' } },
        { path: 'user/:id/fans', name: 'user-fans', component: () => import('@/views/user/FollowListView.vue'), meta: { followType: 'fans' } },
        { path: 'messages', name: 'messages', component: () => import('@/views/message/MessagesView.vue'), meta: { requiresAuth: true } }
      ]
    },
    { path: '/login', name: 'login', component: () => import('@/views/auth/LoginView.vue') },
    { path: '/register', name: 'register', component: () => import('@/views/auth/RegisterView.vue') },
    { path: '/:pathMatch(.*)*', redirect: '/' }
  ]
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
})

export default router
