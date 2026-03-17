# Phase 2：居民端侧边栏布局改造

## 目标

重写 `ResidentLayout.vue`，从"顶部导航 + 居中窄容器"改为"侧边栏 + 全宽内容区"，与 AdminLayout 结构对齐。

## 设计要点

1. **桌面端**：左侧 `el-aside`（220px/64px 可折叠）+ 右侧 `el-header` + `el-main`
2. **移动端**：底部 5 Tab + "更多"抽屉（保持不变）
3. **分组导航**：核心服务 / 健康管理 / 个人 三组
4. **无暗黑模式切换**、**无角色标签**
5. **各页面移除 max-width**

## 导航分组

### 核心服务
- 服务首页 (Home)
- 预约挂号 (CalendarDays)
- 候诊进度 (Clock)
- 就诊记录 (FileText)

### 健康管理
- 我的档案 (FolderOpen)
- 疫苗接种 (Syringe)

### 个人
- 家庭成员 (Users)
- 个人中心 (UserRound)

## 改动文件
1. `ResidentLayout.vue` — 完全重写
2. `HomePage.vue` — 移除 max-width
3. `ProfilePage.vue` — 移除 max-width
4. `VaccinePage.vue` — 移除 max-width
5. `AppointmentPage.vue` — 移除 max-width
