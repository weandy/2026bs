# 开发指南

---

## 一、环境

| 工具 | 版本 | 验证 |
|------|------|------|
| Java | 17+ | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Node.js | 18+ | `node -v` |
| MySQL | 8.0+ | `python NEW/tools/db/check_connection.py` |

MySQL: `192.168.8.212:3306` / `root` / `mysql_eAk4W2`
端口: 后端 8080, 前端 5173

## 二、开发顺序（前后端交叉）

### 阶段1：骨架  [1]项目初始化 [2]公共模块 [3]安全模块 [4]前端初始化
### 阶段2：认证+居民  [5]登录后端 [6]登录前端 [7]预约后端 [8]预约前端 [9]档案后端 [10]档案前端 [11]疫苗后端 [12]疫苗前端 [13]PDF+消息 [14]消息前端
### 阶段3：医护  [15]接诊后端 [16]接诊前端 [17]处方+发药 [18]处方前端 [19]档案管理 [20]档案前端 [21]随访+公卫 [22]随访前端 [23]排班 [24]排班前端
### 阶段4：管理员  [25]用户管理 [26]用户前端 [27]排班管理 [28]排班前端 [29]库存管理 [30]库存前端 [31]报表+日志 [32]报表前端 [33]配置+公告 [34]公告前端
### 阶段5：收尾  [35]定时任务 [36]联调 [37]E2E测试 [38]回归

## 三、编码规范

| 规则 | 内容 |
|------|------|
| 参数校验 | `@Valid` + JSR-303 在 DTO |
| 事务 | 写操作 `@Transactional` |
| 跨库 | 禁止 JOIN，Service 层拼装 |
| 处方快照 | 写入时从 drug 字典复制 name/spec/unit |
| 号源 | 乐观锁 `WHERE version=? AND remaining>0` |
| 审计 | `@AuditLog` 注解 |
| 脱敏 | VO 层 / 转换层 |
| 响应 | 统一 `Result<T>` |

## 四、测试

### 单元测试（JUnit 5 + Mockito）
必须覆盖：AppointmentService, PrescriptionService, PharmacyService, FollowUpService

### 接口测试
每模块创建 `tools/verify/tests/test_{module}.json`，用 `api_tester.py` 运行。
覆盖：正向 + 越权(403) + 业务冲突(10001-10006) + 参数校验(400)

### 前端 E2E
使用浏览器工具验证页面渲染和交互。

## 五、Git 提交

格式：`{type}({scope}): {描述}` + 测试通过列表 + 对应步骤号

type: feat/fix/refactor/test/chore/docs

**每个模块完成后立即 commit，不积累。Bug 修复单独 commit。**

## 六、禁止事项

跳过测试 / 多模块一个 commit / Controller 返回 Entity /
居民接口接收 residentId / 跨库 JOIN / 硬编码密钥 / Git 提交密码
