<template>
  <el-container class="admin-layout">
    <!-- 侧边栏（桌面端） -->
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="logo">
        <div class="logo-icon">
          <HeartPulse :size="18" />
        </div>
        <span v-if="!isCollapsed" class="logo-text">社区卫生服务中心</span>
      </div>
      <el-menu :default-active="$route.path" router :collapse="isCollapsed">
        <template v-for="item in currentNav" :key="item.path">
          <el-menu-item v-if="item.visible !== false" :index="item.path">
            <el-icon><component :is="item.icon" :size="16" /></el-icon>
            <template #title>{{ item.label }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <!-- 右侧主区域 -->
    <el-container class="main-container">
      <el-header class="topbar">
        <div class="topbar-left">
          <button class="collapse-btn" @click="isCollapsed = !isCollapsed" data-testid="sidebar-toggle">
            <Menu :size="18" />
          </button>
          <span class="page-title">{{ $route.meta.title }}</span>
        </div>
        <div class="topbar-right">
          <button class="icon-toggle-btn" @click="toggleDark" title="切换暗黑模式">
            <Moon v-if="!isDark" :size="18" />
            <Sun v-else :size="18" />
          </button>
          <span class="user-name">{{ userStore.userInfo?.name }}</span>
          <el-tag size="small" :type="isAdmin ? 'warning' : 'success'" round>{{ isAdmin ? '管理员' : '医护' }}</el-tag>
          <button class="logout-btn" @click="handleLogout" data-testid="btn-logout">退出</button>
        </div>
      </el-header>
      <el-main>
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>

  <!-- 移动端底部导航 —— 4 主入口 + 更多 -->
  <nav class="mobile-tabbar">
    <router-link
      v-for="item in primaryMobileNav"
      :key="item.path"
      :to="item.path"
      class="m-tab"
      active-class="active"
    >
      <component :is="item.icon" :size="20" />
      <span>{{ item.shortLabel || item.label }}</span>
    </router-link>
    <button v-if="overflowMobileNav.length > 0" class="m-tab" @click="moreDrawerVisible = true">
      <Menu :size="20" />
      <span>更多</span>
    </button>
  </nav>

  <!-- 更多导航抽屉 -->
  <el-drawer v-model="moreDrawerVisible" title="全部功能" direction="btt" size="auto" :with-header="true">
    <div class="more-nav-list">
      <router-link
        v-for="item in allVisibleNav"
        :key="item.path"
        :to="item.path"
        class="more-nav-item"
        active-class="active"
        @click="moreDrawerVisible = false"
      >
        <component :is="item.icon" :size="18" />
        <span>{{ item.label }}</span>
      </router-link>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { forceLogout } from '@/utils/request'
import {
  Monitor as MonitorIcon, FileText, FolderOpen, ClipboardList, Pill,
  CalendarDays, LayoutDashboard, Users, Stethoscope, Settings,
  ScrollText, HeartPulse, Menu, Syringe, Building2, BookOpen,
  PackageSearch, ArrowLeftRight, Sun, Moon, BarChart3, Repeat2, MessageSquare,
  UserSearch, FileSignature, Newspaper, Trophy
} from 'lucide-vue-next'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const isCollapsed = ref(false)

const isAdmin = computed(() => route.path.startsWith('/admin'))
const isMedical = computed(() => route.path.startsWith('/medical'))
const userRole = computed(() => userStore.role || '')

/* ── 暗黑模式逻辑 ── */
const isDark = ref(document.documentElement.classList.contains('dark'))
if (localStorage.getItem('chp-theme-dark') === '1') {
  isDark.value = true
  document.documentElement.classList.add('dark')
}
function toggleDark() {
  const next = !isDark.value
  isDark.value = next
  if (next) document.documentElement.classList.add('dark')
  else document.documentElement.classList.remove('dark')
  localStorage.setItem('chp-theme-dark', next ? '1' : '0')
}

/* ── 导航配置 —— 侧边栏 & 移动端共用 ── */
const medicalNav = computed(() => [
  { path: '/medical/workbench',     label: '接诊工作台', shortLabel: '工作台', icon: MonitorIcon },
  { path: '/medical/record-manage', label: '居民档案',   shortLabel: '档案',   icon: FolderOpen },
  { path: '/medical/follow-up',     label: '签约与随访', shortLabel: '随访',   icon: ClipboardList },
  { path: '/medical/vaccination',   label: '接种管理',   shortLabel: '接种',   icon: Syringe },
  { path: '/medical/prescription',  label: '处方记录',   shortLabel: '处方',   icon: FileText },
  { path: '/medical/my-schedule',   label: '我的排班',   shortLabel: '排班',   icon: CalendarDays },
])

const adminNav = computed(() => [
  { path: '/admin/dashboard',  label: '数据看板',   shortLabel: '看板', icon: LayoutDashboard },
  { path: '/admin/staff',      label: '用户管理',   shortLabel: '人员', icon: Users },
  { path: '/admin/schedule',   label: '排班管理',   shortLabel: '排班', icon: CalendarDays },
  { path: '/admin/dept',       label: '科室管理',   shortLabel: '科室', icon: Building2 },
  { path: '/admin/resident',   label: '居民管理',   shortLabel: '居民', icon: UserSearch },
  { path: '/admin/contract',   label: '签约管理',   shortLabel: '签约', icon: FileSignature },
])

const currentNav = computed(() => isAdmin.value ? adminNav.value : medicalNav.value)
const allVisibleNav = computed(() => currentNav.value.filter(i => i.visible !== false))
const primaryMobileNav = computed(() => allVisibleNav.value.slice(0, 4))
const overflowMobileNav = computed(() => allVisibleNav.value.slice(4))
const moreDrawerVisible = ref(false)

function handleLogout() {
  forceLogout()
}
</script>

<style scoped>
/* ═══════════════════════════════════════
   整体布局
═══════════════════════════════════════ */
.admin-layout { height: 100vh; }

/* ═══════════════════════════════════════
   侧边栏 — 浅色白底风格 / 暗黑适配
═══════════════════════════════════════ */
.sidebar {
  background: var(--surface-soft);
  border-right: 1px solid var(--border);
  transition: width 0.25s ease;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

/* Logo 区域：顶部保留品牌绿色条 */
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 14px;
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
}

.logo-icon {
  width: 32px;
  height: 32px;
  background: var(--primary-light);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.logo-icon svg {
  width: 18px;
  height: 18px;
  stroke: var(--primary-strong);
}

.logo-text {
  color: var(--primary-strong);
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
}

/* Element Plus 菜单覆盖 — 浅色主题 */
.sidebar :deep(.el-menu) {
  background: transparent !important;
  border-right: none !important;
  padding: 8px 0;
}

.sidebar :deep(.el-menu-item) {
  margin: 2px 8px;
  height: 40px;
  border-radius: 10px;
  color: var(--muted) !important;
  font-size: 13.5px;
  transition: all 0.2s ease;
}

.sidebar :deep(.el-menu-item:hover) {
  background: var(--primary-light) !important;
  color: var(--primary-strong) !important;
}

.sidebar :deep(.el-menu-item.is-active) {
  background: var(--primary-light) !important;
  color: var(--primary-strong) !important;
  font-weight: 600;
  /* 左侧绿色激活线 */
  box-shadow: inset 3px 0 0 var(--primary);
}

.sidebar :deep(.el-menu-item .el-icon) {
  color: inherit !important;
}

/* 折叠时图标居中 */
.sidebar.collapsed :deep(.el-menu-item) {
  margin: 2px 4px;
  justify-content: center;
}

/* ═══════════════════════════════════════
   顶栏
═══════════════════════════════════════ */
.main-container { overflow: hidden; max-width: 100vw; }

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--border);
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  padding: 0 20px;
  flex-shrink: 0;
  z-index: 10;
}
html.dark .topbar {
  background: rgba(30, 30, 30, 0.75);
}

.topbar-left { display: flex; align-items: center; gap: 14px; }
.topbar-right { display: flex; align-items: center; gap: 12px; }

.collapse-btn {
  width: 34px;
  height: 34px;
  border: none;
  background: none;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--muted);
  transition: all 0.2s ease;
}
.collapse-btn:hover { background: var(--primary-light); color: var(--primary-strong); }
.collapse-btn svg { width: 18px; height: 18px; }

.page-title { font-size: 15px; font-weight: 600; color: var(--text); }
.user-name { font-size: 14px; color: var(--text); }

.logout-btn {
  padding: 6px 14px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: none;
  font-size: 13px;
  color: var(--muted);
  cursor: pointer;
  font-family: var(--font-sans);
  transition: all 0.2s ease;
}
.logout-btn:hover { background: var(--bg-danger); border-color: var(--danger); color: var(--danger); }

.icon-toggle-btn {
  width: 32px;
  height: 32px;
  border: 1px solid transparent;
  background: none;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--muted);
  transition: all 0.2s ease;
}
.icon-toggle-btn:hover {
  background: var(--surface-soft);
  border-color: var(--border);
  color: var(--text);
}

/* 路由过渡 */
.fade-enter-active, .fade-leave-active { transition: all 0.22s ease; }
.fade-enter-from  { opacity: 0; transform: translateY(8px); }
.fade-leave-to    { opacity: 0; transform: translateY(-4px); }

/* ═══════════════════════════════════════
   移动端底部导航（≤ 768px）
═══════════════════════════════════════ */
.mobile-tabbar { display: none; }

@media (max-width: 768px) {
  /* 侧边栏隐藏 */
  .sidebar { display: none !important; }

  /* 主内容区占满，留出底部 tabbar 空间 */
  .main-container {
    flex-direction: column;
  }

  .main-container :deep(.el-main) {
    padding: 12px;
    padding-bottom: 80px; /* tabbar 高度 + 余量 */
    overflow-y: auto;
  }

  /* 顶栏折叠按钮隐藏（无侧边栏需要折叠） */
  .collapse-btn { display: none; }

  /* 移动端底部导航显示 */
  .mobile-tabbar {
    display: grid;
    grid-template-columns: repeat(5, 1fr); /* 4 主入口 + 1 更多 */
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background: rgba(255, 255, 255, 0.96);
    backdrop-filter: blur(14px);
    -webkit-backdrop-filter: blur(14px);
    border-top: 1px solid var(--border);
    border-radius: 18px 18px 0 0;
    box-shadow: 0 -4px 20px rgba(35, 49, 45, 0.08);
    padding: 6px 8px;
    padding-bottom: max(10px, env(safe-area-inset-bottom));
    z-index: 200;
  }

  .m-tab {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 3px;
    padding: 6px 4px;
    border-radius: 10px;
    text-decoration: none;
    color: var(--muted);
    font-size: 10px;
    font-weight: 500;
    transition: all 0.2s ease;
  }

  .m-tab svg {
    width: 21px;
    height: 21px;
    display: block;
    flex-shrink: 0;
  }

  .m-tab:hover { background: var(--primary-light); color: var(--primary-strong); }

  .m-tab.active {
    color: var(--primary-strong);
    background: var(--primary-light);
    font-weight: 700;
  }
}

/* ── 更多导航抽屉内容 ── */
.more-nav-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  padding: 8px 12px 20px;
}
.more-nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 14px 8px;
  border-radius: 12px;
  text-decoration: none;
  color: var(--text);
  font-size: 12px;
  font-weight: 500;
  transition: background 0.15s;
}
.more-nav-item:hover,
.more-nav-item.active {
  background: var(--primary-light);
  color: var(--primary-strong);
}
</style>
