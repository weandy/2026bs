# 从 0 到 1 全自动化项目构建指南

> **本文件是整个项目的唯一入口。新对话中只需让 AI 读取本文件即可。**

---

## 🚀 启动指令（复制粘贴到新对话即可）

```
请阅读 e:\ALL\gptcodex\NEW\README.md，然后按照其中的指引读取所有文档，从 PROGRESS.md 中的当前步骤开始自动执行开发。全程自动驾驶，无需等待我确认。
```

---

## 📁 文件结构

```
NEW/
├── README.md                ← 你正在读的这个文件（唯一入口）
├── PROGRESS.md              ← Agent 进度追踪（断点续传）
├── docs/
│   ├── AGENT_PROMPT.md      ← AI 自动驾驶工作流程定义
│   ├── system_architecture.md ← 系统架构说明书
│   ├── database_design.md   ← 数据库设计（完整 SQL）
│   ├── api_amendments.md    ← API 设计修正
│   ├── security_design.md   ← 安全设计
│   └── development_guide.md ← 开发指南
├── sql/
│   └── seed_data.sql        ← 测试种子数据
└── tools/
    ├── db/                  ← 数据库工具
    │   ├── check_connection.py
    │   ├── run_sql_file.py
    │   └── query.py
    └── verify/              ← 自动化验证
        ├── check_health.py
        ├── api_tester.py
        └── tests/
            └── test_auth.json
```

## 📖 Agent 必读文档（按顺序）

| 顺序 | 文件 | 作用 |
|------|------|------|
| 1 | `PROGRESS.md` | 确定从哪一步开始 |
| 2 | `docs/AGENT_PROMPT.md` | 自动驾驶循环规则 |
| 3 | `docs/system_architecture.md` | 技术选型、架构图 |
| 4 | `docs/database_design.md` | 建表 SQL |
| 5 | 根目录 `api_design.md` + `docs/api_amendments.md` | 76 个接口 |
| 6 | `docs/security_design.md` | JWT/权限/脱敏 |
| 7 | `docs/development_guide.md` | 编码规范/测试要求 |

## 🔑 测试账号

| 角色 | 账号 | 密码 |
|------|------|------|
| 医生 | DR001 | 123456 |
| 护士 | NS001 | 123456 |
| 管理员 | AD001 | 123456 |
| 居民 | 13800000001 | 123456 |

## ⚙️ 自动驾驶规则摘要

1. **Agent 读取 PROGRESS.md** → 确定当前步骤 N
2. **执行步骤 N** → 编码 → 启动 → 测试
3. **测试通过** → git commit → 更新 PROGRESS.md → N+1
4. **测试失败** → debug → 重试（最多 3 轮）
5. **3 轮仍失败** → 记录在 PROGRESS.md 跳过表 → 跳到下一个无依赖步骤
6. **前端验证** → 使用内置浏览器打开页面截图验证

## ⚠️ 关键约束

- 文档冲突时：`docs/` 下的文档优先于根目录同名旧文档
- 禁止跨库 JOIN、禁止 Controller 返回 Entity
- HTTP 状态码与 code 保持一致（不是统一返回 200）
- 每步完成后立即 git commit
