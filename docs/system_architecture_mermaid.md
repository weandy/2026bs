# 系统总体架构图（Mermaid 正式版）

下面这版更适合直接放进论文初稿或作为 draw.io 重绘底稿。

## 1. Mermaid 代码

```mermaid
flowchart TB
    U1["居民用户"]
    U2["医护人员"]
    U3["系统管理员"]

    subgraph FE["前端表示层"]
        F1["居民端（Vue 3）"]
        F2["医护端（Vue 3）"]
        F3["管理员端（Vue 3）"]
    end

    subgraph API["接口与认证层"]
        A1["Axios 请求封装"]
        A2["JWT 认证与刷新机制"]
        A3["Vue Router 路由守卫"]
    end

    subgraph BE["后端业务层（Spring Boot 多模块单体）"]
        B0["chp-starter 启动模块"]
        B1["chp-common 公共能力模块"]
        B2["chp-security 安全认证模块"]
        B3["chp-resident 居民业务模块"]
        B4["chp-medical 医护业务模块"]
        B5["chp-admin 管理业务模块"]
    end

    subgraph CORE["公共支撑能力"]
        C1["统一返回结果"]
        C2["全局异常处理"]
        C3["AOP 权限控制"]
        C4["缓存机制（Caffeine）"]
        C5["定时任务"]
        C6["审计日志"]
    end

    subgraph DATA["数据存储层"]
        D1["居民域数据库（chp_resident）"]
        D2["管理域数据库（chp_admin）"]
    end

    U1 --> F1
    U2 --> F2
    U3 --> F3

    F1 --> A1
    F2 --> A1
    F3 --> A1

    A1 --> A2
    A1 --> A3
    A2 --> B0
    A3 --> B0

    B0 --> B1
    B0 --> B2
    B0 --> B3
    B0 --> B4
    B0 --> B5

    B1 --> C1
    B1 --> C2
    B1 --> C3
    B1 --> C4
    B1 --> C5
    B1 --> C6

    B2 --> D1
    B2 --> D2
    B3 --> D1
    B4 --> D2
    B5 --> D2
```

---

## 2. 论文配图标题建议

建议标题写成：

**图 4-1 系统总体架构图**

---

## 3. 图下注释建议

可直接放在论文中的说明文字：

本系统采用前后端分离架构。前端按照居民端、医护端和管理员端三类角色组织页面；后端采用 Spring Boot 多模块单体架构，并划分为公共能力、安全认证、居民业务、医护业务和管理业务等模块。数据层采用双数据源方案，将居民域数据与管理域数据分开存储，以提升系统结构清晰度与数据边界性。

---

## 4. 使用建议

1. 如果直接放论文，建议先截图或导出 SVG，再插入 Word。
2. 如果后续要做正式美化，可以把这版结构导入 draw.io 重新绘制。
3. 如果老师更偏好“论文风”配图，建议在 draw.io 中改成分层矩形框样式，减少箭头交叉。

