<template>
  <el-container class="resident-layout">
    <!-- 侧边栏（桌面端） -->
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="logo">
        <div class="logo-icon">
          <Plus :size="18" />
        </div>
        <span v-if="!isCollapsed" class="logo-text">社区卫生服务中心</span>
      </div>
      <el-menu :default-active="$route.path" router :collapse="isCollapsed">
        <!-- 核心服务 -->
        <div v-if="!isCollapsed" class="nav-group-label">核心服务</div>
        <el-menu-item v-for="item in coreNav" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" :size="16" /></el-icon>
          <template #title>{{ item.label }}</template>
        </el-menu-item>
        <!-- 健康管理 -->
        <div v-if="!isCollapsed" class="nav-group-label">健康管理</div>
        <el-menu-item v-for="item in healthNav" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" :size="16" /></el-icon>
          <template #title>{{ item.label }}</template>
        </el-menu-item>
        <!-- 个人 -->
        <div v-if="!isCollapsed" class="nav-group-label">个人</div>
        <el-menu-item v-for="item in personalNav" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" :size="16" /></el-icon>
          <template #title>{{ item.label }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 右侧主区域 -->
    <el-container class="main-container">
      <el-header class="topbar">
        <div class="topbar-left">
          <button class="collapse-btn" @click="isCollapsed = !isCollapsed">
            <Menu :size="18" />
          </button>
          <span class="page-title">{{ $route.meta.title }}</span>
        </div>
        <div class="topbar-right">
          <span class="user-name">{{ userStore.userInfo?.name }}</span>
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

  <!-- 移动端底部导航 — 5 主入口 -->
  <nav class="mobile-tabbar">
    <router-link
      v-for="tab in mobileTabs"
      :key="tab.path"
      :to="tab.path"
      class="m-tab"
      active-class="active"
    >
      <component :is="tab.icon" :size="20" />
      <span>{{ tab.shortLabel }}</span>
    </router-link>
    <button v-if="moreItems.length > 0" class="m-tab" @click="moreDrawerVisible = true">
      <Menu :size="20" />
      <span>更多</span>
    </button>
  </nav>

  <!-- 更多导航抽屉 -->
  <el-drawer v-model="moreDrawerVisible" title="全部功能" direction="btt" size="auto" :with-header="true">
    <div class="more-nav-list">
      <router-link
        v-for="item in allNav"
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
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { forceLogout } from '@/utils/request'
import {
  Home, CalendarDays, Clock, FileText, FolderOpen,
  Syringe, Users, UserRound, Plus, Menu, ClipboardList, Bell
} from 'lucide-vue-next'

const userStore = useUserStore()
const router = useRouter()
const isCollapsed = ref(false)
const moreDrawerVisible = ref(false)

/* ── 分组导航 ── */
const coreNav = [
  { path: '/resident/home',           label: '服务首页', icon: Home },
  { path: '/resident/appointment',    label: '预约挂号', icon: CalendarDays },
  { path: '/resident/queue-progress', label: '候诊进度', icon: Clock },
  { path: '/resident/visit-records',  label: '就诊记录', icon: FileText },
]

const healthNav = [
  { path: '/resident/health-record', label: '我的档案', icon: FolderOpen },
  { path: '/resident/follow-up',     label: '签约与随访', icon: ClipboardList },
  { path: '/resident/vaccine',       label: '疫苗接种', icon: Syringe },
]

const personalNav = [
  { path: '/resident/message', label: '消息通知', icon: Bell },
  { path: '/resident/family',  label: '家庭成员', icon: Users },
  { path: '/resident/profile', label: '个人中心', icon: UserRound },
]

const allNav = computed(() => [...coreNav, ...healthNav, ...personalNav])

/* ── 移动端 Tab（前 4 个 + 更多） ── */
const mobileTabs = [
  { path: '/resident/home',        shortLabel: '首页', icon: Home },
  { path: '/resident/appointment', shortLabel: '预约', icon: CalendarDays },
  { path: '/resident/visit-records', shortLabel: '就诊', icon: FileText },
  { path: '/resident/health-record', shortLabel: '档案', icon: FolderOpen },
]

const moreItems = computed(() => allNav.value.filter(n => !mobileTabs.some(t => t.path === n.path)))

function handleLogout() {
  forceLogout()
}
</script>

<style scoped>
/* ═══════════════════════════════════════
   整体布局
═══════════════════════════════════════ */
.resident-layout { height: 100vh; }

/* ═══════════════════════════════════════
   侧边栏
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

/* Logo 区域 */
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

/* 导航分组标签 */
.nav-group-label {
  padding: 16px 20px 6px;
  font-size: 11px;
  font-weight: 600;
  color: var(--muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* Element Plus 菜单覆盖 */
.sidebar :deep(.el-menu) {
  background: transparent !important;
  border-right: none !important;
  padding: 0;
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

/* 路由过渡 */
.fade-enter-active, .fade-leave-active { transition: all 0.22s ease; }
.fade-enter-from  { opacity: 0; transform: translateY(8px); }
.fade-leave-to    { opacity: 0; transform: translateY(-4px); }

/* ═══════════════════════════════════════
   移动端底部导航（≤ 768px）
═══════════════════════════════════════ */
.mobile-tabbar { display: none; }

@media (max-width: 768px) {
  .sidebar { display: none !important; }

  .main-container { flex-direction: column; }

  .main-container :deep(.el-main) {
    padding: 12px;
    padding-bottom: 80px;
    overflow-y: auto;
  }

  .collapse-btn { display: none; }

  .mobile-tabbar {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
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
    border: none;
    background: none;
    cursor: pointer;
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
