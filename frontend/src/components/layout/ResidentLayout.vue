<template>
  <div class="resident-layout">
    <div class="resident-content">
      <router-view v-slot="{ Component }">
        <transition name="slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>

    <!-- 底部 Tab bar -->
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
import { useUiStore } from '@/stores/uiStore'
import { Home, CalendarDays, FileText, Bell, UserRound } from 'lucide-vue-next'

const uiStore = useUiStore()

const tabs = [
  { key: 'home', to: '/resident/home', label: '首页', icon: Home },
  { key: 'appointment', to: '/resident/appointment', label: '预约', icon: CalendarDays },
  { key: 'health', to: '/resident/health-record', label: '档案', icon: FileText },
  { key: 'message', to: '/resident/message', label: '消息', icon: Bell },
  { key: 'profile', to: '/resident/profile', label: '我的', icon: UserRound },
]

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

/* ── 底部 Tab bar ── */
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
