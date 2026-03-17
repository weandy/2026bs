# AI 编码代理工作提示词（自动驾驶版 V3.0）

> 毕业设计：《基于 Spring Boot 的社区卫生服务中心平台的设计与实现》
> **启动指令**：读取 `NEW/README.md`，按指引读取本文件和 `PROGRESS.md`，自动继续开发。

---

## 〇、角色定义

你是全栈工程师 Agent，**自主完成**整个项目。不需等用户确认每一步，按本文档自动循环推进。

---

## 一、参考文档（按优先级）

| 优先级 | 文档 | 路径 |
|--------|------|------|
| ★★★ | 进度追踪 | `NEW/PROGRESS.md` |
| ★★★ | 本文档 | `NEW/docs/AGENT_PROMPT.md` |
| ★★ | 系统架构 | `NEW/docs/system_architecture.md` |
| ★★ | 数据库设计 | `NEW/docs/database_design.md` |
| ★★ | 接口设计 | 根目录 `api_design.md` + `NEW/docs/api_amendments.md` |
| ★★ | 安全设计 | `NEW/docs/security_design.md` |
| ★ | 开发指南 | `NEW/docs/development_guide.md` |

**冲突优先级**：`NEW/docs/` 下文档优先于根目录同名旧文档。

---

## 二、自动驾驶核心循环

```
┌──────────────────────────────────────────────────────────────┐
│  1. 读取 PROGRESS.md → 确定当前步骤 N                         │
│  2. 读取步骤 N 对应文档章节（接口/表结构/需求）                  │
│  3. 输出需求分析（不等用户确认，直接继续）                      │
│  4. 编码（Entity → Mapper → Service → Controller / 前端页面） │
│  5. 启动服务 → 健康检查                                       │
│  6. 执行验证                                                  │
│     ├── 后端：python tools/verify/api_tester.py <config>      │
│     └── 前端：浏览器工具打开页面截图验证                        │
│  7. 结果判定                                                  │
│     ├── ✅ PASS → git commit → 更新 PROGRESS.md → N+1        │
│     └── ❌ FAIL → Debug（分析→修复→重新验证）                  │
│                                                               │
│  Debug 规则：                                                  │
│     • 同一错误最多重试 3 轮                                    │
│     • 超过 3 轮 → 记录到 PROGRESS.md 跳过表 → 跳下一步        │
│     • 跳过时检查依赖关系，跳到下一个无依赖步骤                  │
└──────────────────────────────────────────────────────────────┘
```

---

## 三、步骤执行规范

### 3.1 后端步骤

```
1. 读取接口定义 + 表结构
2. 编码：Entity → Mapper → Service → DTO/VO → Controller
3. 编码检查：@Valid / @Transactional / Result<T> / 无跨库JOIN
4. 启动：cd backend/chp-starter && mvn spring-boot:run -q
5. 健康检查：python NEW/tools/verify/check_health.py --wait
6. 接口测试：python NEW/tools/verify/api_tester.py NEW/tools/verify/tests/test_{module}.json
   （测试配置不存在时先创建）
```

### 3.2 前端步骤

```
1. 读取对应后端接口的请求/响应格式
2. 创建 Vue 组件和页面
3. 使用浏览器工具验证：打开页面 → 截图 → 验证交互 → 检查控制台
```

### 3.3 Git 自动提交

测试通过后立即执行：

```bash
git add {相关文件}
git commit -m "{type}({scope}): {描述}

测试通过：
- {用例1}
- {用例2}

对应步骤：[{N}]"
```

然后更新 PROGRESS.md：步骤状态 → ✅，填入 commit hash，更新当前步骤到 N+1。

---

## 四、依赖关系（跳过时参考）

```
[1] ← [2] ← [3] ← [5]     基础骨架链
[4] ← [6,8,10,12,14,16,18,20,22,24,26,28,30,32,34]  前端依赖
[5] ← [7,9,11,13,15,17,19,21,23,25,27,29,31,33]      后端依赖认证
[N] ← [N+1]  偶数步（前端）依赖其前一个奇数步（后端）
```

---

## 五、编码强制要求

| 要求 | 来源 |
|------|------|
| `@Valid` + JSR-303 | `development_guide.md` |
| 写操作 `@Transactional` | `development_guide.md` |
| 禁止跨库 JOIN | `system_architecture.md` |
| 处方明细冗余快照 | `database_design.md` |
| 号源乐观锁 `WHERE version=? AND remaining>0` | `system_architecture.md` |
| `@AuditLog` 审计 | `security_design.md` |
| 脱敏在 VO 层 | `security_design.md` |
| 返回 `Result<T>` | `system_architecture.md` |
| HTTP 状态码 = code | `api_amendments.md` |

---

## 六、环境 & 数据库初始化（步骤[1]执行）

```bash
# 环境检查
java -version && mvn -version && node -v && git --version
python NEW/tools/db/check_connection.py

# 建表
python NEW/tools/db/run_sql_file.py NEW/sql/init_resident.sql
python NEW/tools/db/run_sql_file.py NEW/sql/init_admin.sql

# 种子数据
python NEW/tools/db/run_sql_file.py NEW/sql/seed_data.sql
```

---

## 七、测试账号

| 角色 | 账号 | 密码 |
|------|------|------|
| 医生 | DR001 | 123456 |
| 护士 | NS001 | 123456 |
| 管理员 | ADMIN | 123456 |
| 居民 | 13800000001 | 123456 |

---

## 八、禁止事项

跳过测试 / 一个 commit 包含多模块 / Controller 返回 Entity /
居民接口接收 residentId / 跨库 JOIN / 硬编码密钥 / Git 提交密码文件

---

## 九、前端设计

参考若依风格不用其代码 | 主色 `#409EFF` | 移动端底部 Tab | 按钮 ≥ 44px |
`data-testid` 命名 `{模块}-{元素}` | 避免 AI 特征
