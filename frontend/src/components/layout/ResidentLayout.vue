<template>
  <div class="resident-layout">

    <!-- PC 端顶部导航 (>= 768px 可见) -->
    <header class="desktop-topbar">
      <div class="topbar-inner">
        <div class="topbar-brand">
          <span class="brand-icon">+</span>
          <span class="brand-name">社区卫生服务中心</span>
        </div>
        <nav class="topbar-nav">
          <router-link
            v-for="tab in tabs"
            :key="tab.to"
            :to="tab.to"
            class="topbar-link"
            active-class="active"
          >
            <component :is="tab.icon" :size="16" />
            <span>{{ tab.label }}</span>
          </router-link>
        </nav>
        <div class="topbar-right">
          <span class="topbar-user">{{ userStore.userInfo?.name || '居民' }}</span>
          <button class="topbar-logout" @click="handleLogout">退出</button>
        </div>
      </div>
    </header>

    <div class="resident-content">
      <router-view v-slot="{ Component }">
        <transition name="slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>

    <!-- 移动端底部 Tab bar (< 768px 可见) -->
    <nav class="bottom-tabbar">
      <router-link
        v-for="tab in tabs"
        :key="tab.to"
        :to="tab.to"
        class="tab-item"
        active-class="active"
        :data-testid="`tab-${tab.key}`"
      >
        <span class="tab-icon"><component :is="tab.icon" :size="22" /></span>
        <span class="tab-label">{{ tab.label }}</span>
      </router-link>
    </nav>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUiStore } from '@/stores/uiStore'
import { useUserStore } from '@/stores/user'
import { Home, CalendarDays, FileText, Bell, UserRound } from 'lucide-vue-next'

const router    = useRouter()
const uiStore   = useUiStore()
const userStore = useUserStore()

const tabs = [
  { key: 'home', to: '/resident/home', label: '首页', icon: Home },
  { key: 'appointment', to: '/resident/appointment', label: '预约', icon: CalendarDays },
  { key: 'visit', to: '/resident/visit-records', label: '就诊', icon: FileText },
  { key: 'health', to: '/resident/health-record', label: '档案', icon: Bell },
  { key: 'profile', to: '/resident/profile', label: '我的', icon: UserRound },
]

function handleLogout() {
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  uiStore.setHeaderHeight(64)
})
</script>

<style scoped>
.resident-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  max-width: 100vw;
  overflow-x: hidden;
  background: var(--bg);
}

.resident-content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding-bottom: 72px;
  width: 100%;
  max-width: 100vw;
}

/* ══════════════════════════════════════════
   PC 端顶部导航 (>= 768px)
   ══════════════════════════════════════════ */
.desktop-topbar {
  display: none;     /* 移动端默认隐藏 */
}

@media (min-width: 768px) {
  .desktop-topbar {
    display: block;
    position: sticky;
    top: 0;
    z-index: 200;
    background: var(--surface);
    border-bottom: 1px solid var(--border);
    box-shadow: 0 1px 8px rgba(35, 49, 45, 0.06);
  }

  .topbar-inner {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
    height: 56px;
  }

  .topbar-brand {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 700;
    font-size: 15px;
    color: var(--primary-strong);
    white-space: nowrap;
  }

  .brand-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    border-radius: 8px;
    background: var(--primary);
    color: #fff;
    font-size: 18px;
    font-weight: 700;
    line-height: 1;
  }

  .topbar-nav {
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .topbar-link {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    border-radius: 10px;
    font-size: 14px;
    font-weight: 500;
    color: var(--muted);
    text-decoration: none;
    transition: all 0.2s ease;
  }
  .topbar-link:hover {
    background: var(--primary-light);
    color: var(--primary-strong);
  }
  .topbar-link.active {
    background: var(--primary-light-hover);
    color: var(--primary-strong);
    font-weight: 600;
  }

  .topbar-right {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 13px;
    color: var(--muted);
    white-space: nowrap;
  }

  .topbar-user {
    font-weight: 500;
    color: var(--text);
  }

  .topbar-logout {
    padding: 5px 12px;
    border-radius: 8px;
    border: 1px solid var(--border);
    background: transparent;
    color: var(--muted);
    font-size: 12px;
    cursor: pointer;
    transition: all 0.2s ease;
    font-family: var(--font-sans);
  }
  .topbar-logout:hover {
    background: var(--bg-danger);
    color: var(--text-danger);
    border-color: var(--danger);
  }

  /* PC 端隐藏底部 Tab + 取消 content 的 bottom padding */
  .bottom-tabbar {
    display: none !important;
  }
  .resident-content {
    padding-bottom: 0;
  }
}

/* ══════════════════════════════════════════
   移动端底部 Tab bar (< 768px)
   ══════════════════════════════════════════ */
.bottom-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  padding: 8px 12px;
  padding-bottom: max(12px, env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-radius: 20px 20px 0 0;
  box-shadow: 0 -4px 24px rgba(35, 49, 45, 0.08);
  z-index: 100;
}
html.dark .bottom-tabbar {
  background: rgba(30, 30, 30, 0.75);
  box-shadow: 0 -4px 24px rgba(0, 0, 0, 0.3);
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  text-decoration: none;
  color: var(--muted);
  font-size: 11px;
  font-weight: 500;
  padding: 7px 4px;
  border-radius: 12px;
  transition: all 0.2s ease;
}

.tab-item:hover {
  background: var(--primary-light);
  color: var(--primary-strong);
}

.tab-item.active {
  background: var(--primary-light-hover);
  color: var(--primary-strong);
  font-weight: 700;
}

.tab-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  min-width: 24px;
  min-height: 24px;
  flex-shrink: 0;
}

.tab-icon :deep(svg) {
  display: block;
  width: 100%;
  height: 100%;
}

.tab-label {
  font-size: 10px;
  white-space: nowrap;
}

/* 路由过渡 */
.slide-enter-active, .slide-leave-active { transition: all 0.22s ease; }
.slide-enter-from  { opacity: 0; transform: translateY(8px); }
.slide-leave-to    { opacity: 0; transform: translateY(-4px); }
</style>
